# PRD - Mes Entretiens Moto

## 1. Vue d'ensemble

### 1.1 But de l'application
**Mes Entretiens Moto** est une application mobile de suivi et de gestion des entretiens pour propriétaires de motos. Elle permet aux utilisateurs de :
- Enregistrer leurs motos
- Planifier les entretiens à effectuer
- Historiser les entretiens réalisés
- Synchroniser les données dans le cloud

### 1.2 Problème résolu
Les motards doivent suivre régulièrement l'entretien de leur(s) moto(s) (vidanges, changements de pneus, filtres, etc.). Cette application leur permet de ne jamais oublier un entretien et de conserver un historique complet.

### 1.3 Utilisateurs cibles
- Propriétaires de motos (route, tout-terrain, cross)
- Utilisateurs souhaitant suivre leurs entretiens en **kilomètres** (motos de route) ou en **heures** (motos tout-terrain)

---

## 2. Fonctionnalités

### 2.1 Gestion des motos

| Fonctionnalité | Description |
|----------------|-------------|
| Ajouter une moto | L'utilisateur saisit le nom de sa moto |
| Modifier une moto | L'utilisateur peut changer le nom et la méthode de comptage |
| Supprimer une moto | Non implémenté dans la version actuelle |
| Méthode de comptage | Choix entre **Kilomètres (KM)** ou **Heures (H)** |

### 2.2 Gestion des entretiens

| Fonctionnalité | Description |
|----------------|-------------|
| Ajouter un entretien "À faire" | Entretien planifié sans kilométrage/heure |
| Ajouter un entretien "Fait" | Entretien réalisé avec kilométrage/heure et date |
| Marquer comme fait | Convertir un entretien "À faire" en "Fait" |
| Supprimer un entretien | Swipe pour supprimer avec possibilité d'annuler |
| Historique | Liste des entretiens triés par kilométrage/heure (décroissant) |

### 2.3 Synchronisation cloud

| Fonctionnalité | Description |
|----------------|-------------|
| Authentification | Connexion via Google Sign-In (Firebase Auth) |
| Stockage cloud | Firebase Realtime Database |
| Mode hors-ligne | Stockage local SQLite, sync automatique |

---

## 3. Modèles de données

### 3.1 Entité : Bike (Moto)

```
Bike {
    id: Long (auto-généré)
    name: String (obligatoire)
    countingMethod: Enum [KM, HOURS]
    firebaseReference: String? (pour sync)
}
```

### 3.2 Entité : Maintenance (Entretien)

```
Maintenance {
    id: Long (auto-généré)
    name: String (obligatoire) - ex: "Vidange", "Pneu avant"
    nbHoursOrKm: Float (-1 si non défini, sinon valeur positive)
    date: Long (timestamp en millisecondes)
    isDone: Boolean (false = à faire, true = fait)
    bikeId: Long (clé étrangère vers Bike)
    firebaseReference: String? (pour sync)
}
```

### 3.3 Relations
- **1 Bike → N Maintenances** (relation un-à-plusieurs)
- Un entretien appartient toujours à une moto

### 3.4 Structure Firebase

```
users/
  {userId}/
    bikes/
      {bikeRef}/
        nameBike: "Ma Moto"
        countingMethod: "KM"
        maintenances/
          {maintenanceRef}/
            nameMaintenance: "Vidange"
            nbHoursMaintenance: 5000
            dateMaintenance: 1640000000000
            isDone: true
```

---

## 4. Écrans et navigation

### 4.1 Flux de navigation

```
[Écran d'accueil - Liste des motos]
         │
         │ (tap sur une moto)
         ▼
[Écran entretiens - 2 onglets]
    ├── Onglet "Faits" (entretiens réalisés)
    └── Onglet "À faire" (entretiens planifiés)
```

### 4.2 Écran 1 : Liste des motos

**URL/Route** : `/bikes` (écran principal)

**Affichage** :
- Liste scrollable des motos de l'utilisateur
- Chaque cellule affiche le nom de la moto + icône d'édition
- Message "Vous n'avez pour l'instant ajouté aucune moto" si liste vide
- Bouton flottant (FAB) "+" pour ajouter une moto

**Actions utilisateur** :
| Action | Résultat |
|--------|----------|
| Tap sur une moto | Navigation vers écran entretiens |
| Long press sur une moto | Ouverture dialog modification |
| Tap sur icône édition | Ouverture dialog modification |
| Tap sur FAB "+" | Ouverture dialog ajout moto |

### 4.3 Dialog : Ajouter une moto

**Champs** :
- Nom de la moto (texte, obligatoire)

**Validation** :
- Le nom ne doit pas être vide

**Résultat** :
- Création de la moto en base locale
- Synchronisation Firebase si connecté
- Rafraîchissement de la liste

### 4.4 Dialog : Modifier une moto

**Champs** :
- Nom de la moto (texte, pré-rempli)
- Toggle Heures / Kilomètres

**Comportement** :
- Modification en temps réel (auto-save)
- Synchronisation Firebase si connecté

### 4.5 Écran 2 : Entretiens d'une moto

**URL/Route** : `/bikes/{bikeId}/maintenances`

**Structure** :
- Header avec image parallax
- 2 onglets avec ViewPager :
  - **"Faits"** (entretiens isDone=true) - header bleu
  - **"À faire"** (entretiens isDone=false) - header vert

**Affichage par cellule d'entretien** :
- Nom de l'entretien (ex: "Vidange")
- Si fait : valeur km/h + date (format dd/MM/yyyy)
- Format valeur : "X KM" ou "X H" selon méthode de comptage de la moto

**Tri** :
- Par valeur km/h décroissante (les plus récents en haut)

**Actions utilisateur** :
| Action | Résultat |
|--------|----------|
| Tap sur FAB "+" (onglet Faits) | Dialog ajout entretien fait |
| Tap sur FAB "+" (onglet À faire) | Dialog ajout entretien à faire |
| Tap sur entretien "À faire" | Dialog pour marquer comme fait |
| Swipe gauche/droite | Suppression avec Snackbar "Annuler" |

### 4.6 Dialog : Ajouter un entretien "À faire"

**Champs** :
- Type d'entretien (texte, obligatoire)

**Résultat** :
- Création entretien avec isDone=false, nbHoursOrKm=-1

### 4.7 Dialog : Ajouter un entretien "Fait"

**Champs** :
- Type d'entretien (texte, obligatoire)
- Nombre de km/heures (nombre décimal, obligatoire)
  - Label dynamique selon méthode de comptage de la moto

**Résultat** :
- Création entretien avec isDone=true, date=maintenant

### 4.8 Dialog : Marquer comme fait

**Contexte** : Apparaît quand on tape sur un entretien "À faire"

**Champs** :
- Nombre de km/heures (nombre décimal, obligatoire)
- Message explicatif adapté à la méthode de comptage

**Résultat** :
- Mise à jour : isDone=true, nbHoursOrKm=valeur, date=maintenant
- L'entretien passe de l'onglet "À faire" à "Faits"

