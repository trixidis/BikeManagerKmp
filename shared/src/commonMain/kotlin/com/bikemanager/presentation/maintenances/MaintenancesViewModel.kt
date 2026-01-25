package com.bikemanager.presentation.maintenances

import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.usecase.maintenance.AddMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.DeleteMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.GetMaintenancesUseCase
import com.bikemanager.domain.usecase.maintenance.MarkMaintenanceDoneUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for managing maintenances of a bike.
 * Uses Firebase Realtime Database with offline persistence.
 */
class MaintenancesViewModel(
    private val bikeId: String,
    private val getMaintenancesUseCase: GetMaintenancesUseCase,
    private val addMaintenanceUseCase: AddMaintenanceUseCase,
    private val markMaintenanceDoneUseCase: MarkMaintenanceDoneUseCase,
    private val deleteMaintenanceUseCase: DeleteMaintenanceUseCase
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow<MaintenancesUiState>(MaintenancesUiState.Loading)
    val uiState: StateFlow<MaintenancesUiState> = _uiState.asStateFlow()

    private var deletedMaintenance: Maintenance? = null

    init {
        observeMaintenances()
    }

    /**
     * Observes maintenances from Firebase (real-time updates).
     */
    private fun observeMaintenances() {
        viewModelScope.launch {
            getMaintenancesUseCase(bikeId)
                .catch { throwable ->
                    Napier.e(throwable) { "Error observing maintenances" }
                    _uiState.value = MaintenancesUiState.Error(
                        throwable.message ?: "Unknown error"
                    )
                }
                .collect { (done, todo) ->
                    _uiState.value = MaintenancesUiState.Success(
                        doneMaintenances = done,
                        todoMaintenances = todo
                    )
                }
        }
    }

    /**
     * Adds a done maintenance.
     */
    fun addDoneMaintenance(name: String, value: Float) {
        if (name.isBlank() || value < 0) return

        viewModelScope.launch {
            try {
                addMaintenanceUseCase.addDone(name, value, bikeId)
            } catch (e: Exception) {
                Napier.e(e) { "Error adding done maintenance" }
                _uiState.value = MaintenancesUiState.Error(e.message ?: "Error adding maintenance")
            }
        }
    }

    /**
     * Adds a todo maintenance.
     */
    fun addTodoMaintenance(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            try {
                addMaintenanceUseCase.addTodo(name, bikeId)
            } catch (e: Exception) {
                Napier.e(e) { "Error adding todo maintenance" }
                _uiState.value = MaintenancesUiState.Error(e.message ?: "Error adding maintenance")
            }
        }
    }

    /**
     * Marks a maintenance as done.
     */
    fun markMaintenanceDone(maintenanceId: String, value: Float) {
        if (value < 0) return

        viewModelScope.launch {
            try {
                markMaintenanceDoneUseCase(maintenanceId, bikeId, value)
            } catch (e: Exception) {
                Napier.e(e) { "Error marking maintenance as done" }
                _uiState.value = MaintenancesUiState.Error(e.message ?: "Error updating maintenance")
            }
        }
    }

    /**
     * Deletes a maintenance and stores it for potential undo.
     */
    fun deleteMaintenance(maintenance: Maintenance) {
        deletedMaintenance = maintenance

        viewModelScope.launch {
            try {
                deleteMaintenanceUseCase(maintenance.id, bikeId)
            } catch (e: Exception) {
                Napier.e(e) { "Error deleting maintenance" }
                deletedMaintenance = null
                _uiState.value = MaintenancesUiState.Error(e.message ?: "Error deleting maintenance")
            }
        }
    }

    /**
     * Undoes the last deletion by re-adding the maintenance.
     */
    fun undoDelete() {
        val maintenance = deletedMaintenance ?: return
        deletedMaintenance = null

        viewModelScope.launch {
            try {
                addMaintenanceUseCase(maintenance.copy(id = ""))
            } catch (e: Exception) {
                Napier.e(e) { "Error restoring maintenance" }
            }
        }
    }
}
