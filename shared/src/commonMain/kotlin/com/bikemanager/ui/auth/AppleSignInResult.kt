package com.bikemanager.ui.auth

/**
 * Résultat d'une tentative de connexion Apple Sign In.
 * Unified result model pour les implémentations iOS et Android.
 */
sealed class AppleSignInResult {
    /**
     * Connexion réussie avec les informations nécessaires pour Firebase Auth.
     *
     * @param idToken Token d'identité JWT signé par Apple
     * @param nonce Nonce original (non hashé) utilisé pour la requête
     * @param email Email de l'utilisateur (peut être null si non partagé)
     * @param fullName Nom complet de l'utilisateur (peut être null si non partagé)
     */
    data class Success(
        val idToken: String,
        val nonce: String,
        val email: String?,
        val fullName: String?
    ) : AppleSignInResult()

    /**
     * Échec de la connexion avec message d'erreur.
     *
     * @param error Message d'erreur descriptif en français
     */
    data class Failure(val error: String) : AppleSignInResult()

    /**
     * L'utilisateur a annulé le processus de connexion.
     * Aucune action nécessaire, ne pas afficher d'erreur.
     */
    data object Cancelled : AppleSignInResult()
}
