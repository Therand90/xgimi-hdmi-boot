# XGIMI HDMI Boot

[English](README.md) · [Télécharger l'APK v1.1](https://github.com/Therand90/xgimi-hdmi-boot/releases/tag/v1.1)

**Sélection automatique de HDMI 2 sur le XGIMI Horizon original, sans remplacer le launcher Android TV.**

## Fonctionnalités

- Ouvre l'entrée HDMI 2 native du projecteur à la sortie de veille.
- Utilise l'application HDMI de XGIMI : pas de root, de Tasker ni de launcher alternatif.
- Ne modifie pas les réglages HDMI-CEC ou HDMI ARC.
- Permet d'activer ou désactiver l'automatisation et de tester manuellement HDMI 2.

## Compatibilité

| Élément | Configuration testée |
| --- | --- |
| Projecteur | XGIMI Horizon original (Full HD, XK03H) |
| Android TV | 11 |
| Firmware | RT.3.24.327 |
| Entrée | HDMI 2 (`Hdmi2TvInputService/HW2`) |

Les autres modèles XGIMI et versions du firmware n'ont pas été testés. L'identifiant de l'entrée HDMI est spécifique au modèle.

**Limitation connue de la v1.1 :** après un redémarrage complet d'Android, le service de détection du réveil peut ne pas se relancer. La sélection automatique à la sortie de veille a été confirmée **après activation du service**, mais un redémarrage complet a ensuite laissé ce service inactif. Ouvrir de nouveau l'application permet de le relancer. Un correctif est à l'étude. Ne pas compter sur cette version pour un démarrage autonome après une coupure de courant.

## Installer l'APK

1. [Télécharger l'APK signé](https://github.com/Therand90/xgimi-hdmi-boot/releases/download/v1.1/XGIMI-HDMI-Boot.apk) depuis la **Release v1.1**.
2. Installer l'APK sur le projecteur avec un gestionnaire de fichiers Android TV compatible ou ADB.
3. Ouvrir **XGIMI HDMI Boot** au moins une fois et sélectionner **Activer le HDMI 2 automatique**.
4. Autoriser l'**affichage par-dessus les autres applications**, si Android TV propose le réglage. Sur certains firmwares, ce menu est masqué ; une alternative ADB figure ci-dessous.
5. Tester avec **Tester HDMI 2 maintenant**, puis mettre le projecteur en veille et le rallumer avec la télécommande.

Une petite notification permanente est normale lorsque le service de détection du réveil est actif.

### Installation et autorisation via ADB (facultatif)

Activer et autoriser le débogage Android sur le projecteur. Remplacer `IP_DU_PROJECTEUR` par son adresse IP locale :

```bash
adb connect IP_DU_PROJECTEUR:5555
adb -s IP_DU_PROJECTEUR:5555 install --no-incremental -r XGIMI-HDMI-Boot.apk
adb -s IP_DU_PROJECTEUR:5555 shell appops set be.therand.xgimi.hdmiboot SYSTEM_ALERT_WINDOW allow
```

Le projecteur doit accepter ADB sur TCP pour cet exemple. L'application n'a ensuite **pas besoin du réseau** pour fonctionner.

## Dépannage

Si la commutation automatique cesse après un redémarrage complet, rouvrir **XGIMI HDMI Boot** pour relancer son service (limitation connue en v1.1). Vérifier que l'automatisation est activée et que l'autorisation de superposition est accordée.

Pour récupérer les logs :

```bash
adb -s IP_DU_PROJECTEUR:5555 logcat -d -s XgimiHdmiBoot:I ActivityTaskManager:W
```

## Compilation depuis les sources

Consulter la [documentation de développement](docs/DEVELOPMENT.md). Une compilation locale utilise une **clé de signature différente** : l'APK obtenu ne pourra pas remplacer directement l'APK officiel sans désinstallation préalable.

## Licence et avertissement

[MIT](LICENSE). Projet communautaire indépendant, sans affiliation ni approbation de XGIMI.
