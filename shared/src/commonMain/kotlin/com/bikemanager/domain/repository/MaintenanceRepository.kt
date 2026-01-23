package com.bikemanager.domain.repository

import com.bikemanager.domain.model.Maintenance
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for maintenance operations.
 * Abstracts the data layer from the domain layer.
 */
interface MaintenanceRepository {
    /**
     * Gets all maintenances for a bike as a Flow.
     */
    fun getMaintenancesByBikeId(bikeId: Long): Flow<List<Maintenance>>

    /**
     * Gets completed maintenances for a bike as a Flow.
     */
    fun getDoneMaintenances(bikeId: Long): Flow<List<Maintenance>>

    /**
     * Gets pending maintenances for a bike as a Flow.
     */
    fun getTodoMaintenances(bikeId: Long): Flow<List<Maintenance>>

    /**
     * Adds a new maintenance and returns its id.
     */
    suspend fun addMaintenance(maintenance: Maintenance): Long

    /**
     * Updates an existing maintenance.
     */
    suspend fun updateMaintenance(maintenance: Maintenance)

    /**
     * Marks a maintenance as done.
     */
    suspend fun markMaintenanceDone(id: Long, value: Float, date: Long)

    /**
     * Deletes a maintenance by its id.
     */
    suspend fun deleteMaintenance(id: Long)
}
