package com.bikemanager.domain.usecase.bike

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.repository.BikeRepository

/**
 * Use case for adding a new bike.
 */
class AddBikeUseCase(
    private val repository: BikeRepository
) {
    /**
     * Adds a new bike and returns its Firebase key.
     */
    suspend operator fun invoke(bike: Bike): Result<String> {
        if (bike.name.isBlank()) {
            return Result.Failure(
                AppError.ValidationError(
                    errorMessage = "Bike name cannot be empty",
                    field = "name"
                )
            )
        }
        return repository.addBike(bike)
    }
}
