package com.bikemanager.domain.common

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException

/**
 * Utility for converting exceptions to domain-specific AppError types.
 * Provides centralized exception mapping with proper logging via Napier.
 */
object ErrorHandler {

    /**
     * Converts a Throwable to an AppError, mapping specific exception types
     * to appropriate domain errors. Logs the error with Napier.
     *
     * @param throwable The exception to convert
     * @param context Optional context string for logging (e.g., "signing in", "fetching bikes")
     * @return The corresponding AppError
     */
    fun handle(throwable: Throwable, context: String? = null): AppError {
        // Log the error with context
        if (context != null) {
            Napier.e(throwable) { "Error $context" }
        } else {
            Napier.e(throwable) { "Error occurred" }
        }

        return when {
            // Network-related errors
            isNetworkError(throwable) -> AppError.NetworkError(
                errorMessage = throwable.message ?: "Network error",
                originalCause = throwable
            )

            // Authentication errors
            isAuthError(throwable) -> AppError.AuthError(
                errorMessage = throwable.message ?: "Authentication error",
                originalCause = throwable
            )

            // Database/Firebase errors
            isDatabaseError(throwable) -> AppError.DatabaseError(
                errorMessage = throwable.message ?: "Database error",
                originalCause = throwable
            )

            // Validation errors
            isValidationError(throwable) -> AppError.ValidationError(
                errorMessage = throwable.message ?: "Validation error"
            )

            // Unknown/unexpected errors
            else -> AppError.UnknownError(
                errorMessage = throwable.message ?: "An unexpected error occurred",
                originalCause = throwable
            )
        }
    }

    /**
     * Wraps a suspending operation in Result, converting any exceptions to AppError.
     *
     * IMPORTANT: CancellationException is re-thrown to allow proper coroutine cancellation.
     * Catching and suppressing CancellationException would cause memory leaks and prevent
     * proper cleanup of coroutine resources.
     *
     * @param context Optional context string for logging
     * @param block The operation to execute
     * @return Result.Success with the operation result, or Result.Failure with AppError
     */
    suspend fun <T> catching(context: String? = null, block: suspend () -> T): Result<T> {
        return try {
            Result.Success(block())
        } catch (e: CancellationException) {
            // CRITICAL: Always re-throw CancellationException to allow proper coroutine cancellation
            // Suppressing this exception would cause memory leaks and prevent cleanup
            throw e
        } catch (e: Throwable) {
            Result.Failure(handle(e, context))
        }
    }

    /**
     * Checks if a throwable represents a network-related error.
     */
    private fun isNetworkError(throwable: Throwable): Boolean {
        val message = throwable.message?.lowercase() ?: ""
        val className = throwable::class.simpleName?.lowercase() ?: ""

        return message.contains("network") ||
                message.contains("connection") ||
                message.contains("timeout") ||
                message.contains("unreachable") ||
                className.contains("network") ||
                className.contains("ioexception") ||
                className.contains("socketexception")
    }

    /**
     * Checks if a throwable represents an authentication error.
     */
    private fun isAuthError(throwable: Throwable): Boolean {
        val message = throwable.message?.lowercase() ?: ""
        val className = throwable::class.simpleName?.lowercase() ?: ""

        return message.contains("auth") ||
                message.contains("credential") ||
                message.contains("token") ||
                message.contains("unauthorized") ||
                message.contains("sign-in") ||
                message.contains("sign in") ||
                className.contains("firebaseauthexception") ||
                className.contains("authexception")
    }

    /**
     * Checks if a throwable represents a database or data persistence error.
     */
    private fun isDatabaseError(throwable: Throwable): Boolean {
        val message = throwable.message?.lowercase() ?: ""
        val className = throwable::class.simpleName?.lowercase() ?: ""

        return message.contains("database") ||
                message.contains("firestore") ||
                message.contains("firebase") ||
                message.contains("permission") ||
                message.contains("not found") ||
                className.contains("firebaseexception") ||
                className.contains("firestoreexception")
    }

    /**
     * Checks if a throwable represents a validation error.
     */
    private fun isValidationError(throwable: Throwable): Boolean {
        val message = throwable.message?.lowercase() ?: ""
        val className = throwable::class.simpleName?.lowercase() ?: ""

        return message.contains("validation") ||
                message.contains("invalid") ||
                message.contains("required") ||
                message.contains("blank") ||
                message.contains("empty") ||
                className.contains("validation") ||
                className.contains("illegalargument")
    }
}
