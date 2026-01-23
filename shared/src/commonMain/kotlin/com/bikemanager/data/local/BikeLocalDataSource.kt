package com.bikemanager.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.bikemanager.Bike as SqlDelightBike
import com.bikemanager.domain.model.Bike as DomainBike
import com.bikemanager.domain.model.CountingMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Local data source for bike operations using SQLDelight.
 */
class BikeLocalDataSource(private val database: BikeManagerDatabase) {

    private val queries = database.databaseQueries

    /**
     * Gets all bikes as a Flow, ordered by id descending.
     */
    fun getAllBikes(): Flow<List<DomainBike>> {
        return queries.getAllBikes()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { bikes -> bikes.map { it.toDomainModel() } }
    }

    /**
     * Gets a bike by its id.
     */
    suspend fun getBikeById(id: Long): DomainBike? = withContext(Dispatchers.IO) {
        queries.getBikeById(id).executeAsOneOrNull()?.toDomainModel()
    }

    /**
     * Inserts a new bike and returns its id.
     */
    suspend fun insertBike(bike: DomainBike): Long = withContext(Dispatchers.IO) {
        queries.insertBike(
            name = bike.name,
            countingMethod = bike.countingMethod.name,
            firebaseRef = bike.firebaseRef
        )
        queries.getLastInsertedBikeId().executeAsOne()
    }

    /**
     * Updates an existing bike.
     */
    suspend fun updateBike(bike: DomainBike) = withContext(Dispatchers.IO) {
        queries.updateBike(
            name = bike.name,
            countingMethod = bike.countingMethod.name,
            firebaseRef = bike.firebaseRef,
            id = bike.id
        )
    }

    /**
     * Deletes a bike by its id.
     */
    suspend fun deleteBike(id: Long) = withContext(Dispatchers.IO) {
        queries.deleteBike(id)
    }

    private fun SqlDelightBike.toDomainModel(): DomainBike {
        return DomainBike(
            id = this.id,
            name = this.name,
            countingMethod = CountingMethod.valueOf(this.countingMethod),
            firebaseRef = this.firebaseRef
        )
    }
}
