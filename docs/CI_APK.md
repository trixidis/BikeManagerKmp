# APK de test via Firebase App Distribution

Le workflow `.github/workflows/android-apk.yml` construit un **APK release signé** et l'envoie sur
**Firebase App Distribution**. La distribution est privée : seuls les testeurs invités peuvent installer.
Aucun APK n'est publié sur GitHub (le dépôt est public).

| Déclencheur | Notes de version affichées dans App Tester |
|---|---|
| PR mergée (push sur `main`) | `main : <message du commit>` |
| PR ouverte / mise à jour | `PR #12 : <titre de la PR>` (pour tester **avant** de merger) |
| Manuel (*Actions* → *Android APK* → *Run workflow*) | `Build manuel (<branche>)` |

Le `versionName` est suffixé (`1.5.0-ci.42+abc1234` pour `main`, `1.5.0-pr12.43+abc1234` pour une PR)
pour savoir quel build est installé. Le `versionCode` n'est pas modifié : chaque build s'installe par-dessus le précédent.

## 1. Mettre en place Firebase App Distribution (une seule fois)

1. Console Firebase → **App Distribution** → *Commencer* sur l'app Android.
2. Onglet *Testeurs et groupes* → créer un groupe dont l'**alias** est **`testers`** → s'y ajouter avec son adresse mail.
3. Google Cloud console (même projet) → *IAM et administration → Comptes de service* → *Créer un compte de service*
   → rôle **Firebase App Distribution Admin** → onglet *Clés* → *Ajouter une clé* → JSON.
4. Sur le téléphone : accepter l'invitation reçue par mail et installer l'app **Firebase App Tester**.
   Tu reçois ensuite une notification à chaque nouveau build.

## 2. Secrets GitHub

*Settings → Secrets and variables → Actions → New repository secret*

| Secret | Contenu | Pour le générer |
|---|---|---|
| `GOOGLE_SERVICES_JSON_RELEASE` | `androidApp/src/release/google-services.json` en base64 | `base64 -w0 androidApp/src/release/google-services.json` (macOS : `base64 -i <fichier>`) |
| `ANDROID_KEYSTORE_BASE64` | Le keystore `.jks` en base64 | `base64 -w0 chemin/vers/keystore.jks` |
| `ANDROID_KEYSTORE_PASSWORD` | `storePassword` de `keystore.properties` | |
| `ANDROID_KEY_ALIAS` | `keyAlias` | |
| `ANDROID_KEY_PASSWORD` | `keyPassword` | |
| `FIREBASE_APP_ID` | ID de l'app Android, du type `1:1234567890:android:abcdef…` | Console Firebase → *Paramètres du projet* → *Vos applications* |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | Contenu brut du fichier JSON de la clé du compte de service | |

S'il manque un secret, le workflow ne casse pas : il affiche un avertissement et ne construit rien.
Les PR venant d'un fork n'ont pas accès aux secrets, donc elles ne déclenchent aucun build ni distribution.

## Points d'attention

- **Google Sign-In** : l'empreinte SHA-1 du keystore utilisé doit être déclarée dans Firebase
  (*Paramètres du projet → Vos applications → Android → Empreintes SHA*), sinon la connexion Google échoue.
  Pour l'obtenir : `keytool -list -v -keystore keystore.jks -alias <alias>`.
- **Conflit avec la version Play Store** : si Play App Signing est activé, la version du Play Store est signée
  par la clé de Google, pas par ta clé d'upload. Android refusera d'installer le build de la CI par-dessus :
  désinstalle une fois la version Play Store (les données sont synchronisées via Firebase).
