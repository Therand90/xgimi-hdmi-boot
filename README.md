# XGIMI HDMI Boot

**Automatically switch an XGIMI Horizon projector to HDMI 2 on wake or boot, without replacing the Android TV launcher.**

[Version française](README.fr.md)

## Why?

The original XGIMI Horizon may suggest switching to the HDMI/ARC sound system connected to HDMI 1, even when the actual video source is connected to HDMI 2. This small Android TV app opens the native XGIMI HDMI 2 input using the TV input passthrough URI, without using Projectivy Launcher, Tasker, root, or simulated remote-control clicks.

## Confirmed working

- **Device tested:** XGIMI Horizon XK03H (original Full HD model)
- **Android TV:** 11
- **Firmware:** RT.3.24.327
- **Version:** 1.1
- **Real-world result:** after power off/on with the projector's remote, the projector automatically switches to HDMI 2. HDMI 1 source suggestion no longer appears in the tested setup. HDMI ARC/CEC continues functioning.

**Important:** Only the wake-from-standby path has been confirmed end-to-end on this firmware. The `BOOT_COMPLETED` full-Android-reboot path is implemented, but not independently confirmed. Other XGIMI models and firmware builds may use different HDMI input IDs or impose different background-launch restrictions.

## How it works

- A small foreground Android service listens for `ACTION_SCREEN_ON` and `ACTION_SCREEN_OFF`.
- On screen on, it schedules the HDMI 2 launch after **4 seconds**.
- On screen off, it cancels pending switches.
- After Android `BOOT_COMPLETED`, it attempts an HDMI 2 launch after **15 seconds**.
- It launches the manufacturer's activity `com.xgimi.xhplayer/.InputActivity`, with the HDMI 2 passthrough URI:

```text
content://android.media.tv/passthrough/com.xgimi.tvinput%2F.inputservice.Hdmi2TvInputService%2FHW2
```

It does **not** disable HDMI-CEC or ARC, and does **not** require network access on the projector. The foreground service displays a persistent notification while enabled.

## Build and install from Ubuntu/Linux

You need an Ubuntu/Linux PC with `adb`, Java JDK (17 or later), `curl`, `zip`, `unzip`, `openssl`, and `keytool`. The installation script downloads the official Android command-line tools / SDK when absent.

```bash
sudo apt update
sudo apt install -y adb openjdk-17-jdk-headless curl zip unzip openssl

git clone https://github.com/Therand90/xgimi-hdmi-boot.git
cd xgimi-hdmi-boot
chmod +x INSTALL.sh
./INSTALL.sh PROJECTOR_IP:5555
```

Replace `PROJECTOR_IP` with the address of your projector. Enable and authorize ADB debugging on the projector beforehand. The example GitHub URL is intended for this project once the repository is published.

For local build only:

```bash
./INSTALL.sh --build-only
```

After installation, open **XGIMI HDMI Boot** once on the projector and leave the automation enabled. This first launch is important: Android excludes force-stopped applications from boot events. The installer tries to grant the overlay app-op using ADB; on other systems, grant it manually if needed. Use the app's **Test HDMI 2 now** button first, then test powering off/on using the normal remote control.

### Diagnose

```bash
adb connect PROJECTOR_IP:5555
adb -s PROJECTOR_IP:5555 logcat -d -s XgimiHdmiBoot:I ActivityTaskManager:W
```

If no automatic switching occurs, check that the service is running, overlay permission is granted, and `SCREEN_ON` is logged after a remote wake.

### Signing key / updating

The installer **generates a local signing key** in `.signing/` and uses it for future updates. **Back it up safely and never commit or share it.** Keep the same `.signing/` directory when updating from version 1.0 or 1.1: APKs signed with different keys cannot update an existing installation. `.gitignore` excludes `.signing/` and generated `.apk` files.

### Uninstall

```bash
adb -s PROJECTOR_IP:5555 uninstall be.therand.xgimi.hdmiboot
```

## Compatibility and limitations

- The HDMI input URI is hard-coded for the **original Horizon's HDMI 2 input**. Other models require verification with `adb shell dumpsys tv_input` and may need code changes.
- Android's background activity restrictions and vendor battery management can prevent automatic switching on other devices.
- A foreground-service notification is expected. This application does not monitor or modify the HDMI-CEC state.
- This is an independent community project, not affiliated with XGIMI.

## License

MIT — see [LICENSE](LICENSE).
