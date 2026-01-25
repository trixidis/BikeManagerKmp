package com.bikemanager.domain.usecase.maintenance

import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Use case for getting maintenances for a bike.
 */
class GetMaintenancesUseCase(private val repository: MaintenanceRepository) {

    /**
     * Gets both done and todo maintenances for a bike.
     * @return A Flow emitting a Result of Pair of (doneMaintenances, todoMaintenances)
     */
    operator fun invoke(bikeId: String): Flow<Result<Pair<List<Maintenance>, List<Maintenance>>>> {
        return combine(
            repository.getDoneMaintenances(bikeId),
            repository.getTodoMaintenances(bikeId)
        ) { doneResult, todoResult ->
            when {
                doneResult is Result.Failure -> doneResult
                todoResult is Result.Failure -> todoResult
                doneResult is Result.Success && todoResult is Result.Success -> {
                    Result.Success(Pair(doneResult.value, todoResult.value))
                }
                else -> Result.Failure(IllegalStateException("Unexpected result state"))
            }
        }
    }

    /**
     * Gets only done maintenances for a bike.
     */
    fun getDone(bikeId: String): Flow<Result<List<Maintenance>>> {
        return repository.getDoneMaintenances(bikeId)
    }

    /**
     * Gets only todo maintenances for a bike.
     */
    fun getTodo(bikeId: String): Flow<Result<List<Maintenance>>> {
        return repository.getTodoMaintenances(bikeId)
    }
}
