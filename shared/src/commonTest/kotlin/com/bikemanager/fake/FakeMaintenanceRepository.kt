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
    private var nextId = 1

    override fun getMaintenancesByBikeId(bikeId: String): Flow<List<Maintenance>> {
        return maintenancesFlow.map { list ->
            list.filter { it.bikeId == bikeId }
        }
    }

    override fun getDoneMaintenances(bikeId: String): Flow<List<Maintenance>> {
        return maintenancesFlow.map { list ->
            list.filter { it.bikeId == bikeId && it.isDone }
        }
    }

    override fun getTodoMaintenances(bikeId: String): Flow<List<Maintenance>> {
        return maintenancesFlow.map { list ->
            list.filter { it.bikeId == bikeId && !it.isDone }
        }
    }

    override suspend fun addMaintenance(maintenance: Maintenance): String {
        val newId = "maintenance_${nextId++}"
        val newMaintenance = maintenance.copy(id = newId)
        maintenancesFlow.value = maintenancesFlow.value + newMaintenance
        return newId
    }

    override suspend fun updateMaintenance(maintenance: Maintenance) {
        maintenancesFlow.value = maintenancesFlow.value.map {
            if (it.id == maintenance.id) maintenance else it
        }
    }

    override suspend fun markMaintenanceDone(id: String, bikeId: String, value: Float, date: Long) {
        maintenancesFlow.value = maintenancesFlow.value.map {
            if (it.id == id && it.bikeId == bikeId) it.copy(isDone = true, value = value, date = date) else it
        }
    }

    override suspend fun deleteMaintenance(id: String, bikeId: String) {
        maintenancesFlow.value = maintenancesFlow.value.filter { !(it.id == id && it.bikeId == bikeId) }
    }

    override suspend fun deleteAllMaintenancesForBike(bikeId: String) {
        maintenancesFlow.value = maintenancesFlow.value.filter { it.bikeId != bikeId }
    }

    /**
     * Helper to set maintenances for testing.
     */
    fun setMaintenances(maintenances: List<Maintenance>) {
        maintenancesFlow.value = maintenances
    }

    /**
     * Helper to get current maintenances for verification.
     */
    fun getCurrentMaintenances(): List<Maintenance> = maintenancesFlow.value
}
