# Development and local builds

This page documents source builds and diagnostics. Most users should install the [signed release APK](https://github.com/Therand90/xgimi-hdmi-boot/releases/tag/v1.1).

## Prerequisites

Linux/Ubuntu, Java JDK 17 or later, ADB, curl, zip, unzip and OpenSSL. The installer downloads Android SDK command-line tools if needed.

```bash
sudo apt update
sudo apt install -y adb openjdk-17-jdk-headless curl zip unzip openssl

git clone https://github.com/Therand90/xgimi-hdmi-boot.git
cd xgimi-hdmi-boot
./INSTALL.sh --build-only
```

To compile, install and open the app over ADB:

```bash
./INSTALL.sh PROJECTOR_IP:5555
```

**Signing:** the installer creates `.signing/release.jks` and `.signing/password` on the local machine. Keep those files private and backed up. Each independent local build using a fresh key cannot update an APK signed with another key, including the official published release. CI only verifies compilation and uses a temporary key; **CI APKs are not distributable updates**.

## Android implementation

- `BootReceiver`: listens for `BOOT_COMPLETED` and attempts to start `WakeService`, then schedules a 15-second input switch.
- `WakeService`: a foreground service that dynamically registers for `SCREEN_ON` and `SCREEN_OFF`.
- `SwitchReceiver`: uses `AlarmManager` for delayed switching and checks the overlay permission.
- `HdmiSwitch`: opens `com.xgimi.xhplayer/.InputActivity` with the HDMI 2 TV passthrough URI:

```text
content://android.media.tv/passthrough/com.xgimi.tvinput%2F.inputservice.Hdmi2TvInputService%2FHW2
```

### Diagnostics

```bash
D=PROJECTOR_IP:5555
PKG=be.therand.xgimi.hdmiboot

adb connect "$D"
adb -s "$D" shell getprop sys.boot_completed
adb -s "$D" shell dumpsys package "$PKG" | grep 'User 0:'
adb -s "$D" shell dumpsys activity services "$PKG"
adb -s "$D" logcat -d -b all | grep -iE 'XgimiHdmiBoot|WakeService|BootReceiver|ForegroundService'
```

When working on cold-boot handling, do not confuse `adb reboot` with a projector standby/wake from the remote. Both cases need independent testing.

## Known issue (v1.1)

A complete Android reboot may leave `WakeService` inactive. Opening `SetupActivity` manually restores service startup, after which HDMI 2 switching on wake has been observed to work. The root cause is not yet confirmed; collect boot logs and service state before changing startup logic. Do not publish a new release as fixed without checking both boot and standby/wake on real hardware.

## Compatibility

Only the original Horizon XK03H running Android TV 11 / RT.3.24.327 has been tested. Other models may expose different TV input identifiers.

## Contributing

Please include the XGIMI model, firmware, Android version, and relevant logs in issues. Never upload signing keys, private device logs, or home-network addresses.
