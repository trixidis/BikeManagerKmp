package com.bikemanager.domain.usecase.maintenance

import com.bikemanager.domain.repository.MaintenanceRepository
import com.bikemanager.util.currentTimeMillis

/**
 * Use case for marking a maintenance as done.
 */
class MarkMaintenanceDoneUseCase(
    private val repository: MaintenanceRepository
) {
    /**
     * Marks a maintenance as done with the given value.
     */
    suspend operator fun invoke(maintenanceId: String, bikeId: String, value: Float) {
        require(value >= 0) { "Value must be positive" }
        require(maintenanceId.isNotEmpty()) { "Maintenance id cannot be empty" }

        repository.markMaintenanceDone(
            id = maintenanceId,
            bikeId = bikeId,
            value = value,
            date = currentTimeMillis()
        )
    }
}
