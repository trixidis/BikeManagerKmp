package com.bikemanager.domain.repository

import com.bikemanager.domain.model.Bike
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for bike operations.
 * Abstracts the data layer from the domain layer.
 */
interface BikeRepository {
    /**
     * Gets all bikes as a Flow.
     */
    fun getAllBikes(): Flow<List<Bike>>

    /**
     * Gets a bike by its id.
     */
    suspend fun getBikeById(id: Long): Bike?

    /**
     * Adds a new bike and returns its id.
     */
    suspend fun addBike(bike: Bike): Long

    /**
     * Updates an existing bike.
     */
    suspend fun updateBike(bike: Bike)

    /**
     * Deletes a bike by its id.
     */
    suspend fun deleteBike(id: Long)
}
