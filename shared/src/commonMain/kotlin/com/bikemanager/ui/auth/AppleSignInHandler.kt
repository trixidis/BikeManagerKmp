package com.bikemanager.ui.auth

/**
 * Gestionnaire platform-specific pour Apple Sign In.
 *
 * Implémentations :
 * - iOS : Utilise AuthenticationServices framework (natif Apple)
 * - Android : Utilise Firebase OAuthProvider avec Chrome Custom Tabs
 *
 * Le handler gère :
 * - La génération et validation du nonce sécurisé
 * - L'interaction avec les APIs natives de chaque plateforme
 * - La conversion des résultats en [AppleSignInResult] unifié
 */
expect class AppleSignInHandler() {
    /**
     * Démarre le processus de connexion Apple Sign In.
     *
     * Cette méthode suspend jusqu'à ce que l'utilisateur complète ou annule
     * le processus de connexion.
     *
     * @return [AppleSignInResult.Success] avec idToken et nonce si connexion réussie
     *         [AppleSignInResult.Failure] avec message d'erreur si échec
     *         [AppleSignInResult.Cancelled] si l'utilisateur annule
     *
     * @throws CancellationException si la coroutine est annulée (jamais catchée)
     */
    suspend fun signIn(): AppleSignInResult
}
