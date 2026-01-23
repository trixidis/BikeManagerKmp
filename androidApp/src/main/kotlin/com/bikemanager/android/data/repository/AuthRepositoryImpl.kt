package com.bikemanager.android.data.repository

import com.bikemanager.domain.model.User
import com.bikemanager.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.toUser()
    }

    override fun observeAuthState(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogle(idToken: String): User {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        return result.user?.toUser()
            ?: throw IllegalStateException("Sign in succeeded but user is null")
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override fun isSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    private fun FirebaseUser.toUser(): User {
        return User(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl?.toString()
        )
    }
}
