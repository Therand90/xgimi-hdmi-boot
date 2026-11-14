#!/usr/bin/env bash
# Enable or disable only the XGIMI HDMI Boot accessibility bootstrap.
# Usage: bash tools/accessibility.sh PROJECTOR_IP:5555 [enable|disable]
# Does not modify any other enabled accessibility services.
set -euo pipefail

DEVICE="${1:-}"
MODE="${2:-enable}"
[[ -n "$DEVICE" ]] || { echo "Usage: bash tools/accessibility.sh IP:5555 [enable|disable]" >&2; exit 2; }
[[ "$MODE" == "enable" || "$MODE" == "disable" ]] || { echo "Invalid mode: $MODE" >&2; exit 2; }

PKG="be.therand.xgimi.hdmiboot"
SVC="$PKG/$PKG.BootAccessibilityService"

command -v adb >/dev/null || { echo "ADB not found." >&2; exit 1; }
adb connect "$DEVICE" >/dev/null
adb -s "$DEVICE" shell pm path "$PKG" | grep -q 'package:' || {
  echo "XGIMI HDMI Boot is not installed." >&2
  exit 1
}
adb -s "$DEVICE" shell dumpsys package "$PKG" |
  grep -Fq "BootAccessibilityService" || {
    echo "The installed app has no accessibility helper. Install the experimental APK first." >&2
    exit 1
  }

OLD_RAW="$(adb -s "$DEVICE" shell settings get secure enabled_accessibility_services | tr -d '\r')"
OLD="$OLD_RAW"
[[ "$OLD" != "null" ]] || OLD=""

# Preserve other services, prevent duplicate entry, handle short and full IDs.
NEW=""
IFS=':' read -r -a SERVICES <<< "$OLD"
for service in "${SERVICES[@]}"; do
  [[ -n "$service" ]] || continue
  case "$service" in
    "$SVC"|"$PKG/.BootAccessibilityService")
      [[ "$MODE" == "disable" ]] && continue
      service="$SVC"
      ;;
  esac
  case ":$NEW:" in *":$service:"*) continue ;; esac
  NEW="${NEW:+$NEW:}$service"
done

if [[ "$MODE" == "enable" ]]; then
  case ":$NEW:" in *":$SVC:"*) ;; *) NEW="${NEW:+$NEW:}$SVC" ;; esac
fi

echo "Previous enabled services: ${OLD:-<none>}"
echo "New enabled services:      ${NEW:-<none>}"

if [[ -n "$NEW" ]]; then
  adb -s "$DEVICE" shell settings put secure enabled_accessibility_services "$NEW"
  adb -s "$DEVICE" shell settings put secure accessibility_enabled 1
else
  adb -s "$DEVICE" shell settings delete secure enabled_accessibility_services
  adb -s "$DEVICE" shell settings put secure accessibility_enabled 0
fi

echo "Result:"
adb -s "$DEVICE" shell settings get secure enabled_accessibility_services
adb -s "$DEVICE" shell settings get secure accessibility_enabled
echo "Accessibility is a sensitive permission. Only enable code you trust."
