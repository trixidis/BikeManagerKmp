package com.bikemanager.domain.usecase.bike

import com.bikemanager.domain.repository.BikeRepository
import com.bikemanager.domain.repository.MaintenanceRepository

/**
 * Use case for deleting a bike with cascade delete of all associated maintenances.
 */
class DeleteBikeUseCase(
    private val bikeRepository: BikeRepository,
    private val maintenanceRepository: MaintenanceRepository
) {
    /**
     * Deletes a bike and all its associated maintenance records.
     */
    suspend operator fun invoke(bikeId: String) {
        require(bikeId.isNotEmpty()) { "Bike id cannot be empty" }

        // Delete all maintenances for this bike first
        maintenanceRepository.deleteAllMaintenancesForBike(bikeId)

        // Then delete the bike itself
        bikeRepository.deleteBike(bikeId)
    }
}
