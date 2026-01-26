package com.bikemanager.domain.usecase.maintenance

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Use case for getting maintenances for a bike.
 *
 * Follows Single Responsibility Principle: orchestrates fetching both done and todo maintenances,
 * combining them into a single result for the ViewModel.
 */
class GetMaintenancesUseCase(private val repository: MaintenanceRepository) {

    /**
     * Gets both done and todo maintenances for a bike.
     *
     * Combines two repository streams into a single stream, returning both lists together.
     * If either stream fails, the failure is propagated.
     *
     * @param bikeId The ID of the bike
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
                // This should never happen, but handle it safely with proper error type
                else -> Result.Failure(
                    AppError.UnknownError("Unexpected result state in GetMaintenancesUseCase")
                )
            }
        }
    }
}
