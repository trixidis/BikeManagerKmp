package com.bikemanager.domain.common

/**
 * Domain-level error types for the application.
 * Sealed class hierarchy extending Throwable for use with Result.Failure.
 */
sealed class AppError(
    message: String,
    cause: Throwable? = null
) : Throwable(message, cause) {

    /**
     * Network-related errors (connectivity, timeout, etc.).
     */
    data class NetworkError(
        val errorMessage: String,
        private val originalCause: Throwable? = null
    ) : AppError(errorMessage, originalCause)

    /**
     * Authentication and authorization errors.
     */
    data class AuthError(
        val errorMessage: String,
        private val originalCause: Throwable? = null
    ) : AppError(errorMessage, originalCause)

    /**
     * Database and data persistence errors.
     */
    data class DatabaseError(
        val errorMessage: String,
        private val originalCause: Throwable? = null
    ) : AppError(errorMessage, originalCause)

    /**
     * Input validation errors.
     */
    data class ValidationError(
        val errorMessage: String,
        val field: String? = null
    ) : AppError(errorMessage)

    /**
     * Unknown or unexpected errors.
     */
    data class UnknownError(
        val errorMessage: String,
        private val originalCause: Throwable? = null
    ) : AppError(errorMessage, originalCause)
}
