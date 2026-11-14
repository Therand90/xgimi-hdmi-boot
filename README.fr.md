# XGIMI HDMI Boot — Documentation française

[English README](README.md)

**[Télécharger l'APK signé v1.1](https://github.com/Therand90/xgimi-hdmi-boot/releases/download/v1.1/XGIMI-HDMI-Boot.apk)** · [Notes de version](https://github.com/Therand90/xgimi-hdmi-boot/releases/tag/v1.1)


**Une petite application Android TV qui sélectionne automatiquement HDMI 2 quand on rallume son XGIMI Horizon avec la télécommande.** Plus besoin de changer de launcher, d'installer Tasker ou de simuler des touches.

## Matériel testé et résultat

- **XGIMI Horizon original (XK03H)**, Android TV 11, firmware `RT.3.24.327`.
- **Version 1.1** : testée et validée en conditions réelles avec extinction/rallumage par la télécommande.
- HDMI 2 s'ouvre automatiquement ; dans la configuration testée, la proposition intempestive de passer sur HDMI 1 (home cinéma ARC) a également disparu.
- Les fonctions ARC/CEC n'ont pas été modifiées.

**Limite :** la sortie de veille est validée. La réception de `BOOT_COMPLETED` et le passage sur HDMI 2 après un vrai redémarrage complet d'Android sont implémentés mais pas encore confirmés de bout en bout. Les autres modèles XGIMI n'ont pas été testés.

## Principe

Le service léger détecte `SCREEN_ON`, attend 4 secondes, puis lance l'activité HDMI native avec l'identifiant `Hdmi2TvInputService/HW2`. À `SCREEN_OFF`, il annule toute commutation programmée. Sur un démarrage Android complet, un récepteur tente la commutation après 15 secondes.

Le service reste en premier plan (notification persistante), pour pouvoir écouter la sortie de veille sans root. L'application n'a pas besoin d'accès réseau sur le projecteur et ne modifie pas les réglages ARC/CEC.

## Installation sous Ubuntu

Prérequis : ADB autorisé sur le projecteur et outils locaux :

```bash
sudo apt update
sudo apt install -y adb openjdk-17-jdk-headless curl zip unzip openssl

git clone https://github.com/Therand90/xgimi-hdmi-boot.git
cd xgimi-hdmi-boot
./INSTALL.sh IP_DU_PROJECTEUR:5555
```

Remplacer `IP_DU_PROJECTEUR` par l'adresse réelle du projecteur. Le script télécharge automatiquement le SDK Android officiel s'il manque, compile l'APK, le signe localement, puis l'installe.

Pour compiler sans installer : `./INSTALL.sh --build-only`.

Au premier lancement, ouvrir **XGIMI HDMI Boot** sur le projecteur. Vérifier le service actif et l'autorisation de superposition, puis tester **Tester HDMI 2 maintenant**. Ensuite, éteindre et rallumer normalement avec la télécommande.

## Diagnostic

```bash
adb connect IP_DU_PROJECTEUR:5555
adb -s IP_DU_PROJECTEUR:5555 logcat -d -s XgimiHdmiBoot:I ActivityTaskManager:W
```

Rechercher notamment `SCREEN_ON`, `Commutation programmee` et `Demande de commutation HDMI 2 envoyee`.

## Attention à la signature Android

Le dossier **`.signing/` contient la clé privée de signature** générée localement. Il ne doit jamais être publié. Conserver ce dossier pour les mises à jour futures : une nouvelle clé ne permet pas de remplacer l'APK déjà installé. Le `.gitignore` exclut cette clé, le dossier `build/` et les APK générés.

## Compatibilité

L'identifiant HDMI 2 est spécifique au Horizon original testé. Sur d'autres modèles, vérifier les entrées avec `adb shell dumpsys tv_input` avant toute adaptation. Les restrictions de lancement d'applications en arrière-plan varient selon les firmwares.

Projet communautaire indépendant, sans affiliation avec XGIMI. Licence MIT.
