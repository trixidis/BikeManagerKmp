package com.bikemanager.domain.usecase.maintenance

import com.bikemanager.domain.common.Result
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
    suspend operator fun invoke(maintenanceId: String, bikeId: String, value: Float): Result<Unit> {
        if (value < 0) {
            return Result.Failure(IllegalArgumentException("Value must be positive"))
        }
        if (maintenanceId.isEmpty()) {
            return Result.Failure(IllegalArgumentException("Maintenance id cannot be empty"))
        }

        return repository.markMaintenanceDone(
            id = maintenanceId,
            bikeId = bikeId,
            value = value,
            date = currentTimeMillis()
        )
    }
}
