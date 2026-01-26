package com.bikemanager.domain.usecase.bike

import com.bikemanager.domain.common.Result
import com.bikemanager.domain.repository.BikeRepository
import com.bikemanager.domain.repository.MaintenanceRepository

/**
 * Use case for deleting a bike with cascade delete of all associated maintenances.
 *
 * Deletion order:
 * 1. Delete all maintenances for the bike
 * 2. Delete the bike itself
 *
 * If any step fails, the operation stops and returns the error.
 */
class DeleteBikeUseCase(
    private val bikeRepository: BikeRepository,
    private val maintenanceRepository: MaintenanceRepository
) {
    /**
     * Deletes a bike and all its associated maintenance records.
     *
     * @param bikeId The ID of the bike to delete (must not be empty)
     * @return Result<Unit> Success if deleted, Failure with AppError otherwise
     */
    suspend operator fun invoke(bikeId: String): Result<Unit> {
        require(bikeId.isNotEmpty()) { "Bike id cannot be empty" }

        // Delete all maintenances for this bike first
        val deleteMaintenancesResult = maintenanceRepository.deleteAllMaintenancesForBike(bikeId)
        if (deleteMaintenancesResult is Result.Failure) {
            return deleteMaintenancesResult
        }

        // Then delete the bike itself
        return bikeRepository.deleteBike(bikeId)
    }
}
