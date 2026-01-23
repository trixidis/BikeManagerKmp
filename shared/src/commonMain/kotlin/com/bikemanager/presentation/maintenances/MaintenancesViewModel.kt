package com.bikemanager.presentation.maintenances

import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.BikeRepository
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
 */
class MaintenancesViewModel(
    private val bikeId: Long,
    private val bikeRepository: BikeRepository,
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
        loadMaintenances()
    }

    /**
     * Loads all maintenances for the bike.
     */
    fun loadMaintenances() {
        viewModelScope.launch {
            _uiState.value = MaintenancesUiState.Loading
            getMaintenancesUseCase(bikeId)
                .catch { throwable ->
                    Napier.e(throwable) { "Error loading maintenances" }
                    _uiState.value = MaintenancesUiState.Error(
                        throwable.message ?: "Unknown error occurred"
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
     * @param name The name/type of maintenance
     * @param value The km/hours value
     */
    fun addDoneMaintenance(name: String, value: Float) {
        if (name.isBlank()) return
        if (value < 0) return

        viewModelScope.launch {
            try {
                addMaintenanceUseCase.addDone(name, value, bikeId)
            } catch (e: Exception) {
                Napier.e(e) { "Error adding done maintenance" }
                _uiState.value = MaintenancesUiState.Error(
                    e.message ?: "Error adding maintenance"
                )
            }
        }
    }

    /**
     * Adds a todo maintenance.
     * @param name The name/type of maintenance
     */
    fun addTodoMaintenance(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            try {
                addMaintenanceUseCase.addTodo(name, bikeId)
            } catch (e: Exception) {
                Napier.e(e) { "Error adding todo maintenance" }
                _uiState.value = MaintenancesUiState.Error(
                    e.message ?: "Error adding maintenance"
                )
            }
        }
    }

    /**
     * Marks a maintenance as done.
     * @param maintenanceId The id of the maintenance
     * @param value The km/hours value
     */
    fun markMaintenanceDone(maintenanceId: Long, value: Float) {
        if (value < 0) return

        viewModelScope.launch {
            try {
                markMaintenanceDoneUseCase(maintenanceId, value)
            } catch (e: Exception) {
                Napier.e(e) { "Error marking maintenance as done" }
                _uiState.value = MaintenancesUiState.Error(
                    e.message ?: "Error updating maintenance"
                )
            }
        }
    }

    /**
     * Deletes a maintenance and stores it for potential undo.
     * @param maintenance The maintenance to delete
     */
    fun deleteMaintenance(maintenance: Maintenance) {
        deletedMaintenance = maintenance

        viewModelScope.launch {
            try {
                deleteMaintenanceUseCase(maintenance.id)
            } catch (e: Exception) {
                Napier.e(e) { "Error deleting maintenance" }
                deletedMaintenance = null
                _uiState.value = MaintenancesUiState.Error(
                    e.message ?: "Error deleting maintenance"
                )
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
                addMaintenanceUseCase(maintenance.copy(id = 0))
            } catch (e: Exception) {
                Napier.e(e) { "Error restoring maintenance" }
            }
        }
    }
}
