package com.bikemanager.domain.usecase.bike

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.repository.BikeRepository
import com.bikemanager.domain.repository.SyncRepository

/**
 * Use case for adding a new bike.
 * Supports optional Firebase sync when syncRepository is provided.
 */
class AddBikeUseCase(
    private val repository: BikeRepository,
    private val syncRepository: SyncRepository? = null
) {
    /**
     * Adds a new bike and returns its id.
     * If syncRepository is provided and user is connected, syncs to Firebase.
     *
     * @param bike The bike to add
     * @return The id of the newly created bike
     * @throws IllegalArgumentException if the bike name is empty
     */
    suspend operator fun invoke(bike: Bike): Long {
        require(bike.name.isNotBlank()) { "Bike name cannot be empty" }

        val bikeId = repository.addBike(bike)

        // Sync to Firebase if connected
        syncRepository?.let { sync ->
            if (sync.isUserConnected()) {
                try {
                    val bikeWithId = bike.copy(id = bikeId)
                    val firebaseRef = sync.syncBike(bikeWithId)
                    repository.updateBike(bikeWithId.copy(firebaseRef = firebaseRef))
                } catch (e: Exception) {
                    // Offline-first: log but don't fail
                }
            }
        }

        return bikeId
    }
}
