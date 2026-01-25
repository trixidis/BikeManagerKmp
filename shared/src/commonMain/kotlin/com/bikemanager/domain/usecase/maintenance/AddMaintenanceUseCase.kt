package com.bikemanager.domain.usecase.maintenance

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.Result
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

    /**
     * Adds a done maintenance with current timestamp.
     */
    suspend fun addDone(name: String, value: Float, bikeId: String): Result<String> {
        if (name.isBlank()) {
            return Result.Failure(
                AppError.ValidationError(
                    errorMessage = "Maintenance name cannot be empty",
                    field = "name"
                )
            )
        }
        if (value < 0) {
            return Result.Failure(
                AppError.ValidationError(
                    errorMessage = "Value must be positive",
                    field = "value"
                )
            )
        }

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
    suspend fun addTodo(name: String, bikeId: String): Result<String> {
        if (name.isBlank()) {
            return Result.Failure(
                AppError.ValidationError(
                    errorMessage = "Maintenance name cannot be empty",
                    field = "name"
                )
            )
        }

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
