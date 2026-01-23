package com.bikemanager.fake

import com.bikemanager.domain.model.Bike
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake implementation of BikeLocalDataSource for testing.
 */
class FakeBikeLocalDataSource {
    private val bikesFlow = MutableStateFlow<List<Bike>>(emptyList())
    private var nextId = 1L

    fun getAllBikes(): Flow<List<Bike>> = bikesFlow

    suspend fun getBikeById(id: Long): Bike? {
        return bikesFlow.value.find { it.id == id }
    }

    suspend fun insertBike(bike: Bike): Long {
        val newBike = bike.copy(id = nextId++)
        bikesFlow.value = bikesFlow.value + newBike
        return newBike.id
    }

    suspend fun updateBike(bike: Bike) {
        bikesFlow.value = bikesFlow.value.map {
            if (it.id == bike.id) bike else it
        }
    }

    suspend fun deleteBike(id: Long) {
        bikesFlow.value = bikesFlow.value.filter { it.id != id }
    }

    // Test helpers
    fun setBikes(bikes: List<Bike>) {
        bikesFlow.value = bikes
        nextId = (bikes.maxOfOrNull { it.id } ?: 0L) + 1
    }

    fun getCurrentBikes(): List<Bike> = bikesFlow.value
}
