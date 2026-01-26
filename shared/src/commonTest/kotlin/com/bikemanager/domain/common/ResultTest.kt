package com.bikemanager.domain.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResultTest {

    // ========== Result.Success Tests ==========

    @Test
    fun `Success creates result with value`() {
        val result = Result.Success("test value")

        assertEquals("test value", result.value)
        assertTrue(result.isSuccess)
        assertFalse(result.isFailure)
    }

    @Test
    fun `Success with null value is valid`() {
        val result = Result.Success<String?>(null)

        assertNull(result.value)
        assertTrue(result.isSuccess)
    }

    // ========== Result.Failure Tests ==========

    @Test
    fun `Failure creates result with error`() {
        val error = AppError.UnknownError("test error")
        val result = Result.Failure(error)

        assertEquals(error, result.error)
        assertFalse(result.isSuccess)
        assertTrue(result.isFailure)
    }

    @Test
    fun `Failure with AppError is valid`() {
        val error = AppError.ValidationError("Invalid input")
        val result = Result.Failure(error)

        assertEquals(error, result.error)
        assertIs<AppError.ValidationError>(result.error)
    }

    // ========== getOrNull Tests ==========

    @Test
    fun `getOrNull returns value for Success`() {
        val result: Result<String> = Result.Success("test")

        assertEquals("test", result.getOrNull())
    }

    @Test
    fun `getOrNull returns null for Failure`() {
        val result: Result<String> = Result.Failure(AppError.UnknownError("error"))

        assertNull(result.getOrNull())
    }

    // ========== getOrElse Tests ==========

    @Test
    fun `getOrElse returns value for Success`() {
        val result: Result<String> = Result.Success("test")

        val value = result.getOrElse { "default" }

        assertEquals("test", value)
    }

    @Test
    fun `getOrElse returns default for Failure`() {
        val error = AppError.UnknownError("error")
        val result: Result<String> = Result.Failure(error)

        val value = result.getOrElse { "default: ${it.message}" }

        assertEquals("default: error", value)
    }

    // ========== getOrThrow Tests ==========

    @Test
    fun `getOrThrow returns value for Success`() {
        val result: Result<String> = Result.Success("test")

        assertEquals("test", result.getOrThrow())
    }

    @Test
    fun `getOrThrow throws error for Failure`() {
        val error = AppError.UnknownError("test error")
        val result: Result<String> = Result.Failure(error)

        val thrown = assertFailsWith<AppError.UnknownError> {
            result.getOrThrow()
        }
        assertEquals("test error", thrown.message)
    }

    // ========== map Tests ==========

    @Test
    fun `map transforms Success value`() {
        val result: Result<Int> = Result.Success(5)

        val mapped = result.map { it * 2 }

        assertTrue(mapped.isSuccess)
        assertEquals(10, mapped.getOrNull())
    }

    @Test
    fun `map returns unchanged Failure`() {
        val error = AppError.UnknownError("test error")
        val result: Result<Int> = Result.Failure(error)

        val mapped = result.map { it * 2 }

        assertTrue(mapped.isFailure)
        assertIs<Result.Failure>(mapped)
        assertEquals(error, (mapped as Result.Failure).error)
    }

    @Test
    fun `map can change result type`() {
        val result: Result<Int> = Result.Success(42)

        val mapped: Result<String> = result.map { "Number: $it" }

        assertEquals("Number: 42", mapped.getOrNull())
    }

    // ========== flatMap Tests ==========

    @Test
    fun `flatMap transforms Success to Success`() {
        val result: Result<Int> = Result.Success(5)

        val flatMapped = result.flatMap { Result.Success(it * 2) }

        assertTrue(flatMapped.isSuccess)
        assertEquals(10, flatMapped.getOrNull())
    }

    @Test
    fun `flatMap transforms Success to Failure`() {
        val result: Result<Int> = Result.Success(5)

        val flatMapped = result.flatMap {
            Result.Failure(AppError.UnknownError("error"))
        }

        assertTrue(flatMapped.isFailure)
    }

    @Test
    fun `flatMap returns unchanged Failure`() {
        val error = AppError.UnknownError("test error")
        val result: Result<Int> = Result.Failure(error)

        val flatMapped = result.flatMap { Result.Success(it * 2) }

        assertTrue(flatMapped.isFailure)
        assertEquals(error, (flatMapped as Result.Failure).error)
    }

    // ========== fold Tests ==========

    @Test
    fun `fold calls onSuccess for Success`() {
        val result: Result<Int> = Result.Success(5)

        val value = result.fold(
            onSuccess = { it * 2 },
            onFailure = { -1 }
        )

        assertEquals(10, value)
    }

    @Test
    fun `fold calls onFailure for Failure`() {
        val error = AppError.UnknownError("error message")
        val result: Result<Int> = Result.Failure(error)

        val value = result.fold(
            onSuccess = { it * 2 },
            onFailure = { it.message ?: "unknown" }
        )

        assertEquals("error message", value)
    }

    // ========== onSuccess Tests ==========

    @Test
    fun `onSuccess executes action for Success`() {
        val result: Result<String> = Result.Success("test")
        var executed = false

        val returned = result.onSuccess { executed = true }

        assertTrue(executed)
        assertEquals(result, returned)
    }

    @Test
    fun `onSuccess does not execute action for Failure`() {
        val result: Result<String> = Result.Failure(AppError.UnknownError("error"))
        var executed = false

        val returned = result.onSuccess { executed = true }

        assertFalse(executed)
        assertEquals(result, returned)
    }

    // ========== onFailure Tests ==========

    @Test
    fun `onFailure executes action for Failure`() {
        val error = AppError.UnknownError("test")
        val result: Result<String> = Result.Failure(error)
        var capturedError: AppError? = null

        val returned = result.onFailure { capturedError = it }

        assertEquals(error, capturedError)
        assertEquals(result, returned)
    }

    @Test
    fun `onFailure does not execute action for Success`() {
        val result: Result<String> = Result.Success("test")
        var executed = false

        val returned = result.onFailure { executed = true }

        assertFalse(executed)
        assertEquals(result, returned)
    }

    @Test
    fun `onSuccess and onFailure can be chained`() {
        var successCalled = false
        var failureCalled = false

        Result.Success("test")
            .onSuccess { successCalled = true }
            .onFailure { failureCalled = true }

        assertTrue(successCalled)
        assertFalse(failureCalled)
    }

    // ========== runCatching Tests ==========

    @Test
    fun `runCatching returns Success for successful block`() {
        val result = com.bikemanager.domain.common.runCatching { "test" }

        assertTrue(result.isSuccess)
        assertEquals("test", result.getOrNull())
    }

    @Test
    fun `runCatching returns Failure for throwing block`() {
        val result = com.bikemanager.domain.common.runCatching {
            throw IllegalStateException("test error")
        }

        assertTrue(result.isFailure)
        // runCatching now converts all exceptions to AppError via ErrorHandler
        assertIs<AppError>((result as Result.Failure).error)
    }

    @Test
    fun `runCatching captures exception message`() {
        val result = com.bikemanager.domain.common.runCatching<String> {
            throw IllegalArgumentException("invalid argument")
        }

        val error = (result as Result.Failure).error
        assertEquals("invalid argument", error.message)
    }

    // ========== toResult Tests ==========

    @Test
    fun `toResult returns Success for non-null value`() {
        val value: String? = "test"

        val result = value.toResult { AppError.ValidationError("null value") }

        assertTrue(result.isSuccess)
        assertEquals("test", result.getOrNull())
    }

    @Test
    fun `toResult returns Failure for null value`() {
        val value: String? = null

        val result = value.toResult { AppError.ValidationError("null value") }

        assertTrue(result.isFailure)
        assertIs<AppError.ValidationError>((result as Result.Failure).error)
    }

    @Test
    fun `toResult uses provided error factory`() {
        val value: String? = null

        val result = value.toResult {
            AppError.ValidationError("Value is required")
        }

        val error = (result as Result.Failure).error
        assertIs<AppError.ValidationError>(error)
        assertEquals("Value is required", error.message)
    }

    // ========== CancellationException Tests ==========

    @Test
    fun `runCatching re-throws CancellationException`() {
        var cancellationExceptionThrown = false
        try {
            val result = com.bikemanager.domain.common.runCatching<Unit> {
                throw CancellationException("Coroutine cancelled")
            }
            // If we reach here, CancellationException was NOT re-thrown (test should fail)
            assertFalse(true, "Should not reach here - CancellationException should be re-thrown")
        } catch (e: CancellationException) {
            cancellationExceptionThrown = true
            assertEquals("Coroutine cancelled", e.message)
        }
        assertTrue(cancellationExceptionThrown, "CancellationException should be re-thrown, not caught")
    }

    @Test
    fun `runCatching does not catch CancellationException`() {
        // CancellationException should propagate, not be converted to Result.Failure
        var exceptionThrown = false
        try {
            val result = com.bikemanager.domain.common.runCatching<Unit> {
                throw CancellationException("Cancelled")
            }
            // If we reach here, the exception was caught (which is wrong)
            assertFalse(true, "Should not reach here - result type is ${result::class.simpleName}")
        } catch (e: CancellationException) {
            exceptionThrown = true
        }
        assertTrue(exceptionThrown, "CancellationException should have been re-thrown")
    }

    @Test
    fun `runCatching handles other exceptions while preserving CancellationException behavior`() {
        // Regular exceptions are caught and wrapped in Result.Failure
        val result1 = com.bikemanager.domain.common.runCatching<Unit> {
            throw IllegalStateException("Regular error")
        }
        assertTrue(result1.isFailure)

        // CancellationException is re-thrown
        var cancellationExceptionThrown = false
        try {
            val result2 = com.bikemanager.domain.common.runCatching<Unit> {
                throw CancellationException("Cancelled")
            }
            assertFalse(true, "Should not reach here")
        } catch (e: CancellationException) {
            cancellationExceptionThrown = true
        }
        assertTrue(cancellationExceptionThrown, "CancellationException should be re-thrown")
    }
}

