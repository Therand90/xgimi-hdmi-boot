# Standby/wake delay experiment (v1.2-8s-test)

The published v1.2 remains unchanged. This test changes **only** the `ACTION_SCREEN_ON` delay from 4,000 ms to 8,000 ms, to determine whether the HDMI 2 request was being issued before the projector was ready or before another HDMI component selected its source.

**Full Android reboot remains at 6,000 ms.** No HDMI-CEC/ARC setting is altered. Only **one HDMI switch** is requested per screen-on; there is no retry which could override later manual user source choices.

## Install over the signed v1.2 on the original Horizon XK03H

Use the same local `.signing/` directory as for the published release. Do not uninstall the working app.

```bash
cd ~/Téléchargements/xgimi-hdmi-boot
git fetch origin
git switch --track origin/fix/standby-wake-delay-8s
test -f .signing/release.jks && test -f .signing/password
./INSTALL.sh 192.168.0.7:5555
```

The APK is `1.2-8s-test` (**versionCode 9**, unchanged from v1.2) and uses the same signing key. Keeping the same versionCode makes it possible to reinstall the stable v1.2 with `adb install -r` if the test fails; this is a temporary test build, **not** a release. Before the test, confirm the app's Accessibility and overlay permissions remain enabled. The Accessibility setting must not be silently changed.

## Controlled standby/wake observations

With the HDMI 2 source currently selected, put the projector into standby using the normal remote control, then wake it again. Note whether HDMI 1 briefly appears, whether HDMI 2 ever appears, and what input is active after approximately 15–20 seconds. Repeat on separate wake cycles if useful.

Only after the wake and **before rebooting Android** collect:

```bash
D=192.168.0.7:5555
adb connect "$D"
adb -s "$D" shell dumpsys activity services be.therand.xgimi.hdmiboot |
  grep -E 'WakeService|isForeground'
adb -s "$D" logcat -d -s XgimiHdmiBoot:I '*:S' | tail -60
```

The relevant logs should say `SCREEN_ON recu : HDMI 2 programme dans 8 secondes` and `Commutation programmee dans 8 s`, then `Demande de commutation HDMI 2 envoyee`.

**Interpretation:** the last message means `startActivity()` returned without throwing; it does **not** prove XGIMI actually selected HDMI 2. If HDMI 1 still wins, the next step is to inspect XGIMI HDMI and CEC activity around the wake and confirm whether a second source-selection event overrides our request. Do not blindly increase delays or automatically retry repeatedly.

## Reverting

To restore the current public build, reinstall the *published* v1.2 APK with `adb install --no-incremental -r XGIMI-HDMI-Boot.apk` (downloaded from the official v1.2 GitHub Release). VersionCode stays 9 and the same release signing certificate is used, so normal reinstalls preserve settings. Avoid uninstalling or resetting app data unnecessarily. The change remains isolated on a test branch.

Only publish a new compatible release after on-device confirmation.
