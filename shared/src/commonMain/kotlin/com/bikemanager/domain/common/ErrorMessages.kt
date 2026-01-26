package com.bikemanager.domain.common

/**
 * Maps AppError types to user-friendly French error messages.
 * Provides localized, actionable error messages for the UI layer.
 */
object ErrorMessages {

    /**
     * Converts an AppError to a user-friendly French error message.
     */
    fun getMessage(error: AppError): String {
        return when (error) {
            is AppError.NetworkError -> "Erreur de connexion. Veuillez vérifier votre connexion internet et réessayer."
            is AppError.AuthError -> "Erreur d'authentification. Veuillez vous reconnecter."
            is AppError.DatabaseError -> "Erreur lors de la sauvegarde. Veuillez réessayer."
            is AppError.ValidationError -> {
                if (error.field != null) {
                    "Saisie invalide pour le champ : ${error.field}"
                } else {
                    "Saisie invalide. Veuillez vérifier vos données."
                }
            }
            is AppError.UnknownError -> "Une erreur inattendue est survenue. Veuillez réessayer."
        }
    }

    // Specific error messages for common scenarios
    const val NETWORK_ERROR = "Erreur de connexion. Veuillez vérifier votre connexion internet et réessayer."
    const val AUTH_ERROR = "Erreur d'authentification. Veuillez vous reconnecter."
    const val DATABASE_ERROR = "Erreur lors de la sauvegarde. Veuillez réessayer."
    const val VALIDATION_ERROR = "Saisie invalide. Veuillez vérifier vos données."
    const val UNKNOWN_ERROR = "Une erreur inattendue est survenue. Veuillez réessayer."

    // Specific auth error messages
    const val AUTH_TOKEN_EXPIRED = "Votre session a expiré. Veuillez vous reconnecter."
    const val AUTH_INVALID_CREDENTIALS = "Identifiants invalides. Veuillez réessayer."
    const val AUTH_USER_NOT_FOUND = "Utilisateur introuvable."

    // Specific network error messages
    const val NETWORK_TIMEOUT = "La connexion a expiré. Veuillez réessayer."
    const val NETWORK_NO_CONNECTION = "Aucune connexion internet. Veuillez vérifier votre connexion."

    // Specific database error messages
    const val DATABASE_SAVE_FAILED = "Échec de la sauvegarde. Veuillez réessayer."
    const val DATABASE_DELETE_FAILED = "Échec de la suppression. Veuillez réessayer."
    const val DATABASE_UPDATE_FAILED = "Échec de la mise à jour. Veuillez réessayer."
}
