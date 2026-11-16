# Archive: diagnostic du popup HDMI, novembre 2026

Ce document décrit le **protocole expérimental ayant permis d'identifier** le popup XGIMI et de valider une fermeture ciblée. Le **bouton de diagnostic et les logs `POPUP_PROBE` ont été supprimés de la version v1.3**.

## Preuves sur le Horizon XK03H

- 19:26:19.889 : HDMI 1 devient actif (`[XGIMI CEC] Notify HDMIport:1 5v chg to:1`).
- 19:26:19.988 : `com.xgimi.xhplayer.InputActivity.signalChange()` appelle `showNewSignalDialog()`.
- Le service OEM indépendant `HDMITipWindowManager` signale `not show` dans ce cas.
- L'API matérielle `getHdmiAutoSwitch()` retourne **1**, mais l'accès à un setter pour une application Android non privilégiée n'est **pas établi**.
- L'événement Accessibility du popup porte `com.xgimi.dialog.XGMDialog`. Ce composant est générique : il ne suffit pas à lui seul pour justifier un appui Retour.
- 19:56:43.779 : un événement `TvInputManager.onInputStateChanged(HDMI1, CONNECTED)` a été reçu par l'application.
- 19:56:43.825 : événement `XGMDialog` (46 ms plus tard).
- 19:56:43.832 : `performGlobalAction(GLOBAL_ACTION_BACK)` accepté (7 ms plus tard) ; fermeture du popup **confirmée visuellement** et HDMI 2 conservé.
- L'utilisateur a confirmé que le son ARC, le volume et l'extinction synchronisée fonctionnaient pendant les essais avec One Touch Play désactivé.

## Limites

- Un événement `XGMDialog` indépendant affiché durant la courte fenêtre après HDMI1 pourrait aussi être fermé ; v1.3 conserve donc un interrupteur de désactivation.
- La fermeture du popup ne bloque **pas** les commandes HDMI-CEC `Active Source` / `One Touch Play` qui peuvent forcer un autre port HDMI.
- Pour préserver ARC/eARC et les fonctions d'alimentation CEC, **désactiver seulement « Lecture en une touche » / « One Touch Play » sur l'appareil source qui impose son entrée**, si nécessaire. Ce réglage peut concerner diverses marques et diverses catégories d'appareils ; le Philips EasyLink est uniquement l'exemple testé.
- Les nouveaux délais de la v1.3 (1 s / 3 s) nécessitent des essais supplémentaires sur le projecteur.

Le suivi d'origine figure dans [l'issue #3](https://github.com/Therand90/xgimi-hdmi-boot/issues/3) et la [PR #5](https://github.com/Therand90/xgimi-hdmi-boot/pull/5).