---

## 5. Règles métier

### 5.1 Validation des données

| Champ | Règle |
|-------|-------|
| Nom moto | Non vide |
| Nom entretien | Non vide |
| Valeur km/h | Nombre positif ou -1 (non défini) |

### 5.2 Comportement suppression

- Swipe pour supprimer un entretien
- Snackbar affiché pendant 3-5 secondes
- Bouton "Annuler" restaure l'entretien
- Si non annulé → suppression définitive locale + Firebase

### 5.3 Synchronisation

- **Écriture locale d'abord** (SQLite)
- **Puis sync Firebase** si utilisateur connecté
- Chaque entité stocke sa `firebaseReference` pour les mises à jour

---

## 6. Spécifications techniques (pour KMP)

### 6.1 Architecture recommandée

```
shared/
  ├── data/
  │   ├── model/          # Bike, Maintenance, enums
  │   ├── repository/     # Interfaces repository
  │   └── datasource/     # Local (SQLDelight) + Remote (Firebase)
  ├── domain/
  │   └── usecase/        # Logique métier
  └── presentation/
      └── viewmodel/      # ViewModels partagés (KMM ViewModel)

androidApp/
  └── ui/                 # Compose UI Android

iosApp/
  └── ui/                 # SwiftUI
```

### 6.2 Comportements spécifiques

**Mode Debug vs Release** :
- **DEBUG** : L'authentification Firebase est bypassée, l'app démarre directement sur l'écran des motos
- **RELEASE** : Authentification Google obligatoire au premier lancement

**Détection première connexion** :
- Au lancement, l'app vérifie `IS_USER_CONNECTED` dans SharedPreferences
- Si `false` → Affiche l'écran de connexion Google
- Si `true` → Affiche directement la liste des motos

**Backup Android** :
- `android:allowBackup="true"` - Sauvegarde automatique Android activée
- `android:fullBackupContent="true"` - Backup complet des données

**Support RTL** :
- `android:supportsRtl="true"` - Support des langues de droite à gauche

**Informations de version** :
- versionCode: 3
- versionName: "1.1"
- minSdk: 16 (Android 4.1)
- targetSdk: 28 (Android 9)

### 6.3 Technologies suggérées pour KMP

| Composant | Technologie |
|-----------|-------------|
| Base de données locale | SQLDelight |
| Injection de dépendances | Koin Multiplatform |
| Réseau/Firebase | Ktor + Firebase SDK KMP |
| Programmation réactive | Kotlin Coroutines + Flow |
| Navigation | Voyager ou Decompose |
| UI Android | Jetpack Compose |
| UI iOS | SwiftUI |

### 6.3 Fonctionnalités à conserver

- [x] Multi-motos
- [x] Deux méthodes de comptage (KM/Heures)
- [x] États entretien : À faire / Fait
- [x] Authentification Google
- [x] Synchronisation Firebase
- [x] Suppression avec undo
- [x] Mode hors-ligne

---

## 7. Constantes et ressources

### 7.1 Localisation - Français (défaut) et Anglais

L'application supporte **2 langues** : Français (défaut) et Anglais.

#### Strings complètes

| Clé | Français | English |
|-----|----------|---------|
| `app_name` | Mes entretiens moto | My bikes maintenance |
| `title_activity_manage_bikes` | Mes motos | My bikes |
| `title_activity_manage_maintenances` | Entretiens | Maintenances |
| `hint_no_bikes` | Vous n'avez pour l'instant ajouté aucune moto | You have not added any bike for now |
| `text_no_maintenance_to_show` | Aucun entretien pour cette moto | No maintenance for this bike |
| `title_done` | Faits | Done |
| `title_to_do` | A faire | To do |
| `title_add_bike` | Ajout de moto | Adding a bike |
| `message_add_bike_fill_name` | Veuillez renseigner le nom de la moto à ajouter | Please fill the name of the bike to add |
| `title_add_maintenance` | Ajout d'un entretien | Adding a maintenance |
| `title_add_maintenance_to_do` | Ajout d'un entretien à faire | Adding a maintenance to do |
| `message_add_maintenance_fill_name` | Veuillez renseigner le type d'entretien à ajouter | Please fill the type of maintenance to add |
| `hint_maintenance_type` | Type d'entretien | Maintenance type |
| `hint_maintenance_nb_hours` | Nombre d'heures | Number of hours |
| `hint_maintenance_nb_km` | Nombre de kilomètres | Number of kilometers |
| `title_mark_maintenance_done` | Marquer cet entretien comme terminé | Mark this maintenance as done |
| `ask_maintenance_done` | Marquer cet entretien comme terminé ? | Mark this maintenance as done ? |
| `message_add_maintenance_fill_nb_hours` | Veuillez renseigner le nombre d'heures auquel vous avez effectué cet entretien | Please enter the number of hours you did this maintenance |
| `message_add_maintenance_fill_nb_km` | Veuillez renseigner le nombre de kilomètres auquel vous avez effectué cet entretien | Please enter the number of kilometers you did this maintenance |
| `positive` | Ajouter | Add |
| `cancel` | Annuler | Cancel |
| `yes` | Oui | Yes |
| `no` | Non | No |
| `text_delete_maitenance` | Entretien supprimé | Maintenance removed |
| `toast_please_fill_inputs` | Veuillez remplir correctement les champs ! | Please fill correctly the fields ! |
| `text_input_error_message` | Saisie invalide | Invalid input |
| `error_title` | Erreur | Error |
| `error_login` | Une erreur est survenue | An error occured |
| `error_retrieving_maintenances` | Erreur de récupération des entretiens | Error while getting maintenances |
| `error_adding_maintenances` | Erreur pendant l'ajout d'un entretien | Error while adding a maintenance |
| `error_removing_maintenances` | Erreur pendant la suppression d'un entretien | Error while removing a maintenance |
| `count_bike_method_change` | Compter en : | Count by : |
| `hours` | Heures | Hours |
| `km` | Kilomètres | Kilometers |
| `add_maintenance_done` | Entretien effectué | Maintenance done |
| `add_maintenance_to_do` | Entretien à faire | Maintenance to do |

#### Strings non traduisibles

| Clé | Valeur | Usage |
|-----|--------|-------|
| `bike_holder` | bike | Placeholder |
| `shared_preferences_use_is_connected` | user_connection_status | Clé SharedPreferences |
| `ok` | OK | Bouton universel |

### 7.2 Ressources graphiques

#### Icônes (toutes densités : mdpi → xxxhdpi)

| Fichier | Taille | Usage |
|---------|--------|-------|
| `ic_add_black_24dp.png` | 24dp | Bouton ajouter (sombre) |
| `ic_add_white_24dp.png` | 24dp | FAB ajouter |
| `ic_build_black_24dp.png` | 24dp | Icône entretien/outil |
| `ic_motorcycle_white_48dp.png` | 48dp | Icône moto |
| `ic_baseline_edit_24px.xml` | 24dp | Icône édition (vector) |

