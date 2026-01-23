package com.bikemanager.domain.usecase.auth

import com.bikemanager.fake.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SignInUseCaseTest {
    private lateinit var repository: FakeAuthRepository
    private lateinit var useCase: SignInUseCase

    @BeforeTest
    fun setup() {
        repository = FakeAuthRepository()
        useCase = SignInUseCase(repository)
    }

    @Test
    fun `invoke signs in user and returns user`() = runTest {
        val user = useCase("test-id-token")

        assertNotNull(user)
        assertTrue(user.uid.contains("test-id-token"))
        assertEquals("test@example.com", user.email)
        assertEquals("Test User", user.displayName)
    }

    @Test
    fun `invoke throws exception when id token is empty`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase("")
        }
    }

    @Test
    fun `invoke throws exception when id token is blank`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase("   ")
        }
    }

    @Test
    fun `invoke throws exception when sign in fails`() = runTest {
        repository.setSignInThrows(true, IllegalStateException("Auth failed"))

        assertFailsWith<IllegalStateException> {
            useCase("valid-token")
        }
    }

    @Test
    fun `invoke updates repository state`() = runTest {
        useCase("test-token")

        assertTrue(repository.isSignedIn())
        assertNotNull(repository.getCurrentUser())
    }
}
