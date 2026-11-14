# XGIMI HDMI Boot

[Français](README.fr.md) · [Download v1.2 APK](https://github.com/Therand90/xgimi-hdmi-boot/releases/tag/v1.2)

**Automatically select native HDMI 2 on the original XGIMI Horizon**, after Android TV reboots and wake-from-standby, without replacing the launcher.

## Features

- Selects HDMI 2 after a **full Android boot** or **remote-control standby/wake**.
- Tested timing: approximately **6 seconds after the boot helper connects**, and **4 seconds after normal wake**.
- Leaves HDMI-CEC and HDMI 1 ARC settings untouched; no root, Tasker or alternative launcher needed.
- Clean on-device interface: an automation toggle, overlay-permission button and permission status.

## Compatibility

| Item | Tested configuration |
| --- | --- |
| Projector | Original XGIMI Horizon Full HD (XK03H) |
| Android TV | 11 |
| Firmware | RT.3.24.327 |
| Input | HDMI 2 (`Hdmi2TvInputService/HW2`) |

**Other XGIMI models and firmware versions are not tested.** HDMI 2 switching relies on the tested projector's native input identifier.

## Install with the TV remote

1. [Download the v1.2 APK](https://github.com/Therand90/xgimi-hdmi-boot/releases/download/v1.2/XGIMI-HDMI-Boot.apk) and sideload it with a compatible Android TV file manager.
2. Open **XGIMI HDMI Boot**. Automatic HDMI 2 is enabled by default; a toggle can disable it.
3. **Enable startup after full reboot** under `Settings → Device Preferences → Accessibility → XGIMI HDMI Boot (autostart) → Enable`. This path and remote-control activation were tested on the original Horizon XK03H.
4. **Allow the app to display over other apps:** `Settings → Apps → Special app access → Display over other apps → XGIMI HDMI Boot`. The tested projector exposes this system menu. The app also includes a **Manage overlay permission** shortcut.
5. Reopen the app to confirm the permission statuses, then test standby/wake using the remote.

**ADB-free setup caveat:** enabling Accessibility using only the remote has been verified. The overlay menu is visible on the tested projector, but a completely fresh install without ADB has not yet been independently validated. An optional ADB fallback is provided below.

The optional Accessibility bootstrap **does not request accessibility events, screen content or gestures**. Accessibility is still a sensitive system permission; enable it only if you trust the app. Without the helper, the tested XGIMI firmware may prevent the app from restarting automatically after a full boot.

A small persistent notification is expected while Android's foreground wake listener is running.

## Optional ADB installation

Replace `PROJECTOR_IP` with the projector's local IP address:

```bash
adb connect PROJECTOR_IP:5555
adb -s PROJECTOR_IP:5555 install --no-incremental -r XGIMI-HDMI-Boot.apk
adb -s PROJECTOR_IP:5555 shell appops set be.therand.xgimi.hdmiboot SYSTEM_ALERT_WINDOW allow
```

Accessibility should be explicitly enabled from the projector's settings (or through the opt-in developer helper `tools/accessibility.sh`). The application doesn't need any network connection once configured.

## Upgrading from v1.1

The **official v1.2 release APK** uses the same signing certificate as v1.1, allowing a direct update without uninstalling or losing app settings. GitHub Actions builds use a **temporary signing key** and are not distributable updates.

## Help and development

If switching doesn't work, check both permissions. Reopening the app refreshes the displayed statuses. Report issues in [GitHub Issues](https://github.com/Therand90/xgimi-hdmi-boot/issues).

[Release notes](RELEASE_NOTES_v1.2.md) · [Build/development instructions](docs/DEVELOPMENT.md)

Independent community project, [MIT licensed](LICENSE); not affiliated with or endorsed by XGIMI.