#### Images (xxxhdpi uniquement)

| Fichier | Usage |
|---------|-------|
| `backgournd_mechanic.jpeg` | Header parallax onglet "Faits" (⚠️ typo dans le nom) |
| `list.jpeg` | Header parallax onglet "À faire" |

**Note pour KMP** : Convertir les PNG en WebP, utiliser des vectors SVG quand possible.

### 7.3 Couleurs

```kotlin
// Couleurs principales
val colorPrimary = Color(0xFF3F51B5)      // Indigo
val colorPrimaryDark = Color(0xFF303F9F)
val colorAccent = Color(0xFFFF4081)       // Pink

// Couleurs secondaires
val indigo = Color(0xFF3F51B5)
val teal = Color(0xFF009688)              // Header "À faire"
val darkDeepOrange = Color(0xFFE64A19)
val darkGreen = Color(0xFF388E3C)
val darkBlueGrey = Color(0xFF455A64)
val darkRed = Color(0xFFD32F2F)
val white = Color(0xFFFFFFFF)

// Texte
val primaryText = Color(0xFF212121)
val secondaryText = Color(0xFF727272)
```

---

## 8. Fichiers source de référence

### 8.1 Modèles
- `app/src/main/java/fr/nextgear/mesentretiensmoto/core/model/Bike.kt`
- `app/src/main/java/fr/nextgear/mesentretiensmoto/core/model/Maintenance.kt`

### 8.2 Logique métier
- `app/src/main/java/fr/nextgear/mesentretiensmoto/features/manageBikes/InteractorManageBikes.kt`
- `app/src/main/java/fr/nextgear/mesentretiensmoto/features/manageMaintenancesOfBike/InteractorManageMaintenances.kt`

### 8.3 ViewModels
- `app/src/main/java/fr/nextgear/mesentretiensmoto/features/manageBikes/ManageBikesViewModel.kt`
- `app/src/main/java/fr/nextgear/mesentretiensmoto/features/manageMaintenancesOfBike/ManageMaintenancesViewModel.kt`

### 8.4 Écrans/UI
- `app/src/main/java/fr/nextgear/mesentretiensmoto/features/manageBikes/FragmentManageBikes.kt`
- `app/src/main/java/fr/nextgear/mesentretiensmoto/features/manageMaintenancesOfBike/FragmentManageMaintenances.kt`

---

---

## 9. Spécifications UI/UX détaillées

### 9.1 Design System

**Palette de couleurs** :
```
primary         = #3F51B5 (Indigo)
primaryDark     = #303F9F
accent          = #FF4081 (Pink)

teal            = #009688 (header tab "À faire")
darkGreen       = #388E3C
indigo          = #3F51B5 (header tab "Faits")

primaryText     = #212121
secondaryText   = #727272
white           = #FFFFFF
```

**Typographie** :
- Texte principal : Roboto Regular
- Titres/Noms : Roboto Bold
- Taille texte cellule : 14-16sp

### 9.2 Wireframes textuels

#### Écran 1 : Liste des motos
```
┌─────────────────────────────────────┐
│  [Toolbar: "Mes entretiens moto"]   │
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🏍️ Yamaha XT 500      [✏️] │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🏍️ Honda CRF 450       [✏️] │   │
│  └─────────────────────────────┘   │
│                                     │
│  (ou si vide:)                      │
│  "Vous n'avez pour l'instant        │
│   ajouté aucune moto"               │
│                                     │
│                            [+ FAB]  │
└─────────────────────────────────────┘
```

#### Écran 2 : Entretiens d'une moto
```
┌─────────────────────────────────────┐
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  │  <- Header parallax (200dp)
│  ▓▓▓  [Image mécanique/liste]  ▓▓▓  │     Bleu si "Faits"
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  │     Vert si "À faire"
├────────────────┬────────────────────┤
│    [Faits]     │    [A faire]       │  <- Tabs
├────────────────┴────────────────────┤
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Vidange                      │   │
│  │ 5000 KM           12/03/2024 │   │  <- CardView 100dp
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Pneu avant                   │   │
│  │ 3500 KM           05/01/2024 │   │
│  └─────────────────────────────┘   │
│                                     │
│                            [+ FAB]  │
└─────────────────────────────────────┘
```

### 9.3 Dimensions et espacements

| Élément | Dimension |
|---------|-----------|
| CardView hauteur (moto) | wrap_content |
| CardView hauteur (entretien) | 100dp |
| CardView margin | 8dp |
| Padding interne cellule | 6dp |
| FAB margin | 16dp |
| Header parallax | 200dp |
| Margin texte gauche | 10-20dp |

### 9.4 Animations et transitions

| Interaction | Animation |
|-------------|-----------|
| Navigation moto → entretiens | Transition standard activité |
| Swipe suppression | Glissement horizontal avec fond coloré |
| FAB scroll | Masquage automatique au scroll down, réapparition au scroll up |
| Ajout élément liste | Animation "fall down" (LayoutAnimationController) |
| Changement onglet | ViewPager slide animation |
| Apparition liste | Animation cascade sur tous les items |

### 9.5 Couleurs dynamiques

