package com.bikemanager.domain.usecase.maintenance

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
     * @param bikeId The id of the bike
     * @return A Flow emitting a Pair of (doneMaintenances, todoMaintenances)
     */
    operator fun invoke(bikeId: Long): Flow<Pair<List<Maintenance>, List<Maintenance>>> {
        return combine(
            repository.getDoneMaintenances(bikeId),
            repository.getTodoMaintenances(bikeId)
        ) { done, todo ->
            Pair(done, todo)
        }
    }

    /**
     * Gets only done maintenances for a bike.
     */
    fun getDone(bikeId: Long): Flow<List<Maintenance>> {
        return repository.getDoneMaintenances(bikeId)
    }

    /**
     * Gets only todo maintenances for a bike.
     */
    fun getTodo(bikeId: Long): Flow<List<Maintenance>> {
        return repository.getTodoMaintenances(bikeId)
    }
}
