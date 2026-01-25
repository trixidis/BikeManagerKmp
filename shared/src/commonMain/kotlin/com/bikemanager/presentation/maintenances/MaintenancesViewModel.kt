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
                .collect { result ->
                    when (result) {
                        is com.bikemanager.domain.common.Result.Success -> {
                            val (done, todo) = result.value
                            _uiState.value = MaintenancesUiState.Success(
                                doneMaintenances = done,
                                todoMaintenances = todo
                            )
                        }
                        is com.bikemanager.domain.common.Result.Failure -> {
                            Napier.e(result.error) { "Error loading maintenances" }
                            _uiState.value = MaintenancesUiState.Error(
                                result.error.message ?: "Unknown error"
                            )
                        }
                    }
                }
        }
    }

    /**
     * Adds a done maintenance.
     */
    fun addDoneMaintenance(name: String, value: Float) {
        if (name.isBlank() || value < 0) return

        viewModelScope.launch {
            when (val result = addMaintenanceUseCase.addDone(name, value, bikeId)) {
                is com.bikemanager.domain.common.Result.Success -> {
                    // Success - UI will update automatically via Flow
                }
                is com.bikemanager.domain.common.Result.Failure -> {
                    Napier.e(result.error) { "Error adding done maintenance" }
                    _uiState.value = MaintenancesUiState.Error(result.error.message ?: "Error adding maintenance")
                }
            }
        }
    }

    /**
     * Adds a todo maintenance.
     */
    fun addTodoMaintenance(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            when (val result = addMaintenanceUseCase.addTodo(name, bikeId)) {
                is com.bikemanager.domain.common.Result.Success -> {
                    // Success - UI will update automatically via Flow
                }
                is com.bikemanager.domain.common.Result.Failure -> {
                    Napier.e(result.error) { "Error adding todo maintenance" }
                    _uiState.value = MaintenancesUiState.Error(result.error.message ?: "Error adding maintenance")
                }
            }
        }
    }

    /**
     * Marks a maintenance as done.
     */
    fun markMaintenanceDone(maintenanceId: String, value: Float) {
        if (value < 0) return

        viewModelScope.launch {
            when (val result = markMaintenanceDoneUseCase(maintenanceId, bikeId, value)) {
                is com.bikemanager.domain.common.Result.Success -> {
                    // Success - UI will update automatically via Flow
                }
                is com.bikemanager.domain.common.Result.Failure -> {
                    Napier.e(result.error) { "Error marking maintenance as done" }
                    _uiState.value = MaintenancesUiState.Error(result.error.message ?: "Error updating maintenance")
                }
            }
        }
    }

    /**
     * Deletes a maintenance and stores it for potential undo.
     */
    fun deleteMaintenance(maintenance: Maintenance) {
        deletedMaintenance = maintenance

        viewModelScope.launch {
            when (val result = deleteMaintenanceUseCase(maintenance.id, bikeId)) {
                is com.bikemanager.domain.common.Result.Success -> {
                    // Success - UI will update automatically via Flow
                }
                is com.bikemanager.domain.common.Result.Failure -> {
                    Napier.e(result.error) { "Error deleting maintenance" }
                    deletedMaintenance = null
                    _uiState.value = MaintenancesUiState.Error(result.error.message ?: "Error deleting maintenance")
                }
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
            when (val result = addMaintenanceUseCase(maintenance.copy(id = ""))) {
                is com.bikemanager.domain.common.Result.Success -> {
                    // Success - UI will update automatically via Flow
                }
                is com.bikemanager.domain.common.Result.Failure -> {
                    Napier.e(result.error) { "Error restoring maintenance" }
                }
            }
        }
    }
}
