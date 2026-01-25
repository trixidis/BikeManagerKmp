package com.bikemanager.presentation.maintenances

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.ErrorMessages
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.usecase.maintenance.AddMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.DeleteMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.GetMaintenancesUseCase
import com.bikemanager.domain.usecase.maintenance.MarkMaintenanceDoneUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
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
                    coroutineContext.ensureActive()
                    Napier.e(throwable) { "Error observing maintenances" }
                    _uiState.value = MaintenancesUiState.Error(
                        ErrorMessages.UNKNOWN_ERROR
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
                            val errorMessage = when (val error = result.error) {
                                is AppError -> ErrorMessages.getMessage(error)
                                else -> ErrorMessages.UNKNOWN_ERROR
                            }
                            Napier.e { "Error loading maintenances: $errorMessage" }
                            _uiState.value = MaintenancesUiState.Error(errorMessage)
                        }
                    }
                }
        }
    }

    /**
     * Adds a done maintenance.
     * Constructs the Maintenance object with current timestamp and calls the use case.
     */
    fun addDoneMaintenance(name: String, value: Float) {
        if (name.isBlank() || value < 0) return

        viewModelScope.launch {
            val maintenance = Maintenance(
                name = name,
                value = value,
                date = com.bikemanager.util.currentTimeMillis(),
                isDone = true,
                bikeId = bikeId
            )

            when (val result = addMaintenanceUseCase(maintenance)) {
                is com.bikemanager.domain.common.Result.Success -> {
                    // Success - UI will update automatically via Flow
                }
                is com.bikemanager.domain.common.Result.Failure -> {
                    val errorMessage = when (val error = result.error) {
                        is AppError -> ErrorMessages.getMessage(error)
                        else -> ErrorMessages.UNKNOWN_ERROR
                    }
                    Napier.e { "Error adding done maintenance: $errorMessage" }
                    _uiState.value = MaintenancesUiState.Error(errorMessage)
                }
            }
        }
    }

    /**
     * Adds a todo maintenance.
     * Constructs the Maintenance object with default values for todo items.
     */
    fun addTodoMaintenance(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            val maintenance = Maintenance(
                name = name,
                value = -1f,
                date = 0,
                isDone = false,
                bikeId = bikeId
            )

            when (val result = addMaintenanceUseCase(maintenance)) {
                is com.bikemanager.domain.common.Result.Success -> {
                    // Success - UI will update automatically via Flow
                }
                is com.bikemanager.domain.common.Result.Failure -> {
                    val errorMessage = when (val error = result.error) {
                        is AppError -> ErrorMessages.getMessage(error)
                        else -> ErrorMessages.UNKNOWN_ERROR
                    }
                    Napier.e { "Error adding todo maintenance: $errorMessage" }
                    _uiState.value = MaintenancesUiState.Error(errorMessage)
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
                    val errorMessage = when (val error = result.error) {
                        is AppError -> ErrorMessages.getMessage(error)
                        else -> ErrorMessages.UNKNOWN_ERROR
                    }
                    Napier.e { "Error marking maintenance as done: $errorMessage" }
                    _uiState.value = MaintenancesUiState.Error(errorMessage)
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
                    val errorMessage = when (val error = result.error) {
                        is AppError -> ErrorMessages.getMessage(error)
                        else -> ErrorMessages.UNKNOWN_ERROR
                    }
                    Napier.e { "Error deleting maintenance: $errorMessage" }
                    deletedMaintenance = null
                    _uiState.value = MaintenancesUiState.Error(errorMessage)
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
                    val errorMessage = when (val error = result.error) {
                        is AppError -> ErrorMessages.getMessage(error)
                        else -> ErrorMessages.UNKNOWN_ERROR
                    }
                    Napier.e { "Error restoring maintenance: $errorMessage" }
                }
            }
        }
    }
}
