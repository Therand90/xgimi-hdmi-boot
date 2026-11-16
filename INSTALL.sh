#!/usr/bin/env bash
# XGIMI HDMI Boot - generation locale d'un APK Android TV 11 sans Gradle.
# Usage : ./INSTALL.sh <adresse:port ADB>  ou ./INSTALL.sh --build-only
set -euo pipefail

BASE="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
BUILD="$BASE/build"
SDK="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Android/Sdk}}"
TARGET=""
BUILD_ONLY=0

if [[ "${1:-}" == "--build-only" ]]; then
  BUILD_ONLY=1
elif [[ -n "${1:-}" ]]; then
  TARGET="$1"
fi

for bin in java javac curl unzip zip keytool; do
  if ! command -v "$bin" >/dev/null; then
    echo "ERREUR: outil manquant: $bin" >&2
    exit 1
  fi
done
if [[ $BUILD_ONLY -eq 0 ]] && ! command -v adb >/dev/null; then
  echo "ERREUR: adb absent. Installe-le : sudo apt install adb" >&2
  exit 1
fi

mkdir -p "$BUILD" "$SDK"
export ANDROID_HOME="$SDK" ANDROID_SDK_ROOT="$SDK"

# Utilise d'abord le SDK deja present ; telecharge les outils de Google si besoin.
SDKMANAGER="$SDK/cmdline-tools/latest/bin/sdkmanager"
if [[ ! -x "$SDKMANAGER" ]]; then
  SDKMANAGER="$(command -v sdkmanager || true)"
fi
if [[ -z "$SDKMANAGER" || ! -x "$SDKMANAGER" ]]; then
  echo '=== Installation des outils Android officiels (une fois) ==='
  TOOLS_ZIP="$BUILD/commandlinetools.zip"
  curl -fL --retry 3 --connect-timeout 20 \
    -o "$TOOLS_ZIP" \
    'https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip'
  mkdir -p "$SDK/cmdline-tools/tmp-setup"
  unzip -q -o "$TOOLS_ZIP" -d "$SDK/cmdline-tools/tmp-setup"
  if [[ ! -x "$SDK/cmdline-tools/tmp-setup/cmdline-tools/bin/sdkmanager" ]]; then
    echo 'ERREUR: archive Android invalide.' >&2
    exit 1
  fi
  mv "$SDK/cmdline-tools/tmp-setup/cmdline-tools" "$SDK/cmdline-tools/latest"
  rmdir "$SDK/cmdline-tools/tmp-setup"
  SDKMANAGER="$SDK/cmdline-tools/latest/bin/sdkmanager"
fi

PLATFORM="$SDK/platforms/android-30/android.jar"
TOOLS="$SDK/build-tools/35.0.0"
if [[ ! -f "$PLATFORM" || ! -x "$TOOLS/aapt2" || ! -x "$TOOLS/d8" || ! -x "$TOOLS/apksigner" ]]; then
  echo '=== Installation des composants Android SDK (une fois) ==='
  # yes se termine avec SIGPIPE quand sdkmanager arrete de lire.
  yes | "$SDKMANAGER" --sdk_root="$SDK" --licenses >/dev/null 2>&1 || true
  "$SDKMANAGER" --sdk_root="$SDK" --install 'platforms;android-30' 'build-tools;35.0.0'
fi
for f in "$PLATFORM" "$TOOLS/aapt2" "$TOOLS/d8" "$TOOLS/zipalign" "$TOOLS/apksigner"; do
  [[ -e "$f" ]] || { echo "ERREUR: outil manquant $f" >&2; exit 1; }
done

mkdir -p "$BUILD/classes" "$BUILD/dex"
rm -rf "$BUILD/classes" "$BUILD/dex"
mkdir -p "$BUILD/classes" "$BUILD/dex"

printf '\n=== Compilation Android / Java ===\n'
mapfile -d '' SOURCES < <(find "$BASE/src" -name '*.java' -print0)
javac -source 8 -target 8 -Xlint:-options -cp "$PLATFORM" \
  -d "$BUILD/classes" "${SOURCES[@]}"
(cd "$BUILD/classes" && zip -q -r "$BUILD/classes.jar" .)
"$TOOLS/d8" --min-api 26 --lib "$PLATFORM" --output "$BUILD/dex" "$BUILD/classes.jar"
# Compile optional Android XML resources used by the boot helper.
RESOURCE_ARGS=()
if [[ -d "$BASE/res" ]]; then
  mkdir -p "$BUILD/resources"
  "$TOOLS/aapt2" compile --dir "$BASE/res" -o "$BUILD/resources"
  while IFS= read -r -d '' file; do
    RESOURCE_ARGS+=( -R "$file" )
  done < <(find "$BUILD/resources" -name '*.flat' -print0)
