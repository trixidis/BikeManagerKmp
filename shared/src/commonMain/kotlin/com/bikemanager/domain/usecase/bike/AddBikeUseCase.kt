package com.bikemanager.domain.usecase.bike

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
    suspend operator fun invoke(bike: Bike): String {
        require(bike.name.isNotBlank()) { "Bike name cannot be empty" }
        return repository.addBike(bike)
    }
}
