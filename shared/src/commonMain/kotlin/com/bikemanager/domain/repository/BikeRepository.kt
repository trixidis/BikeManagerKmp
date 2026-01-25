package com.bikemanager.domain.repository

import com.bikemanager.domain.model.Bike
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for bike operations.
 * Uses Firebase Realtime Database with offline persistence.
 */
interface BikeRepository {
    /**
     * Gets all bikes as a Flow (real-time updates).
     */
    fun getAllBikes(): Flow<List<Bike>>

    /**
     * Gets a bike by its id.
     */
    suspend fun getBikeById(id: String): Bike?

    /**
     * Adds a new bike and returns its Firebase key.
     */
    suspend fun addBike(bike: Bike): String

    /**
     * Updates an existing bike.
     */
    suspend fun updateBike(bike: Bike)

    /**
     * Deletes a bike by its id.
     */
    suspend fun deleteBike(id: String)
}
