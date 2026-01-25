package com.bikemanager.fake

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.User
import com.bikemanager.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Fake implementation of AuthRepository for testing.
 */
class FakeAuthRepository : AuthRepository {
    private val currentUserFlow = MutableStateFlow<User?>(null)
    private var shouldFailOnSignIn = false
    private var signInError: Throwable? = null
    private var shouldFailOnSignOut = false
    private var signOutError: Throwable? = null

    override fun getCurrentUser(): User? = currentUserFlow.value

    override fun observeAuthState(): Flow<User?> = currentUserFlow

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        if (shouldFailOnSignIn) {
            return Result.Failure(signInError ?: AppError.AuthError("Sign in failed"))
        }
        val user = User(
            uid = "test-uid-$idToken",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null
        )
        currentUserFlow.value = user
        return Result.Success(user)
    }

    override suspend fun signOut(): Result<Unit> {
        if (shouldFailOnSignOut) {
            return Result.Failure(signOutError ?: AppError.AuthError("Sign out failed"))
        }
        currentUserFlow.value = null
        return Result.Success(Unit)
    }

    override fun isSignedIn(): Boolean = currentUserFlow.value != null

    /**
     * Helper to set the current user for testing.
     */
    fun setCurrentUser(user: User?) {
        currentUserFlow.value = user
    }

    /**
     * Helper to make sign-in return a failure.
     */
    fun setSignInFails(shouldFail: Boolean, error: Throwable? = null) {
        shouldFailOnSignIn = shouldFail
        signInError = error
    }

    /**
     * Helper to make sign-in throw an exception (backward compatibility alias).
     * @deprecated Use setSignInFails instead
     */
    @Deprecated("Use setSignInFails instead", ReplaceWith("setSignInFails(shouldThrow, exception)"))
    fun setSignInThrows(shouldThrow: Boolean, exception: Exception? = null) {
        setSignInFails(shouldThrow, exception)
    }

    /**
     * Helper to make sign-out return a failure.
     */
    fun setSignOutFails(shouldFail: Boolean, error: Throwable? = null) {
        shouldFailOnSignOut = shouldFail
        signOutError = error
    }
}