class ErrorHandlerTest {

    // ========== handle() with Network Errors ==========

    @Test
    fun `handle converts network exception to NetworkError`() {
        val exception = Exception("Network connection failed")

        val error = ErrorHandler.handle(exception)

        assertIs<AppError.NetworkError>(error)
        assertEquals("Network connection failed", error.errorMessage)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `handle detects network error by timeout message`() {
        val exception = Exception("Request timeout")

        val error = ErrorHandler.handle(exception)

        assertIs<AppError.NetworkError>(error)
    }

    @Test
    fun `handle detects network error by connection message`() {
        val exception = Exception("Connection refused")

        val error = ErrorHandler.handle(exception)

        assertIs<AppError.NetworkError>(error)
    }

    // ========== handle() with Auth Errors ==========

    @Test
    fun `handle converts auth exception to AuthError`() {
        val exception = Exception("Authentication failed")

        val error = ErrorHandler.handle(exception)

        assertIs<AppError.AuthError>(error)
        assertEquals("Authentication failed", error.errorMessage)
    }

    @Test
    fun `handle detects auth error by credential message`() {
        val exception = Exception("Invalid credentials")

        val error = ErrorHandler.handle(exception)

        assertIs<AppError.AuthError>(error)
    }

    @Test
    fun `handle detects auth error by token message`() {
        val exception = Exception("Invalid token")

        val error = ErrorHandler.handle(exception)

        assertIs<AppError.AuthError>(error)
    }

    @Test
    fun `handle detects auth error by unauthorized message`() {
        val exception = Exception("Unauthorized access")

        val error = ErrorHandler.handle(exception)

        assertIs<AppError.AuthError>(error)
    }

    // ========== handle() with Database Errors ==========

    @Test
    fun `handle converts database exception to DatabaseError`() {
        val exception = Exception("Database operation failed")

        val error = ErrorHandler.handle(exception)

        assertIs<AppError.DatabaseError>(error)
        assertEquals("Database operation failed", error.errorMessage)
    }

    @Test
    fun `handle detects database error by firestore message`() {
        val exception = Exception("Firestore operation failed")

        val error = ErrorHandler.handle(exception)

        assertIs<AppError.DatabaseError>(error)
    }

    @Test
    fun `handle detects database error by permission message`() {
        val exception = Exception("Permission denied")

        val error = ErrorHandler.handle(exception)

        assertIs<AppError.DatabaseError>(error)
    }

    // ========== handle() with Validation Errors ==========

    @Test
    fun `handle converts IllegalArgumentException to ValidationError`() {
        val exception = IllegalArgumentException("Invalid input")

        val error = ErrorHandler.handle(exception)

        assertIs<AppError.ValidationError>(error)
        assertEquals("Invalid input", error.errorMessage)
    }

    @Test
    fun `handle detects validation error by validation message`() {
        val exception = Exception("Validation failed")

        val error = ErrorHandler.handle(exception)

        assertIs<AppError.ValidationError>(error)
    }

    @Test
    fun `handle detects validation error by required message`() {
        val exception = Exception("Field is required")

        val error = ErrorHandler.handle(exception)

        assertIs<AppError.ValidationError>(error)
    }

    @Test
    fun `handle detects validation error by blank message`() {
        val exception = Exception("Value cannot be blank")

        val error = ErrorHandler.handle(exception)

        assertIs<AppError.ValidationError>(error)
    }

    // ========== handle() with Unknown Errors ==========

    @Test
    fun `handle converts unknown exception to UnknownError`() {
        val exception = Exception("Something went wrong")

        val error = ErrorHandler.handle(exception)

        assertIs<AppError.UnknownError>(error)
        assertEquals("Something went wrong", error.errorMessage)
    }

    @Test
    fun `handle provides default message for exception without message`() {
        val exception = Exception()

        val error = ErrorHandler.handle(exception)

        assertIs<AppError.UnknownError>(error)
        assertEquals("An unexpected error occurred", error.errorMessage)
    }

    // ========== handle() with context ==========

    @Test
    fun `handle accepts context parameter`() {
        val exception = Exception("Test error")

        val error = ErrorHandler.handle(exception, "signing in")

        assertNotNull(error)
        assertIs<AppError.UnknownError>(error)
    }

    // ========== catching() Tests ==========

    @Test
    fun `catching returns Success for successful operation`() = runTest {
        val result = ErrorHandler.catching {
            "success"
        }

        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
    }

    @Test
    fun `catching returns Failure with AppError for exception`() = runTest {
        val result = ErrorHandler.catching {
            throw Exception("network error occurred")
        }

        assertTrue(result.isFailure)
        val error = (result as Result.Failure).error
        assertIs<AppError>(error)
        assertIs<AppError.NetworkError>(error)
    }

    @Test
    fun `catching converts exception to appropriate AppError type`() = runTest {
        val result = ErrorHandler.catching {
            throw IllegalArgumentException("Invalid input value")
        }

        assertTrue(result.isFailure)
        val error = (result as Result.Failure).error
        assertIs<AppError.ValidationError>(error)
        assertEquals("Invalid input value", error.message)
    }

    @Test
    fun `catching accepts context parameter`() = runTest {
        val result = ErrorHandler.catching("fetching bikes") {
            throw Exception("Database error")
        }

        assertTrue(result.isFailure)
        val error = (result as Result.Failure).error
        assertIs<AppError.DatabaseError>(error)
    }

    @Test
    fun `catching works with suspend functions`() = runTest {
        suspend fun suspendingOperation(): String {
            return "suspended result"
        }

        val result = ErrorHandler.catching {
            suspendingOperation()
        }

        assertTrue(result.isSuccess)
        assertEquals("suspended result", result.getOrNull())
    }

    // ========== CancellationException Tests ==========

    @Test
    fun `catching re-throws CancellationException`() = runTest {
        var cancellationExceptionThrown = false
        try {
            ErrorHandler.catching("testing cancellation") {
                throw CancellationException("Coroutine cancelled")
            }
        } catch (e: CancellationException) {
            cancellationExceptionThrown = true
            assertEquals("Coroutine cancelled", e.message)
        }
        assertTrue(cancellationExceptionThrown, "CancellationException should be re-thrown, not caught")
    }

    @Test
    fun `catching does not catch CancellationException`() = runTest {
        // CancellationException should propagate, not be converted to Result.Failure
        var exceptionThrown = false
        try {
            ErrorHandler.catching {
                throw CancellationException("Cancelled")
            }
        } catch (e: CancellationException) {
            exceptionThrown = true
        }
        assertTrue(exceptionThrown, "CancellationException should have been re-thrown")
    }

    @Test
    fun `catching handles other exceptions while preserving CancellationException behavior`() = runTest {
        // Regular exceptions are caught and wrapped in Result.Failure
        val result1 = ErrorHandler.catching {
            throw IllegalStateException("Regular error")
        }
        assertTrue(result1.isFailure)

        // CancellationException is re-thrown
        var cancellationExceptionThrown = false
        try {
            ErrorHandler.catching {
                throw CancellationException("Cancelled")
            }
        } catch (e: CancellationException) {
            cancellationExceptionThrown = true
        }
        assertTrue(cancellationExceptionThrown, "CancellationException should be re-thrown")
    }
}
