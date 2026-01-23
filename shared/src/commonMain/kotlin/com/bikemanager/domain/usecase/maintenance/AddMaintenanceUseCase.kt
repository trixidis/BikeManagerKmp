package com.bikemanager.domain.usecase.maintenance

import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.BikeRepository
import com.bikemanager.domain.repository.MaintenanceRepository
import com.bikemanager.domain.repository.SyncRepository
import com.bikemanager.util.currentTimeMillis

/**
 * Use case for adding a new maintenance.
 * Supports optional Firebase sync when syncRepository is provided.
 */
class AddMaintenanceUseCase(
    private val repository: MaintenanceRepository,
    private val bikeRepository: BikeRepository? = null,
    private val syncRepository: SyncRepository? = null
) {

    /**
     * Adds a new maintenance and returns its id.
     * If syncRepository is provided and user is connected, syncs to Firebase.
     *
     * @param maintenance The maintenance to add
     * @return The id of the newly created maintenance
     * @throws IllegalArgumentException if the maintenance name is empty
     */
    suspend operator fun invoke(maintenance: Maintenance): Long {
        require(maintenance.name.isNotBlank()) { "Maintenance name cannot be empty" }
        val maintenanceId = repository.addMaintenance(maintenance)
        syncMaintenanceIfConnected(maintenance.copy(id = maintenanceId))
        return maintenanceId
    }

    /**
     * Adds a done maintenance with current timestamp.
     * @param name The name/type of maintenance
     * @param value The km/hours value
     * @param bikeId The id of the bike
     * @return The id of the newly created maintenance
     */
    suspend fun addDone(name: String, value: Float, bikeId: Long): Long {
        require(name.isNotBlank()) { "Maintenance name cannot be empty" }
        require(value >= 0) { "Value must be positive" }

        val maintenance = Maintenance(
            name = name,
            value = value,
            date = currentTimeMillis(),
            isDone = true,
            bikeId = bikeId
        )
        val maintenanceId = repository.addMaintenance(maintenance)
        syncMaintenanceIfConnected(maintenance.copy(id = maintenanceId))
        return maintenanceId
    }

    /**
     * Adds a todo maintenance (not done yet).
     * @param name The name/type of maintenance
     * @param bikeId The id of the bike
     * @return The id of the newly created maintenance
     */
    suspend fun addTodo(name: String, bikeId: Long): Long {
        require(name.isNotBlank()) { "Maintenance name cannot be empty" }

        val maintenance = Maintenance(
            name = name,
            value = -1f,
            date = 0,
            isDone = false,
            bikeId = bikeId
        )
        val maintenanceId = repository.addMaintenance(maintenance)
        syncMaintenanceIfConnected(maintenance.copy(id = maintenanceId))
        return maintenanceId
    }

    private suspend fun syncMaintenanceIfConnected(maintenance: Maintenance) {
        val sync = syncRepository ?: return
        val bikeRepo = bikeRepository ?: return

        if (!sync.isUserConnected()) return

        val bike = bikeRepo.getBikeById(maintenance.bikeId) ?: return
        val bikeFirebaseRef = bike.firebaseRef ?: return

        try {
            val firebaseRef = sync.syncMaintenance(maintenance, bikeFirebaseRef)
            repository.updateMaintenance(maintenance.copy(firebaseRef = firebaseRef))
        } catch (e: Exception) {
            // Offline-first: log but don't fail
        }
    }
}
