package com.bikemanager.domain.usecase.maintenance

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.MaintenanceRepository

/**
 * Use case for adding a new maintenance.
 *
 * Follows Single Responsibility Principle: validates and adds a maintenance.
 * The caller (ViewModel) is responsible for constructing the appropriate Maintenance object.
 */
class AddMaintenanceUseCase(
    private val repository: MaintenanceRepository
) {
    /**
     * Adds a new maintenance and returns its Firebase key.
     *
     * Validates that the maintenance name is not blank before adding.
     *
     * @param maintenance The maintenance to add (can be done or todo)
     * @return Result.Success with the Firebase key, or Result.Failure with validation error
     */
    suspend operator fun invoke(maintenance: Maintenance): Result<String> {
        if (maintenance.name.isBlank()) {
            return Result.Failure(
                AppError.ValidationError(
                    errorMessage = "Maintenance name cannot be empty",
                    field = "name"
                )
            )
        }
        return repository.addMaintenance(maintenance)
    }
}
