package com.bikemanager.ui.auth

import io.github.aakira.napier.Napier
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNotificationName
import kotlin.coroutines.resume

/**
 * Implémentation iOS du handler Apple Sign In.
 *
 * Cette implémentation utilise un helper Swift natif (AppleSignInHelper.swift)
 * qui gère directement Firebase Auth et ASAuthorizationController.
 *
 * Communication via NSNotification pour éviter les problèmes de cinterop.
 */
actual class AppleSignInHandler {

    actual suspend fun signIn(): AppleSignInResult {
        return suspendCancellableCoroutine { continuation ->
            // Observer pour le succès
            val successObserver = NSNotificationCenter.defaultCenter.addObserverForName(
                name = "AppleSignInSuccess" as NSNotificationName,
                `object` = null,
                queue = null
            ) { _ ->
                Napier.d { "Apple Sign In iOS: Success notification received" }
                if (continuation.isActive) {
                    continuation.resume(
                        AppleSignInResult.Success(
                            idToken = "",
                            nonce = "",
                            email = null,
                            fullName = null
                        )
                    )
                }
            }

            // Observer pour l'échec
            val failureObserver = NSNotificationCenter.defaultCenter.addObserverForName(
                name = "AppleSignInFailure" as NSNotificationName,
                `object` = null,
                queue = null
            ) { notification ->
                val error = notification?.userInfo?.get("error") as? String ?: "Erreur inconnue"
                Napier.e { "Apple Sign In iOS: Failure notification - $error" }
                if (continuation.isActive) {
                    continuation.resume(AppleSignInResult.Failure(error))
                }
            }

            // Observer pour l'annulation
            val cancelledObserver = NSNotificationCenter.defaultCenter.addObserverForName(
                name = "AppleSignInCancelled" as NSNotificationName,
                `object` = null,
                queue = null
            ) { _ ->
                Napier.d { "Apple Sign In iOS: Cancelled notification received" }
                if (continuation.isActive) {
                    continuation.resume(AppleSignInResult.Cancelled)
                }
            }

            // Poster la notification pour démarrer le sign in
            NSNotificationCenter.defaultCenter.postNotificationName(
                "StartAppleSignIn" as NSNotificationName,
                `object` = null
            )

            // Cleanup observers when cancelled
            continuation.invokeOnCancellation {
                NSNotificationCenter.defaultCenter.removeObserver(successObserver)
                NSNotificationCenter.defaultCenter.removeObserver(failureObserver)
                NSNotificationCenter.defaultCenter.removeObserver(cancelledObserver)
            }
        }
    }
}
