package com.bikemanager.fake

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.repository.BikeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake implementation of BikeRepository for testing.
 */
class FakeBikeRepository : BikeRepository {
    private val bikesFlow = MutableStateFlow<List<Bike>>(emptyList())
    private var nextId = 1L

    override fun getAllBikes(): Flow<List<Bike>> = bikesFlow

    override suspend fun getBikeById(id: Long): Bike? {
        return bikesFlow.value.find { it.id == id }
    }

    override suspend fun addBike(bike: Bike): Long {
        val newBike = bike.copy(id = nextId++)
        bikesFlow.value = bikesFlow.value + newBike
        return newBike.id
    }

    override suspend fun updateBike(bike: Bike) {
        bikesFlow.value = bikesFlow.value.map {
            if (it.id == bike.id) bike else it
        }
    }

    override suspend fun deleteBike(id: Long) {
        bikesFlow.value = bikesFlow.value.filter { it.id != id }
    }

    /**
     * Helper to set bikes for testing.
     */
    fun setBikes(bikes: List<Bike>) {
        bikesFlow.value = bikes
        nextId = (bikes.maxOfOrNull { it.id } ?: 0L) + 1
    }

    /**
     * Helper to get current bikes for verification.
     */
    fun getCurrentBikes(): List<Bike> = bikesFlow.value
}
