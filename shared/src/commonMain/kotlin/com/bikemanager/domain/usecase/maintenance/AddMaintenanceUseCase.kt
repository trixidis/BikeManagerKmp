package com.bikemanager.domain.usecase.maintenance

import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.MaintenanceRepository
import com.bikemanager.util.currentTimeMillis

/**
 * Use case for adding a new maintenance.
 */
class AddMaintenanceUseCase(
    private val repository: MaintenanceRepository
) {
    /**
     * Adds a new maintenance and returns its Firebase key.
     */
    suspend operator fun invoke(maintenance: Maintenance): String {
        require(maintenance.name.isNotBlank()) { "Maintenance name cannot be empty" }
        return repository.addMaintenance(maintenance)
    }

    /**
     * Adds a done maintenance with current timestamp.
     */
    suspend fun addDone(name: String, value: Float, bikeId: String): String {
        require(name.isNotBlank()) { "Maintenance name cannot be empty" }
        require(value >= 0) { "Value must be positive" }

        val maintenance = Maintenance(
            name = name,
            value = value,
            date = currentTimeMillis(),
            isDone = true,
            bikeId = bikeId
        )
        return repository.addMaintenance(maintenance)
    }

    /**
     * Adds a todo maintenance (not done yet).
     */
    suspend fun addTodo(name: String, bikeId: String): String {
        require(name.isNotBlank()) { "Maintenance name cannot be empty" }

        val maintenance = Maintenance(
            name = name,
            value = -1f,
            date = 0,
            isDone = false,
            bikeId = bikeId
        )
        return repository.addMaintenance(maintenance)
    }
}
