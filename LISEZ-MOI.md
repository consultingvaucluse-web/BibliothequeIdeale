# Bibliothèque Idéale — Application Android native (WebView + AdMob)

Ce projet remplace la version TWA par une vraie application Android qui peut
afficher une bannière AdMob native en bas de l'écran, sous forme d'un projet
Android Studio complet et prêt à compiler.

## Avant toute chose : 1 modification encore nécessaire

### ~~1. Ton URL de site~~ ✅ Déjà fait

L'URL `https://labibliothequeideale.netlify.app/` est déjà en place dans
`MainActivity.kt`, vérifiée fonctionnelle.

### 1. Les ID AdMob de test

Ce projet utilise les **ID de test officiels de Google** (les mêmes pour tout
le monde) à deux endroits :
- `AndroidManifest.xml` → App ID de test
- `activity_main.xml` → ID d'unité publicitaire (bannière) de test

Ils fonctionnent immédiatement, sans compte AdMob, pour vérifier que tout
s'affiche bien. **Mais avant la publication réelle**, il faut les remplacer
par tes propres ID, créés sur [admob.google.com](https://admob.google.com) :
1. Crée un compte AdMob (gratuit)
2. Ajoute ton application (même nom de package que ci-dessous)
3. Crée une unité publicitaire "Bannière"
4. Remplace les deux ID de test par les tiens

⚠️ Publier avec de vrais ID mais en continuant à cliquer dessus toi-même
pendant les tests peut entraîner une **suspension du compte AdMob**. Grade
les ID de test jusqu'à la toute fin.

## Étape 1 — Mettre ce code sur GitHub

1. Crée un compte sur [github.com](https://github.com) si tu n'en as pas
2. Crée un nouveau dépôt (**New repository**), nom libre, ne coche aucune
   case d'initialisation
3. Sur ton ordinateur, dans ce dossier :
   ```
   git init
   git add .
   git commit -m "Version initiale"
   git branch -M main
   git remote add origin https://github.com/TON-COMPTE/TON-DEPOT.git
   git push -u origin main
   ```
   (Remplace l'URL par celle de ton dépôt, affichée sur GitHub après sa création.)

## Étape 2 — Récupérer l'APK de test (aucun secret requis)

Dès que le code est poussé, direction l'onglet **Actions** de ton dépôt
GitHub : la compilation démarre automatiquement (2-3 minutes). Une fois
terminée, clique sur le run → tout en bas, section **Artifacts** →
télécharge **app-debug-apk**, à installer directement sur ton téléphone
Android pour vérifier que tout fonctionne (WebView + bannière AdMob).

## Étape 3 — Générer ta clé de signature (pour l'AAB final)

Sur ton ordinateur, avec Java installé :

```
keytool -genkeypair -v -keystore release.keystore -alias bibliotheque -keyalg RSA -keysize 2048 -validity 10000
```

Cela te demande un mot de passe et quelques informations (nom, organisation...).
**Conserve ce fichier `release.keystore` et ce mot de passe précieusement** —
comme évoqué précédemment, les perdre empêche toute future mise à jour de l'app.

## Étape 4 — Ajouter les secrets sur GitHub

Sur ton dépôt GitHub : **Settings** → **Secrets and variables** → **Actions**
→ **New repository secret**, à créer un par un :

| Nom du secret | Valeur |
|---|---|
| `KEYSTORE_BASE64` | Résultat de la commande ci-dessous |
| `KEYSTORE_PASSWORD` | Le mot de passe choisi à l'étape 3 |
| `KEY_ALIAS` | `bibliotheque` (ou l'alias choisi) |
| `KEY_PASSWORD` | Le mot de passe de la clé (souvent identique au précédent) |

Pour obtenir la valeur de `KEYSTORE_BASE64` :
```
base64 -i release.keystore | tr -d '\n'
```
(sur Windows : `certutil -encode release.keystore keystore.b64`, puis copie
le contenu du fichier généré en retirant les lignes `-----BEGIN...` et
`-----END...`)

## Étape 5 — Récupérer l'AAB signé

Refais un `git push` (ou relance manuellement le workflow depuis l'onglet
Actions → **Run workflow**). Cette fois, l'artefact **app-release-aab**
contient un fichier `.aab` signé, prêt à être envoyé sur Google Play Console
exactement comme décrit précédemment (Test → Test fermé → Créer une
release).

## Point important : cette app vs. la version TWA précédente

Si tu avais déjà commencé une fiche Play Console avec la version TWA :
- Pour **remplacer** cette fiche par cette nouvelle version : réutilise le
  **même nom de package** (`com.bibliothequeideale.app` → à adapter si tu
  avais choisi autre chose) et surtout la **même clé de signature** que
  celle générée dans PWABuilder, sinon Google refusera l'upload en le
  considérant comme une application différente.
- Pour repartir sur une **fiche neuve** : garde le nom de package actuel,
  et il faudra recommencer le cycle des 12 testeurs/14 jours.

## Ce que fait ce projet

- Une `WebView` plein écran qui charge ton site (le catalogue de 500 livres,
  avis compris — `domStorageEnabled` est activé, donc les avis en
  `localStorage` fonctionnent normalement)
- Une bannière AdMob native ancrée en bas, en dehors de la zone de contenu
  (contrairement à la TWA, ici c'est officiellement supporté par Google)
- Bouton retour Android → navigue dans l'historique de la page plutôt que de
  fermer l'app directement