| Élément | Onglet "Faits" | Onglet "À faire" |
|---------|----------------|------------------|
| Header parallax | Bleu (indigo #3F51B5) | Vert (teal #009688) |
| Image header | Image mécanique | Image liste |
| FAB | Accent (#FF4081) | Primary (#3F51B5) |

### 9.6 Gestures

| Gesture | Zone | Action |
|---------|------|--------|
| Tap | Cellule moto | Navigation vers entretiens |
| Long press | Cellule moto | Dialog modification |
| Tap | Icône édition | Dialog modification |
| Tap | Cellule entretien "À faire" | Dialog "Marquer comme fait" |
| Swipe gauche/droite | Cellule entretien | Suppression avec undo |
| Tap | FAB | Dialog ajout |
| Scroll vertical | Liste | Pagination + masquage FAB |

---

## 10. Architecture technique détaillée

### 10.1 Flux de données

```
┌─────────────────────────────────────────────────────────────┐
│                         UI LAYER                            │
│  ┌─────────────┐     ┌─────────────────────────────────┐   │
│  │  Fragment   │ ◄── │  LiveData<List<T>>              │   │
│  │  (observe)  │     │  - bikes: MutableLiveData       │   │
│  └─────────────┘     │  - maintenances: MutableLiveData│   │
│         │            │  - error: MutableLiveData       │   │
│         ▼            └─────────────────────────────────┘   │
│  ┌─────────────┐              ▲                            │
│  │  ViewModel  │──────────────┘                            │
│  └─────────────┘                                           │
│         │                                                   │
└─────────│───────────────────────────────────────────────────┘
          │ RxJava (Schedulers.newThread → AndroidSchedulers.mainThread)
          ▼
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                           │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  Interactor                                            │ │
│  │  - addBike() : Completable                            │ │
│  │  - addMaintenance() : Single<Maintenance>             │ │
│  │  - removeMaintenance() : Completable                  │ │
│  │  - getBikesFromSQLite() : Observable<List<Bike>>      │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────│───────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────────┐
│                       DATA LAYER                            │
│  ┌────────────────────┐    ┌────────────────────────────┐  │
│  │  SQLite (OrmLite)  │    │  Firebase Realtime DB      │  │
│  │  - BikeDao         │    │  - users/{uid}/bikes/      │  │
│  │  - MaintenanceDao  │    │  - .../maintenances/       │  │
│  └────────────────────┘    └────────────────────────────┘  │
│           ▲                           ▲                     │
│           │     LOCAL FIRST           │  SYNC SI CONNECTÉ   │
│           └───────────────────────────┘                     │
└─────────────────────────────────────────────────────────────┘
```

### 10.2 Flux de synchronisation Firebase

```
ÉCRITURE:
1. Utilisateur crée/modifie → ViewModel.addBike()
2. Interactor écrit en SQLite LOCAL (immédiat)
3. SI user connecté → Push vers Firebase
4. UI mise à jour via LiveData

LECTURE (au démarrage):
1. ViewModel.init() → Lit SQLite local → Affiche
2. SI user connecté → Ajoute ChildEventListener sur Firebase
3. onChildAdded() → Vérifie si existe en local
4. Si nouveau → Sauve en local → Met à jour LiveData
```

### 10.3 Communication inter-composants (Event Bus)

```kotlin
// Événements Otto utilisés
EventAddMaintenance(maintenance: Maintenance)  // Quand on marque un entretien comme fait
EventRefreshBikesList()                        // Après modification d'une moto
EventMarkMaintenanceDone(maintenance, hours)   // Déclenche la mise à jour
```

**Flux "Marquer comme fait"** :
```
1. User tap sur entretien "À faire"
2. Dialog s'ouvre, user entre valeur km/h
3. ViewModel (ToDo).updateMaintenaceToDone()
4. Supprime de la liste locale (ToDo)
5. Post EventAddMaintenance sur le bus
6. ViewModel (Done) reçoit l'événement
7. Ajoute l'entretien à sa liste (Done)
8. Les deux onglets se rafraîchissent
```

### 10.4 Injection de dépendances (Koin)

```kotlin
// Modules actuels
ManageBikesModule {
    viewModel { ManageBikesViewModel() }
}

ManageMaintenanceModule {
    viewModel { (bike: Bike, isDone: Boolean) ->
        ManageMaintenancesViewModel(bike, isDone)
    }
}
```

---

## 11. Cas d'erreur et gestion

### 11.1 Énumération des erreurs

```kotlin
enum ErrorManageMaintenances {
    NONE,                                // Pas d'erreur
    ERROR_COULD_NOT_RETRIEVE_MAINTENANCES,  // Erreur lecture BDD
    ERROR_ADDING_MAINTENANCE,            // Erreur ajout (SQLException)
    ERROR_REMOVING_MAINTENANCE           // Erreur suppression
}
```

### 11.2 Tableau des cas d'erreur

| Scénario | Cause | Comportement actuel | Comportement recommandé KMP |
|----------|-------|---------------------|------------------------------|
| Échec connexion Google | Auth Firebase | Dialog d'erreur affiché | Dialog + option réessayer |
| Nom moto vide | Validation | Dialog refuse de fermer | Afficher message inline |
| Nom entretien vide | Validation | Toast "Veuillez remplir..." | Afficher message inline |
| Valeur km/h vide | Validation | Toast "Veuillez remplir..." | Afficher message inline |
| Erreur ajout SQLite | SQLException | error LiveData = ERROR_ADDING | Snackbar + retry option |
| Erreur suppression | SQLException | error LiveData = ERROR_REMOVING | Restaurer élément + message |
| Pas de connexion Firebase | Réseau | Fonctionne en local | Indicateur "hors-ligne" |
| Utilisateur non connecté | Auth | Mode local uniquement | Proposer connexion |
| Firebase sync échoue | Réseau/Serveur | Logs erreur (Logger.e) | Queue pour retry |
| KotlinNullPointerException | BDD corrompue | onError propagé | Afficher erreur + support |

### 11.3 Messages d'erreur utilisateur

```
# Validation
validation_empty_field = "Veuillez remplir correctement les champs !"

# Erreurs réseau (à implémenter en KMP)
error_network_offline = "Vous êtes hors-ligne. Les données seront synchronisées ultérieurement."
error_sync_failed = "La synchronisation a échoué. Réessayer ?"

# Erreurs BDD (à implémenter en KMP)
error_save_failed = "Impossible de sauvegarder. Réessayez."
error_delete_failed = "Impossible de supprimer. Réessayez."
error_load_failed = "Impossible de charger les données."
```

### 11.4 Stratégie de gestion d'erreurs recommandée (KMP)

```
1. VALIDATION
   - Validation côté UI (champs requis, format)
   - Messages d'erreur inline sous les champs
   - Bouton désactivé tant que invalide

2. ERREURS RÉSEAU
   - Détection état réseau (ConnectivityManager / NWPathMonitor)
   - Queue d'opérations en attente
   - Retry automatique quand connexion revient
   - Badge/indicateur "hors-ligne" visible

3. ERREURS BASE DE DONNÉES
   - Try-catch autour des opérations DAO
   - Logging détaillé pour debug
   - Message utilisateur générique
   - Option de retry

4. ERREURS FIREBASE
   - Listener sur les erreurs de sync
   - Fallback sur données locales
   - Notification utilisateur non-bloquante
```

---

## 12. Scénarios utilisateur complets

### 12.1 Scénario : Première utilisation

```
1. Utilisateur ouvre l'app
2. Écran de connexion Google (Firebase Auth)
3. Après connexion → Écran motos (vide)
4. Message "Vous n'avez pour l'instant ajouté aucune moto"
5. Tap sur FAB "+"
6. Dialog "Nom de la moto"
7. Saisie "Ma première moto" → Ajouter
8. Moto apparaît dans la liste
9. Données sync vers Firebase
```

### 12.2 Scénario : Ajouter un entretien fait

```
1. Tap sur une moto → Écran entretiens
2. Onglet "Faits" actif
3. Tap sur FAB "+"
4. Dialog avec 2 champs :
   - "Type d'entretien" : "Vidange"
   - "Nombre de kilomètres" : "5000"
5. Tap "Ajouter"
6. Entretien apparaît dans la liste avec :
   - Nom : "Vidange"
   - Valeur : "5000 KM"
   - Date : date du jour (format dd/MM/yyyy)
```

### 12.3 Scénario : Convertir un "À faire" en "Fait"

```
1. Onglet "A faire" → Liste des entretiens planifiés
2. Tap sur "Changement pneu"
3. Dialog "Marquer cet entretien comme terminé"
4. Saisie "12000" dans le champ km/h
5. Tap "Ajouter"
6. L'entretien disparaît de "A faire"
7. L'entretien apparaît dans "Faits" avec la valeur et date
```

### 12.4 Scénario : Supprimer avec undo

```
1. Swipe gauche sur un entretien
2. Entretien supprimé de la liste
3. Snackbar apparaît : "Entretien supprimé" [Annuler]
4. CAS A : User ne fait rien → Suppression définitive après timeout
5. CAS B : User tap "Annuler" → Entretien restauré dans la liste
```

---

## 13. Résumé pour redéveloppement KMP

L'application est un **gestionnaire d'entretiens moto** avec :

1. **2 écrans principaux** : Liste des motos → Entretiens d'une moto (2 onglets)
2. **2 entités** : Bike et Maintenance (relation 1-N)
3. **Feature clé** : Comptage en KM ou Heures selon le type de moto
4. **Sync cloud** : Firebase Auth + Realtime Database
5. **UX importante** : Swipe-to-delete avec undo, dialogs Material Design

### Points d'attention pour KMP

- **Architecture** : Maximiser le code partagé (modèles, ViewModels, logique métier)
- **UI** : Jetpack Compose (Android) / SwiftUI (iOS) avec design cohérent
- **Offline-first** : SQLDelight + sync Firebase avec queue d'opérations
- **Erreurs** : Gestion robuste avec messages utilisateur clairs
- **Tests** : Tests unitaires partagés pour la logique métier

L'agent qui redéveloppera devra implémenter ces fonctionnalités en utilisant une architecture KMP moderne avec code partagé maximal entre Android et iOS.

---

## 18. Configuration Firebase et sécurité

### 18.1 Structure Firebase Realtime Database

```
{
  "users": {
    "{userId}": {
      "bikes": {
        "{bikeRef}": {
          "nameBike": "string",
          "countingMethod": "KM" | "HOURS",
          "maintenances": {
            "{maintenanceRef}": {
              "nameMaintenance": "string",
              "nbHoursMaintenance": "float",
              "dateMaintenance": "long (timestamp ms)",
              "isDone": "boolean"
            }
          }
        }
      }
    }
  }
}
```

### 18.2 Règles de sécurité Firebase (à implémenter)

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid",
        "bikes": {
          "$bikeId": {
            ".validate": "newData.hasChildren(['nameBike'])",
            "maintenances": {
              "$maintenanceId": {
                ".validate": "newData.hasChildren(['nameMaintenance', 'isDone'])"
              }
            }
          }
        }
      }
    }
  }
}
```

**⚠️ CRITIQUE** : L'application actuelle n'a PAS de règles de sécurité documentées. Ces règles doivent être implémentées.

### 18.3 Configuration Firebase requise

| Élément | Android | iOS |
|---------|---------|-----|
| Fichier config | `google-services.json` | `GoogleService-Info.plist` |
| Package/Bundle ID | `fr.nextgear.mesentretiensmoto` | À définir |
| Auth providers | Google Sign-In | Google Sign-In |
| Services activés | Authentication, Realtime Database | Idem |

### 18.4 Permissions Android

```xml
<!-- Permissions implicites (via Firebase/GMS) -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Aucune permission dangereuse requise -->
```

### 18.5 Sécurité des données

| Aspect | État actuel | Recommandation KMP |
|--------|-------------|-------------------|
| Stockage tokens auth | SharedPreferences (non chiffré) | EncryptedSharedPreferences / Keychain |
| Données SQLite | Non chiffré | SQLCipher optionnel |
| Communication réseau | HTTPS (Firebase) | Conserver HTTPS |
| Validation entrées | Côté client uniquement | + Validation Firebase Rules |

---

## 19. Librairies tierces utilisées

### 19.1 Librairies principales (documentées)

| Librairie | Version | Usage | Alternative KMP |
|-----------|---------|-------|-----------------|
| Firebase Auth | 16.0.1 | Authentification | Firebase KMP SDK |
| Firebase Database | 16.0.1 | BDD temps réel | Firebase KMP SDK |
| Koin | 1.0.2 | DI | Koin Multiplatform |
| OrmLite | 5.0 | ORM SQLite | SQLDelight |
| RxJava 2 | 2.2.0 | Réactivité | Kotlin Coroutines + Flow |
| Otto | 1.3.8 | Event Bus | StateFlow / SharedFlow |

### 19.2 Librairies UI (non documentées précédemment)

| Librairie | Version | Usage | Alternative KMP |
|-----------|---------|-------|-----------------|
| MaterialViewPager | 1.2.3 | ViewPager avec header parallax | Compose Pager + custom header |
| Smart Adapters | 1.3.1 | RecyclerView adapters | LazyColumn (Compose) |
| Material Dialogs | 0.9.6.0 | Dialogs personnalisés | AlertDialog (Compose) |
| Lovely Dialog | 1.0.7 | Dialogs avec input | AlertDialog (Compose) |
| Toasty | 1.4.0 | Toast personnalisés | Snackbar (Compose) |
| FAB Library | 1.6.4 | FloatingActionButton | FAB (Compose Material3) |
| Glide | 4.6.0 | Chargement images | Coil Multiplatform |
| KenBurnsView | 1.0.7 | Animation pan/zoom images | Custom Compose animation |

### 19.3 Librairies architecture

| Librairie | Version | Usage | Alternative KMP |
|-----------|---------|-------|-----------------|
| Mosby MVP | 3.1.0 | Architecture MVP | MVVM natif |
| Android Architecture | 1.1.1 | ViewModel, LiveData | KMP ViewModel + StateFlow |
| EasyFlow | 1.3.1 | State machine | Sealed classes + when |

---

## 14. Analyse de l'existant - Problèmes à ne pas reproduire

### 14.1 État des tests actuels

| Type de test | État | Fichier |
|--------------|------|---------|
| Tests unitaires | ❌ 1 seul test trivial (2+2=4) | `src/test/.../ExampleUnitTest.java` |
| Tests instrumentés | ⚠️ 1 test BDD (insertion BDD) | `src/androidTest/.../TestDatabase.java` |
| Tests UI (Espresso) | ❌ Aucun | - |
| Tests ViewModel | ❌ Aucun | - |
| Tests Interactor | ❌ Aucun | - |

**Dépendances de test présentes mais non utilisées** : JUnit, Truth, Mockito, Koin Test, Espresso

### 14.2 Problèmes de qualité de code

#### A. Gestion des erreurs (CRITIQUE)

| Problème | Occurrences | Impact |
|----------|-------------|--------|
| `printStackTrace()` au lieu de Logger | 12+ | Pas de logs en production |
| Opérateur `!!` (non-null assertion) | 43+ | Crashes potentiels |
| Exceptions avalées silencieusement | Multiple | Bugs masqués |
| Catch blocks retournant -1 ou null | Multiple | Erreurs non détectées |

**Exemples de code problématique** :
```kotlin
// ❌ MAUVAIS - À NE PAS REPRODUIRE
catch (e: SQLException) {
    e.printStackTrace()  // Pas de log en prod
    return -1            // Erreur silencieuse
}

