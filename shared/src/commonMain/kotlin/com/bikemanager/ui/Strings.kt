package com.bikemanager.ui

/**
 * Hardcoded strings for the shared UI.
 * In a future iteration, these could be migrated to Compose Resources for proper localization.
 */
object Strings {
    const val APP_NAME = "Mes entretiens moto"
    const val MY_BIKES = "Mes motos"
    const val MAINTENANCES = "Entretiens"
    const val NO_BIKES = "Vous n'avez pour l'instant ajouté aucune moto"
    const val NO_MAINTENANCE = "Aucun entretien pour cette moto"
    const val TAB_DONE = "Faits"
    const val TAB_TODO = "A faire"
    const val ADD_BIKE_TITLE = "Ajout de moto"
    const val ADD_BIKE_MESSAGE = "Veuillez renseigner le nom de la moto à ajouter"
    const val ADD_MAINTENANCE_DONE = "Entretien effectué"
    const val ADD_MAINTENANCE_TODO = "Entretien à faire"
    const val MAINTENANCE_TYPE_HINT = "Type d'entretien"
    const val NB_HOURS_HINT = "Nombre d'heures"
    const val NB_KM_HINT = "Nombre de kilomètres"
    const val MARK_DONE_TITLE = "Marquer cet entretien comme terminé"
    const val ADD = "Ajouter"
    const val CANCEL = "Annuler"
    const val CONFIRM_YES = "Oui"
    const val CONFIRM_NO = "Non"
    const val MAINTENANCE_DELETED = "Entretien supprimé"
    const val INVALID_INPUT = "Saisie invalide"
    const val COUNT_BY = "Compter en :"
    const val HOURS = "Heures"
    const val KM = "Kilomètres"
    const val UNDO = "Annuler"
    const val ERROR_LOGIN = "Une erreur est survenue"
    const val SIGN_IN_WITH_GOOGLE = "Se connecter avec Google"
    const val EDIT = "Modifier"
    const val DELETE = "Supprimer"
    const val DELETE_BIKE_TITLE = "Supprimer cette moto ?"
    const val DELETE_BIKE_MESSAGE = "Cette action supprimera également tous les entretiens associés. Cette action est irréversible."

    // Error messages
    const val ERROR_NETWORK = "Erreur de connexion. Veuillez vérifier votre connexion internet."
    const val ERROR_AUTH = "Erreur d'authentification. Veuillez vous reconnecter."
    const val ERROR_DATABASE = "Erreur lors de la sauvegarde. Veuillez réessayer."
    const val ERROR_VALIDATION = "Saisie invalide. Veuillez vérifier vos données."
    const val ERROR_UNKNOWN = "Une erreur inattendue est survenue."
    const val ERROR_RETRY = "Réessayer"
}
