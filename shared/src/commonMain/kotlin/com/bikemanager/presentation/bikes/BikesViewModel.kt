package com.bikemanager.presentation.bikes

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.usecase.bike.AddBikeUseCase
import com.bikemanager.domain.usecase.bike.GetBikesUseCase
import com.bikemanager.domain.usecase.bike.UpdateBikeUseCase
import com.bikemanager.domain.usecase.sync.ObserveAndSyncFromCloudUseCase
import com.bikemanager.domain.usecase.sync.SyncResult
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
 * Uses a local-first approach: displays local data immediately while syncing with cloud in background.
 */
class BikesViewModel(
    private val getBikesUseCase: GetBikesUseCase,
    private val addBikeUseCase: AddBikeUseCase,
    private val updateBikeUseCase: UpdateBikeUseCase,
    private val observeAndSyncFromCloudUseCase: ObserveAndSyncFromCloudUseCase? = null
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow<BikesUiState>(BikesUiState.Loading)
    val uiState: StateFlow<BikesUiState> = _uiState.asStateFlow()

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)

    init {
        // Local-first: load local data immediately, then sync in background
        loadBikesLocalFirst()
    }

    /**
     * Local-first approach:
     * 1. Immediately loads bikes from local database (fast)
     * 2. Starts cloud sync in background (parallel)
     * 3. Local database updates trigger automatic UI refresh via Flow
     */
    private fun loadBikesLocalFirst() {
        // Start observing local bikes immediately
        loadBikes()

        // Start cloud sync in background (parallel, non-blocking)
        startBackgroundSync()
    }

    /**
     * Starts observing and syncing from cloud in background.
     * Updates sync status without blocking the UI.
     */
    private fun startBackgroundSync() {
        observeAndSyncFromCloudUseCase?.let { useCase ->
            viewModelScope.launch {
                useCase()
                    .catch { e ->
                        Napier.e(e) { "Error in background cloud sync" }
                        updateSyncStatus(SyncStatus.ERROR)
                    }
                    .collect { result ->
                        when (result) {
                            is SyncResult.Syncing -> {
                                Napier.d("Background cloud sync started...")
                                updateSyncStatus(SyncStatus.SYNCING)
                            }
                            is SyncResult.Success -> {
                                Napier.d("Background cloud sync completed")
                                updateSyncStatus(SyncStatus.SUCCESS)
                            }
                            is SyncResult.Error -> {
                                Napier.e { "Background sync error: ${result.message}" }
                                updateSyncStatus(SyncStatus.ERROR)
                            }
                            is SyncResult.NotConnected -> {
                                Napier.d("User not connected, sync skipped")
                                updateSyncStatus(SyncStatus.IDLE)
                            }
                        }
                    }
            }
        }
    }

    /**
     * Updates the sync status in the current UI state.
     */
    private fun updateSyncStatus(status: SyncStatus) {
        _syncStatus.value = status
        val currentState = _uiState.value
        _uiState.value = when (currentState) {
            is BikesUiState.Success -> currentState.copy(syncStatus = status)
            is BikesUiState.Empty -> currentState.copy(syncStatus = status)
            else -> currentState
        }
    }

    /**
     * Triggers a manual refresh: reloads local data and re-syncs from cloud.
     */
    fun refresh() {
        loadBikesLocalFirst()
    }

    /**
     * Loads all bikes from the local repository.
     * This is fast since it reads from local SQLite database.
     */
    fun loadBikes() {
        viewModelScope.launch {
            _uiState.value = BikesUiState.Loading
            getBikesUseCase()
                .catch { throwable ->
                    Napier.e(throwable) { "Error loading bikes from local database" }
                    _uiState.value = BikesUiState.Error(
                        throwable.message ?: "Unknown error occurred"
                    )
                }
                .collect { bikes ->
                    val currentSyncStatus = _syncStatus.value
                    _uiState.value = if (bikes.isEmpty()) {
                        BikesUiState.Empty(syncStatus = currentSyncStatus)
                    } else {
                        BikesUiState.Success(bikes, syncStatus = currentSyncStatus)
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
