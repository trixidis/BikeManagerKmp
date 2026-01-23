package com.bikemanager.fake

import com.bikemanager.domain.model.Maintenance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake implementation of MaintenanceLocalDataSource for testing.
 */
class FakeMaintenanceLocalDataSource {
    private val maintenancesFlow = MutableStateFlow<List<Maintenance>>(emptyList())
    private var nextId = 1L

    fun getMaintenancesByBikeId(bikeId: Long): Flow<List<Maintenance>> {
        return maintenancesFlow.map { maintenances ->
            maintenances.filter { it.bikeId == bikeId }
        }
    }

    fun getDoneMaintenances(bikeId: Long): Flow<List<Maintenance>> {
        return maintenancesFlow.map { maintenances ->
            maintenances.filter { it.bikeId == bikeId && it.isDone }
        }
    }

    fun getTodoMaintenances(bikeId: Long): Flow<List<Maintenance>> {
        return maintenancesFlow.map { maintenances ->
            maintenances.filter { it.bikeId == bikeId && !it.isDone }
        }
    }

    suspend fun insertMaintenance(maintenance: Maintenance): Long {
        val newMaintenance = maintenance.copy(id = nextId++)
        maintenancesFlow.value = maintenancesFlow.value + newMaintenance
        return newMaintenance.id
    }

    suspend fun updateMaintenance(maintenance: Maintenance) {
        maintenancesFlow.value = maintenancesFlow.value.map {
            if (it.id == maintenance.id) maintenance else it
        }
    }

    suspend fun markMaintenanceDone(id: Long, value: Float, date: Long) {
        maintenancesFlow.value = maintenancesFlow.value.map {
            if (it.id == id) it.copy(isDone = true, value = value, date = date) else it
        }
    }

    suspend fun deleteMaintenance(id: Long) {
        maintenancesFlow.value = maintenancesFlow.value.filter { it.id != id }
    }

    // Test helpers
    fun setMaintenances(maintenances: List<Maintenance>) {
        maintenancesFlow.value = maintenances
        nextId = (maintenances.maxOfOrNull { it.id } ?: 0L) + 1
    }

    fun getCurrentMaintenances(): List<Maintenance> = maintenancesFlow.value
}
