# Test — événements de fenêtre du popup HDMI (XK03H)

Cette branche est un **diagnostic expérimental**. Elle ne désactive PAS la commutation HDMI et ne ferme PAS de fenêtre. Elle permet seulement de déterminer si Android Accessibility distingue le popup XGIMI du simple affichage HDMI 2.

## Sécurité et vie privée

- Diagnostic **désactivé par défaut**, à activer explicitement depuis l'application.
- Écoute uniquement `TYPE_WINDOW_STATE_CHANGED` du paquet `com.xgimi.xhplayer`.
- Journalise **seulement** le nom de classe de la fenêtre, son identifiant et l'heure de l'événement.
- `canRetrieveWindowContent=false` et `canPerformGestures=false` : pas de lecture de texte ou d'arbre d'accessibilité, pas de capture, aucun appui sur Retour, aucune action sur les entrées.
- Le service d'accessibilité existant reste utilisé pour amorcer l'écoute du réveil ; la commutation HDMI 2 reste à **4 s** au réveil, **6 s** après reboot.
- Ne pas diffuser les logs complets d'autres applications.

## Installation

Sur Ubuntu, avec les clefs de signature `.signing/release.jks` et `.signing/password` déjà présentes dans le checkout (ne jamais les commiter) :

```bash
cd ~/Téléchargements/xgimi-hdmi-boot
git fetch origin
git switch --track origin/test/popup-window-events
./INSTALL.sh 192.168.0.7:5555
```

Le `versionCode` reste volontairement à **9** (comme la v1.2 stable), afin de revenir à la v1.2 en réinstallant **sans désinstaller** avec la même signature. Le `versionName` est `1.2-popup-probe`.

## Procédure

1. Sur le XGIMI, ouvrir **XGIMI HDMI Boot**, vérifier que **XGIMI HDMI Boot (autostart)** est activé dans Accessibilité.
2. Appuyer sur **Activer le diagnostic du popup**.
3. Laisser le projecteur sur HDMI 2, sans popup : accéder une fois à HDMI 2 depuis l'écran d'accueil et relever les événements normaux.
4. Désactiver temporairement « Lecture 1 pression » sur le Philips (le temps du test seulement). Rallumer le Philips pour faire apparaître « Source détectée » ; ne pas fermer tout de suite.
5. Sur Ubuntu, lire uniquement les événements du diagnostic :
   ```bash
   adb -s 192.168.0.7:5555 logcat -d -s XgimiHdmiBoot:I | grep POPUP_PROBE | tail -40
   ```
6. Appuyer une fois sur **Retour** pour fermer le popup, puis relever à nouveau les événements.
7. Désactiver **Diagnostic du popup** dans l'application.

Comparer `class` et `windowId` AVANT / PENDANT / APRÈS pour vérifier s'il existe une signature **spécifique à ce seul popup**. Si les événements ne donnent pas une signature fiable, **ne pas automatiser BACK**.

## Retour à la version officielle

```bash
cd ~/Téléchargements/xgimi-hdmi-boot
git switch main
./INSTALL.sh 192.168.0.7:5555
```

Le service d'accessibilité peut nécessiter un arrêt/réactivation dans les paramètres du XGIMI après mise à jour, selon le firmware.
