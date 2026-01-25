package com.bikemanager.fake

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.Result
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
    private var nextId = 1
    private var shouldFailOnGetAll = false
    private var getAllError: Throwable? = null
    private var shouldFailOnGetById = false
    private var getByIdError: Throwable? = null
    private var shouldFailOnAdd = false
    private var addError: Throwable? = null
    private var shouldFailOnUpdate = false
    private var updateError: Throwable? = null
    private var shouldFailOnDelete = false
    private var deleteError: Throwable? = null

    override fun getAllBikes(): Flow<Result<List<Bike>>> = bikesFlow.map { bikes ->
        if (shouldFailOnGetAll) {
            Result.Failure(getAllError ?: AppError.DatabaseError("Failed to get bikes"))
        } else {
            Result.Success(bikes)
        }
    }

    override suspend fun getBikeById(id: String): Result<Bike?> {
        if (shouldFailOnGetById) {
            return Result.Failure(getByIdError ?: AppError.DatabaseError("Failed to get bike"))
        }
        return Result.Success(bikesFlow.value.find { it.id == id })
    }

    override suspend fun addBike(bike: Bike): Result<String> {
        if (shouldFailOnAdd) {
            return Result.Failure(addError ?: AppError.DatabaseError("Failed to add bike"))
        }
        val newId = "bike_${nextId++}"
        val newBike = bike.copy(id = newId)
        bikesFlow.value = bikesFlow.value + newBike
        return Result.Success(newId)
    }

    override suspend fun updateBike(bike: Bike): Result<Unit> {
        if (shouldFailOnUpdate) {
            return Result.Failure(updateError ?: AppError.DatabaseError("Failed to update bike"))
        }
        bikesFlow.value = bikesFlow.value.map {
            if (it.id == bike.id) bike else it
        }
        return Result.Success(Unit)
    }

    override suspend fun deleteBike(id: String): Result<Unit> {
        if (shouldFailOnDelete) {
            return Result.Failure(deleteError ?: AppError.DatabaseError("Failed to delete bike"))
        }
        bikesFlow.value = bikesFlow.value.filter { it.id != id }
        return Result.Success(Unit)
    }

    /**
     * Helper to set bikes for testing.
     */
    fun setBikes(bikes: List<Bike>) {
        bikesFlow.value = bikes
    }

    /**
     * Helper to get current bikes for verification.
     */
    fun getCurrentBikes(): List<Bike> = bikesFlow.value

    /**
     * Helper to make getAllBikes return a failure.
     */
    fun setGetAllFails(shouldFail: Boolean, error: Throwable? = null) {
        shouldFailOnGetAll = shouldFail
        getAllError = error
    }

    /**
     * Helper to make getBikeById return a failure.
     */
    fun setGetByIdFails(shouldFail: Boolean, error: Throwable? = null) {
        shouldFailOnGetById = shouldFail
        getByIdError = error
    }

    /**
     * Helper to make addBike return a failure.
     */
    fun setAddFails(shouldFail: Boolean, error: Throwable? = null) {
        shouldFailOnAdd = shouldFail
        addError = error
    }

    /**
     * Helper to make updateBike return a failure.
     */
    fun setUpdateFails(shouldFail: Boolean, error: Throwable? = null) {
        shouldFailOnUpdate = shouldFail
        updateError = error
    }

    /**
     * Helper to make deleteBike return a failure.
     */
    fun setDeleteFails(shouldFail: Boolean, error: Throwable? = null) {
        shouldFailOnDelete = shouldFail
        deleteError = error
    }
}
