# v1.2-rc2 experimental boot-recovery test

**Not a public release.** The v1.1 release is unchanged.

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

The expected APK is **1.2-rc2 (versionCode 4)**.

## Enable Accessibility when the Android TV settings screen is missing

On some XGIMI firmware, Android has no Activity which handles `Settings.ACTION_ACCESSIBILITY_SETTINGS`. It is therefore **not available from the app UI**. The experimental build shows a readable status instead.

From Ubuntu, explicitly opt in through ADB:

```bash
bash tools/accessibility.sh PROJECTOR_IP:5555 enable
```

This preserves other accessibility services already enabled on the device, and activates only `be.therand.xgimi.hdmiboot/.BootAccessibilityService`.

To withdraw that permission later:

```bash
bash tools/accessibility.sh PROJECTOR_IP:5555 disable
```

The script never changes HDMI-CEC or ARC. The overlay permission must also remain granted.

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

## UI changes in rc2

The on-projector screen is now scrollable with one enable/disable button and permission status labels. Removed: the manual HDMI test, the delayed test, the broken Accessibility Settings shortcut, and the superposition Settings shortcut. Permission management stays in the Linux/ADB guide.
