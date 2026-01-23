package com.bikemanager.domain.usecase.bike

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.repository.BikeRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting all bikes.
 */
class GetBikesUseCase(private val repository: BikeRepository) {
    /**
     * Gets all bikes as a Flow.
     */
    operator fun invoke(): Flow<List<Bike>> {
        return repository.getAllBikes()
    }
}
