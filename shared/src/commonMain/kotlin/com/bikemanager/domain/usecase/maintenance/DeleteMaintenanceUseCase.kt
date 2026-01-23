package com.bikemanager.domain.usecase.maintenance

import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.BikeRepository
import com.bikemanager.domain.repository.MaintenanceRepository
import com.bikemanager.domain.repository.SyncRepository

/**
 * Use case for deleting a maintenance.
 * Supports optional Firebase sync when syncRepository is provided.
 */
class DeleteMaintenanceUseCase(
    private val repository: MaintenanceRepository,
    private val bikeRepository: BikeRepository? = null,
    private val syncRepository: SyncRepository? = null
) {

    /**
     * Deletes a maintenance by its id.
     * @param maintenanceId The id of the maintenance to delete
     */
    suspend operator fun invoke(maintenanceId: Long) {
        repository.deleteMaintenance(maintenanceId)
    }

    /**
     * Deletes a maintenance and syncs the deletion to Firebase if connected.
     * @param maintenance The maintenance to delete
     */
    suspend fun deleteWithSync(maintenance: Maintenance) {
        repository.deleteMaintenance(maintenance.id)
        deleteFromCloudIfConnected(maintenance)
    }

    private suspend fun deleteFromCloudIfConnected(maintenance: Maintenance) {
        val sync = syncRepository ?: return
        val bikeRepo = bikeRepository ?: return

        if (!sync.isUserConnected()) return

        val maintenanceFirebaseRef = maintenance.firebaseRef ?: return
        val bike = bikeRepo.getBikeById(maintenance.bikeId) ?: return
        val bikeFirebaseRef = bike.firebaseRef ?: return

        try {
            sync.deleteMaintenanceFromCloud(bikeFirebaseRef, maintenanceFirebaseRef)
        } catch (e: Exception) {
            // Offline-first: log but don't fail
        }
    }
}
