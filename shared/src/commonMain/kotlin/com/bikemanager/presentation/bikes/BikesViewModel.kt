package com.bikemanager.presentation.bikes

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.ErrorHandler
import com.bikemanager.domain.common.ErrorMessages
import com.bikemanager.domain.common.fold
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.usecase.bike.AddBikeUseCase
import com.bikemanager.domain.usecase.bike.GetBikesUseCase
import com.bikemanager.domain.usecase.bike.UpdateBikeUseCase
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
 * ViewModel for managing bikes list screen.
 * Uses Firebase Realtime Database with offline persistence.
 */
class BikesViewModel(
    private val getBikesUseCase: GetBikesUseCase,
    private val addBikeUseCase: AddBikeUseCase,
    private val updateBikeUseCase: UpdateBikeUseCase
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow<BikesUiState>(BikesUiState.Loading)
    val uiState: StateFlow<BikesUiState> = _uiState.asStateFlow()

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
                    // CRITICAL: Ensure CancellationException is re-thrown for proper coroutine cleanup
                    coroutineContext.ensureActive()

                    Napier.e(throwable) { "Error observing bikes" }
                    val appError = if (throwable is AppError) {
                        throwable
                    } else {
                        ErrorHandler.handle(throwable, "observing bikes")
                    }
                    _uiState.value = BikesUiState.Error(
                        ErrorMessages.getMessage(appError)
                    )
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { bikes ->
                            _uiState.value = if (bikes.isEmpty()) {
                                BikesUiState.Empty()
                            } else {
                                BikesUiState.Success(bikes)
                            }
                        },
                        onFailure = { error ->
                            Napier.e(error) { "Error loading bikes" }
                            val appError = if (error is AppError) {
                                error
                            } else {
                                ErrorHandler.handle(error, "loading bikes")
                            }
                            _uiState.value = BikesUiState.Error(
                                ErrorMessages.getMessage(appError)
                            )
                        }
                    )
                }
        }
    }

    /**
     * Adds a new bike with the given name.
     */
    fun addBike(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            val result = addBikeUseCase(Bike(name = name))
            result.fold(
                onSuccess = { bikeId ->
                    Napier.d { "Bike added successfully: $bikeId" }
                },
                onFailure = { error ->
                    Napier.e(error) { "Error adding bike" }
                    val appError = if (error is AppError) {
                        error
                    } else {
                        ErrorHandler.handle(error, "adding bike")
                    }
                    _uiState.value = BikesUiState.Error(
                        ErrorMessages.getMessage(appError)
                    )
                }
            )
        }
    }

    /**
     * Updates an existing bike.
     */
    fun updateBike(bike: Bike) {
        viewModelScope.launch {
            val result = updateBikeUseCase(bike)
            result.fold(
                onSuccess = {
                    Napier.d { "Bike updated successfully: ${bike.name}" }
                },
                onFailure = { error ->
                    Napier.e(error) { "Error updating bike" }
                    val appError = if (error is AppError) {
                        error
                    } else {
                        ErrorHandler.handle(error, "updating bike")
                    }
                    _uiState.value = BikesUiState.Error(
                        ErrorMessages.getMessage(appError)
                    )
                }
            )
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
}
