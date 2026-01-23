package com.bikemanager.fake

import com.bikemanager.domain.model.User
import com.bikemanager.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Fake implementation of AuthRepository for testing.
 */
class FakeAuthRepository : AuthRepository {
    private val currentUserFlow = MutableStateFlow<User?>(null)
    private var shouldThrowOnSignIn = false
    private var signInException: Exception? = null

    override fun getCurrentUser(): User? = currentUserFlow.value

    override fun observeAuthState(): Flow<User?> = currentUserFlow

    override suspend fun signInWithGoogle(idToken: String): User {
        if (shouldThrowOnSignIn) {
            throw signInException ?: IllegalStateException("Sign in failed")
        }
        val user = User(
            uid = "test-uid-$idToken",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null
        )
        currentUserFlow.value = user
        return user
    }

    override suspend fun signOut() {
        currentUserFlow.value = null
    }

    override fun isSignedIn(): Boolean = currentUserFlow.value != null

    /**
     * Helper to set the current user for testing.
     */
    fun setCurrentUser(user: User?) {
        currentUserFlow.value = user
    }

    /**
     * Helper to make sign-in throw an exception.
     */
    fun setSignInThrows(shouldThrow: Boolean, exception: Exception? = null) {
        shouldThrowOnSignIn = shouldThrow
        signInException = exception
    }
}
