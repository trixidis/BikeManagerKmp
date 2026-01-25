package com.bikemanager.domain.usecase.maintenance

import com.bikemanager.domain.repository.MaintenanceRepository

/**
 * Use case for deleting a maintenance.
 */
class DeleteMaintenanceUseCase(
    private val repository: MaintenanceRepository
) {
    /**
     * Deletes a maintenance.
     */
    suspend operator fun invoke(maintenanceId: String, bikeId: String) {
        require(maintenanceId.isNotEmpty()) { "Maintenance id cannot be empty" }
        repository.deleteMaintenance(maintenanceId, bikeId)
    }
}
