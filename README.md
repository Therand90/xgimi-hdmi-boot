# XGIMI HDMI Boot

[Français](README.fr.md) · [Download APK v1.1](https://github.com/Therand90/xgimi-hdmi-boot/releases/tag/v1.1)

**Automatically select HDMI 2 on the original XGIMI Horizon without replacing the Android TV launcher.**

## Features

- Opens the projector's native HDMI 2 input when waking from standby.
- Uses the XGIMI HDMI input application; no root, Tasker or alternative launcher.
- Does not change HDMI-CEC or HDMI ARC settings.
- Provides an on-device enable/disable switch and a manual HDMI 2 test.

## Compatibility

| Item | Tested configuration |
| --- | --- |
| Projector | XGIMI Horizon (original Full HD model, XK03H) |
| Android TV | 11 |
| Firmware | RT.3.24.327 |
| Input | HDMI 2 (`Hdmi2TvInputService/HW2`) |

Other XGIMI models and firmware versions have not been tested. This app uses a model-specific HDMI input identifier.

**Known limitation in v1.1:** automatic recovery after a complete Android reboot is unreliable on the tested projector. The wake-from-standby functionality was confirmed **after the app's service had been started**, but a later full reboot left the service inactive. Opening the app again restores service startup. A fix is being investigated. Do not rely on v1.1 for unattended startup after a power interruption.

## Install the APK

1. [Download the signed APK](https://github.com/Therand90/xgimi-hdmi-boot/releases/download/v1.1/XGIMI-HDMI-Boot.apk) from the **v1.1 release**.
2. Sideload it on the projector using a compatible Android TV file manager or ADB.
3. Open **XGIMI HDMI Boot** on the projector at least once and select **Enable automatic HDMI 2**.
4. Grant the **Display over other apps** permission, if offered by Android TV. On some firmware versions this setting is hidden; see the ADB alternative below.
5. Check **Test HDMI 2 now**, then put the projector in standby and wake it using the remote.

A small persistent notification is expected while the wake-detection service runs.

### Optional ADB installation and permission

Enable and authorize Android debugging on the projector. Replace `PROJECTOR_IP` with its LAN IP address:

```bash
adb connect PROJECTOR_IP:5555
adb -s PROJECTOR_IP:5555 install --no-incremental -r XGIMI-HDMI-Boot.apk
adb -s PROJECTOR_IP:5555 shell appops set be.therand.xgimi.hdmiboot SYSTEM_ALERT_WINDOW allow
```

The projector must support ADB over TCP for this example. The app itself does **not** need a network connection after installation.

## Troubleshooting

If wake switching stops working after a full reboot, open **XGIMI HDMI Boot** again on the projector to restart its wake-detection service (v1.1 known issue). Ensure automation remains enabled and the overlay permission is granted.

For diagnostic logs:

```bash
adb -s PROJECTOR_IP:5555 logcat -d -s XgimiHdmiBoot:I ActivityTaskManager:W
```

## Build from source

Developers can follow [Build and development](docs/DEVELOPMENT.md). Building locally generates a **different signing key**; a locally signed APK cannot update the official release APK without uninstalling it first.

## License and disclaimer

[MIT](LICENSE). Independent community project; not affiliated with or endorsed by XGIMI.
