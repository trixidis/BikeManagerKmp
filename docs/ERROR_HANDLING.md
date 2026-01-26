# Error Handling Guide

This guide describes the error handling pattern used throughout the BikeManager application. The pattern provides consistent, type-safe error handling with user-friendly French error messages and proper logging.

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Result Type](#result-type)
4. [AppError Types](#apperror-types)
5. [ErrorHandler](#errorhandler)
6. [ErrorMessages](#errormessages)
7. [Usage Patterns](#usage-patterns)
8. [Adding New Error Types](#adding-new-error-types)
9. [Testing](#testing)
10. [Best Practices](#best-practices)

## Overview

The error handling system is built on four core components:

- **Result**: A sealed class wrapper that represents success or failure
- **AppError**: Domain-specific error types
- **ErrorHandler**: Utility for converting exceptions to AppError
- **ErrorMessages**: French user-friendly error messages

This approach provides:
- Type-safe error handling without exceptions in business logic
- Consistent error logging via Napier
- User-friendly localized error messages
- Easy testing with predictable error types

## Architecture

Error handling flows through the application layers:

```
Repository Layer → Use Case Layer → ViewModel Layer → UI Layer
     ↓                  ↓                 ↓
Result<T>          Result<T>      ErrorMessages.getMessage()
     ↓                  ↓                 ↓
AppError          AppError          French String
```

Each layer handles errors appropriately:
- **Repository**: Wraps operations in `Result`, uses `ErrorHandler.catching()`
- **Use Case**: Validates inputs, propagates `Result` from repository
- **ViewModel**: Converts `AppError` to user-friendly messages via `ErrorMessages`
- **UI**: Displays error messages to users

## Result Type

The `Result` sealed class wraps operation results that can succeed or fail.

### Definition

```kotlin
sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure(val error: Throwable) : Result<Nothing>()
}
```

### Creating Results

```kotlin
// Success
val result = Result.Success("data")

// Failure
val result = Result.Failure(AppError.NetworkError("Connection failed"))

// From exceptions
val result = runCatching {
    riskyOperation()
}

// From nullable values
val result = nullableValue.toResult {
    AppError.DatabaseError("Value not found")
}
```

### Working with Results

```kotlin
// Check if success/failure
if (result.isSuccess) { /* ... */ }
if (result.isFailure) { /* ... */ }

// Get value or null
val value = result.getOrNull()

// Get value or default
val value = result.getOrElse { error ->
    defaultValue
}

// Get value or throw
val value = result.getOrThrow()

// Transform success values
val mapped = result.map { value ->
    value.uppercase()
}

// Chain operations
val chained = result.flatMap { value ->
    anotherOperation(value)
}

// Handle both cases
result.fold(
    onSuccess = { value ->
        println("Success: $value")
    },
    onFailure = { error ->
        println("Error: ${error.message}")
    }
)

// Side effects
result
    .onSuccess { value -> log("Success: $value") }
    .onFailure { error -> log("Error: $error") }
```

## AppError Types

Domain-specific error types that extend `Throwable` for use with `Result.Failure`.

### Available Error Types

```kotlin
sealed class AppError(message: String, cause: Throwable? = null) : Throwable(message, cause) {

    // Network-related errors (connectivity, timeout, etc.)
    data class NetworkError(
        val errorMessage: String,
        private val originalCause: Throwable? = null
    ) : AppError(errorMessage, originalCause)

    // Authentication and authorization errors
    data class AuthError(
        val errorMessage: String,
        private val originalCause: Throwable? = null
    ) : AppError(errorMessage, originalCause)

    // Database and data persistence errors
    data class DatabaseError(
        val errorMessage: String,
        private val originalCause: Throwable? = null
    ) : AppError(errorMessage, originalCause)

    // Input validation errors
    data class ValidationError(
        val errorMessage: String,
        val field: String? = null
    ) : AppError(errorMessage)

    // Unknown or unexpected errors
    data class UnknownError(
        val errorMessage: String,
        private val originalCause: Throwable? = null
    ) : AppError(errorMessage, originalCause)
}
```

### When to Use Each Type

- **NetworkError**: Connection failures, timeouts, unreachable hosts
- **AuthError**: Sign-in failures, expired tokens, unauthorized access
- **DatabaseError**: Firebase/Firestore failures, permission errors, data not found
- **ValidationError**: Invalid input, missing required fields, format errors
- **UnknownError**: Unexpected errors that don't fit other categories

## ErrorHandler

Utility for converting exceptions to `AppError` with automatic logging.

### Basic Usage

```kotlin
// Convert an exception to AppError
val appError = ErrorHandler.handle(
    throwable = exception,
    context = "signing in"  // Optional context for logging
)

// Wrap suspending operations
val result = ErrorHandler.catching("adding bike") {
    repository.addBike(bike)
}
```

### How It Works

`ErrorHandler` examines exceptions and maps them to appropriate `AppError` types based on:
- Exception class name (e.g., `FirebaseAuthException` → `AuthError`)
- Exception message content (e.g., "network" → `NetworkError`)

All errors are logged via Napier with the provided context:
```
Error adding bike: NetworkException: Connection timeout
```

### Exception Mapping Rules

| Exception Pattern | Mapped To | Examples |
|------------------|-----------|----------|
| `*network*`, `*connection*`, `*timeout*` | `NetworkError` | `IOException`, `SocketException` |
| `*auth*`, `*credential*`, `*token*` | `AuthError` | `FirebaseAuthException` |
| `*database*`, `*firestore*`, `*firebase*` | `DatabaseError` | `FirestoreException` |
| `*validation*`, `*invalid*`, `*blank*` | `ValidationError` | `IllegalArgumentException` |
| Everything else | `UnknownError` | Generic exceptions |

## ErrorMessages

Provides user-friendly French error messages for the UI layer.

### Usage

```kotlin
// Convert AppError to French message
val message = ErrorMessages.getMessage(error)

// Example outputs:
// NetworkError → "Erreur de connexion. Veuillez vérifier votre connexion internet et réessayer."
// AuthError → "Erreur d'authentification. Veuillez vous reconnecter."
// DatabaseError → "Erreur lors de la sauvegarde. Veuillez réessayer."
// ValidationError → "Saisie invalide. Veuillez vérifier vos données."
// ValidationError(field="name") → "Saisie invalide pour le champ : name"
```

### Available Constants

```kotlin
ErrorMessages.NETWORK_ERROR
ErrorMessages.AUTH_ERROR
ErrorMessages.DATABASE_ERROR
ErrorMessages.VALIDATION_ERROR
ErrorMessages.UNKNOWN_ERROR

// Specific messages
ErrorMessages.AUTH_TOKEN_EXPIRED
ErrorMessages.NETWORK_TIMEOUT
ErrorMessages.DATABASE_SAVE_FAILED
// ... and more
```

## Usage Patterns

### Repository Layer

Repositories use `ErrorHandler.catching()` to wrap operations and return `Result<T>`.

```kotlin
class BikeRepositoryImpl : BikeRepository {

    override suspend fun addBike(bike: Bike): Result<String> {
        val ref = bikesRef()
        if (ref == null) {
            return Result.Failure(
                AppError.AuthError("User not authenticated")
            )
        }

        return ErrorHandler.catching("adding bike") {
            val newRef = ref.push()
            val bikeData = mapOf(
                "nameBike" to bike.name,
                "countingMethod" to bike.countingMethod.name
            )
            newRef.setValue(bikeData)

            val key = newRef.key ?: throw IllegalStateException("Failed to get Firebase key")
            Napier.d { "Bike added: ${bike.name} (id=$key)" }
            key
        }
    }

    override fun getAllBikes(): Flow<Result<List<Bike>>> {
        val ref = bikesRef()
        if (ref == null) {
            return flow {
                emit(Result.Failure(
                    AppError.AuthError("User not authenticated")
                ))
            }
        }

        return ref.valueEvents.map { snapshot ->
            try {
                val bikes = snapshot.children.mapNotNull { /* parse bike */ }
                Result.Success(bikes)
            } catch (e: Throwable) {
                Result.Failure(ErrorHandler.handle(e, "loading bikes"))
            }
        }
    }
}
```

**Key Points:**
- Return `Result<T>` instead of throwing exceptions
- Use `ErrorHandler.catching()` for suspending operations
- Use `ErrorHandler.handle()` for exception mapping in flows
- Return explicit `AppError` for known error conditions
- Add context strings for better logging

### Use Case Layer

Use cases validate inputs and propagate `Result` from repositories.

```kotlin
class AddBikeUseCase(
    private val repository: BikeRepository
) {
    suspend operator fun invoke(bike: Bike): Result<String> {
        // Input validation
        if (bike.name.isBlank()) {
            return Result.Failure(
                AppError.ValidationError(
                    errorMessage = "Bike name cannot be empty",
                    field = "name"
                )
            )
        }

        // Propagate Result from repository
        return repository.addBike(bike)
    }
}
```

**Key Points:**
- Validate inputs before calling repository
- Return `ValidationError` for invalid inputs with field information
- Propagate repository `Result` without modification
- No try-catch needed (Result handles errors)

### ViewModel Layer

ViewModels convert `AppError` to user-friendly messages and update UI state.

```kotlin
class BikesViewModel(
    private val addBikeUseCase: AddBikeUseCase
) {
    private val _uiState = MutableStateFlow<BikesUiState>(BikesUiState.Loading)
    val uiState: StateFlow<BikesUiState> = _uiState.asStateFlow()

    fun addBike(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            val result = addBikeUseCase(Bike(name = name))
            result.fold(
                onSuccess = { bikeId ->
                    Napier.d { "Bike added successfully: $bikeId" }
                },
                onFailure = { error ->
                    Napier.e(error) { "Error adding bike" }
                    val appError = if (error is AppError) {
                        error
                    } else {
                        ErrorHandler.handle(error, "adding bike")
                    }
                    _uiState.value = BikesUiState.Error(
                        ErrorMessages.getMessage(appError)
                    )
                }
            )
        }
    }

    private fun observeBikes() {
        viewModelScope.launch {
            getBikesUseCase()
                .catch { throwable ->
                    // Handle Flow exceptions
                    Napier.e(throwable) { "Error observing bikes" }
                    val appError = if (throwable is AppError) {
                        throwable
                    } else {
                        ErrorHandler.handle(throwable, "observing bikes")
                    }
                    _uiState.value = BikesUiState.Error(
                        ErrorMessages.getMessage(appError)
                    )
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { bikes ->
                            _uiState.value = if (bikes.isEmpty()) {
                                BikesUiState.Empty()
                            } else {
                                BikesUiState.Success(bikes)
                            }
                        },
                        onFailure = { error ->
                            val appError = if (error is AppError) {
                                error
                            } else {
                                ErrorHandler.handle(error, "loading bikes")
                            }
                            _uiState.value = BikesUiState.Error(
                                ErrorMessages.getMessage(appError)
                            )
                        }
                    )
                }
        }
    }
}
```

**Key Points:**
- Use `result.fold()` to handle success and failure cases
- Convert non-AppError exceptions using `ErrorHandler.handle()`
- Use `ErrorMessages.getMessage()` to get French error messages
- Update UI state with error messages
- Use `.catch {}` for Flow exception handling
- Log errors with Napier

### UI State Pattern

Error states in UI state sealed classes:

```kotlin
sealed class BikesUiState {
    object Loading : BikesUiState()
    data class Success(val bikes: List<Bike>) : BikesUiState()
    data class Error(val message: String) : BikesUiState()  // French message from ErrorMessages
    data class Empty(val message: String = "Aucun vélo ajouté") : BikesUiState()
}
```

## Adding New Error Types

To add a new error type:

### 1. Add to AppError sealed class

```kotlin
// In AppError.kt
data class PermissionError(
    val errorMessage: String,
    val requiredPermission: String,
    private val originalCause: Throwable? = null
) : AppError(errorMessage, originalCause)
```

### 2. Update ErrorHandler mapping

```kotlin
// In ErrorHandler.kt
fun handle(throwable: Throwable, context: String? = null): AppError {
    // ...
    return when {
        // Add new mapping rule
        isPermissionError(throwable) -> AppError.PermissionError(
            errorMessage = throwable.message ?: "Permission error",
            requiredPermission = extractPermission(throwable),
            originalCause = throwable
        )
        // ... existing rules
    }
}

private fun isPermissionError(throwable: Throwable): Boolean {
    val message = throwable.message?.lowercase() ?: ""
    return message.contains("permission") ||
           message.contains("forbidden") ||
           throwable::class.simpleName?.contains("PermissionException") == true
}
```

### 3. Add ErrorMessages mapping

```kotlin
// In ErrorMessages.kt
fun getMessage(error: AppError): String {
    return when (error) {
        is AppError.PermissionError ->
            "Permissions insuffisantes. ${error.requiredPermission} requis."
        // ... existing mappings
    }
}

// Add constants
const val PERMISSION_ERROR = "Permissions insuffisantes."
```

### 4. Update tests

```kotlin
// In ErrorHandlerTest.kt
@Test
fun `handle converts permission exception to PermissionError`() {
    val exception = Exception("Permission denied: CAMERA")
    val error = ErrorHandler.handle(exception)

    assertTrue(error is AppError.PermissionError)
    assertEquals("CAMERA", (error as AppError.PermissionError).requiredPermission)
}

// In ErrorMessagesTest.kt (create if needed)
@Test
fun `getMessage returns French message for PermissionError`() {
    val error = AppError.PermissionError(
        errorMessage = "Permission denied",
        requiredPermission = "CAMERA"
    )
    val message = ErrorMessages.getMessage(error)

    assertTrue(message.contains("Permissions insuffisantes"))
}
```

## Testing

### Testing Use Cases with Result

```kotlin
@Test
fun `addBike returns Failure when name is blank`() = runTest {
    val useCase = AddBikeUseCase(fakeBikeRepository)

    val result = useCase(Bike(name = ""))

    assertTrue(result.isFailure)
    val error = (result as Result.Failure).error
    assertTrue(error is AppError.ValidationError)
    assertEquals("name", (error as AppError.ValidationError).field)
}

@Test
fun `addBike returns Success when bike is valid`() = runTest {
    val useCase = AddBikeUseCase(fakeBikeRepository)

    val result = useCase(Bike(name = "Mountain Bike"))

    assertTrue(result.isSuccess)
    val bikeId = (result as Result.Success).value
    assertNotNull(bikeId)
}
```

### Testing ViewModels with Error States

```kotlin
@Test
fun `addBike updates state to Error when use case fails`() = runTest {
    fakeBikeRepository.setAddBikeFails(
        AppError.NetworkError("Connection failed")
    )
    val viewModel = BikesViewModel(addBikeUseCase, getBikesUseCase, updateBikeUseCase)

    viewModel.addBike("Test Bike")
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state is BikesUiState.Error)
    assertTrue((state as BikesUiState.Error).message.contains("connexion"))
}
```

### Testing Fake Repositories

Fake repositories should return `Result`:

```kotlin
class FakeBikeRepository : BikeRepository {
    private var addBikeResult: Result<String> = Result.Success("test-id")

    fun setAddBikeFails(error: AppError) {
        addBikeResult = Result.Failure(error)
    }

    override suspend fun addBike(bike: Bike): Result<String> {
        return addBikeResult
    }

    override fun getAllBikes(): Flow<Result<List<Bike>>> = flow {
        emit(Result.Success(bikes))
    }
}
```

### Testing ErrorHandler

```kotlin
@Test
fun `handle converts network exception to NetworkError`() {
    val exception = Exception("Connection timeout")
    val error = ErrorHandler.handle(exception, "testing")

    assertTrue(error is AppError.NetworkError)
    assertEquals("Connection timeout", (error as AppError.NetworkError).errorMessage)
}

@Test
fun `catching returns Success when operation succeeds`() = runTest {
    val result = ErrorHandler.catching("test") {
        "success"
    }

    assertTrue(result.isSuccess)
    assertEquals("success", result.getOrNull())
}

@Test
fun `catching returns Failure when operation throws`() = runTest {
    val result = ErrorHandler.catching("test") {
        throw Exception("Network error")
    }

    assertTrue(result.isFailure)
    val error = (result as Result.Failure).error
    assertTrue(error is AppError.NetworkError)
}
```

## Best Practices

### Do's ✅

1. **Always return Result from repositories and use cases**
   ```kotlin
   suspend fun addBike(bike: Bike): Result<String>
   ```

2. **Use ErrorHandler.catching() for suspending operations**
   ```kotlin
   return ErrorHandler.catching("adding bike") {
       database.addBike(bike)
   }
   ```

3. **Validate inputs in use cases**
   ```kotlin
   if (input.isBlank()) {
       return Result.Failure(AppError.ValidationError("Input required", "input"))
   }
   ```

4. **Convert AppError to French messages in ViewModels**
   ```kotlin
   _uiState.value = UiState.Error(ErrorMessages.getMessage(appError))
   ```

5. **Add context to error handling**
   ```kotlin
   ErrorHandler.catching("updating user profile") { /* ... */ }
   ```

6. **Log errors with Napier**
   ```kotlin
   Napier.e(error) { "Error adding bike" }
   ```

7. **Check error type before converting in ViewModels**
   ```kotlin
   val appError = if (error is AppError) {
       error
   } else {
       ErrorHandler.handle(error, "context")
   }
   ```

### Don'ts ❌

1. **Don't throw exceptions in repositories or use cases**
   ```kotlin
   // ❌ Bad
   suspend fun addBike(bike: Bike): String {
       throw Exception("Error")
   }

   // ✅ Good
   suspend fun addBike(bike: Bike): Result<String> {
       return Result.Failure(AppError.DatabaseError("Error"))
   }
   ```

2. **Don't use try-catch in use cases** (Result handles errors)
   ```kotlin
   // ❌ Bad
   suspend fun invoke(bike: Bike): Result<String> {
       try {
           return repository.addBike(bike)
       } catch (e: Exception) {
           return Result.Failure(ErrorHandler.handle(e))
       }
   }

   // ✅ Good
   suspend fun invoke(bike: Bike): Result<String> {
       return repository.addBike(bike)
   }
   ```

3. **Don't expose technical error messages to users**
   ```kotlin
   // ❌ Bad
   _uiState.value = UiState.Error(exception.message ?: "Error")

   // ✅ Good
   _uiState.value = UiState.Error(ErrorMessages.getMessage(appError))
   ```

4. **Don't mix English and French error messages**
   ```kotlin
   // ❌ Bad
   "Error: User not found"

   // ✅ Good
   "Utilisateur introuvable."
   ```

5. **Don't forget to log errors**
   ```kotlin
   // ❌ Bad
   result.fold(
       onSuccess = { /* ... */ },
       onFailure = { error ->
           _uiState.value = UiState.Error(ErrorMessages.getMessage(error))
       }
   )

   // ✅ Good
   result.fold(
       onSuccess = { /* ... */ },
       onFailure = { error ->
           Napier.e(error) { "Error adding bike" }
           _uiState.value = UiState.Error(ErrorMessages.getMessage(error))
       }
   )
   ```

6. **Don't catch exceptions in ViewModels** (use Result)
   ```kotlin
   // ❌ Bad
   try {
       val result = useCase(data)
   } catch (e: Exception) {
       // handle error
   }

   // ✅ Good
   useCase(data).fold(
       onSuccess = { /* ... */ },
       onFailure = { error -> /* ... */ }
   )
   ```

### Flow Error Handling

For Flows, use `.catch {}` operator:

```kotlin
// ✅ Good
getBikesUseCase()
    .catch { throwable ->
        Napier.e(throwable) { "Error observing bikes" }
        val appError = if (throwable is AppError) {
            throwable
        } else {
            ErrorHandler.handle(throwable, "observing bikes")
        }
        _uiState.value = BikesUiState.Error(
            ErrorMessages.getMessage(appError)
        )
    }
    .collect { result ->
        result.fold(/* ... */)
    }
```

## Summary

The error handling pattern provides:

1. **Type Safety**: `Result<T>` makes success and failure explicit
2. **Consistency**: All errors flow through the same pipeline
3. **Localization**: French error messages via `ErrorMessages`
4. **Logging**: Automatic error logging via `ErrorHandler`
5. **Testability**: Easy to test with fake repositories returning `Result`
6. **User Experience**: Clear, actionable error messages

By following this pattern, errors are handled consistently across the entire application, improving both developer experience and user experience.

---

## CancellationException Handling

### Why CancellationException Must Be Re-thrown

In Kotlin coroutines, `CancellationException` is a special exception that signals a coroutine has been cancelled. It is **critical** that this exception is never caught and suppressed, as doing so would:

- Prevent proper coroutine cancellation
- Cause memory leaks by keeping cancelled coroutines alive
- Block cleanup of coroutine resources
- Break structured concurrency guarantees

### Implementation

Our error handling utilities (`ErrorHandler.catching()` and `runCatching()`) properly handle `CancellationException`:

```kotlin
suspend fun <T> catching(context: String? = null, block: suspend () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: CancellationException) {
        // CRITICAL: Always re-throw CancellationException
        throw e
    } catch (e: Throwable) {
        Result.Failure(handle(e, context))
    }
}
```

### Pattern for Manual Exception Handling

If you need to manually catch exceptions in coroutines, **always** re-throw `CancellationException` first:

```kotlin
// ✅ Correct pattern
try {
    someOperation()
} catch (e: CancellationException) {
    // CRITICAL: Must re-throw before handling other exceptions
    throw e
} catch (e: Exception) {
    // Handle other exceptions
    handleError(e)
}

// ❌ WRONG - Will cause memory leaks!
try {
    someOperation()
} catch (e: Throwable) {  // Catches CancellationException too!
    handleError(e)
}

// ❌ WRONG - Catches CancellationException
try {
    someOperation()
} catch (e: Exception) {  // CancellationException extends Exception
    handleError(e)
}
```

### Testing CancellationException

When testing functions that handle `CancellationException`, use fully qualified names to avoid conflicts with Kotlin's stdlib `runCatching`:

```kotlin
@Test
fun `test cancellation exception is re-thrown`() = runTest {
    var exceptionThrown = false
    try {
        com.bikemanager.domain.common.runCatching<Unit> {
            throw CancellationException("Cancelled")
        }
    } catch (e: CancellationException) {
        exceptionThrown = true
    }
    assertTrue(exceptionThrown)
}
```

### Why This Matters

Without proper CancellationException handling:
- Cancelled coroutines continue consuming resources
- Parent-child coroutine relationships break
- Timeouts and job cancellation stop working
- Memory leaks accumulate over time
- App performance degrades

All code in BikeManager properly re-throws `CancellationException` to prevent these issues.
