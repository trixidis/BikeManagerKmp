package com.bikemanager.ui.auth

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Implémentation Android du handler Apple Sign In.
 * Utilise Firebase Auth Android SDK avec OAuthProvider.
 *
 * ⚠️ LIMITATION: Nécessite une Activity pour démarrer le flow OAuth.
 * Cette implémentation utilise un ActivityProvider global.
 */
actual class AppleSignInHandler {

    private val auth = FirebaseAuth.getInstance()

    /**
     * Démarre le processus Apple Sign In via Firebase Auth Android SDK.
     *
     * Firebase gère automatiquement :
     * - L'ouverture de Chrome Custom Tab
     * - Le flow OAuth Apple
     * - La génération/validation du nonce
     * - La création de l'utilisateur Firebase
     */
    actual suspend fun signIn(): AppleSignInResult {
        return try {
            // Récupérer l'Activity courante
            val activity = ActivityProvider.currentActivity

            if (activity == null) {
                Napier.e { "Apple Sign In Android: Activity non disponible" }
                return AppleSignInResult.Failure(
                    "Erreur système: impossible de démarrer Apple Sign In. " +
                    "Veuillez redémarrer l'application."
                )
            }

            suspendCancellableCoroutine { continuation ->
                // Créer le provider Apple avec Firebase
                val provider = OAuthProvider.newBuilder("apple.com").apply {
                    // Demander email et nom
                    scopes = listOf("email", "name")
                }.build()

                // Démarrer le flow OAuth
                auth.startActivityForSignInWithProvider(activity, provider)
                    .addOnSuccessListener { authResult ->
                        if (!continuation.isActive) {
                            Napier.d { "Apple Sign In Android: continuation not active" }
                            return@addOnSuccessListener
                        }

                        val user = authResult.user
                        if (user != null) {
                            Napier.d { "Apple Sign In Android success: email=${user.email}" }

                            continuation.resume(
                                AppleSignInResult.Success(
                                    idToken = "", // Firebase gère le token en interne
                                    nonce = "", // Firebase gère le nonce en interne
                                    email = user.email,
                                    fullName = user.displayName
                                )
                            )
                        } else {
                            Napier.e { "Apple Sign In Android: Success but no user" }
                            continuation.resume(
                                AppleSignInResult.Failure("Erreur: utilisateur non trouvé après connexion")
                            )
                        }
                    }
                    .addOnFailureListener { exception ->
                        if (!continuation.isActive) {
                            Napier.d { "Apple Sign In Android: continuation not active" }
                            return@addOnFailureListener
                        }

                        Napier.e(throwable = exception) { "Apple Sign In Android failure" }

                        // Détection annulation utilisateur
                        val isCancelled = exception.message?.contains("cancel", ignoreCase = true) == true ||
                                         exception.message?.contains("cancelled", ignoreCase = true) == true

                        if (isCancelled) {
                            Napier.d { "Apple Sign In Android cancelled by user" }
                            continuation.resume(AppleSignInResult.Cancelled)
                        } else {
                            continuation.resume(
                                AppleSignInResult.Failure(
                                    exception.message ?: "Erreur de connexion Apple"
                                )
                            )
                        }
                    }

                // Cleanup si la coroutine est annulée
                continuation.invokeOnCancellation {
                    Napier.d { "Apple Sign In Android coroutine cancelled" }
                }
            }
        } catch (e: CancellationException) {
            // CRITIQUE : ne jamais catch CancellationException (fuites mémoire)
            throw e
        } catch (e: Exception) {
            Napier.e(throwable = e) { "Apple Sign In Android unexpected error" }
            AppleSignInResult.Failure(
                error = e.message ?: "Erreur de connexion Apple"
            )
        }
    }
}

/**
 * Provider global pour accéder à l'Activity courante.
 * Doit être configuré dans MainActivity.
 */
object ActivityProvider {
    var currentActivity: Activity? = null
}
