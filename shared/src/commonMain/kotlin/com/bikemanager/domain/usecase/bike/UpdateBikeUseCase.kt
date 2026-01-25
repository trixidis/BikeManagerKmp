package com.bikemanager.domain.usecase.bike

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
    suspend operator fun invoke(bike: Bike) {
        require(bike.name.isNotBlank()) { "Bike name cannot be empty" }
        require(bike.id.isNotEmpty()) { "Bike id cannot be empty" }
        repository.updateBike(bike)
    }
}
