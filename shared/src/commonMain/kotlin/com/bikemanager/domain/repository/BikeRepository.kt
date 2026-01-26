package com.bikemanager.domain.repository

import com.bikemanager.domain.common.Result
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
    fun getAllBikes(): Flow<Result<List<Bike>>>

    /**
     * Gets a bike by its id.
     */
    suspend fun getBikeById(id: String): Result<Bike?>

    /**
     * Adds a new bike and returns its Firebase key.
     */
    suspend fun addBike(bike: Bike): Result<String>

    /**
     * Updates an existing bike.
     */
    suspend fun updateBike(bike: Bike): Result<Unit>

    /**
     * Deletes a bike by its id.
     */
    suspend fun deleteBike(id: String): Result<Unit>
}
