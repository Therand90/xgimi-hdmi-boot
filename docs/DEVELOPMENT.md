# Development and local builds — v1.3

Official release: [v1.3](https://github.com/Therand90/xgimi-hdmi-boot/releases/tag/v1.3). The v1.3 wake timing and full reboot behavior were verified on the original Horizon XK03H. This document also describes the maintainer-only signing/release procedure.

## Prerequisites

Ubuntu/Linux, Java JDK 17+, ADB for installation, curl, zip, unzip and OpenSSL. Android SDK tools are downloaded by `INSTALL.sh` if not installed.

```bash
sudo apt update
sudo apt install -y adb openjdk-17-jdk-headless curl zip unzip openssl
git clone https://github.com/Therand90/xgimi-hdmi-boot.git
cd xgimi-hdmi-boot
./INSTALL.sh --build-only
```

For installation on a projector with USB/network ADB: `./INSTALL.sh PROJECTOR_IP:5555`.

**Signing:** keep the original `.signing/release.jks` and `.signing/password` locally. `INSTALL.sh` generates a new key only if no key is present; a new key CANNOT update an existing official app. Never commit or upload private signing materials. CI APKs use disposable signing keys.

## Android design

- `BootReceiver` receives `BOOT_COMPLETED`, starts `WakeService`, schedules HDMI2 after **3 seconds**.
- `BootAccessibilityService` is opt-in and recovers from XGIMI firmware's `stopped=true` behavior after reboot. If already interactive when connected, it schedules HDMI2 after **3 seconds**.
- `WakeService` registers `ACTION_SCREEN_ON/OFF`; screen-on schedules HDMI2 after **1 second**, screen-off cancels pending switches.
- `SwitchReceiver` uses `AlarmManager` and the overlay permission to open `InputActivity` in background; if starting that activity throws, it retries **only once after 3 seconds**. An accepted `startActivity` is NOT proof HDMI2 is visible: test on actual hardware.
- `HdmiSwitch` opens native `com.xgimi.xhplayer/.InputActivity` using passthrough URI:

```text
content://android.media.tv/passthrough/com.xgimi.tvinput%2F.inputservice.Hdmi2TvInputService%2FHW2
```

## Guarded source-popup dismissal

The native XGIMI HDMI player can display a custom `XGMDialog` when a previously inactive HDMI1 source starts providing 5 V / a video signal. An unrelated OEM service also exposes HDMI tips; its appearance is not the source-dialog confirmation.

`BootAccessibilityService` registers a `TvInputManager.TvInputCallback` and records when the exact `Hdmi1TvInputService/HW1` becomes `INPUT_STATE_CONNECTED`. If `com.xgimi.xhplayer` raises a `TYPE_WINDOW_STATE_CHANGED` event with class exactly `com.xgimi.dialog.XGMDialog` within **4 seconds**, a single `GLOBAL_ACTION_BACK` can dismiss the popup. Rate limiting is **15 seconds**. The feature is enabled by default but can be switched off in the app. If the primary HDMI2 automation is disabled, the service never sends BACK.

The Accessibility service is explicitly user-enabled. It requests **only XHPlayer window-state events**; `canRetrieveWindowContent=false`, `canPerformGestures=false`. It does not retrieve view trees, screen text, other-app activity or screenshots. `XGMDialog` is a generic XGIMI component and there remains a bounded risk of dismissing an unrelated dialog opened during the 4-second HDMI1 connection window.

**CEC source routing remains separate.** For external devices that select themselves using HDMI-CEC One Touch Play, document that users may need to disable that narrow input-selection feature. Never disable global CEC/ARC as a workaround.

## Test checklist

1. Standby/wake repeatedly with HDMI2 displayed; validate the new **1 s** wake attempt.
2. Full Android reboot; validate the **3 s** boot attempt, Accessibility helper connection, service persistence.
3. Turn on an HDMI1 external source with One Touch Play disabled; popup must close without leaving HDMI2.
4. Confirm ARC/eARC audio, volume and CEC power controls still work.
5. Temporarily disable the popup dismissal toggle and verify normal manual BACK behavior.
6. Confirm unrelated XGIMI dialogs and gameplay are unaffected.

Relevant non-invasive diagnostics:

```bash
D=PROJECTOR_IP:5555
adb -s "$D" logcat -d -s XgimiHdmiBoot:I | tail -100
adb -s "$D" shell dumpsys accessibility | grep -i BootAccessibilityService
```

## Publish — maintainer only

Once the release candidate is hardware-tested and merged into `main`, run from the **original local signed checkout**:

```bash
git switch main
git pull --ff-only
bash PUBLISH_v1.3.sh
```

`PUBLISH_v1.3.sh` checks versionCode/versionName, a clean up-to-date `main`, latest successful CI for that exact commit, presence of original signing key, and matching APK signing digest against the official v1.2 release. It creates and uploads a signed **draft release** before making it public.

Do not bypass certificate/CI guards. Do not publish a GitHub Actions APK.
