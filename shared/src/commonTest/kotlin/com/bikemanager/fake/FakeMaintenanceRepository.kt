package com.bikemanager.fake

import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake implementation of MaintenanceRepository for testing.
 */
class FakeMaintenanceRepository : MaintenanceRepository {
    private val maintenancesFlow = MutableStateFlow<List<Maintenance>>(emptyList())
    private var nextId = 1L

    override fun getMaintenancesByBikeId(bikeId: Long): Flow<List<Maintenance>> {
        return maintenancesFlow.map { list ->
            list.filter { it.bikeId == bikeId }
        }
    }

    override fun getDoneMaintenances(bikeId: Long): Flow<List<Maintenance>> {
        return maintenancesFlow.map { list ->
            list.filter { it.bikeId == bikeId && it.isDone }
        }
    }

    override fun getTodoMaintenances(bikeId: Long): Flow<List<Maintenance>> {
        return maintenancesFlow.map { list ->
            list.filter { it.bikeId == bikeId && !it.isDone }
        }
    }

    override suspend fun addMaintenance(maintenance: Maintenance): Long {
        val newMaintenance = maintenance.copy(id = nextId++)
        maintenancesFlow.value = maintenancesFlow.value + newMaintenance
        return newMaintenance.id
    }

    override suspend fun updateMaintenance(maintenance: Maintenance) {
        maintenancesFlow.value = maintenancesFlow.value.map {
            if (it.id == maintenance.id) maintenance else it
        }
    }

    override suspend fun markMaintenanceDone(id: Long, value: Float, date: Long) {
        maintenancesFlow.value = maintenancesFlow.value.map {
            if (it.id == id) it.copy(isDone = true, value = value, date = date) else it
        }
    }

    override suspend fun deleteMaintenance(id: Long) {
        maintenancesFlow.value = maintenancesFlow.value.filter { it.id != id }
    }

    /**
     * Helper to set maintenances for testing.
     */
    fun setMaintenances(maintenances: List<Maintenance>) {
        maintenancesFlow.value = maintenances
        nextId = (maintenances.maxOfOrNull { it.id } ?: 0L) + 1
    }

    /**
     * Helper to get current maintenances for verification.
     */
    fun getCurrentMaintenances(): List<Maintenance> = maintenancesFlow.value
}