poMaintenance.bike?.reference!!  // Crash si null

throwable.message!!  // Crash si message null
```

#### B. Null Safety (CRITIQUE)

**Patterns dangereux utilisés** :
- `?.` suivi de `!!` (défait la sécurité)
- `value!!.addAll(...)` sans vérification
- `p0.key!!` sur valeurs Firebase nullable

**Recommandation KMP** : Utiliser des types nullables explicites et `?.let {}` / `?: return`

#### C. Documentation

| Aspect | État |
|--------|------|
| KDoc sur les classes | ❌ Absent |
| KDoc sur les méthodes | ❌ Absent |
| Commentaires inline | ⚠️ 1 seul commentaire utile |
| README technique | ❌ Absent |

#### D. Conventions de nommage (obsolètes)

| Pattern utilisé | Exemple | Recommandation KMP |
|-----------------|---------|-------------------|
| Notation hongroise | `psNameBike`, `pbIsDone`, `poMaintenance` | `bikeName`, `isDone`, `maintenance` |
| Préfixe `m` | `mViewModel`, `mBike` | `viewModel`, `bike` |
| Préfixe `lo` | `loMaintenanceBuilder` | `maintenanceBuilder` |

### 14.3 Problèmes d'architecture

#### A. Pattern MVVM mal appliqué

| Violation | Localisation | Impact |
|-----------|--------------|--------|
| Firebase listeners dans ViewModel | `ManageBikesViewModel.init` | Couplage fort |
| Interactors instanciés manuellement | `private val mInteractor = Interactor()` | Non testable |
| DAO embarqué dans les modèles | `Bike.BikeDao`, `Maintenance.MaintenanceDao` | Couplage données/domaine |
| Navigation dans les Views | `BikeCellView.bind()` | Responsabilité UI |

#### B. God Classes identifiées

| Classe | Responsabilités | Recommandation |
|--------|----------------|----------------|
| `ManageBikesViewModel` | 5+ (Firebase, state, business, data, RxJava) | Séparer en ViewModel + Repository |
| `ManageMaintenancesViewModel` | 6+ (Firebase, state, events, undo, business, transform) | Séparer en ViewModel + UseCase + Repository |
| `FragmentManageMaintenances` | 5+ (UI, state, dialogs, events, errors) | Extraire state management |

#### C. Couches non séparées

```
ARCHITECTURE ACTUELLE (problématique):
┌─────────────────────────────────────────────────────┐
│ Activity/Fragment                                    │
│     ↓                                               │
│ ViewModel ──────────────────────┐                   │
│     ↓                           ↓                   │
│ Interactor ← Firebase      MainThreadBus            │
│     ↓                                               │
│ Model.DAO ← SQLite                                  │
└─────────────────────────────────────────────────────┘
Problèmes: Firebase dans ViewModel, DAO dans Model, pas de Repository
```

#### D. Injection de dépendances insuffisante

```kotlin
// ❌ ACTUEL - Modules Koin vides
val ManageBikesmodule = module {
    viewModel { ManageBikesViewModel() }  // Aucune dépendance injectée
}

