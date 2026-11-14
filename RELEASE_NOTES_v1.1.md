# XGIMI HDMI Boot v1.1

First public release of a small Android TV utility that switches the original XGIMI Horizon to its native HDMI 2 input without changing the launcher.

### Tested configuration
- XGIMI Horizon XK03H (original Full HD model)
- Android TV 11, firmware RT.3.24.327
- HDMI 1 connected to an ARC home-cinema system, HDMI 2 used for video
- Remote-control standby/wake tested successfully: HDMI 2 is selected automatically and the unwanted HDMI 1 switch prompt no longer appears in this setup

### What's included
- HDMI 2 switching 4 seconds after screen-on using a lightweight foreground service
- Cancels pending switching on screen-off
- `BOOT_COMPLETED` handling with a 15-second delay (**implemented, but full cold-boot behavior has not been independently validated**)
- Manual HDMI 2 test and enable/disable controls
- Does not disable or modify HDMI-CEC / ARC

### Installation
Download `XGIMI-HDMI-Boot-v1.1.apk` from this release and sideload it onto the projector. Open the application once, enable the automation, and grant the Android overlay permission (on the tested Horizon firmware it can be allowed via ADB):

```bash
adb connect PROJECTOR_IP:5555
adb -s PROJECTOR_IP:5555 install -r XGIMI-HDMI-Boot-v1.1.apk
adb -s PROJECTOR_IP:5555 shell appops set be.therand.xgimi.hdmiboot SYSTEM_ALERT_WINDOW allow
```

ADB debugging must be enabled and approved on the projector. Follow the [README](README.md) for detailed instructions and source builds.

### Compatibility notes
This APK is designed for the original Horizon's HDMI 2 identifier, `com.xgimi.tvinput/.inputservice.Hdmi2TvInputService/HW2`. Other models and firmware versions are untested. Android firmware restrictions may prevent background launch on other devices.

### Signing
The release APK is signed with the maintainer's local signing key, not the temporary key generated in CI. Keep this same signing key for future updates. Never publish the `.signing/` directory.
