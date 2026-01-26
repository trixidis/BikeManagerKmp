package com.bikemanager.presentation.bikes

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.ErrorMessages
import com.bikemanager.domain.common.fold
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.usecase.bike.AddBikeUseCase
import com.bikemanager.domain.usecase.bike.GetBikesUseCase
import com.bikemanager.domain.usecase.bike.UpdateBikeUseCase
import com.bikemanager.presentation.base.MviViewModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing bikes list screen.
 * Follows MVI pattern with automatic state management from base class.
 *
 * Uses:
 * - StateFlow for persistent UI state (Loading, Empty, Success, Error)
 * - Channel for one-shot events (ShowError, ShowSuccess)
 * - Base MviViewModel for infrastructure (execute, emitEvent, observeFlow)
 *
 * CancellationException handling is guaranteed by base class.
 *
 * @param getBikesUseCase Use case to observe bikes stream
 * @param addBikeUseCase Use case to add a bike
 * @param updateBikeUseCase Use case to update a bike
 */
class BikesViewModelMvi(
    private val getBikesUseCase: GetBikesUseCase,
    private val addBikeUseCase: AddBikeUseCase,
    private val updateBikeUseCase: UpdateBikeUseCase
) : MviViewModel<BikesUiState, BikeEvent>(
    initialState = BikesUiState.Loading
) {

    /**
     * UI State exposed for backward compatibility with existing UI.
     * Maps from base class state property.
     */
    val uiState: StateFlow<BikesUiState> = state

    init {
        observeBikes()
    }

    // ========== Public API - Intent Handlers ==========

    /**
     * Add a new bike with the given name.
     *
     * Validates input and creates a bike with default values.
     *
     * On success:
     * - UI updates automatically via Flow
     * - Emits ShowSuccess event
     *
     * On failure: Emits ShowError event
     *
     * @param name Bike name (must not be blank)
     */
    fun addBike(name: String) {
        if (name.isBlank()) return

        execute(
            onSuccess = {
                Napier.d { "Bike added successfully" }
                emitEvent(BikeEvent.ShowSuccess("Vélo ajouté"))
            }
        ) {
            addBikeUseCase(Bike(name = name))
        }
    }

    /**
     * Update an existing bike.
     *
     * On success:
     * - UI updates automatically via Flow
     * - Logs success
     *
     * On failure: Emits ShowError event
     *
     * @param bike The bike to update
     */
    fun updateBike(bike: Bike) {
        execute(
            onSuccess = {
                Napier.d { "Bike updated successfully: ${bike.name}" }
            }
        ) {
            updateBikeUseCase(bike)
        }
    }

    /**
     * Update the name of a bike.
     *
     * Finds the bike in current state and updates its name.
     *
     * @param bikeId ID of the bike to update
     * @param newName New name for the bike
     */
    fun updateBikeName(bikeId: String, newName: String) {
        val currentState = _state.value
        if (currentState is BikesUiState.Success) {
            val bike = currentState.bikes.find { it.id == bikeId } ?: return
            updateBike(bike.copy(name = newName))
        }
    }

    /**
     * Update the counting method of a bike.
     *
     * Finds the bike in current state and updates its counting method.
     *
     * @param bikeId ID of the bike to update
     * @param countingMethod New counting method
     */
    fun updateBikeCountingMethod(bikeId: String, countingMethod: CountingMethod) {
        val currentState = _state.value
        if (currentState is BikesUiState.Success) {
            val bike = currentState.bikes.find { it.id == bikeId } ?: return
            updateBike(bike.copy(countingMethod = countingMethod))
        }
    }

    // ========== Private Implementation ==========

    /**
     * Observe bikes stream and update state reactively.
     *
     * Uses observeFlow() helper from base class which:
     * - Handles CancellationException properly
     * - Manages coroutine lifecycle
     * - Updates state automatically
     *
     * State transitions:
     * - Loading → Success/Empty (on success)
     * - Loading → Error (on failure)
     * - Success → Success/Empty (on updates)
     */
    private fun observeBikes() {
        observeFlow(
            flow = getBikesUseCase(),
            transform = { result ->
                result.fold(
                    onSuccess = { bikes ->
                        if (bikes.isEmpty()) {
                            BikesUiState.Empty()
                        } else {
                            BikesUiState.Success(bikes)
                        }
                    },
                    onFailure = { error ->
                        // error is AppError (type-safe)
                        BikesUiState.Error(ErrorMessages.getMessage(error))
                    }
                )
            }
        )
    }

    /**
     * Convert errors to user-facing messages and emit error event.
     *
     * Called automatically by base class when operations fail.
     * Never receives CancellationException (handled by base class).
     *
     * @param error The application-level error
     */
    override fun handleError(error: AppError) {
        Napier.e { "Bike error: ${error.message}" }
        emitEvent(BikeEvent.ShowError(ErrorMessages.getMessage(error)))
    }
}
