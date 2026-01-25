package com.bikemanager.domain.usecase.bike

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.repository.BikeRepository

/**
 * Use case for updating an existing bike.
 */
class UpdateBikeUseCase(
    private val repository: BikeRepository
) {
    /**
     * Updates an existing bike.
     */
    suspend operator fun invoke(bike: Bike): Result<Unit> {
        if (bike.name.isBlank()) {
            return Result.Failure(
                AppError.ValidationError(
                    errorMessage = "Bike name cannot be empty",
                    field = "name"
                )
            )
        }
        if (bike.id.isEmpty()) {
            return Result.Failure(
                AppError.ValidationError(
                    errorMessage = "Bike id cannot be empty",
                    field = "id"
                )
            )
        }
        return repository.updateBike(bike)
    }
}
