# XGIMI HDMI Boot

[Français](README.fr.md) · [Latest public stable APK: v1.2](https://github.com/Therand90/xgimi-hdmi-boot/releases/tag/v1.2) · [v1.3 release candidate](https://github.com/Therand90/xgimi-hdmi-boot/pull/5)

**Automatically select native HDMI 2 on the original XGIMI Horizon** after Android TV reboot or wake-from-standby, without replacing the Android TV launcher.

## v1.3 features

- Select HDMI 2 on wake and full boot.
- **Faster tentative timing:** 1 second after screen-on and 3 seconds after the boot Accessibility helper connects (previously 4 / 6 seconds). These faster timings require on-device regression testing. An Android activity-launch exception triggers at most **one retry** 3 seconds later.
- **Dismiss the native XGIMI “Source detected” dialog** when an HDMI 1 device is connected while HDMI 2 is selected, using a guarded Accessibility action. The feature can be turned off in the app.
- No development-only popup diagnostic button.
- No global HDMI-CEC or ARC/eARC setting changes, no root, Tasker or custom launcher.

## HDMI-CEC: One Touch Play / source routing

**Many HDMI-CEC source devices** (AV receivers, soundbars, media players, game consoles, and others) can ask a projector or TV to switch input automatically when powered on. This is separate from the XGIMI “Source detected” popup. Closing the popup cannot prevent an incoming HDMI-CEC source-selection command.

**If your HDMI device steals focus from HDMI 2, disable only its “One Touch Play”, “One-touch playback” or equivalent input-switching feature** in that device's HDMI-CEC settings. The wording varies by manufacturer (for example EasyLink, Anynet+, SIMPLINK, BRAVIA Sync). **Keep global HDMI-CEC enabled** if you rely on ARC/eARC, synced power or volume control.

The tested example was a Philips home cinema system with **EasyLink → One Touch Play disabled**. Philips is **not required**; different HDMI devices need independent verification.

## Compatibility

| Item | Tested configuration |
| --- | --- |
| Projector | Original XGIMI Horizon Full HD (XK03H) |
| Android TV | 11 |
| Firmware | RT.3.24.327 |
| Target | HDMI 2 (`Hdmi2TvInputService/HW2`) |
| Dialog | Native XGIMI `XGMDialog` following HDMI 1 detection |

Other XGIMI models/firmware versions have **not** been tested.

## Install using the remote

1. Until the v1.3 candidate is validated and published, [download the signed v1.2 stable APK](https://github.com/Therand90/xgimi-hdmi-boot/releases/download/v1.2/XGIMI-HDMI-Boot.apk).
2. Open **XGIMI HDMI Boot**. HDMI 2 automation is enabled by default. v1.3 also offers a toggle to disable popup dismissal.
3. **Enable the optional Accessibility service**: `Settings → Device Preferences → Accessibility → XGIMI HDMI Boot (autostart) → Enable`. On the tested projector, this service recovers after full boot and (in v1.3) receives XGIMI HDMI-player window-state events.
4. **Allow Display over other apps**: `Settings → Apps → Special app access → Display over other apps → XGIMI HDMI Boot`. The app also has a shortcut to manage this permission.
5. Reopen the app to confirm both permissions; test standby/wake and a full Android reboot.

**Privacy and limits:** v1.3 does not request access to window contents, screenshots, user text or other apps' Accessibility events. It only receives window-state changes from `com.xgimi.xhplayer`. Automatic BACK requires both an HDMI1-connected TV-input callback and an `XGMDialog` event within 4 seconds, and at most one BACK every 15 seconds. As `XGMDialog` is generic, another XGIMI dialog appearing in the same short interval could be dismissed. This optional behavior can be disabled.

The persistent notification for the wake listener is expected.

## Optional ADB install

```bash
adb connect PROJECTOR_IP:5555
adb -s PROJECTOR_IP:5555 install --no-incremental -r XGIMI-HDMI-Boot.apk
adb -s PROJECTOR_IP:5555 shell appops set be.therand.xgimi.hdmiboot SYSTEM_ALERT_WINDOW allow
```

Accessibility still requires explicit consent from the projector user. Internet access is not needed for normal operation once configured.

## Updates and releases

The v1.2 release is signed with the original maintainer certificate. Official v1.3 builds must use the **same key** so users can update without uninstalling. GitHub Actions builds are signed with a **temporary key** and must never be shipped as compatible release APKs.

[v1.3 release notes](RELEASE_NOTES_v1.3.md) · [Development guide](docs/DEVELOPMENT.md) · [GitHub Issues](https://github.com/Therand90/xgimi-hdmi-boot/issues)

Independent [MIT-licensed](LICENSE) community project, not affiliated with or endorsed by XGIMI.
