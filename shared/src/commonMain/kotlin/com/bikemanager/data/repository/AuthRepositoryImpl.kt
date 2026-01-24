package com.bikemanager.data.repository

import com.bikemanager.domain.model.User
import com.bikemanager.domain.repository.AuthRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
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

    override suspend fun signInWithGoogle(idToken: String): User {
        Napier.d { "Signing in with Google credential" }
        val credential = GoogleAuthProvider.credential(idToken, null)
        val result = auth.signInWithCredential(credential)
        val user = result.user ?: throw Exception("Sign-in failed: no user returned")
        Napier.d { "Successfully signed in: ${user.uid}" }
        return user.toUser()
    }

    override suspend fun signOut() {
        Napier.d { "Signing out" }
        auth.signOut()
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
