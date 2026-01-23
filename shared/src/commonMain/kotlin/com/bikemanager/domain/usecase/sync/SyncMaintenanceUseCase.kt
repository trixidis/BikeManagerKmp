package com.bikemanager.domain.usecase.sync

import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.BikeRepository
import com.bikemanager.domain.repository.MaintenanceRepository
import com.bikemanager.domain.repository.SyncRepository

/**
 * Use case for syncing a maintenance to Firebase and updating the local reference.
 */
class SyncMaintenanceUseCase(
    private val maintenanceRepository: MaintenanceRepository,
    private val bikeRepository: BikeRepository,
    private val syncRepository: SyncRepository
) {
    /**
     * Syncs the maintenance to Firebase if user is connected.
     * Updates the local maintenance with the Firebase reference.
     *
     * @param maintenance The maintenance to sync
     * @return The updated maintenance with Firebase reference, or the original if not connected
     */
    suspend operator fun invoke(maintenance: Maintenance): Maintenance {
        if (!syncRepository.isUserConnected()) {
            return maintenance
        }

        val bike = bikeRepository.getBikeById(maintenance.bikeId)
            ?: return maintenance

        val bikeFirebaseRef = bike.firebaseRef
            ?: return maintenance // Bike must be synced first

        return try {
            val firebaseRef = syncRepository.syncMaintenance(maintenance, bikeFirebaseRef)
            val updatedMaintenance = maintenance.copy(firebaseRef = firebaseRef)
            maintenanceRepository.updateMaintenance(updatedMaintenance)
            updatedMaintenance
        } catch (e: Exception) {
            // Log error but don't fail - offline first approach
            maintenance
        }
    }
}
