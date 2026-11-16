# XGIMI HDMI Boot v1.3

**Faster HDMI 2 startup and automatic dismissal of XGIMI “Source detected” prompts on the original Horizon XK03H.**

## Changes

- **Guarded popup dismissal:** after `TvInputManager` detects HDMI1 becoming connected, an accessibility window-state event identifying `com.xgimi.dialog.XGMDialog` within 4 seconds triggers one `GLOBAL_ACTION_BACK`. There is a 15-second cooldown. On-device test confirmed the popup disappeared and HDMI2 stayed visible. Can be switched off in the app.
- **Faster planned switching:** wake delay now **1 second** (previously 4), full-boot Accessibility bootstrap now **3 seconds** (previously 6). If launching the HDMI2 Activity fails with an exception, a single retry occurs 3 seconds later. Both faster wake and full-reboot switching have now been tested successfully on the Horizon XK03H.
- **Clean interface:** removed the developer popup-event diagnostic button and logging. Popup handling remains independently configurable.
- **HDMI-CEC and ARC/eARC preserved:** no changes to Android's global CEC or XGIMI firmware. No root or alternative launcher needed.

## HDMI-CEC One Touch Play — brand-neutral guidance

Some HDMI-CEC devices such as **AV receivers, soundbars, media players, set-top boxes or game consoles** may send an automatic source-switching command when powered on. This is **separate from XGIMI's source-detected dialog** and is not blocked by popup dismissal.

If another HDMI source takes priority over HDMI2, disable **only that device's “One Touch Play”, “One-touch playback”, « Lecture en une touche », or equivalent auto-input-selection feature**. Different brands expose the setting under names such as EasyLink, Anynet+, SIMPLINK or BRAVIA Sync. **Do not disable global HDMI-CEC** if ARC/eARC, volume or power synchronization is needed.

The only verified example was a Philips home cinema with EasyLink → “Lecture 1 pression” disabled. **Philips is not a requirement** for the app.

## Installation

Install the **official maintainer-signed** `XGIMI-HDMI-Boot.apk`, not a GitHub Actions CI artifact. This release's signing certificate must match the public v1.2 APK so existing installations can update without uninstalling.

1. Keep HDMI2 automation enabled in the app.
2. Enable `Settings → Device Preferences → Accessibility → XGIMI HDMI Boot (autostart)` on the projector, for reboot recovery and popup dismissal.
3. Grant `Display over other apps` permission in Android TV's special-access settings.
4. If another device uses One Touch Play to take over the source, disable only that option on that external device.
5. Test standby/wake, full reboot, popup handling and CEC/ARC.

**Accessibility privacy and residual risk:** Subscribes only to XGIMI HDMI-player window-state changes, with `canRetrieveWindowContent=false` and `canPerformGestures=false`. Does not read texts or screen contents. Because `XGMDialog` is a generic XGIMI class, an unrelated dialog appearing within 4 seconds of HDMI1 detection could also be closed. The feature is user-disableable.

Tested device: original XGIMI Horizon Full HD XK03H, Android TV 11, firmware RT.3.24.327. Other models not verified.

See [French instructions](https://github.com/Therand90/xgimi-hdmi-boot/blob/main/README.fr.md) and [English instructions](https://github.com/Therand90/xgimi-hdmi-boot/blob/main/README.md).

---

## Français — nouveautés v1.3

- **Fermeture automatique du popup « Source détectée »** après détection HDMI1 et apparition du dialogue XGIMI (test validé sur le Horizon original, HDMI2 conservé). Fonction désactivable.
- **Passage plus rapide à HDMI2 :** **1 seconde à la sortie de veille** et **3 secondes après connexion du service au redémarrage complet**, contre 4/6 secondes. Les deux nouveaux délais ont été validés sur le Horizon XK03H (sortie de veille et redémarrage complet).
- **Interface nettoyée :** suppression du diagnostic expérimental, maintien d'un bouton d'activation/désactivation de la fermeture automatique.
- **Conseil générique HDMI-CEC :** si un équipement HDMI externe impose sa source au démarrage, désactiver **uniquement « Lecture en une touche » / « One Touch Play » (ou équivalent) sur cet équipement**. Garder HDMI-CEC global et ARC/eARC activés. EasyLink / Philips est seulement l'exemple testé.
- L'accessibilité reste volontaire : événements de fenêtres du lecteur HDMI XGIMI uniquement, sans lecture de l'écran. Exception possible : un autre dialogue XGIMI ouvert juste après la connexion HDMI1 pourrait être fermé.
- **Mise à jour depuis la v1.2 :** la Release officielle doit utiliser la même signature privée que la v1.2, jamais l'APK temporaire de GitHub Actions.

Projet indépendant sous licence MIT, sans affiliation avec XGIMI.