// ❌ Dans ViewModel - instanciation manuelle
private val mInteractorManageBikes = InteractorManageBikes()
```

### 14.4 Risques de memory leaks

| Risque | Localisation | Cause |
|--------|--------------|-------|
| Firebase listeners jamais désenregistrés | `ManageBikesViewModel.init`, `ManageMaintenancesViewModel.init` | Pas de cleanup dans `onCleared()` |
| Singleton App.instance | `App.kt` | Référence statique |
| Kotlin Synthetics (déprécié) | Tous les fragments | Import `kotlinx.android.synthetic.*` |
| Handler dans MainThreadBus | `MainThreadBus.kt` | Callbacks longue durée |

### 14.5 Problèmes de threading

| Problème | Code | Impact |
|----------|------|--------|
| Création de nouveaux threads | `Schedulers.newThread()` partout | Surconsommation mémoire |
| Modifications LiveData non synchronisées | `maintenances.value!!.add()` | Race conditions |
| UI depuis thread Firebase | `ChildEventListener.onChildAdded()` | Crash potentiel |

**Recommandation** : Utiliser `Dispatchers.IO` avec Coroutines

### 14.6 APIs et librairies obsolètes

| Librairie | Version actuelle | État | Remplacement KMP |
|-----------|------------------|------|------------------|
| Support Library | 28.0.0 | ❌ Déprécié | AndroidX |
| android.arch.lifecycle | - | ❌ Déprécié | androidx.lifecycle |
| Otto (Event Bus) | 1.3.8 | ❌ Archivé | StateFlow / SharedFlow |
| OrmLite | 5.0 | ⚠️ Peu maintenu | SQLDelight |
| Kotlin Synthetics | - | ❌ Déprécié | ViewBinding / Compose |
| SmartAdapters | 1.3.1 | ⚠️ Abandonné | LazyColumn (Compose) |

---

## 15. Architecture recommandée pour KMP

### 15.1 Structure de projet

```
shared/
├── src/commonMain/kotlin/
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Bike.kt
│   │   │   ├── Maintenance.kt
│   │   │   └── CountingMethod.kt
│   │   ├── repository/
│   │   │   ├── BikeRepository.kt (interface)
│   │   │   └── MaintenanceRepository.kt (interface)
│   │   └── usecase/
│   │       ├── AddBikeUseCase.kt
│   │       ├── AddMaintenanceUseCase.kt
│   │       ├── MarkMaintenanceDoneUseCase.kt
│   │       └── DeleteMaintenanceUseCase.kt
│   ├── data/
│   │   ├── local/
│   │   │   ├── DatabaseDriverFactory.kt (expect)
│   │   │   ├── BikeLocalDataSource.kt
│   │   │   └── MaintenanceLocalDataSource.kt
│   │   ├── remote/
│   │   │   ├── FirebaseDataSource.kt (expect)
│   │   │   └── SyncManager.kt
│   │   └── repository/
│   │       ├── BikeRepositoryImpl.kt
│   │       └── MaintenanceRepositoryImpl.kt
│   ├── presentation/
│   │   ├── bikes/
│   │   │   ├── BikesViewModel.kt
│   │   │   └── BikesUiState.kt
│   │   └── maintenances/
│   │       ├── MaintenancesViewModel.kt
│   │       └── MaintenancesUiState.kt
│   └── di/
│       └── SharedModule.kt
│
├── src/androidMain/kotlin/
│   └── data/local/DatabaseDriverFactory.kt (actual)
│
└── src/iosMain/kotlin/
    └── data/local/DatabaseDriverFactory.kt (actual)

androidApp/
├── ui/
│   ├── bikes/
│   │   └── BikesScreen.kt (Compose)
│   └── maintenances/
│       └── MaintenancesScreen.kt (Compose)
└── di/
    └── AndroidModule.kt

