package com.bikemanager.domain.common

/**
 * A wrapper for operation results that can either succeed or fail.
 * This type provides a functional approach to error handling.
 *
 * @param T The type of the successful result value
 */
sealed class Result<out T> {
    /**
     * Represents a successful operation result.
     */
    data class Success<T>(val value: T) : Result<T>()

    /**
     * Represents a failed operation result.
     */
    data class Failure(val error: Throwable) : Result<Nothing>()

    /**
     * Returns true if this result is a success.
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Returns true if this result is a failure.
     */
    val isFailure: Boolean
        get() = this is Failure
}

/**
 * Returns the value if this is a Success, or null if this is a Failure.
 */
fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> value
    is Result.Failure -> null
}

/**
 * Returns the value if this is a Success, or the result of [default] if this is a Failure.
 */
inline fun <T> Result<T>.getOrElse(default: (Throwable) -> T): T = when (this) {
    is Result.Success -> value
    is Result.Failure -> default(error)
}

/**
 * Returns the value if this is a Success, or throws the error if this is a Failure.
 */
fun <T> Result<T>.getOrThrow(): T = when (this) {
    is Result.Success -> value
    is Result.Failure -> throw error
}

/**
 * Transforms the value of a Success result using the given transformation function.
 * Returns the original Failure unchanged.
 */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(value))
    is Result.Failure -> this
}

/**
 * Transforms the value of a Success result using the given transformation function
 * that returns another Result. Returns the original Failure unchanged.
 */
inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
    is Result.Success -> transform(value)
    is Result.Failure -> this
}

/**
 * Transforms this Result into a value using the provided transformation functions.
 * The [onSuccess] function is called for Success results, [onFailure] for Failure results.
 */
inline fun <T, R> Result<T>.fold(
    onSuccess: (T) -> R,
    onFailure: (Throwable) -> R
): R = when (this) {
    is Result.Success -> onSuccess(value)
    is Result.Failure -> onFailure(error)
}

/**
 * Performs the given action if this is a Success.
 * Returns the original Result unchanged.
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(value)
    }
    return this
}

/**
 * Performs the given action if this is a Failure.
 * Returns the original Result unchanged.
 */
inline fun <T> Result<T>.onFailure(action: (Throwable) -> Unit): Result<T> {
    if (this is Result.Failure) {
        action(error)
    }
    return this
}

/**
 * Wraps the execution of a block in a Result, catching any exceptions as Failure.
 */
inline fun <T> runCatching(block: () -> T): Result<T> = try {
    Result.Success(block())
} catch (e: Throwable) {
    Result.Failure(e)
}

/**
 * Converts a nullable value to a Result.
 * Returns Success if the value is not null, Failure with the given error otherwise.
 */
fun <T> T?.toResult(error: () -> Throwable): Result<T> = if (this != null) {
    Result.Success(this)
} else {
    Result.Failure(error())
}
