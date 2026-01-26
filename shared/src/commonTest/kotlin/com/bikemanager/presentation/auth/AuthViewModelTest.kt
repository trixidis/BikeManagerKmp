package com.bikemanager.presentation.auth

import app.cash.turbine.test
import com.bikemanager.domain.model.User
import com.bikemanager.domain.usecase.auth.GetCurrentUserUseCase
import com.bikemanager.domain.usecase.auth.SignInUseCase
import com.bikemanager.domain.usecase.auth.SignOutUseCase
import com.bikemanager.fake.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private lateinit var repository: FakeAuthRepository
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var signInUseCase: SignInUseCase
    private lateinit var signOutUseCase: SignOutUseCase
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeAuthRepository()
        getCurrentUserUseCase = GetCurrentUserUseCase(repository)
        signInUseCase = SignInUseCase(repository)
        signOutUseCase = SignOutUseCase(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Checking then NotAuthenticated when no user`() = runTest {
        val viewModel = AuthViewModelMvi(getCurrentUserUseCase, signInUseCase, signOutUseCase)

        viewModel.uiState.test {
            assertEquals(AuthUiState.Checking, awaitItem())

            advanceUntilIdle()
            assertEquals(AuthUiState.NotAuthenticated, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state is Checking then Authenticated when user exists`() = runTest {
        val testUser = User(
            uid = "test-uid",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null
        )
        repository.setCurrentUser(testUser)

        val viewModel = AuthViewModelMvi(getCurrentUserUseCase, signInUseCase, signOutUseCase)

        viewModel.uiState.test {
            assertEquals(AuthUiState.Checking, awaitItem())

            advanceUntilIdle()
            val state = awaitItem()
            assertTrue(state is AuthUiState.Authenticated)
            assertEquals(testUser, (state as AuthUiState.Authenticated).user)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signInWithGoogle transitions to Loading then Authenticated on success`() = runTest {
        val viewModel = AuthViewModelMvi(getCurrentUserUseCase, signInUseCase, signOutUseCase)
        advanceUntilIdle()

        viewModel.uiState.test {
            // Skip initial states
            awaitItem()

            viewModel.signInWithGoogle("test-token")

            assertEquals(AuthUiState.Loading, awaitItem())

            advanceUntilIdle()
            val state = awaitItem()
            assertTrue(state is AuthUiState.Authenticated)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signInWithGoogle with auth error shows French error message`() = runTest {
        repository.setSignInFails(true, com.bikemanager.domain.common.AppError.AuthError("Auth failed"))

        val viewModel = AuthViewModelMvi(getCurrentUserUseCase, signInUseCase, signOutUseCase)
        advanceUntilIdle()

        viewModel.uiState.test {
            // Skip initial state
            awaitItem()

            viewModel.signInWithGoogle("test-token")

            assertEquals(AuthUiState.Loading, awaitItem())

            advanceUntilIdle()
            val state = awaitItem()
            assertTrue(state is AuthUiState.Error)
            assertEquals("Erreur d'authentification. Veuillez vous reconnecter.", (state as AuthUiState.Error).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signInWithGoogle with network error shows French error message`() = runTest {
        repository.setSignInFails(true, com.bikemanager.domain.common.AppError.NetworkError("Network failed"))

        val viewModel = AuthViewModelMvi(getCurrentUserUseCase, signInUseCase, signOutUseCase)
        advanceUntilIdle()

        viewModel.uiState.test {
            // Skip initial state
            awaitItem()

            viewModel.signInWithGoogle("test-token")

            assertEquals(AuthUiState.Loading, awaitItem())

            advanceUntilIdle()
            val state = awaitItem()
            assertTrue(state is AuthUiState.Error)
            assertEquals("Erreur de connexion. Veuillez vérifier votre connexion internet et réessayer.", (state as AuthUiState.Error).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signInWithGoogle with blank token shows French validation error`() = runTest {
        val viewModel = AuthViewModelMvi(getCurrentUserUseCase, signInUseCase, signOutUseCase)
        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem()

            viewModel.signInWithGoogle("")
            val state = awaitItem()
            assertTrue(state is AuthUiState.Error)
            assertEquals("Identifiants invalides. Veuillez réessayer.", (state as AuthUiState.Error).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signOut transitions to NotAuthenticated`() = runTest {
        repository.setCurrentUser(
            User(uid = "uid", email = null, displayName = null, photoUrl = null)
        )

        val viewModel = AuthViewModelMvi(getCurrentUserUseCase, signInUseCase, signOutUseCase)
        advanceUntilIdle()

        viewModel.uiState.test {
            // Skip to authenticated state
            val authenticated = awaitItem()
            assertTrue(authenticated is AuthUiState.Authenticated)

            viewModel.signOut()
            advanceUntilIdle()

            assertEquals(AuthUiState.NotAuthenticated, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearError resets from Error to NotAuthenticated`() = runTest {
        val viewModel = AuthViewModelMvi(getCurrentUserUseCase, signInUseCase, signOutUseCase)
        advanceUntilIdle()

        // Trigger an error
        viewModel.signInWithGoogle("")

        viewModel.uiState.test {
            val errorState = awaitItem()
            assertTrue(errorState is AuthUiState.Error)

            viewModel.clearError()
            assertEquals(AuthUiState.NotAuthenticated, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `checkAuthState refreshes auth state`() = runTest {
        val viewModel = AuthViewModelMvi(getCurrentUserUseCase, signInUseCase, signOutUseCase)
        advanceUntilIdle()

        // Set up a user
        val testUser = User(
            uid = "new-uid",
            email = "new@example.com",
            displayName = null,
            photoUrl = null
        )
        repository.setCurrentUser(testUser)

        viewModel.uiState.test {
            awaitItem()

            viewModel.checkAuthState()

            assertEquals(AuthUiState.Checking, awaitItem())

            advanceUntilIdle()
            val state = awaitItem()
            assertTrue(state is AuthUiState.Authenticated)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
