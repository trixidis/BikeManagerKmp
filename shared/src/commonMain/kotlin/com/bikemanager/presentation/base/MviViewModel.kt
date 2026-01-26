package com.bikemanager.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.common.onFailure
import com.bikemanager.domain.common.onSuccess
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel for MVI pattern with proper CancellationException handling.
 *
 * Provides reusable infrastructure for:
 * - State management (StateFlow)
 * - Event emission (Channel for one-shot events)
 * - Operation execution with automatic error handling
 * - Flow observation with proper cancellation
 *
 * CRITICAL: This base class ensures CancellationException is always propagated correctly
 * to prevent memory leaks and ensure proper coroutine cleanup.
 *
 * @param State The type representing persistent UI state
 * @param Event The type representing one-shot UI events
 * @param initialState The initial state value
 */
abstract class MviViewModel<State, Event>(
    initialState: State
) : ViewModel() {

    // ========== State Management ==========

    /**
     * Internal mutable state - only accessible within ViewModel hierarchy.
     */
    protected val _state = MutableStateFlow(initialState)

    /**
     * Public state exposed to UI layer.
     * Emits the current state and all subsequent changes.
     */
    val state: StateFlow<State> = _state.asStateFlow()

    // ========== Event Management ==========

    /**
     * Channel for one-shot events.
     * Uses Channel instead of SharedFlow to guarantee delivery and one-time consumption.
     */
    private val _events = Channel<Event>(Channel.BUFFERED)

    /**
     * Public events exposed to UI layer.
     * Each event is consumed exactly once by the collector.
     */
    val events: Flow<Event> = _events.receiveAsFlow()

    // ========== Public API ==========

    /**
     * Execute an operation with automatic error handling and event emission.
     *
     * This method properly handles CancellationException:
     * - If operation() throws CancellationException, it's propagated automatically
     * - If onSuccess() throws CancellationException, it's caught and re-thrown
     * - Only AppError from Result.Failure is passed to handleError()
     *
     * Example:
     * ```kotlin
     * fun deleteMaintenance(item: Item) {
     *     execute(
     *         onSuccess = { emitEvent(ShowUndoSnackbar(item)) }
     *     ) {
     *         deleteUseCase(item.id)
     *     }
     * }
     * ```
     *
     * @param onSuccess Optional callback when operation succeeds (value passed is from Result.Success)
     * @param operation The operation to execute (must return Result<T>)
     */
    protected fun execute(
        onSuccess: suspend (Any?) -> Unit = {},
        operation: suspend () -> Result<*>
    ) {
        viewModelScope.launch {
            try {
                // Execute operation - returns Result, doesn't throw (except CancellationException)
                operation()
                    .onSuccess { value ->
                        // onSuccess callback might throw CancellationException - let it propagate
                        onSuccess(value)
                    }
                    .onFailure { error ->
                        // error is AppError (type-safe after refactoring)
                        // handleError won't throw CancellationException
                        handleError(error)
                    }
            } catch (e: CancellationException) {
                // CRITICAL: Always re-throw CancellationException to prevent memory leaks
                throw e
            } catch (e: Throwable) {
                // Unexpected exception (shouldn't happen if operation properly returns Result)
                Napier.e(e) { "Unexpected exception in execute()" }
                val appError = when (e) {
                    is AppError -> e
                    else -> AppError.UnknownError(
                        errorMessage = e.message ?: "Unexpected error",
                        originalCause = e
                    )
                }
                handleError(appError)
            }
        }
    }

    /**
     * Update state in a thread-safe way using StateFlow.update.
     *
     * Example:
     * ```kotlin
     * updateState { currentState ->
     *     currentState.copy(isLoading = false)
     * }
     * ```
     *
     * IMPORTANT: This is non-suspending and cannot throw CancellationException.
     *
     * @param transform Function that transforms current state to new state
     */
    protected fun updateState(transform: (State) -> State) {
        _state.update(transform)
    }

    /**
     * Emit a one-shot event to the UI layer.
     *
     * Uses trySend() which is non-suspending and safe in ViewModel context.
     * If channel is closed (ViewModel cleared), the event is silently ignored and logged.
     *
     * Example:
     * ```kotlin
     * emitEvent(ShowError("Failed to save"))
     * emitEvent(NavigateToDetail(itemId))
     * ```
     *
     * IMPORTANT: This method never throws, so it won't interfere with CancellationException propagation.
     *
     * @param event The event to emit
     */
    protected fun emitEvent(event: Event) {
        val result = _events.trySend(event)
        if (result.isFailure) {
            // Channel closed (ViewModel cleared) or buffer full
            // This is expected during cleanup - just log it
            Napier.d { "Failed to emit event (ViewModel likely cleared): $event" }
        }
    }

    /**
     * Helper to observe a flow and update state automatically.
     * Properly handles CancellationException.
     *
     * Example:
     * ```kotlin
     * private fun observeItems() {
     *     observeFlow(
     *         flow = getItemsUseCase(),
     *         transform = { result ->
     *             result.fold(
     *                 onSuccess = { items -> State.Loaded(items) },
     *                 onFailure = { error -> State.Error(error.message) }
     *             )
     *         }
     *     )
     * }
     * ```
     *
     * @param flow The flow to observe
     * @param transform Function to transform Result to State
     */
    protected fun <T> observeFlow(
        flow: Flow<Result<T>>,
        transform: (Result<T>) -> State
    ) {
        viewModelScope.launch {
            flow
                .catch { error ->
                    // CRITICAL: Check for cancellation first
                    kotlin.coroutines.coroutineContext.ensureActive()

                    // If we reach here, it's not a CancellationException
                    Napier.e(error) { "Error in flow observation" }

                    // Convert to AppError and handle
                    val appError = when (error) {
                        is AppError -> error
                        else -> AppError.UnknownError(
                            errorMessage = error.message ?: "Unknown error",
                            originalCause = error
                        )
                    }
                    handleError(appError)
                }
                .collect { result ->
                    try {
                        // Transform result to state and update
                        updateState { transform(result) }
                    } catch (e: CancellationException) {
                        // Re-throw if transform throws CancellationException
                        throw e
                    } catch (e: Throwable) {
                        // Unexpected exception in transform
                        Napier.e(e) { "Error transforming state" }
                        val appError = when (e) {
                            is AppError -> e
                            else -> AppError.UnknownError(
                                errorMessage = "Failed to update state",
                                originalCause = e
                            )
                        }
                        handleError(appError)
                    }
                }
        }
    }

    // ========== Abstract Methods ==========

    /**
     * Override this to customize error handling per ViewModel.
     *
     * IMPORTANT: This method receives only AppError, never CancellationException.
     * CancellationException is always re-thrown before reaching this method.
     *
     * Default implementation should emit an error event.
     *
     * Example:
     * ```kotlin
     * override fun handleError(error: AppError) {
     *     Napier.e { "Error: ${error.message}" }
     *     emitEvent(MyEvent.ShowError(ErrorMessages.getMessage(error)))
     * }
     * ```
     *
     * @param error The application-level error to handle
     */
    protected abstract fun handleError(error: AppError)
}
