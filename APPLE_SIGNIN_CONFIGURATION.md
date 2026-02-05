# Configuration Apple Sign In - Guide Étape par Étape

## ✅ Étapes Déjà Complétées

- [x] Code expect/actual créé
- [x] Implémentation iOS (Swift bridge + Kotlin)
- [x] Implémentation Android (stub temporaire)
- [x] Intégration UI dans LoginScreen
- [x] Logo Apple et strings ajoutés

## 🚀 Prochaines Étapes (À Faire par Vous)

### Étape 1 : Ajouter AppleAuthBridge.swift au Projet Xcode

1. Ouvrir `iosApp/iosApp.xcodeproj` dans Xcode
2. Dans le navigateur de fichiers, trouver le dossier `iosApp`
3. **Drag & drop** le fichier `/Users/adrien/dev/BikeManager/iosApp/iosApp/AppleAuthBridge.swift` depuis Finder vers le dossier `iosApp` dans Xcode
4. Dans la popup :
   - ✅ Cocher **"Copy items if needed"**
   - ✅ Cocher target **"iosApp"**
   - Cliquer **"Finish"**
5. Vérifier que le fichier apparaît dans le navigateur Xcode

### Étape 2 : Ajouter la Capability "Sign in with Apple" dans Xcode

1. Dans Xcode, sélectionner le target **iosApp** (icône bleue en haut)
2. Aller dans l'onglet **"Signing & Capabilities"**
3. Cliquer sur **"+ Capability"** (en haut à gauche)
4. Chercher **"Sign in with Apple"**
5. Double-cliquer pour l'ajouter
6. Vérifier que la capability apparaît dans la liste

### Étape 3 : Configurer Firebase Console

#### 3.1 Activer Apple Sign In Provider

