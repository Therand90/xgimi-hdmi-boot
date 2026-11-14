# XGIMI HDMI Boot v1.2

**Automatic HDMI 2 selection on the original XGIMI Horizon — now restored after full Android reboots.**

## What's new
- **Reliable reboot recovery on the tested Horizon XK03H.** Some XGIMI Android 11 firmware resets third-party apps to `stopped=true` at boot, preventing their ordinary `BOOT_COMPLETED` receiver from running. v1.2 includes an **optional Accessibility bootstrap service** which reconnects the HDMI wake listener after a full reboot.
- **Faster switching:** ~6 seconds after the boot helper connects, and ~4 seconds after normal remote-control standby/wake.
- **Simplified TV interface:** an enable/disable button, a useful **Manage overlay permission** button, and concise Accessibility setup directions visible only when needed. Diagnostic test buttons are removed.
- **No launcher replacement, root or Tasker.** HDMI 1 ARC and HDMI-CEC settings remain unchanged.

## Install / upgrade

Download **`XGIMI-HDMI-Boot.apk`** attached to this release and sideload it onto the projector. This release APK is signed with the **same maintainer signing certificate as v1.1**, so the APK can update v1.1 directly without uninstalling it.

1. Open **XGIMI HDMI Boot** and leave HDMI 2 automation enabled.
2. On the original Horizon, go to **Settings → Device Preferences → Accessibility → XGIMI HDMI Boot (autostart) → Enable** using the remote. This route was tested on the XK03H; no ADB is needed to toggle Accessibility.
3. Allow **Display over other apps** for XGIMI HDMI Boot in Android TV's special app access settings (**Settings → Apps → Special app access → Display over other apps**). The APK has a **Manage overlay permission** button, with a fallback to app details. On the tested projector, this setting is visible in the system menu; enabling it on a fresh, ADB-free installation has not yet been validated.
4. Reopen the app to verify both permission statuses, then put the projector in standby and wake it with the remote. A full Android reboot should also open HDMI 2 automatically.

The Accessibility service requests **no accessibility events, window contents or gestures**. Enabling Accessibility is still a sensitive, optional system permission and requires the user's explicit approval. The app's persistent notification is expected while the wake listener is running.

## Tested platform / limits

- **XGIMI Horizon original Full HD, XK03H**
- **Android TV 11; firmware RT.3.24.327**
- Native HDMI 2 input: `com.xgimi.tvinput/.inputservice.Hdmi2TvInputService/HW2`
- Tested full reboot recovery and 6-second HDMI switch; normal 4-second standby/wake behavior was tested in v1.1 and retained in v1.2.
- **Other XGIMI models / firmware versions are untested.** The native HDMI identifier is model-specific.

See [English instructions](https://github.com/Therand90/xgimi-hdmi-boot/blob/main/README.md) or [instructions en français](https://github.com/Therand90/xgimi-hdmi-boot/blob/main/README.fr.md).

---

## Français — nouveautés v1.2

Cette version corrige **le démarrage automatique après un redémarrage complet d'Android TV** sur le XGIMI Horizon original (XK03H). Le service d'accessibilité facultatif permet de relancer la surveillance HDMI lorsque le firmware empêche l'application de démarrer normalement.

- HDMI 2 après environ **6 secondes** au redémarrage (à partir de la connexion du service), et **4 secondes** à la sortie de veille.
- Interface simplifiée avec **Activer/Désactiver** et **Gérer l'autorisation de superposition**.
- Pour activer le démarrage après reboot, avec la télécommande : **Paramètres → Préférences relatives à l'appareil → Accessibilité → XGIMI HDMI Boot (autostart) → Activer**.
- Autoriser également **Afficher par-dessus les autres applications** dans **Paramètres → Applications → Accès spécial aux applications**.
- Le service d'accessibilité ne demande ni événements d'accessibilité, ni lecture de l'écran, ni gestes. Cette permission reste sensible et son activation est volontaire.
- L'APK signé de cette Release utilise **la même clé que la v1.1** et peut la mettre à jour sans désinstallation.

La navigation manuelle vers les deux menus système a été vérifiée sur le Horizon XK03H. L'activation d'Accessibilité avec la télécommande a été confirmée ; une installation entièrement nouvelle sans ADB reste à vérifier pour l'autorisation de superposition. Les autres modèles et firmwares ne sont pas garantis.

Projet indépendant, sans affiliation avec XGIMI. Licence MIT.
