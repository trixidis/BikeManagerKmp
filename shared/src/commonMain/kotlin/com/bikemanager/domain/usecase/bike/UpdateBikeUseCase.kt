package com.bikemanager.domain.usecase.bike

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.repository.BikeRepository
import com.bikemanager.domain.repository.SyncRepository

/**
 * Use case for updating an existing bike.
 * Supports optional Firebase sync when syncRepository is provided.
 */
class UpdateBikeUseCase(
    private val repository: BikeRepository,
    private val syncRepository: SyncRepository? = null
) {
    /**
     * Updates an existing bike.
     * If syncRepository is provided and user is connected, syncs to Firebase.
     *
     * @param bike The bike with updated values
     * @throws IllegalArgumentException if the bike name is empty
     */
    suspend operator fun invoke(bike: Bike) {
        require(bike.name.isNotBlank()) { "Bike name cannot be empty" }
        repository.updateBike(bike)

        // Sync to Firebase if connected
        syncRepository?.let { sync ->
            if (sync.isUserConnected() && bike.firebaseRef != null) {
                try {
                    sync.updateBikeInCloud(bike)
                } catch (e: Exception) {
                    // Offline-first: log but don't fail
                }
            }
        }
    }
}
