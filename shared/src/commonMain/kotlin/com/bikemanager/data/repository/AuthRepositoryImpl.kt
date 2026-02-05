package com.bikemanager.data.repository

import com.bikemanager.domain.common.ErrorHandler
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.User
import com.bikemanager.domain.repository.AuthRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
import dev.gitlive.firebase.auth.auth
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Common implementation of AuthRepository using GitLive Firebase SDK.
 * Works on both Android and iOS.
 */
class AuthRepositoryImpl : AuthRepository {

    private val auth = Firebase.auth

    override fun getCurrentUser(): User? {
        return auth.currentUser?.toUser()
    }

    override fun observeAuthState(): Flow<User?> {
        return auth.authStateChanged.map { firebaseUser ->
            firebaseUser?.toUser()
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return ErrorHandler.catching("signing in with Google") {
            Napier.d { "Signing in with Google credential" }
            val credential = GoogleAuthProvider.credential(idToken, null)
            val result = auth.signInWithCredential(credential)
            val user = result.user ?: throw Exception("Sign-in failed: no user returned")
            Napier.d { "Successfully signed in: ${user.uid}" }
            user.toUser()
        }
    }

    override suspend fun signInWithApple(idToken: String, nonce: String?): Result<User> {
        return ErrorHandler.catching("signing in with Apple") {
            Napier.d { "Signing in with Apple credential" }

            val credential = if (nonce != null) {
                OAuthProvider.credential(
                    providerId = "apple.com",
                    idToken = idToken,
                    rawNonce = nonce
                )
            } else {
                OAuthProvider.credential(
                    providerId = "apple.com",
                    idToken = idToken,
                    rawNonce = null
                )
            }

            val result = auth.signInWithCredential(credential)
            val user = result.user ?: throw Exception("Sign-in failed: no user returned")
            Napier.d { "Successfully signed in with Apple: ${user.uid}" }
            user.toUser()
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return ErrorHandler.catching("signing out") {
            Napier.d { "Signing out" }
            auth.signOut()
        }
    }

    override fun isSignedIn(): Boolean {
        return auth.currentUser != null
    }

    private fun FirebaseUser.toUser(): User {
        return User(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoURL
        )
    }
}
