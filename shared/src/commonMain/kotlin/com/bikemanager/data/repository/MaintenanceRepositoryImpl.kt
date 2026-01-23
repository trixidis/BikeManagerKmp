package com.bikemanager.data.repository

import com.bikemanager.data.local.MaintenanceLocalDataSource
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of MaintenanceRepository.
 * Uses local data source for offline-first approach.
 */
class MaintenanceRepositoryImpl(
    private val localDataSource: MaintenanceLocalDataSource
) : MaintenanceRepository {

    override fun getMaintenancesByBikeId(bikeId: Long): Flow<List<Maintenance>> {
        return localDataSource.getMaintenancesByBikeId(bikeId)
    }

    override fun getDoneMaintenances(bikeId: Long): Flow<List<Maintenance>> {
        return localDataSource.getDoneMaintenances(bikeId)
    }

    override fun getTodoMaintenances(bikeId: Long): Flow<List<Maintenance>> {
        return localDataSource.getTodoMaintenances(bikeId)
    }

    override suspend fun addMaintenance(maintenance: Maintenance): Long {
        return localDataSource.insertMaintenance(maintenance)
    }

    override suspend fun updateMaintenance(maintenance: Maintenance) {
        localDataSource.updateMaintenance(maintenance)
    }

    override suspend fun markMaintenanceDone(id: Long, value: Float, date: Long) {
        localDataSource.markMaintenanceDone(id, value, date)
    }

    override suspend fun deleteMaintenance(id: Long) {
        localDataSource.deleteMaintenance(id)
    }
}
