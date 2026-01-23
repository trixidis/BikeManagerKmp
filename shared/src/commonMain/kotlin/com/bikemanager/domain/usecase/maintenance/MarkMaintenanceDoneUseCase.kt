package com.bikemanager.domain.usecase.maintenance

import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.BikeRepository
import com.bikemanager.domain.repository.MaintenanceRepository
import com.bikemanager.domain.repository.SyncRepository
import com.bikemanager.util.currentTimeMillis

/**
 * Use case for marking a maintenance as done.
 * Supports optional Firebase sync when syncRepository is provided.
 */
class MarkMaintenanceDoneUseCase(
    private val repository: MaintenanceRepository,
    private val bikeRepository: BikeRepository? = null,
    private val syncRepository: SyncRepository? = null
) {

    /**
     * Marks a maintenance as done with the given value.
     * If syncRepository is provided and user is connected, syncs to Firebase.
     *
     * @param maintenanceId The id of the maintenance to mark as done
     * @param value The km/hours value at which the maintenance was done
     * @throws IllegalArgumentException if the value is negative
     */
    suspend operator fun invoke(maintenanceId: Long, value: Float) {
        require(value >= 0) { "Value must be positive" }

        val date = currentTimeMillis()
        repository.markMaintenanceDone(
            id = maintenanceId,
            value = value,
            date = date
        )

        // Note: For sync with full object, use markDoneWithSync() instead
    }

    /**
     * Marks a maintenance as done and syncs to Firebase.
     * @param maintenance The maintenance to mark as done
     * @param value The km/hours value
     */
    suspend fun markDoneWithSync(maintenance: Maintenance, value: Float) {
        require(value >= 0) { "Value must be positive" }

        val date = currentTimeMillis()
        repository.markMaintenanceDone(
            id = maintenance.id,
            value = value,
            date = date
        )

        syncMaintenanceIfConnected(
            maintenance.copy(
                isDone = true,
                value = value,
                date = date
            )
        )
    }

    private suspend fun syncMaintenanceIfConnected(maintenance: Maintenance) {
        val sync = syncRepository ?: return
        val bikeRepo = bikeRepository ?: return

        if (!sync.isUserConnected()) return

        val bike = bikeRepo.getBikeById(maintenance.bikeId) ?: return
        val bikeFirebaseRef = bike.firebaseRef ?: return
        if (maintenance.firebaseRef == null) return

        try {
            sync.updateMaintenanceInCloud(maintenance, bikeFirebaseRef)
        } catch (e: Exception) {
            // Offline-first: log but don't fail
        }
    }
}