fi
"$TOOLS/aapt2" link -I "$PLATFORM" \
  --manifest "$BASE/AndroidManifest.xml" \
  --min-sdk-version 26 --target-sdk-version 30 \
  "${RESOURCE_ARGS[@]}" \
  -o "$BUILD/unsigned.apk"
(cd "$BUILD/dex" && zip -q -j "$BUILD/unsigned.apk" classes.dex)
"$TOOLS/zipalign" -f 4 "$BUILD/unsigned.apk" "$BUILD/aligned.apk"

# Cle conservée dans le dossier du projet pour autoriser les mises a jour.
SIGNING="$BASE/.signing"
mkdir -p "$SIGNING"
chmod 700 "$SIGNING"
if [[ ! -f "$SIGNING/release.jks" ]]; then
  if ! command -v openssl >/dev/null; then
    echo 'ERREUR: openssl manquant pour la cle de signature.' >&2; exit 1
  fi
  openssl rand -hex 16 > "$SIGNING/password"
  chmod 600 "$SIGNING/password"
  keytool -genkeypair -v \
    -keystore "$SIGNING/release.jks" \
    -alias xgimi-hdmi-boot -keyalg RSA -keysize 3072 -validity 36500 \
    -storepass "$(cat "$SIGNING/password")" \
    -keypass "$(cat "$SIGNING/password")" \
    -dname 'CN=XGIMI HDMI Boot, O=Local' >/dev/null 2>&1
fi
if [[ ! -f "$SIGNING/password" ]]; then
  echo 'ERREUR: cle de signature presente mais mot de passe absent.' >&2
  exit 1
fi
APK="$BASE/XGIMI-HDMI-Boot.apk"
"$TOOLS/apksigner" sign \
  --ks "$SIGNING/release.jks" --ks-key-alias xgimi-hdmi-boot \
  --ks-pass "pass:$(cat "$SIGNING/password")" \
  --key-pass "pass:$(cat "$SIGNING/password")" \
  --out "$APK" "$BUILD/aligned.apk"
"$TOOLS/apksigner" verify --verbose "$APK"
echo "=== APK genere : $APK ==="

if [[ $BUILD_ONLY -eq 1 ]]; then
  echo 'Mode compilation seulement : aucune installation sur le Horizon.'
  exit 0
fi

printf '\n=== Installation sur le Horizon ===\n'
adb connect "$TARGET"
adb -s "$TARGET" install --no-incremental -r "$APK"

# Android 11 exige une exemption pour ouvrir une activite depuis un receiver en fond.
# Le script n'utilise pas de fenetres flottantes, mais l'autorisation est utilisee
# pour permettre l'ouverture automatique de l'activite HDMI a l'amorcage.
if adb -s "$TARGET" shell appops set be.therand.xgimi.hdmiboot SYSTEM_ALERT_WINDOW allow; then
  echo 'Permission de superposition configuree via ADB.'
else
  echo 'ATTENTION: impossible de configurer la permission par ADB.' >&2
  echo 'Il faudra l autoriser depuis les parametres de l application sur le TV.' >&2
fi
adb -s "$TARGET" shell appops get be.therand.xgimi.hdmiboot SYSTEM_ALERT_WINDOW || true
adb -s "$TARGET" shell am start -n be.therand.xgimi.hdmiboot/.SetupActivity
cat <<'NOTICE'

Installation terminee.
1. Ouvrir XGIMI HDMI Boot et verifier la commutation automatique.
2. Pour le redemarrage complet : Parametres > Preferences relatives a l appareil > Accessibilite > XGIMI HDMI Boot (autostart) > Activer.
3. Autoriser egalement Afficher par-dessus les autres applications.
4. Tester veille/reveil (1 seconde) puis redemarrage complet (3 secondes apres connexion du service).
5. Conserver HDMI-CEC/ARC. Si une source HDMI externe prend le dessus, desactiver seulement son option One Touch Play.\n6. La notification permanente est normale pour ce service Android.

Logs (depuis Ubuntu) :
  adb logcat -d -s XgimiHdmiBoot:I ActivityTaskManager:W

Desinstallation :
  adb uninstall be.therand.xgimi.hdmiboot
NOTICE
