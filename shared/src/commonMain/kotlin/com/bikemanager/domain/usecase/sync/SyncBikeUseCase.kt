package com.bikemanager.domain.usecase.sync

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.repository.BikeRepository
import com.bikemanager.domain.repository.SyncRepository

/**
 * Use case for syncing a bike to Firebase and updating the local reference.
 */
class SyncBikeUseCase(
    private val bikeRepository: BikeRepository,
    private val syncRepository: SyncRepository
) {
    /**
     * Syncs the bike to Firebase if user is connected.
     * Updates the local bike with the Firebase reference.
     *
     * @param bike The bike to sync
     * @return The updated bike with Firebase reference, or the original if not connected
     */
    suspend operator fun invoke(bike: Bike): Bike {
        if (!syncRepository.isUserConnected()) {
            return bike
        }

        return try {
            val firebaseRef = syncRepository.syncBike(bike)
            val updatedBike = bike.copy(firebaseRef = firebaseRef)
            bikeRepository.updateBike(updatedBike)
            updatedBike
        } catch (e: Exception) {
            // Log error but don't fail - offline first approach
            bike
        }
    }
}