iosApp/
└── UI/
    ├── BikesView.swift (SwiftUI)
    └── MaintenancesView.swift (SwiftUI)
```

### 15.2 Principes à respecter

| Principe | Implémentation |
|----------|----------------|
| **Single Responsibility** | 1 UseCase = 1 action métier |
| **Dependency Inversion** | Interfaces dans domain/, implémentations dans data/ |
| **Repository Pattern** | Abstraction des sources de données |
| **Unidirectional Data Flow** | ViewModel → UiState → UI |
| **Offline-First** | Écriture locale puis sync async |

### 15.3 Gestion d'état recommandée

```kotlin
// État UI immutable avec sealed class
sealed class BikesUiState {
    object Loading : BikesUiState()
    data class Success(val bikes: List<Bike>) : BikesUiState()
    data class Error(val message: String) : BikesUiState()
    object Empty : BikesUiState()
}

// ViewModel avec StateFlow
class BikesViewModel(
    private val getBikesUseCase: GetBikesUseCase,
    private val addBikeUseCase: AddBikeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BikesUiState>(BikesUiState.Loading)
    val uiState: StateFlow<BikesUiState> = _uiState.asStateFlow()

    fun loadBikes() {
        viewModelScope.launch {
            getBikesUseCase()
                .catch { _uiState.value = BikesUiState.Error(it.message ?: "Erreur") }
                .collect { bikes ->
                    _uiState.value = if (bikes.isEmpty())
                        BikesUiState.Empty
                    else
                        BikesUiState.Success(bikes)
                }
        }
    }
}
```

### 15.4 Tests à implémenter

| Couche | Type de test | Couverture cible |
|--------|--------------|------------------|
| **UseCase** | Unit tests (shared) | 100% |
| **Repository** | Unit tests avec mocks | 90% |
| **ViewModel** | Unit tests avec StateFlow | 90% |
| **DataSource** | Integration tests | 80% |
| **UI** | Screenshot tests / UI tests | 70% |

**Framework de test KMP** : kotlin.test + Turbine (pour Flow) + MockK

---

## 16. Checklist de validation pour le redéveloppement

### 16.1 Fonctionnalités (toutes obligatoires)

- [ ] Authentification Google
- [ ] Liste des motos (CRUD)
- [ ] Choix méthode de comptage (KM/Heures)
- [ ] Liste entretiens avec 2 onglets (Faits/À faire)
- [ ] Ajout entretien fait (nom + valeur + date auto)
- [ ] Ajout entretien à faire (nom seul)
- [ ] Marquer entretien comme fait
- [ ] Suppression avec undo (Snackbar)
- [ ] Synchronisation Firebase
- [ ] Mode offline

### 16.2 Qualité de code (toutes obligatoires)

- [ ] Aucun `!!` - utiliser `?.let`, `?:`, `requireNotNull` avec message
- [ ] Aucun `printStackTrace()` - utiliser un logger (Napier pour KMP)
- [ ] 0 warning Kotlin
- [ ] KDoc sur toutes les classes et méthodes publiques
- [ ] Conventions de nommage Kotlin standard (pas de hongrois)

### 16.3 Architecture (toutes obligatoires)

- [ ] Séparation domain/data/presentation
- [ ] Repository pattern implémenté
- [ ] UseCase pour chaque action métier
- [ ] ViewModel sans dépendance Firebase directe
- [ ] DI complète (toutes dépendances injectées)
- [ ] État UI via StateFlow/sealed class

### 16.4 Tests (couverture minimum)

- [ ] Tests unitaires UseCase : 100%
- [ ] Tests unitaires ViewModel : 90%
- [ ] Tests Repository : 80%
- [ ] Au moins 1 test UI par écran

---

## 17. Roadmap de développement

### Phase 0 : Setup projet KMP

**Objectif** : Infrastructure technique prête

| Tâche | Priorité | Dépendance |
|-------|----------|------------|
| Créer projet KMP (Android + iOS) | 🔴 Critique | - |
| Configurer Gradle avec versions catalog | 🔴 Critique | Setup projet |
| Ajouter SQLDelight | 🔴 Critique | Setup projet |
| Ajouter Koin Multiplatform | 🔴 Critique | Setup projet |
| Ajouter Ktor (pour future API) | 🟡 Moyenne | Setup projet |
| Configurer CI/CD basique | 🟢 Basse | Setup projet |

**Livrables** :
- Projet compilant sur Android et iOS
- Architecture de dossiers en place
- DI fonctionnelle

---

### Phase 1 : MVP - Gestion des motos (offline)

**Objectif** : Fonctionnalité core sans réseau

| Fonctionnalité | Couche | Fichiers |
|----------------|--------|----------|
| Modèle Bike | Domain | `Bike.kt`, `CountingMethod.kt` |
| BikeRepository (interface) | Domain | `BikeRepository.kt` |
| SQLDelight schema bikes | Data | `Database.sq` |
| BikeLocalDataSource | Data | `BikeLocalDataSource.kt` |
| BikeRepositoryImpl | Data | `BikeRepositoryImpl.kt` |
| GetBikesUseCase | Domain | `GetBikesUseCase.kt` |
| AddBikeUseCase | Domain | `AddBikeUseCase.kt` |
| UpdateBikeUseCase | Domain | `UpdateBikeUseCase.kt` |
| BikesViewModel | Presentation | `BikesViewModel.kt`, `BikesUiState.kt` |
| BikesScreen (Android) | UI | `BikesScreen.kt` (Compose) |
| BikesView (iOS) | UI | `BikesView.swift` (SwiftUI) |

**Tests obligatoires** :
- [ ] `AddBikeUseCaseTest`
- [ ] `GetBikesUseCaseTest`
- [ ] `BikesViewModelTest`

**Critères de validation** :
- ✅ Ajouter une moto avec nom
- ✅ Modifier le nom d'une moto
- ✅ Modifier la méthode de comptage
- ✅ Liste des motos persistée localement
- ✅ Fonctionne sur Android ET iOS

---

### Phase 2 : Gestion des entretiens (offline)

**Objectif** : CRUD complet entretiens sans réseau

| Fonctionnalité | Couche | Fichiers |
|----------------|--------|----------|
| Modèle Maintenance | Domain | `Maintenance.kt`, `MaintenanceState.kt` |
| MaintenanceRepository (interface) | Domain | `MaintenanceRepository.kt` |
| SQLDelight schema maintenances | Data | `Database.sq` (update) |
| MaintenanceLocalDataSource | Data | `MaintenanceLocalDataSource.kt` |
| MaintenanceRepositoryImpl | Data | `MaintenanceRepositoryImpl.kt` |
| GetMaintenancesUseCase | Domain | `GetMaintenancesUseCase.kt` |
| AddMaintenanceUseCase | Domain | `AddMaintenanceUseCase.kt` |
| MarkMaintenanceDoneUseCase | Domain | `MarkMaintenanceDoneUseCase.kt` |
| DeleteMaintenanceUseCase | Domain | `DeleteMaintenanceUseCase.kt` |
| MaintenancesViewModel | Presentation | `MaintenancesViewModel.kt`, `MaintenancesUiState.kt` |
| MaintenancesScreen (Android) | UI | `MaintenancesScreen.kt` |
| MaintenancesView (iOS) | UI | `MaintenancesView.swift` |

**Tests obligatoires** :
- [ ] `AddMaintenanceUseCaseTest`
- [ ] `MarkMaintenanceDoneUseCaseTest`
- [ ] `DeleteMaintenanceUseCaseTest`
- [ ] `MaintenancesViewModelTest`

**Critères de validation** :
- ✅ Ajouter entretien "À faire" (nom seul)
- ✅ Ajouter entretien "Fait" (nom + valeur + date)
- ✅ Marquer un entretien comme fait
- ✅ Supprimer un entretien avec undo
- ✅ Affichage correct km/h selon méthode de comptage
- ✅ Tri par valeur décroissante
- ✅ 2 onglets (Faits / À faire)

---

### Phase 3 : UX et polish

**Objectif** : Expérience utilisateur complète

| Fonctionnalité | Plateforme | Description |
|----------------|------------|-------------|
| Swipe-to-delete | Android | ItemTouchHelper + Snackbar |
| Swipe-to-delete | iOS | SwiftUI gesture + undo |
| Animations listes | Android | AnimatedVisibility Compose |
| Animations listes | iOS | SwiftUI transitions |
| Dialogs Material | Android | AlertDialog Compose |
| Dialogs | iOS | Sheet / Alert SwiftUI |
| Empty states | Shared | Messages "Aucune moto" etc. |
| Couleurs dynamiques tabs | Shared | Header couleur selon onglet |
| FAB auto-hide on scroll | Android | NestedScrollConnection |

**Critères de validation** :
- ✅ UX identique à l'app originale
- ✅ Animations fluides
- ✅ Dialogs fonctionnels
- ✅ Messages d'erreur clairs

---

### Phase 4 : Authentification Firebase

**Objectif** : Connexion Google fonctionnelle

| Fonctionnalité | Couche | Fichiers |
|----------------|--------|----------|
| AuthRepository (interface) | Domain | `AuthRepository.kt` |
| FirebaseAuthDataSource (Android) | Data | `FirebaseAuthDataSource.kt` |
| FirebaseAuthDataSource (iOS) | Data | `FirebaseAuthDataSource.swift` |
| SignInUseCase | Domain | `SignInUseCase.kt` |
| GetCurrentUserUseCase | Domain | `GetCurrentUserUseCase.kt` |
| AuthViewModel | Presentation | `AuthViewModel.kt` |
| SignInScreen | UI | Écran de connexion |

**Tests obligatoires** :
- [ ] `SignInUseCaseTest` (avec mock)
- [ ] `AuthViewModelTest`

**Critères de validation** :
- ✅ Connexion Google fonctionnelle
- ✅ Détection utilisateur connecté au lancement
- ✅ Gestion erreur connexion
- ✅ Mode debug bypass auth

---

### Phase 5 : Synchronisation Firebase

**Objectif** : Sync bidirectionnelle cloud

| Fonctionnalité | Couche | Fichiers |
|----------------|--------|----------|
| BikeRemoteDataSource | Data | `BikeFirebaseDataSource.kt` |
| MaintenanceRemoteDataSource | Data | `MaintenanceFirebaseDataSource.kt` |
| SyncManager | Data | `SyncManager.kt` |
| Update BikeRepositoryImpl | Data | Ajouter logique sync |
| Update MaintenanceRepositoryImpl | Data | Ajouter logique sync |
| Indicateur offline | UI | Badge "hors-ligne" |

**Stratégie de sync** :
1. Écriture locale immédiate (SQLDelight)
2. Si connecté → Push vers Firebase
3. Listener Firebase → Mise à jour locale si nouveau
4. Queue d'opérations si offline → Replay à la reconnexion

**Tests obligatoires** :
- [ ] `SyncManagerTest`
- [ ] Tests d'intégration sync

**Critères de validation** :
- ✅ Données sync entre appareils
- ✅ Création offline → sync au retour réseau
- ✅ Pas de perte de données
- ✅ Pas de doublons

---

### Phase 6 : Tests et stabilisation

**Objectif** : Couverture de tests et qualité production

| Tâche | Type | Cible |
|-------|------|-------|
| Tests unitaires UseCase | Unit | 100% |
| Tests unitaires ViewModel | Unit | 90% |
| Tests Repository | Integration | 80% |
| Tests UI Android | UI/Screenshot | 1 par écran |
| Tests UI iOS | UI | 1 par écran |
| Tests E2E sync | Integration | Scénarios clés |
| Audit accessibilité | Manual | WCAG AA |
| Audit performance | Profiling | < 16ms/frame |
| Fix bugs identifiés | Bug fix | 0 critique |

**Critères de validation** :
- ✅ Couverture tests > 80% global
- ✅ 0 crash en usage normal
- ✅ Performance acceptable

---

### Phase 7 : Release

**Objectif** : Publication stores

| Tâche | Plateforme | Description |
|-------|------------|-------------|
| App signing | Android | Keystore configuré |
| App Store Connect | iOS | Certificates + Provisioning |
| Screenshots stores | Both | 5+ screenshots par plateforme |
| Description store | Both | Texte marketing FR |
| Privacy policy | Both | Page web conforme |
| Build release | Android | APK/AAB signé |
| Build release | iOS | Archive Xcode |
| Soumission | Both | Upload stores |
| Beta testing | Both | TestFlight / Internal testing |

---

### Résumé des phases

| Phase | Nom | Durée estimée* | Dépendances |
|-------|-----|---------------|-------------|
| 0 | Setup projet KMP | - | - |
| 1 | MVP - Motos (offline) | - | Phase 0 |
| 2 | Entretiens (offline) | - | Phase 1 |
| 3 | UX et polish | - | Phase 2 |
| 4 | Auth Firebase | - | Phase 0 |
| 5 | Sync Firebase | - | Phase 4 + Phase 2 |
| 6 | Tests et stabilisation | - | Phase 5 |
| 7 | Release | - | Phase 6 |

*Pas d'estimation de durée - dépend des ressources et de l'expérience

### Diagramme de dépendances

```
Phase 0 (Setup)
    │
    ├──────────────────┐
    ▼                  ▼
Phase 1 (Motos)    Phase 4 (Auth)
    │                  │
    ▼                  │
Phase 2 (Entretiens)   │
    │                  │
    ▼                  │
Phase 3 (UX)           │
    │                  │
    └────────┬─────────┘
             ▼
      Phase 5 (Sync)
             │
             ▼
      Phase 6 (Tests)
             │
             ▼
      Phase 7 (Release)
```
