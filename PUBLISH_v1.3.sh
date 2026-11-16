#!/usr/bin/env bash
# Publish XGIMI HDMI Boot v1.3 with the MAINTAINER's existing local signing key.
# This deliberately never uploads .signing or uses GitHub Actions' temporary key.
set -euo pipefail

BASE="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$BASE"

REPO="Therand90/xgimi-hdmi-boot"
TAG="v1.3"
APK="$BASE/XGIMI-HDMI-Boot.apk"
PREVIOUS="$BASE/build/XGIMI-HDMI-Boot-v1.2-reference.apk"
SDK="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Android/Sdk}}"
APKSIGNER="$SDK/build-tools/35.0.0/apksigner"

for bin in git gh curl awk; do
    command -v "$bin" >/dev/null || { echo "Missing command: $bin" >&2; exit 1; }
done
[[ -x "$APKSIGNER" ]] || { echo "Missing apksigner at $APKSIGNER" >&2; exit 1; }
[[ -s .signing/release.jks && -s .signing/password ]] || {
    echo "ABORT: existing signing key or password missing. Do not generate a new key for release." >&2
    exit 1
}
[[ "$(git branch --show-current)" == "main" ]] || {
    echo "ABORT: switch to main, then run git pull --ff-only." >&2; exit 1;
}
[[ -z "$(git status --porcelain --untracked-files=no)" ]] || {
    echo "ABORT: tracked source files have uncommitted changes." >&2; exit 1;
}
git fetch -q origin main
[[ "$(git rev-parse HEAD)" == "$(git rev-parse origin/main)" ]] || {
    echo "ABORT: local main is not up-to-date with origin/main." >&2; exit 1;
}
[[ "$(gh repo view "$REPO" --json nameWithOwner --jq .nameWithOwner)" == "$REPO" ]] || {
    echo "ABORT: cannot access expected GitHub repository." >&2; exit 1;
}
if gh release view "$TAG" -R "$REPO" >/dev/null 2>&1; then
    echo "ABORT: release $TAG already exists; do not overwrite it automatically." >&2
    exit 1
fi

echo "=== Validate release source version ==="
grep -q 'android:versionCode="10" android:versionName="1.3"' AndroidManifest.xml || {
    echo "ABORT: source is not final v1.3 (versionCode 10)." >&2; exit 1;
}

echo "=== Verify latest main CI ==="
CI="$(gh run list -R "$REPO" --workflow build.yml --branch main --limit 1 \
    --json headSha,status,conclusion \
    --jq '.[0] | (.headSha // "") + "|" + (.status // "") + "|" + (.conclusion // "")')"
EXPECTED="$(git rev-parse HEAD)|completed|success"
[[ "$CI" == "$EXPECTED" ]] || {
    echo "ABORT: latest CI is not successful for this exact main commit." >&2
    echo "Expected: $EXPECTED" >&2
    echo "Observed: $CI" >&2
    echo "Retry after GitHub Actions finishes." >&2
    exit 1
}

echo "=== Build with original local signing key ==="
./INSTALL.sh --build-only
"$APKSIGNER" verify --verbose --print-certs "$APK" >/dev/null

echo "=== Compare signer to the published v1.2 APK ==="
mkdir -p "$BASE/build"
curl -fLsS --retry 3 \
    "https://github.com/$REPO/releases/download/v1.2/XGIMI-HDMI-Boot.apk" \
    -o "$PREVIOUS"
OLD_SHA="$("$APKSIGNER" verify --print-certs "$PREVIOUS" | \
    awk -F ': ' '/Signer #1 certificate SHA-256 digest:/ {print $2; exit}')"
NEW_SHA="$("$APKSIGNER" verify --print-certs "$APK" | \
    awk -F ': ' '/Signer #1 certificate SHA-256 digest:/ {print $2; exit}')"
[[ -n "$OLD_SHA" && "$OLD_SHA" == "$NEW_SHA" ]] || {
    echo "ABORT: signing certificate differs from v1.2 (or could not be verified)." >&2
    echo "Never publish a non-upgradable APK." >&2
    exit 1
}
echo "OK: same signing certificate as v1.2."

echo "=== Create a draft release and upload the verified signed APK ==="
gh release create "$TAG" "$APK#XGIMI-HDMI-Boot.apk" \
    -R "$REPO" --target main \
    --title "XGIMI HDMI Boot v1.3" \
    --notes-file RELEASE_NOTES_v1.3.md \
    --draft

ASSET="$(gh release view "$TAG" -R "$REPO" --json assets --jq '.assets[].name')"
grep -qx "XGIMI-HDMI-Boot.apk" <<< "$ASSET" || {
    echo "ABORT: upload not confirmed. Release remains a draft; inspect it manually." >&2
    exit 1
}
echo "=== Publish release ==="
gh release edit "$TAG" -R "$REPO" --draft=false --latest
echo "Published: https://github.com/$REPO/releases/tag/$TAG"
