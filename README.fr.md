# XGIMI HDMI Boot

[English](README.md) · [Télécharger l'APK v1.2](https://github.com/Therand90/xgimi-hdmi-boot/releases/tag/v1.2)

**Sélection automatique de l'entrée HDMI 2 du XGIMI Horizon original**, à l'allumage et à la sortie de veille, sans changer le launcher Android TV.

## Ce que fait l'application

- Ouvre automatiquement l'entrée HDMI 2 native après un **redémarrage complet** d'Android TV et à la **sortie de veille**.
- Délai testé : environ **6 secondes après la connexion du service au démarrage** et **4 secondes après le réveil par télécommande**.
- Conserve HDMI-CEC et HDMI 1 ARC ; pas de root, Tasker ou launcher alternatif.
- Interface sobre : activation/désactivation, accès à l'autorisation de superposition et états des autorisations.

## Compatibilité

| Élément | Configuration testée |
| --- | --- |
| Projecteur | XGIMI Horizon original Full HD (XK03H) |
| Android TV | 11 |
| Firmware | RT.3.24.327 |
| Entrée | HDMI 2 (`Hdmi2TvInputService/HW2`) |

**Les autres modèles XGIMI et firmwares ne sont pas testés.** La commande HDMI est spécifique au Horizon XK03H.

## Installation — avec la télécommande

1. [Télécharger l'APK v1.2](https://github.com/Therand90/xgimi-hdmi-boot/releases/download/v1.2/XGIMI-HDMI-Boot.apk), puis installer l'APK sur le projecteur avec un gestionnaire de fichiers Android TV compatible.
2. Ouvrir **XGIMI HDMI Boot**. L'automatisation HDMI 2 est activée par défaut ; un bouton permet de la désactiver.
3. **Autoriser le démarrage après reboot** : `Paramètres → Préférences relatives à l'appareil → Accessibilité → XGIMI HDMI Boot (autostart) → Activer`. Ce chemin et l'activation avec la télécommande ont été vérifiés sur le Horizon XK03H.
4. **Autoriser la superposition** : `Paramètres → Applications → Accès spécial aux applications → Afficher par-dessus les autres applications → XGIMI HDMI Boot`. Ce menu est présent sur le modèle testé. L'application propose également un bouton **Gérer l'autorisation de superposition**.
5. Ouvrir à nouveau l'application pour vérifier les états des autorisations, puis essayer la veille/réveil avec la télécommande.

**Note sur une installation sans ADB :** le réglage d'accessibilité a bien été activé manuellement avec la télécommande. Le menu de superposition est visible, mais l'activation sur une installation entièrement neuve sans ADB reste à confirmer. Si ce réglage est inaccessible, voir la méthode facultative ci-dessous.

Le service d'accessibilité ne demande **aucun événement d'accessibilité**, ne lit pas l'écran et ne pilote pas les gestes. L'activation de ce service reste facultative et soumise à votre accord. Sans ce service, le firmware XGIMI testé peut empêcher le redémarrage automatique de l'application.

Une petite notification permanente est normale lorsque le service de surveillance du réveil fonctionne.

## Méthode ADB (facultative)

Pour les personnes à l'aise avec ADB, en remplaçant `IP_DU_PROJECTEUR` :

```bash
adb connect IP_DU_PROJECTEUR:5555
adb -s IP_DU_PROJECTEUR:5555 install --no-incremental -r XGIMI-HDMI-Boot.apk
adb -s IP_DU_PROJECTEUR:5555 shell appops set be.therand.xgimi.hdmiboot SYSTEM_ALERT_WINDOW allow
```

Le réglage d'accessibilité reste à autoriser volontairement depuis le projecteur (ou à l'aide du script de développement optionnel `tools/accessibility.sh`). Aucun besoin de connexion réseau pour le fonctionnement de l'application après la configuration.

## Mise à jour depuis v1.1

L'APK **officiel de la Release v1.2** conserve la même signature que v1.1. Il s'installe donc par-dessus sans supprimer les données de l'application. Les APK temporaires générés par GitHub Actions ne sont **pas** des versions distribuables et n'utilisent pas cette signature.

## Besoin d'aide ?

En cas de problème, vérifier les deux autorisations. Lancer l'application actualise les états affichés. Pour le suivi des problèmes et le code source : [GitHub Issues](https://github.com/Therand90/xgimi-hdmi-boot/issues).

[Notes de version](RELEASE_NOTES_v1.2.md) · [Documentation technique](docs/DEVELOPMENT.md)

Projet communautaire indépendant sous licence [MIT](LICENSE), sans affiliation avec XGIMI.
