# XGIMI HDMI Boot

[English](README.md) · [Dernière version publique stable : v1.2](https://github.com/Therand90/xgimi-hdmi-boot/releases/tag/v1.2) · [Code et évolutions v1.3](https://github.com/Therand90/xgimi-hdmi-boot/pull/5)

**Sélection automatique de l'entrée HDMI 2 sur le XGIMI Horizon original**, au démarrage et à la sortie de veille, sans remplacer le launcher Android TV.

## Fonctionnalités v1.3

- Ouvre l'entrée HDMI 2 native à la sortie de veille ou après un redémarrage complet d'Android TV.
- **Réduction du délai : 1 seconde à la sortie de veille et 3 secondes après connexion de l'assistant au redémarrage**, contre 4/6 s dans la v1.2. Ces nouveaux délais demandent une validation complète sur le projecteur. Si l'ouverture Android échoue explicitement, une **unique nouvelle tentative 3 secondes plus tard** est prévue.
- **Ferme automatiquement le dialogue XGIMI « Source détectée »** dans la situation vérifiée où un appareil raccordé à HDMI 1 devient actif pendant l'affichage HDMI 2. Cette option est activée par défaut sur le modèle testé et reste désactivable dans l'application.
- Pas de bouton de diagnostic de développement dans l'interface publique.
- Ne modifie pas les paramètres globaux HDMI-CEC ni ARC/eARC ; aucun root, Tasker ou launcher alternatif.

## Important : HDMI-CEC et « lecture en une touche »

**Certains appareils HDMI-CEC** (amplificateurs audio-vidéo, barres de son, lecteurs multimédias, consoles, etc.) peuvent demander au téléviseur ou au projecteur de **basculer directement sur leur entrée HDMI** au démarrage. Cela peut prendre la priorité sur HDMI 2, même si l'application ferme correctement le popup.

Si cela se produit, **désactivez uniquement la fonction « Lecture en une touche », « One Touch Play » ou équivalente sur l'appareil concerné**. Le nom et le chemin du menu dépendent du fabricant ; l'option peut être intégrée à EasyLink, Anynet+, SIMPLINK, BRAVIA Sync ou un autre système HDMI-CEC. **Ne désactivez pas l'HDMI-CEC globalement** si vous utilisez ARC/eARC, le volume ou l'extinction synchronisée.

Le comportement a été validé sur un **home cinéma Philips avec EasyLink → Lecture 1 pression désactivé**. Cette marque n'est **pas une exigence** de l'application ; les autres appareils doivent être testés séparément.

## Compatibilité

| Élément | Configuration confirmée |
| --- | --- |
| Projecteur | XGIMI Horizon original Full HD (XK03H) |
| Android TV | 11 |
| Firmware | RT.3.24.327 |
| Entrée cible | HDMI 2 (`Hdmi2TvInputService/HW2`) |
| Popup testé | XGIMI `XGMDialog` à l'arrivée du signal HDMI 1 |

**Les autres modèles XGIMI et firmwares ne sont pas testés.** La commande native HDMI et le mécanisme de fermeture du popup sont spécifiques au modèle étudié.

## Installation — avec la télécommande

1. Pour une installation publique immédiate, [télécharger l'APK v1.2 stable](https://github.com/Therand90/xgimi-hdmi-boot/releases/download/v1.2/XGIMI-HDMI-Boot.apk). La v1.3 sera publiée après validation de ses nouveaux délais.
2. Ouvrir **XGIMI HDMI Boot** et laisser l'automatisation de HDMI 2 activée. La fermeture du popup possède son propre interrupteur.
3. **Autoriser l'accessibilité** : `Paramètres → Préférences relatives à l'appareil → Accessibilité → XGIMI HDMI Boot (autostart) → Activer`. Sur le Horizon XK03H, cette permission sert à relancer l'application après redémarrage et, en v1.3, à détecter un événement de fenêtre XGIMI.
4. **Autoriser la superposition** : `Paramètres → Applications → Accès spécial aux applications → Afficher par-dessus les autres applications → XGIMI HDMI Boot`. L'application propose un bouton d'accès à ce réglage.
5. Vérifier les autorisations dans l'application, puis tester veille/réveil et redémarrage complet.

**Accessibilité et confidentialité :** le service v1.3 ne demande **pas l'accès au contenu des fenêtres** et ne lit ni texte, ni image, ni contenu d'autres applications. Il reçoit uniquement les changements d'état de fenêtre du lecteur HDMI XGIMI, afin d'identifier la présence possible du popup. Un appui global « Retour » n'est envoyé que si une détection HDMI 1 a précédé le dialogue XGIMI de quatre secondes au maximum, avec un délai de sécurité de 15 secondes entre deux tentatives. Un autre dialogue XGIMI ouvert dans cette courte période pourrait exceptionnellement être fermé. La fonction est désactivable.

Une petite notification permanente est normale lorsque le service de surveillance du réveil fonctionne.

## Méthode ADB (facultative)

```bash
adb connect IP_DU_PROJECTEUR:5555
adb -s IP_DU_PROJECTEUR:5555 install --no-incremental -r XGIMI-HDMI-Boot.apk
adb -s IP_DU_PROJECTEUR:5555 shell appops set be.therand.xgimi.hdmiboot SYSTEM_ALERT_WINDOW allow
```

L'accessibilité s'active volontairement sur le projecteur. Une fois configurée, l'application n'a pas besoin d'Internet.

## Mises à jour et développement

La version publique v1.2 est signée avec la clé originale du projet. La future v1.3 doit utiliser **la même clé** pour mettre à jour les appareils sans désinstallation. Les APK produits par GitHub Actions ne conviennent **pas** à une mise à jour officielle, car ils utilisent une clé temporaire.

[Notes v1.3](RELEASE_NOTES_v1.3.md) · [Documentation technique](docs/DEVELOPMENT.md) · [Issues GitHub](https://github.com/Therand90/xgimi-hdmi-boot/issues)

Projet indépendant sous licence [MIT](LICENSE), sans affiliation avec XGIMI.
