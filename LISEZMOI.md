# Étoiles d'Or : générateur de combinaisons (5 numéros + 2 étoiles)

Application Android native (Kotlin, sans bibliothèque externe).
Un gros bouton **TIRER** au centre : les boules tournent comme sur une machine à sous,
puis s'arrêtent une à une sur une nouvelle combinaison (5 numéros de 1 à 50, 2 étoiles de 1 à 12).
Le hasard vient de `SecureRandom`, sans doublon dans une combinaison.

> Ce code n'a pas été compilé dans l'environnement où il a été écrit (pas de SDK Android).
> La première compilation (étape A ou B) peut signaler une petite erreur de version : voir « Si ça échoue ».

---

## Obtenir l'APK et le fichier pour Google Play

### Option A : sans rien installer (GitHub Actions, gratuit)
1. Créez un compte sur github.com, puis un dépôt **privé** vide.
2. Envoyez-y tout le contenu de ce dossier (bouton « Add file > Upload files »).
3. Onglet **Actions** : la compilation démarre seule (3 à 6 minutes).
4. Ouvrez l'exécution terminée, section **Artifacts** : téléchargez `apk-test`.
   C'est l'APK à installer sur votre téléphone pour tester.

### Option B : Android Studio
1. Installez Android Studio, puis « Open » et choisissez ce dossier.
2. Attendez la synchronisation Gradle.
3. Test : **Build > Build APK(s)**.
4. Publication : **Build > Generate Signed App Bundle / APK > Android App Bundle**.
   Créez votre clé quand l'assistant le propose et **sauvegardez-la**
   (fichier + mots de passe) : sans elle, vous ne pourrez plus mettre l'application à jour.

---

## Publier sur Google Play

**Google Play n'accepte plus les APK pour les nouvelles applications : il faut un `.aab`.**
L'APK sert uniquement à tester. Le `.aab` est produit par l'option B,
ou par l'option A une fois les secrets ci-dessous ajoutés.

### Secrets GitHub pour obtenir un .aab signé (option A)
1. Créez une clé (une seule fois, sur un ordinateur avec Java) :
   `keytool -genkeypair -v -keystore upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000`
2. Convertissez-la en texte : `base64 -w0 upload.jks` (Mac : `base64 -i upload.jks`).
3. Dépôt GitHub > Settings > Secrets and variables > Actions, ajoutez :
   `KEYSTORE_BASE64` (le texte), `KEYSTORE_PASSWORD`, `KEY_ALIAS` (ici `upload`), `KEY_PASSWORD`.
4. Relancez la compilation : l'artefact `aab-google-play` apparaît.

### À faire avant l'envoi
- **Identifiant unique** : changez `com.tiragechance.app` (fichier `app/build.gradle.kts`,
  `applicationId` et `namespace`, et le dossier/`package` de `MainActivity.kt`).
  Il ne pourra plus changer après publication.
- **Compte développeur Google Play** : frais uniques de 25 $. Un compte personnel récent doit
  d'abord faire un test fermé avec au moins 12 testeurs pendant 14 jours avant la production.
- **Fiche** : icône 512×512 px, image de présentation 1024×500 px, captures d'écran, description.
- **Politique de confidentialité** : l'application ne collecte aucune donnée, mais une page
  web qui le dit est demandée. Remplissez aussi le formulaire « Sécurité des données » (rien collecté).
- **Questionnaire de classification** : répondez honnêtement (générateur aléatoire, aucun argent réel).

### Noms et logos
N'utilisez **ni le logo ni le nom « Euromillions »** dans l'application, son titre ou sa fiche :
ce sont des marques déposées, et Google Play refuse les applications qui laissent croire
à un lien officiel. L'application s'appelle donc « Étoiles d'Or » et utilise son propre habillage doré, et la mention en bas de
l'écran précise qu'elle n'est affiliée à aucun opérateur.
Évitez aussi les promesses du type « trouvez les bons numéros » : le tirage est aléatoire,
aucune combinaison n'a plus de chances qu'une autre.

---

## Si ça échoue
- Erreur de version du plugin Android ou de Kotlin : dans Android Studio, utilisez
  **Tools > AGP Upgrade Assistant**, ou ajustez les versions dans `build.gradle.kts` (racine).
- Pour l'option A : dans le fichier `.github/workflows/build.yml`, la ligne `java-version: 17`
  est la bonne valeur pour ces versions.
