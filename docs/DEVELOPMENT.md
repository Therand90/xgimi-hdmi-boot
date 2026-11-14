# Development and local builds

Most users should install the [signed v1.2 release APK](https://github.com/Therand90/xgimi-hdmi-boot/releases/tag/v1.2). This page covers development, signing, diagnostics, and release maintenance.

## Prerequisites

Ubuntu/Linux, Java JDK 17+, ADB (only for device installation), curl, zip, unzip and OpenSSL. The build script downloads official Android SDK tools when needed.

```bash
sudo apt update
sudo apt install -y adb openjdk-17-jdk-headless curl zip unzip openssl

git clone https://github.com/Therand90/xgimi-hdmi-boot.git
cd xgimi-hdmi-boot
./INSTALL.sh --build-only
```

To install and open the locally signed APK on your own projector:

```bash
./INSTALL.sh PROJECTOR_IP:5555
```

**Signing:** `INSTALL.sh` stores its private signing key in `.signing/release.jks` and its password in `.signing/password`. These are kept local and ignored by Git. New developer keys **cannot update the official release APK**. GitHub Actions uses a temporary key to verify compilation, so its APK **must never be published as a compatible update**.

## Android implementation

- `BootReceiver`: listens for `BOOT_COMPLETED`, starts `WakeService`, then schedules an HDMI switch after **6 seconds** (fallback path; XGIMI may stop the app during boot).
- `BootAccessibilityService`: opt-in helper to restart `WakeService` when XGIMI firmware places third-party apps in `stopped=true` at boot. Requires explicit user enablement from **Settings → Device Preferences → Accessibility**. It requests no Accessibility events, window content or gestures. If the projector is interactive at connection, it schedules HDMI 2 after **6 seconds**.
- `WakeService`: foreground service, dynamically receives `SCREEN_ON` and `SCREEN_OFF`. On screen-on it schedules HDMI 2 after **4 seconds**; screen-off cancels pending switches.
- `SwitchReceiver`: uses `AlarmManager` and checks overlay permission for starting the HDMI 2 Activity from background.
- `HdmiSwitch`: opens `com.xgimi.xhplayer/.InputActivity` with native passthrough URI:

```text
content://android.media.tv/passthrough/com.xgimi.tvinput%2F.inputservice.Hdmi2TvInputService%2FHW2
```

The Accessibility permission, which is broad on Android, is fully opt-in. The project requests no screen reading or gesture control.

## Diagnosing device state

```bash
D=PROJECTOR_IP:5555
PKG=be.therand.xgimi.hdmiboot

adb connect "$D"
adb -s "$D" shell dumpsys package "$PKG" | grep 'User 0:'
adb -s "$D" shell dumpsys accessibility | grep -iE 'BootAccessibilityService|Bound services'
adb -s "$D" shell dumpsys activity services "$PKG" | grep -E 'WakeService|isForeground'
adb -s "$D" logcat -d -t 1000 -s XgimiHdmiBoot:I '*:S'
```

A real `adb reboot` and remote standby/wake are different events and should be tested separately. After a full reboot, a successful bootstrap shows `stopped=false`, `Bound services`, and `WakeService` foreground.

## Releases — maintainer only

**Never upload local `.signing` files to GitHub.** The v1.2 publication helper `PUBLISH_v1.2.sh` must be run **from the maintainer's Ubuntu checkout** containing the original v1.1 signing key, after the changes have been merged to `main` and CI is green.

```bash
git switch main
git pull --ff-only
bash PUBLISH_v1.2.sh
```

The script refuses a missing key, wrong version, uncommitted changes, a stale `main` branch, or failed CI. It builds the APK locally, downloads the public v1.1 release APK, checks that the signing certificate matches, creates a **draft** v1.2 GitHub release and uploads the APK, verifies the asset, then publishes the release using the maintainer's authenticated `gh` CLI.

The release APK is **not** the GitHub Actions build. Never circumvent the certificate comparison.

## Compatibility and contributing

Tested on the original XGIMI Horizon XK03H, Android TV 11, RT.3.24.327. Other XGIMI models/firmwares may use different HDMI identifiers and system settings.

Include device model, firmware, Android version and redacted logs when filing issues. Do not publish signing keys or personal device/network details.
