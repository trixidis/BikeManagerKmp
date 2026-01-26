package com.bikemanager.presentation.maintenances

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.ErrorMessages
import com.bikemanager.domain.common.fold
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.usecase.maintenance.AddMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.DeleteMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.GetMaintenancesUseCase
import com.bikemanager.domain.usecase.maintenance.MarkMaintenanceDoneUseCase
import com.bikemanager.presentation.base.MviViewModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for managing bike maintenances.
 * Follows MVI pattern with automatic state management from base class.
 *
 * Uses:
 * - StateFlow for persistent UI state (Loading, Success, Error)
 * - Channel for one-shot events (ShowError, ShowSuccess, ShowUndoSnackbar)
 * - Base MviViewModel for infrastructure (execute, emitEvent, observeFlow)
 *
 * CancellationException handling is guaranteed by base class.
 *
 * @param bikeId The ID of the bike whose maintenances are managed
 * @param getMaintenancesUseCase Use case to observe maintenances stream
 * @param addMaintenanceUseCase Use case to add a maintenance
 * @param markMaintenanceDoneUseCase Use case to mark maintenance as done
 * @param deleteMaintenanceUseCase Use case to delete a maintenance
 */
class MaintenancesViewModelMvi(
    private val bikeId: String,
    private val getMaintenancesUseCase: GetMaintenancesUseCase,
    private val addMaintenanceUseCase: AddMaintenanceUseCase,
    private val markMaintenanceDoneUseCase: MarkMaintenanceDoneUseCase,
    private val deleteMaintenanceUseCase: DeleteMaintenanceUseCase
) : MviViewModel<MaintenancesUiState, MaintenanceEvent>(
    initialState = MaintenancesUiState.Loading
) {

    /**
     * UI State exposed for backward compatibility with existing UI.
     * Maps from base class state property.
     */
    val uiState: StateFlow<MaintenancesUiState> = state

    /**
     * Store deleted maintenance for undo functionality.
     * Used for backward compatibility with existing UI.
     */
    private var deletedMaintenance: Maintenance? = null

    init {
        observeMaintenances()
    }

    // ========== Public API - Intent Handlers ==========

    /**
     * Add a completed maintenance record.
     *
     * Validates input and creates a maintenance with:
     * - Current timestamp
     * - isDone = true
     * - User-provided name and value
     *
     * On success: UI updates automatically via Flow
     * On failure: Emits ShowError event
     *
     * @param name Maintenance name (must not be blank)
     * @param value Maintenance value in km/hours (must be >= 0)
     */
    fun addDoneMaintenance(name: String, value: Float) {
        if (name.isBlank() || value < 0) return

        execute {
            addMaintenanceUseCase(
                Maintenance(
                    name = name,
                    value = value,
                    date = com.bikemanager.util.currentTimeMillis(),
                    isDone = true,
                    bikeId = bikeId
                )
            )
        }
    }

    /**
     * Add a todo maintenance item.
     *
     * Creates a maintenance with:
     * - Default values (value = -1, date = 0)
     * - isDone = false
     * - User-provided name
     *
     * On success: UI updates automatically via Flow
     * On failure: Emits ShowError event
     *
     * @param name Maintenance name (must not be blank)
     */
    fun addTodoMaintenance(name: String) {
        if (name.isBlank()) return

        execute {
            addMaintenanceUseCase(
                Maintenance(
                    name = name,
                    value = -1f,
                    date = 0,
                    isDone = false,
                    bikeId = bikeId
                )
            )
        }
    }

    /**
     * Mark a maintenance as completed.
     *
     * Updates the maintenance with:
     * - isDone = true
     * - New value (current km/hours)
     * - Current timestamp
     *
     * On success: UI updates automatically via Flow
     * On failure: Emits ShowError event
     *
     * @param maintenanceId ID of the maintenance to mark as done
     * @param value Current value in km/hours (must be >= 0)
     */
    fun markMaintenanceDone(maintenanceId: String, value: Float) {
        if (value < 0) return

        execute {
            markMaintenanceDoneUseCase(maintenanceId, bikeId, value)
        }
    }

    /**
     * Delete a maintenance with undo support.
     *
     * Stores the deleted maintenance for potential undo operation.
     *
     * On success:
     * - UI updates automatically via Flow
     * - Emits ShowUndoSnackbar event for UI to display snackbar
     *
     * On failure: Emits ShowError event
     *
     * @param maintenance The maintenance to delete
     */
    fun deleteMaintenance(maintenance: Maintenance) {
        deletedMaintenance = maintenance

        execute(
            onSuccess = {
                // Emit event for UI to show snackbar with undo option
                emitEvent(MaintenanceEvent.ShowUndoSnackbar(maintenance))
            }
        ) {
            deleteMaintenanceUseCase(maintenance.id, bikeId)
        }
    }

    /**
     * Restore the previously deleted maintenance.
     *
     * Creates a new maintenance with same data but new ID.
     *
     * On success: UI updates automatically via Flow
     * On failure: Emits ShowError event
     */
    fun undoDelete() {
        val maintenance = deletedMaintenance ?: return
        deletedMaintenance = null

        execute {
            addMaintenanceUseCase(maintenance.copy(id = ""))
        }
    }

    // ========== Private Implementation ==========

    /**
     * Observe maintenances stream and update state reactively.
     *
     * Uses observeFlow() helper from base class which:
     * - Handles CancellationException properly
     * - Manages coroutine lifecycle
     * - Updates state automatically
     *
     * State transitions:
     * - Loading → Success (on success)
     * - Loading → Error (on failure)
     * - Success → Success (on updates)
     */
    private fun observeMaintenances() {
        observeFlow(
            flow = getMaintenancesUseCase(bikeId),
            transform = { result ->
                result.fold(
                    onSuccess = { (done, todo) ->
                        MaintenancesUiState.Success(
                            doneMaintenances = done,
                            todoMaintenances = todo
                        )
                    },
                    onFailure = { error ->
                        // error is AppError (type-safe)
                        MaintenancesUiState.Error(ErrorMessages.getMessage(error))
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
        Napier.e { "Maintenance error: ${error.message}" }
        emitEvent(MaintenanceEvent.ShowError(ErrorMessages.getMessage(error)))
    }
}
