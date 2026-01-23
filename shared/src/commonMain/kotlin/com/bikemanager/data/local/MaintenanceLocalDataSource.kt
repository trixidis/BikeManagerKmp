package com.bikemanager.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.bikemanager.Maintenance as SqlDelightMaintenance
import com.bikemanager.domain.model.Maintenance as DomainMaintenance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Local data source for maintenance operations using SQLDelight.
 */
class MaintenanceLocalDataSource(private val database: BikeManagerDatabase) {

    private val queries = database.databaseQueries

    /**
     * Gets all maintenances for a bike as a Flow.
     */
    fun getMaintenancesByBikeId(bikeId: Long): Flow<List<DomainMaintenance>> {
        return queries.getMaintenancesByBikeId(bikeId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { maintenances -> maintenances.map { it.toDomainModel() } }
    }

    /**
     * Gets done maintenances for a bike as a Flow.
     */
    fun getDoneMaintenances(bikeId: Long): Flow<List<DomainMaintenance>> {
        return queries.getDoneMaintenances(bikeId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { maintenances -> maintenances.map { it.toDomainModel() } }
    }

    /**
     * Gets todo maintenances for a bike as a Flow.
     */
    fun getTodoMaintenances(bikeId: Long): Flow<List<DomainMaintenance>> {
        return queries.getTodoMaintenances(bikeId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { maintenances -> maintenances.map { it.toDomainModel() } }
    }

    /**
     * Inserts a new maintenance and returns its id.
     */
    suspend fun insertMaintenance(maintenance: DomainMaintenance): Long = withContext(Dispatchers.IO) {
        queries.insertMaintenance(
            name = maintenance.name,
            value_ = maintenance.value.toDouble(),
            date = maintenance.date,
            isDone = if (maintenance.isDone) 1L else 0L,
            bikeId = maintenance.bikeId,
            firebaseRef = maintenance.firebaseRef
        )
        queries.getLastInsertedMaintenanceId().executeAsOne()
    }

    /**
     * Updates an existing maintenance.
     */
    suspend fun updateMaintenance(maintenance: DomainMaintenance) = withContext(Dispatchers.IO) {
        queries.updateMaintenance(
            name = maintenance.name,
            value_ = maintenance.value.toDouble(),
            date = maintenance.date,
            isDone = if (maintenance.isDone) 1L else 0L,
            firebaseRef = maintenance.firebaseRef,
            id = maintenance.id
        )
    }

    /**
     * Marks a maintenance as done with the given value and current timestamp.
     */
    suspend fun markMaintenanceDone(
        id: Long,
        value: Float,
        date: Long
    ) = withContext(Dispatchers.IO) {
        queries.markMaintenanceDone(
            value_ = value.toDouble(),
            date = date,
            id = id
        )
    }

    /**
     * Deletes a maintenance by its id.
     */
    suspend fun deleteMaintenance(id: Long) = withContext(Dispatchers.IO) {
        queries.deleteMaintenance(id)
    }

    private fun SqlDelightMaintenance.toDomainModel(): DomainMaintenance {
        return DomainMaintenance(
            id = this.id,
            name = this.name,
            value = this.value_.toFloat(),
            date = this.date,
            isDone = this.isDone == 1L,
            bikeId = this.bikeId,
            firebaseRef = this.firebaseRef
        )
    }
}
