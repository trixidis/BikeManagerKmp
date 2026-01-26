package com.bikemanager.domain.repository

import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.Maintenance
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for maintenance operations.
 * Uses Firebase Realtime Database with offline persistence.
 */
interface MaintenanceRepository {
    /**
     * Gets all maintenances for a bike as a Flow (real-time updates).
     */
    fun getMaintenancesByBikeId(bikeId: String): Flow<Result<List<Maintenance>>>

    /**
     * Gets completed maintenances for a bike as a Flow.
     */
    fun getDoneMaintenances(bikeId: String): Flow<Result<List<Maintenance>>>

    /**
     * Gets pending maintenances for a bike as a Flow.
     */
    fun getTodoMaintenances(bikeId: String): Flow<Result<List<Maintenance>>>

    /**
     * Adds a new maintenance and returns its Firebase key.
     */
    suspend fun addMaintenance(maintenance: Maintenance): Result<String>

    /**
     * Updates an existing maintenance.
     */
    suspend fun updateMaintenance(maintenance: Maintenance): Result<Unit>

    /**
     * Marks a maintenance as done.
     */
    suspend fun markMaintenanceDone(id: String, bikeId: String, value: Float, date: Long): Result<Unit>

    /**
     * Deletes a maintenance.
     */
    suspend fun deleteMaintenance(id: String, bikeId: String): Result<Unit>

    /**
     * Deletes all maintenances for a bike (cascade delete).
     */
    suspend fun deleteAllMaintenancesForBike(bikeId: String): Result<Unit>
}
