# APK de test construit par GitHub Actions

Le workflow `.github/workflows/android-apk.yml` construit un **APK release signé** :

| Déclencheur | Où récupérer l'APK |
|---|---|
| PR mergée (push sur `main`) | Pre-release **`continuous`** : lien fixe `https://github.com/trixidis/BikeManagerKmp/releases/download/continuous/bikemanager-latest.apk` (+ Firebase App Distribution si configuré) |
| PR ouverte / mise à jour | Artefact du run (onglet *Actions* → le run → *Artifacts*), pour tester **avant** de merger |
| Manuel | *Actions* → *Android APK* → *Run workflow* |

Le `versionName` est suffixé (`1.5.0-ci.42+abc1234` ou `1.5.0-pr12.43+abc1234`) pour savoir quel build est installé.
Le `versionCode` n'est pas modifié : on peut réinstaller par-dessus un build précédent.

## 1. Secrets à créer (une seule fois)

*Settings → Secrets and variables → Actions → New repository secret*

| Secret | Contenu | Commande pour le générer |
|---|---|---|
| `GOOGLE_SERVICES_JSON_RELEASE` | `androidApp/src/release/google-services.json` en base64 | `base64 -w0 androidApp/src/release/google-services.json` (macOS : `base64 -i … `) |
| `ANDROID_KEYSTORE_BASE64` | Le keystore `.jks` en base64 | `base64 -w0 chemin/vers/keystore.jks` |
| `ANDROID_KEYSTORE_PASSWORD` | `storePassword` de `keystore.properties` | |
| `ANDROID_KEY_ALIAS` | `keyAlias` | |
| `ANDROID_KEY_PASSWORD` | `keyPassword` | |

Sans ces secrets, le workflow ne casse pas : il affiche un avertissement et ne construit rien.

> **Google Sign-In** : l'empreinte SHA-1 du keystore utilisé doit être déclarée dans la console Firebase
> (*Paramètres du projet → Vos applications → Android → Empreintes SHA*), sinon la connexion Google échoue.
> `keytool -list -v -keystore keystore.jks -alias <alias>` pour l'obtenir.

> **Conflit avec la version Play Store** : si Play App Signing est activé, l'APK installé depuis le Play Store
> est signé avec la clé de Google, pas votre clé d'upload. Android refusera alors d'installer l'APK CI par-dessus :
> désinstaller la version Play Store une fois avant (les données sont synchronisées via Firebase).

## 2. (Optionnel) Firebase App Distribution — notification à chaque merge

Plutôt que d'aller chercher l'APK, on reçoit une notification sur le téléphone à chaque PR mergée.

1. Console Firebase → **App Distribution** → *Commencer* → créer un groupe de testeurs nommé **`testers`** et s'y ajouter.
2. Google Cloud console → *IAM → Comptes de service* → créer un compte avec le rôle **Firebase App Distribution Admin** → créer une clé JSON.
3. Ajouter les secrets :
   - `FIREBASE_APP_ID` : l'ID d'app Android (`1:1234567890:android:abcdef…`, visible dans les paramètres du projet Firebase)
   - `FIREBASE_SERVICE_ACCOUNT_JSON` : le contenu brut du fichier JSON de la clé
4. Sur le téléphone : accepter l'invitation par mail et installer l'app **Firebase App Tester**.

Si ces deux secrets sont absents, l'étape est simplement ignorée.
