package com.bikemanager.data.repository

import com.bikemanager.data.local.BikeLocalDataSource
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.repository.BikeRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of BikeRepository.
 * Uses local data source for offline-first approach.
 */
class BikeRepositoryImpl(
    private val localDataSource: BikeLocalDataSource
) : BikeRepository {

    override fun getAllBikes(): Flow<List<Bike>> {
        return localDataSource.getAllBikes()
    }

    override suspend fun getBikeById(id: Long): Bike? {
        return localDataSource.getBikeById(id)
    }

    override suspend fun addBike(bike: Bike): Long {
        return localDataSource.insertBike(bike)
    }

    override suspend fun updateBike(bike: Bike) {
        localDataSource.updateBike(bike)
    }

    override suspend fun deleteBike(id: Long) {
        localDataSource.deleteBike(id)
    }
}
