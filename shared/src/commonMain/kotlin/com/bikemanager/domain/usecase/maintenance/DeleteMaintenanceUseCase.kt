package com.bikemanager.domain.usecase.maintenance

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.Result
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
    suspend operator fun invoke(maintenanceId: String, bikeId: String): Result<Unit> {
        if (maintenanceId.isEmpty()) {
            return Result.Failure(
                AppError.ValidationError(
                    errorMessage = "Maintenance id cannot be empty",
                    field = "id"
                )
            )
        }
        return repository.deleteMaintenance(maintenanceId, bikeId)
    }
}
