package com.bikemanager.presentation.maintenances

import androidx.lifecycle.viewModelScope
import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.ErrorMessages
import com.bikemanager.domain.common.fold
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.BikeRepository
import com.bikemanager.domain.usecase.maintenance.AddMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.DeleteMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.GetMaintenancesUseCase
import com.bikemanager.domain.usecase.maintenance.MarkMaintenanceDoneUseCase
import com.bikemanager.domain.usecase.notification.CancelMaintenanceReminderUseCase
import com.bikemanager.domain.usecase.notification.ScheduleMaintenanceReminderUseCase
import com.bikemanager.presentation.base.MviViewModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
 * @param bikeRepository Repository to fetch current bike data
 * @param scheduleReminderUseCase Use case to schedule maintenance reminders
 * @param cancelReminderUseCase Use case to cancel maintenance reminders
 */
class MaintenancesViewModelMvi(
    private val bikeId: String,
    private val getMaintenancesUseCase: GetMaintenancesUseCase,
    private val addMaintenanceUseCase: AddMaintenanceUseCase,
    private val markMaintenanceDoneUseCase: MarkMaintenanceDoneUseCase,
    private val deleteMaintenanceUseCase: DeleteMaintenanceUseCase,
    private val bikeRepository: BikeRepository,
    private val scheduleReminderUseCase: ScheduleMaintenanceReminderUseCase,
    private val cancelReminderUseCase: CancelMaintenanceReminderUseCase
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

    /**
     * Current bike state with live countingMethod.
     * Used to fix race condition when countingMethod is changed.
     */
    private val _currentBike = MutableStateFlow<Bike?>(null)
    val currentBike: StateFlow<Bike?> = _currentBike.asStateFlow()

    init {
        observeMaintenances()
        observeBike()
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
     * On success:
     * - UI updates automatically via Flow
     * - Schedules a reminder notification
     *
     * On failure: Emits ShowError event
     *
     * @param name Maintenance name (must not be blank)
     */
    fun addTodoMaintenance(name: String) {
        if (name.isBlank()) return

        execute(
            onSuccess = { result ->
                // Schedule reminder notification for the newly created todo maintenance
                val maintenanceId = result as? String
                if (maintenanceId != null) {
                    val bike = _currentBike.value
                    if (bike != null) {
                        viewModelScope.launch {
                            scheduleReminderUseCase(
                                maintenanceId = maintenanceId,
                                maintenanceName = name,
                                bikeId = bikeId,
                                bikeName = bike.name,
                                countingMethod = bike.countingMethod
                            ).fold(
                                onSuccess = {
                                    Napier.d { "Notification planifiée pour la maintenance $maintenanceId" }
                                },
                                onFailure = { error ->
                                    // Ne pas bloquer l'UI, juste logger
                                    Napier.w { "Échec de planification de notification : ${error.message}" }
                                }
                            )
                        }
                    } else {
                        Napier.w { "Bike non disponible, notification non planifiée" }
                    }
                } else {
                    Napier.w { "ID de maintenance invalide, notification non planifiée" }
                }
            }
        ) {
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
     * On success:
     * - UI updates automatically via Flow
     * - Cancels the reminder notification
     *
     * On failure: Emits ShowError event
     *
     * @param maintenanceId ID of the maintenance to mark as done
     * @param value Current value in km/hours (must be >= 0)
     */
    fun markMaintenanceDone(maintenanceId: String, value: Float) {
        if (value < 0) return

        execute(
            onSuccess = {
                // Cancel reminder notification when maintenance is marked as done
                viewModelScope.launch {
                    cancelReminderUseCase(maintenanceId).fold(
                        onSuccess = {
                            Napier.d { "Notification annulée pour la maintenance $maintenanceId" }
                        },
                        onFailure = { error ->
                            // Ne pas bloquer l'UI, juste logger
                            Napier.w { "Échec d'annulation de notification : ${error.message}" }
                        }
                    )
                }
            }
        ) {
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
     * - Cancels the reminder notification
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

                // Cancel reminder notification when maintenance is deleted
                viewModelScope.launch {
                    cancelReminderUseCase(maintenance.id).fold(
                        onSuccess = {
                            Napier.d { "Notification annulée pour la maintenance supprimée ${maintenance.id}" }
                        },
                        onFailure = { error ->
                            // Ne pas bloquer l'UI, juste logger
                            Napier.w { "Échec d'annulation de notification : ${error.message}" }
                        }
                    )
                }
            }
        ) {
            deleteMaintenanceUseCase(maintenance.id, bikeId)
        }
    }

    /**
     * Restore the previously deleted maintenance.
     *
     * Restores the maintenance at its original position by preserving its ID.
     * Uses addMaintenance with the original ID to restore at the same position.
     *
     * On success: UI updates automatically via Flow
     * On failure: Emits ShowError event
     *
     * Note: Ne re-planifie PAS de notification - la notification originale persiste.
     * C'est le comportement souhaité car elle était déjà planifiée.
     */
    fun undoDelete() {
        val maintenance = deletedMaintenance ?: return
        deletedMaintenance = null

        execute {
            // Pass maintenance with original ID - repository will use it instead of generating new one
            addMaintenanceUseCase(maintenance)
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
     * Fetch the current bike to get live countingMethod.
     * Handles bike deletion case by emitting BikeDeleted event.
     */
    private fun observeBike() {
        viewModelScope.launch {
            val result = bikeRepository.getBikeById(bikeId)
            result.fold(
                onSuccess = { bike ->
                    if (bike != null) {
                        _currentBike.value = bike
                    } else {
                        // Bike was deleted
                        emitEvent(MaintenanceEvent.BikeDeleted)
                    }
                },
                onFailure = { error ->
                    // Don't block UI, log error
                    Napier.w { "Failed to fetch bike: ${error.message}" }
                }
            )
        }
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
