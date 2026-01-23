package com.bikemanager.presentation.bikes

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.usecase.bike.AddBikeUseCase
import com.bikemanager.domain.usecase.bike.GetBikesUseCase
import com.bikemanager.domain.usecase.bike.UpdateBikeUseCase
import com.bikemanager.domain.usecase.sync.PullFromCloudUseCase
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
 * ViewModel for managing bikes list screen.
 */
class BikesViewModel(
    private val getBikesUseCase: GetBikesUseCase,
    private val addBikeUseCase: AddBikeUseCase,
    private val updateBikeUseCase: UpdateBikeUseCase,
    private val pullFromCloudUseCase: PullFromCloudUseCase? = null
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow<BikesUiState>(BikesUiState.Loading)
    val uiState: StateFlow<BikesUiState> = _uiState.asStateFlow()

    init {
        pullFromCloudAndLoadBikes()
    }

    /**
     * Pulls data from cloud (if available) then loads bikes.
     */
    private fun pullFromCloudAndLoadBikes() {
        viewModelScope.launch {
            // First, pull from cloud if available
            pullFromCloudUseCase?.let { useCase ->
                try {
                    Napier.d("Pulling data from cloud...")
                    useCase()
                    Napier.d("Cloud pull completed")
                } catch (e: Exception) {
                    Napier.e(e) { "Error pulling from cloud, continuing with local data" }
                }
            }
            // Then load bikes (which will include any newly synced data)
            loadBikes()
        }
    }

    /**
     * Loads all bikes from the repository.
     */
    fun loadBikes() {
        viewModelScope.launch {
            _uiState.value = BikesUiState.Loading
            getBikesUseCase()
                .catch { throwable ->
                    Napier.e(throwable) { "Error loading bikes" }
                    _uiState.value = BikesUiState.Error(
                        throwable.message ?: "Unknown error occurred"
                    )
                }
                .collect { bikes ->
                    _uiState.value = if (bikes.isEmpty()) {
                        BikesUiState.Empty
                    } else {
                        BikesUiState.Success(bikes)
                    }
                }
        }
    }

    /**
     * Adds a new bike with the given name.
     * @param name The name of the bike to add
     */
    fun addBike(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            try {
                val bike = Bike(name = name)
                addBikeUseCase(bike)
            } catch (e: Exception) {
                Napier.e(e) { "Error adding bike" }
                _uiState.value = BikesUiState.Error(
                    e.message ?: "Error adding bike"
                )
            }
        }
    }

    /**
     * Updates an existing bike.
     * @param bike The bike to update with new values
     */
    fun updateBike(bike: Bike) {
        viewModelScope.launch {
            try {
                updateBikeUseCase(bike)
            } catch (e: Exception) {
                Napier.e(e) { "Error updating bike" }
                _uiState.value = BikesUiState.Error(
                    e.message ?: "Error updating bike"
                )
            }
        }
    }

    /**
     * Updates the name of a bike.
     * @param bikeId The id of the bike to update
     * @param newName The new name for the bike
     */
    fun updateBikeName(bikeId: Long, newName: String) {
        val currentState = _uiState.value
        if (currentState is BikesUiState.Success) {
            val bike = currentState.bikes.find { it.id == bikeId } ?: return
            updateBike(bike.copy(name = newName))
        }
    }

    /**
     * Updates the counting method of a bike.
     * @param bikeId The id of the bike to update
     * @param countingMethod The new counting method
     */
    fun updateBikeCountingMethod(bikeId: Long, countingMethod: CountingMethod) {
        val currentState = _uiState.value
        if (currentState is BikesUiState.Success) {
            val bike = currentState.bikes.find { it.id == bikeId } ?: return
            updateBike(bike.copy(countingMethod = countingMethod))
        }
    }
}
