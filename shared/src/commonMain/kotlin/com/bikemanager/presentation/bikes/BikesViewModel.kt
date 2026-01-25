package com.bikemanager.presentation.bikes

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.MaintenanceRepository
import com.bikemanager.domain.usecase.bike.AddBikeUseCase
import com.bikemanager.domain.usecase.bike.DeleteBikeUseCase
import com.bikemanager.domain.usecase.bike.GetBikesUseCase
import com.bikemanager.domain.usecase.bike.UpdateBikeUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Data class to store deleted bike with its maintenances for undo functionality.
 */
private data class DeletedBikeData(
    val bike: Bike,
    val maintenances: List<Maintenance>
)

/**
 * ViewModel for managing bikes list screen.
 * Uses Firebase Realtime Database with offline persistence.
 */
class BikesViewModel(
    private val getBikesUseCase: GetBikesUseCase,
    private val addBikeUseCase: AddBikeUseCase,
    private val updateBikeUseCase: UpdateBikeUseCase,
    private val deleteBikeUseCase: DeleteBikeUseCase,
    private val maintenanceRepository: MaintenanceRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow<BikesUiState>(BikesUiState.Loading)
    val uiState: StateFlow<BikesUiState> = _uiState.asStateFlow()

    private var deletedBikeData: DeletedBikeData? = null

    init {
        observeBikes()
    }

    /**
     * Observes bikes from Firebase (real-time updates with offline support).
     */
    private fun observeBikes() {
        viewModelScope.launch {
            getBikesUseCase()
                .catch { throwable ->
                    Napier.e(throwable) { "Error observing bikes" }
                    _uiState.value = BikesUiState.Error(
                        throwable.message ?: "Unknown error"
                    )
                }
                .collect { bikes ->
                    _uiState.value = if (bikes.isEmpty()) {
                        BikesUiState.Empty()
                    } else {
                        BikesUiState.Success(bikes)
                    }
                }
        }
    }

    /**
     * Adds a new bike with the given name.
     */
    fun addBike(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            try {
                addBikeUseCase(Bike(name = name))
            } catch (e: Exception) {
                Napier.e(e) { "Error adding bike" }
                _uiState.value = BikesUiState.Error(e.message ?: "Error adding bike")
            }
        }
    }

    /**
     * Updates an existing bike.
     */
    fun updateBike(bike: Bike) {
        viewModelScope.launch {
            try {
                updateBikeUseCase(bike)
            } catch (e: Exception) {
                Napier.e(e) { "Error updating bike" }
                _uiState.value = BikesUiState.Error(e.message ?: "Error updating bike")
            }
        }
    }

    /**
     * Updates the name of a bike.
     */
    fun updateBikeName(bikeId: String, newName: String) {
        val currentState = _uiState.value
        if (currentState is BikesUiState.Success) {
            val bike = currentState.bikes.find { it.id == bikeId } ?: return
            updateBike(bike.copy(name = newName))
        }
    }

    /**
     * Updates the counting method of a bike.
     */
    fun updateBikeCountingMethod(bikeId: String, countingMethod: CountingMethod) {
        val currentState = _uiState.value
        if (currentState is BikesUiState.Success) {
            val bike = currentState.bikes.find { it.id == bikeId } ?: return
            updateBike(bike.copy(countingMethod = countingMethod))
        }
    }

    /**
     * Deletes a bike and stores it with its maintenances for potential undo.
     */
    fun deleteBike(bike: Bike) {
        viewModelScope.launch {
            try {
                // Fetch maintenances before deletion so we can restore them if needed
                val maintenances = maintenanceRepository.getMaintenancesByBikeId(bike.id).first()

                // Store bike and maintenances for undo
                deletedBikeData = DeletedBikeData(bike, maintenances)

                // Delete bike and all maintenances
                deleteBikeUseCase(bike.id)
            } catch (e: Exception) {
                Napier.e(e) { "Error deleting bike" }
                deletedBikeData = null
                _uiState.value = BikesUiState.Error(e.message ?: "Error deleting bike")
            }
        }
    }

    /**
     * Undoes the last deletion by restoring the bike and its maintenances.
     */
    fun undoDelete() {
        val data = deletedBikeData ?: return
        deletedBikeData = null

        viewModelScope.launch {
            try {
                // Restore the bike with its original ID
                updateBikeUseCase(data.bike)

                // Restore all maintenances with their original IDs
                data.maintenances.forEach { maintenance ->
                    maintenanceRepository.updateMaintenance(maintenance)
                }

                Napier.d { "Bike and ${data.maintenances.size} maintenances restored" }
            } catch (e: Exception) {
                Napier.e(e) { "Error restoring bike" }
                _uiState.value = BikesUiState.Error(e.message ?: "Error restoring bike")
            }
        }
    }
}