1. Aller sur [Firebase Console](https://console.firebase.google.com/)
2. Sélectionner le projet **bikemanager-f6f64**
3. Menu latéral → **Authentication** (icône clé)
4. Onglet **Sign-in method**
5. Trouver **Apple** dans la liste des providers
6. Cliquer sur **Apple**
7. Cocher **Enable**
8. Cliquer **Save**

#### 3.2 Créer un Service ID (pour Android - optionnel pour l'instant)

⚠️ **Note** : L'implémentation Android actuelle est un stub. Cette configuration sera nécessaire quand on implémentera le vrai flow Android.

1. Dans Firebase Console → Authentication → Apple provider
2. Section **Web SDK configuration**
3. Copier l'**OAuth redirect URI** affichée :
   ```
   https://bikemanager-f6f64.firebaseapp.com/__/auth/handler
   ```
4. Garder cette URL de côté pour l'étape suivante

### Étape 4 : Configurer Apple Developer Portal

#### 4.1 Activer Sign In with Apple pour l'App ID iOS

1. Aller sur [Apple Developer Portal](https://developer.apple.com/account)
2. Menu **Certificates, Identifiers & Profiles**
3. Sidebar → **Identifiers**
4. Trouver et cliquer sur **com.bikemanager.ios** (votre Bundle ID iOS)
5. Dans la liste des **Capabilities**, cocher **Sign in with Apple**
6. Cliquer **Save**
7. Confirmer les changements

#### 4.2 (Optionnel) Créer un Service ID pour Android

⚠️ **À faire plus tard** quand on implémentera le vrai flow Android.

1. Dans **Identifiers**, cliquer **+** (ajouter)
2. Sélectionner **Services IDs**
3. Cliquer **Continue**
4. Remplir :
   - **Description** : "BikeManager Apple Sign In"
   - **Identifier** : `com.bikemanager.signin`
5. Cliquer **Continue** → **Register**
6. Configurer le Service ID :
   - Cocher **Sign in with Apple**
   - Cliquer **Configure**
   - **Primary App ID** : Sélectionner `com.bikemanager.ios`
   - **Web Domain** : `bikemanager-f6f64.firebaseapp.com`
   - **Return URLs** : Coller l'URL de l'étape 3.2
   - **Next** → **Done** → **Continue** → **Save**

### Étape 5 : Tester sur iOS

1. Build l'app iOS sur un **device physique** (simulateur nécessite Apple ID)
   ```bash
   cd iosApp
   xcodebuild -workspace iosApp.xcworkspace -scheme iosApp -configuration Debug
   ```

2. Lancer l'app et tester :
   - Le bouton Apple apparaît dans LoginScreen ✅
   - Cliquer dessus affiche le modal natif Apple ✅
   - Se connecter avec un Apple ID ✅
   - L'utilisateur est créé dans Firebase ✅
   - Connexion réussie → navigation vers l'app ✅

3. Vérifier dans Firebase Console → Authentication → Users :
   - L'utilisateur apparaît ✅
   - Provider = "Apple" ✅
   - Email visible (si partagé) ✅

### Étape 6 : Implémenter le Vrai Flow Android (TODO Plus Tard)

⚠️ **Actuellement**, Android affiche un message d'erreur :
> "Apple Sign In n'est pas encore disponible sur Android. Utilisez la connexion Google pour le moment."

**Pour implémenter le vrai flow Android**, il faudra :

1. **Créer un ActivityProvider** ou utiliser `LocalContext.current as Activity` dans le Composable
2. **Modifier `AppleSignInHandler.android.kt`** pour :
   - Récupérer l'Activity via le provider
   - Utiliser `FirebaseAuth.startActivityForSignInWithProvider(activity, provider)`
   - Gérer les callbacks onSuccess/onFailure
3. **Compléter la configuration** Apple Developer Portal (Service ID)
4. **Tester** sur un device Android physique

**Fichier à modifier** : `shared/src/androidMain/kotlin/com/bikemanager/ui/auth/AppleSignInHandler.android.kt`

---

## 📋 Checklist de Validation

### Configuration iOS

- [ ] AppleAuthBridge.swift ajouté au projet Xcode
- [ ] Capability "Sign in with Apple" activée dans Xcode
- [ ] Firebase Console : Apple provider activé
- [ ] Apple Developer : App ID capability activée
- [ ] Tests sur device iOS : bouton visible
- [ ] Tests sur device iOS : flow de connexion fonctionne
- [ ] Tests sur device iOS : utilisateur créé dans Firebase

### Configuration Android (Optionnel pour l'instant)

- [ ] Service ID créé sur Apple Developer Portal
- [ ] Service ID configuré avec return URL Firebase
- [ ] AppleSignInHandler.android.kt implémenté avec Activity
- [ ] Tests sur device Android : flow de connexion fonctionne

---

## ❓ Troubleshooting

### Problème : "Activity non disponible" sur Android

**Cause** : L'implémentation Android actuelle est un stub.

**Solution** : Implémenter le vrai flow en créant un ActivityProvider ou en utilisant LocalContext dans le Composable.

### Problème : Bouton Apple ne s'affiche pas sur iOS

**Causes possibles** :
1. Le fichier `AppleAuthBridge.swift` n'est pas ajouté au projet Xcode
2. La ressource `ic_apple_logo.xml` est manquante
3. Erreur de compilation Swift

**Solutions** :
1. Vérifier dans Xcode que `AppleAuthBridge.swift` est dans le target iosApp
2. Recompiler le projet : `./gradlew :shared:clean :shared:build`
3. Consulter les erreurs de build dans Xcode

### Problème : Modal Apple ne s'affiche pas

**Causes possibles** :
1. Capability "Sign in with Apple" manquante
2. App ID non configuré sur Apple Developer Portal
3. Problème de bridging Swift-Kotlin

**Solutions** :
1. Vérifier les étapes 2 et 4.1 de ce document
2. Consulter les logs Xcode pour les erreurs

### Problème : Connexion réussit mais utilisateur non créé dans Firebase

**Causes possibles** :
1. Apple provider non activé dans Firebase Console
2. Token invalide ou nonce incorrect
3. Problème réseau

**Solutions** :
1. Vérifier l'étape 3.1 de ce document
2. Consulter les logs Firebase : onglet "Events" dans Firebase Console
3. Vérifier que le nonce est correctement hashé (SHA256)

---

## 📚 Ressources

- [Apple Sign In with Firebase iOS](https://firebase.google.com/docs/auth/ios/apple)
- [Apple Sign In with Firebase Android](https://firebase.google.com/docs/auth/android/apple)
- [Apple Authentication Services Documentation](https://developer.apple.com/documentation/authenticationservices)
- [Implementing Apple Sign-In in KMP - Medium](https://medium.com/@Tweeel/implementing-apple-sign-in-in-kotlin-multiplatform-kmp-6e6b1a1cffca)

---

## ✉️ Contact

Si vous rencontrez des problèmes, consultez les logs :
- **iOS** : Xcode Debug Console
- **Android** : Logcat (filtrer par "Napier")
- **Firebase** : Firebase Console → Authentication → Events

**Bon courage ! 🚀**
