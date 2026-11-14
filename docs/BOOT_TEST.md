# v1.2-rc1 experimental boot-recovery test

**Not a public release.** Only tested version 1.1's wake handler has been confirmed on the original Horizon.

This branch adds an **optional Accessibility service** to bootstrap `WakeService` on XGIMI Android 11 builds that mark user-installed apps `stopped=true` during startup. The service does not request accessibility events, read windows or perform gestures. Nevertheless, Accessibility is a powerful Android permission; enable it only if you trust the code. No user-facing accessibility features depend on it.

## Upgrading an existing local installation

The APK version is 1.2-rc1 (versionCode 3). To install over your existing APK, use **the same local .signing directory**, never a new signing key. Preserve your original folder:

```bash
# Run from the GitHub working tree after checking out this branch:
# Only copy your old .signing directory if you trust its origin.
cp -a "$HOME/Téléchargements/XGIMI-HDMI-Boot/.signing" ./.signing
./INSTALL.sh PROJECTOR_IP:5555
```

If you don't have the original key, stop; don't uninstall the working v1.1 just to run this experiment.

## Opt-in configuration

On the projector, open XGIMI HDMI Boot and use **Autoriser le démarrage automatique (Accessibilité)**. Enable only **XGIMI HDMI Boot (autostart)** in system Accessibility settings. It may be necessary to use ADB if XGIMI hides that screen; do not overwrite other accessibility services. The normal overlay permission must also remain enabled. The app's boot helper is optional, so v1.1 behavior remains available without it.

Before reboot, verify `dumpsys accessibility` says the boot helper is **Bound** rather than merely **Binding**, and that `WakeService` is running.

## Full reboot test

Record before, then run a full reboot:

```bash
adb -s PROJECTOR_IP:5555 reboot
```

After reconnecting, **do not open the app manually**:

```bash
D=PROJECTOR_IP:5555
PKG=be.therand.xgimi.hdmiboot
adb connect "$D"
adb -s "$D" shell dumpsys package "$PKG" | grep 'User 0:'
adb -s "$D" shell dumpsys accessibility | grep -i -A 8 'Bound services'
adb -s "$D" shell dumpsys activity services "$PKG" | grep -E 'WakeService|isForeground'
adb -s "$D" logcat -d -t 3000 -s XgimiHdmiBoot:I '*:S'
```

Expected: helper connects and wakes the foreground service automatically. Test **again** after a power-button standby/wake without launching the app. Only then consider merging into `main`. If the service does not bind while `stopped=true`, this firmware also blocks the fallback and we should not merge or call it fixed.
