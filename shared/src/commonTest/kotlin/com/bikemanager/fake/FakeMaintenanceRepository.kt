package com.bikemanager.fake

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake implementation of MaintenanceRepository for testing.
 */
class FakeMaintenanceRepository : MaintenanceRepository {
    private val maintenancesFlow = MutableStateFlow<List<Maintenance>>(emptyList())
    private var nextId = 1
    private var shouldFailOnGetByBikeId = false
    private var getByBikeIdError: AppError? = null
    private var shouldFailOnGetDone = false
    private var getDoneError: AppError? = null
    private var shouldFailOnGetTodo = false
    private var getTodoError: AppError? = null
    private var shouldFailOnAdd = false
    private var addError: AppError? = null
    private var shouldFailOnUpdate = false
    private var updateError: AppError? = null
    private var shouldFailOnMarkDone = false
    private var markDoneError: AppError? = null
    private var shouldFailOnDelete = false
    private var deleteError: AppError? = null

    override fun getMaintenancesByBikeId(bikeId: String): Flow<Result<List<Maintenance>>> {
        return maintenancesFlow.map { list ->
            if (shouldFailOnGetByBikeId) {
                Result.Failure(getByBikeIdError ?: AppError.DatabaseError("Failed to get maintenances"))
            } else {
                Result.Success(list.filter { it.bikeId == bikeId })
            }
        }
    }

    override fun getDoneMaintenances(bikeId: String): Flow<Result<List<Maintenance>>> {
        return maintenancesFlow.map { list ->
            if (shouldFailOnGetDone) {
                Result.Failure(getDoneError ?: AppError.DatabaseError("Failed to get done maintenances"))
            } else {
                Result.Success(list.filter { it.bikeId == bikeId && it.isDone })
            }
        }
    }

    override fun getTodoMaintenances(bikeId: String): Flow<Result<List<Maintenance>>> {
        return maintenancesFlow.map { list ->
            if (shouldFailOnGetTodo) {
                Result.Failure(getTodoError ?: AppError.DatabaseError("Failed to get todo maintenances"))
            } else {
                Result.Success(list.filter { it.bikeId == bikeId && !it.isDone })
            }
        }
    }

    override suspend fun addMaintenance(maintenance: Maintenance): Result<String> {
        if (shouldFailOnAdd) {
            return Result.Failure(addError ?: AppError.DatabaseError("Failed to add maintenance"))
        }
        val newId = "maintenance_${nextId++}"
        val newMaintenance = maintenance.copy(id = newId)
        maintenancesFlow.value = maintenancesFlow.value + newMaintenance
        return Result.Success(newId)
    }

    override suspend fun updateMaintenance(maintenance: Maintenance): Result<Unit> {
        if (shouldFailOnUpdate) {
            return Result.Failure(updateError ?: AppError.DatabaseError("Failed to update maintenance"))
        }
        maintenancesFlow.value = maintenancesFlow.value.map {
            if (it.id == maintenance.id) maintenance else it
        }
        return Result.Success(Unit)
    }

    override suspend fun markMaintenanceDone(id: String, bikeId: String, value: Float, date: Long): Result<Unit> {
        if (shouldFailOnMarkDone) {
            return Result.Failure(markDoneError ?: AppError.DatabaseError("Failed to mark maintenance done"))
        }
        maintenancesFlow.value = maintenancesFlow.value.map {
            if (it.id == id && it.bikeId == bikeId) it.copy(isDone = true, value = value, date = date) else it
        }
        return Result.Success(Unit)
    }

    override suspend fun deleteMaintenance(id: String, bikeId: String): Result<Unit> {
        if (shouldFailOnDelete) {
            return Result.Failure(deleteError ?: AppError.DatabaseError("Failed to delete maintenance"))
        }
        maintenancesFlow.value = maintenancesFlow.value.filter { !(it.id == id && it.bikeId == bikeId) }
        return Result.Success(Unit)
    }

    /**
     * Helper to set maintenances for testing.
     */
    fun setMaintenances(maintenances: List<Maintenance>) {
        maintenancesFlow.value = maintenances
    }

    /**
     * Helper to get current maintenances for verification.
     */
    fun getCurrentMaintenances(): List<Maintenance> = maintenancesFlow.value

    /**
     * Helper to make getMaintenancesByBikeId return a failure.
     */
    fun setGetByBikeIdFails(shouldFail: Boolean, error: AppError? = null) {
        shouldFailOnGetByBikeId = shouldFail
        getByBikeIdError = error
    }

    /**
     * Helper to make getDoneMaintenances return a failure.
     */
    fun setGetDoneFails(shouldFail: Boolean, error: AppError? = null) {
        shouldFailOnGetDone = shouldFail
        getDoneError = error
    }

    /**
     * Helper to make getTodoMaintenances return a failure.
     */
    fun setGetTodoFails(shouldFail: Boolean, error: AppError? = null) {
        shouldFailOnGetTodo = shouldFail
        getTodoError = error
    }

    /**
     * Helper to make addMaintenance return a failure.
     */
    fun setAddFails(shouldFail: Boolean, error: AppError? = null) {
        shouldFailOnAdd = shouldFail
        addError = error
    }

    /**
     * Helper to make updateMaintenance return a failure.
     */
    fun setUpdateFails(shouldFail: Boolean, error: AppError? = null) {
        shouldFailOnUpdate = shouldFail
        updateError = error
    }

    /**
     * Helper to make markMaintenanceDone return a failure.
     */
    fun setMarkDoneFails(shouldFail: Boolean, error: AppError? = null) {
        shouldFailOnMarkDone = shouldFail
        markDoneError = error
    }

    /**
     * Helper to make deleteMaintenance return a failure.
     */
    fun setDeleteFails(shouldFail: Boolean, error: AppError? = null) {
        shouldFailOnDelete = shouldFail
        deleteError = error
    }
}
