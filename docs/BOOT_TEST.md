# v1.2-rc5 experimental boot-recovery test

**Not a public release.** The v1.1 release is unchanged.

On the original Horizon XK03H (Android TV 11 / RT.3.24.327), the optional Accessibility helper has now **survived a full reboot** and restarted WakeService automatically, with the projector returning to HDMI 2. The 6-second full-boot HDMI switch delay has also been tested successfully. Remote standby/wake still uses 4 s.

This branch experiments with an **optional Accessibility service** to bootstrap `WakeService` on XGIMI Android TV 11 firmware that marks user apps `stopped=true` after a full reboot.

The Accessibility service requests **no events**, does not read window contents, and does not perform gestures. Nonetheless, Accessibility is a sensitive Android capability. Enable it only if you trust the application.

## Installation without changing your signing identity

Use your existing GitHub checkout and **keep its existing `.signing/` directory**. If you built v1.1 from a separate source folder, copy the original `.signing/` directory into this checkout *before* building. Do not uninstall v1.1 or make a new signing key accidentally.

```bash
cd ~/Téléchargements/xgimi-hdmi-boot
git fetch origin
git switch fix/boot-accessibility-bootstrap
git pull --ff-only
./INSTALL.sh PROJECTOR_IP:5555
```

The expected APK is **1.2-rc5 (versionCode 7)**.

## Enable Accessibility from the XGIMI remote — verified on Horizon XK03H

The tested original Horizon exposes the accessibility menu **manually**, even though Android's `Settings.ACTION_ACCESSIBILITY_SETTINGS` intent and the tested shortcut activities do not open it.

On the projector, open:

**Settings → Device Preferences → Accessibility → XGIMI HDMI Boot (autostart) → Enable**.

This was confirmed on the original XK03H: the user could disable and re-enable the helper with the remote, without issuing ADB commands. Reopen the app afterward to refresh its status label. No automatic enabling is required or attempted.

**The overlay permission is separate.** The current app checks `Settings.canDrawOverlays()` before launching HDMI 2 from the background. It was granted on the tested projector with ADB by `INSTALL.sh`. **We have not yet validated a fully ADB-free first install**, so do not promise one in a public release until overlay can also be enabled through Android TV UI or an approved alternative mechanism is verified.

### Optional ADB fallback for Accessibility

If a different XGIMI firmware does not expose the service in its Accessibility settings, a user who explicitly opts in can run:

```bash
bash tools/accessibility.sh PROJECTOR_IP:5555 enable
```

This script preserves all other enabled services. To disable only this helper:

```bash
bash tools/accessibility.sh PROJECTOR_IP:5555 disable
```

This script does not change HDMI-CEC, ARC, or the overlay permission.

## Verify service binding **before** reboot

```bash
D=PROJECTOR_IP:5555
adb -s "$D" shell settings get secure enabled_accessibility_services
adb -s "$D" shell dumpsys accessibility | grep -iE 'BootAccessibilityService|Bound services|Binding services'
adb -s "$D" shell dumpsys activity services be.therand.xgimi.hdmiboot | grep -E 'WakeService|isForeground'
adb -s "$D" logcat -d -t 1000 -s XgimiHdmiBoot:I '*:S'
```

Look for `Accessibility helper connected` and `WakeService` active. Merely seeing the service name in secure settings is **not proof** that Android has bound it.

## Test the actual reboot and normal standby

```bash
adb -s PROJECTOR_IP:5555 reboot
```

After reconnection, **do not open the app manually**:

```bash
D=PROJECTOR_IP:5555
PKG=be.therand.xgimi.hdmiboot
adb connect "$D"
adb -s "$D" shell dumpsys package "$PKG" | grep 'User 0:'
adb -s "$D" shell dumpsys accessibility | grep -iE 'BootAccessibilityService|Bound services|Binding services'
adb -s "$D" shell dumpsys activity services "$PKG" | grep -E 'WakeService|isForeground'
adb -s "$D" logcat -d -t 1000 -s XgimiHdmiBoot:I '*:S'
```

Only if Android rebinds the helper and restores `WakeService`, verify subsequent normal remote standby/wake without opening the app. **Do not merge or call this fixed** without both tests on an original Horizon XK03H.

## UI changes in rc5

The on-projector screen is scrollable, with the automation toggle, a restored **Manage overlay permission** button, permission status labels, and remote-control Accessibility menu directions when the boot helper is disabled. The overlay shortcut uses the standard Android intent with an app-details fallback; whether that shortcut visibly opens on XK03H still needs on-device confirmation. Unnecessary manual/delayed HDMI diagnostic buttons and the broken Accessibility shortcut remain removed. Both Accessibility and Overlay permissions have system menus accessible manually with the remote on this model; only Accessibility has been tested for enable/disable through the remote. The ADB installer grants Overlay automatically, so a fresh install without ADB has not yet been validated.
