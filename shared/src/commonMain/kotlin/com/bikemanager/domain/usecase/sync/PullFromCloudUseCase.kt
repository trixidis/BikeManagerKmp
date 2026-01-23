package com.bikemanager.domain.usecase.sync

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.BikeRepository
import com.bikemanager.domain.repository.MaintenanceRepository
import com.bikemanager.domain.repository.SyncRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.first

/**
 * Use case for pulling data from Firebase to local database.
 * Called after authentication to sync existing cloud data.
 */
class PullFromCloudUseCase(
    private val syncRepository: SyncRepository,
    private val bikeRepository: BikeRepository,
    private val maintenanceRepository: MaintenanceRepository
) {
    /**
     * Pulls bikes and maintenances from Firebase and stores them locally.
     * Only adds items that don't already exist locally (matched by firebaseRef).
     */
    suspend operator fun invoke() {
        if (!syncRepository.isUserConnected()) {
            Napier.d("User not connected, skipping cloud pull")
            return
        }

        try {
            // Get current local bikes
            val localBikes = bikeRepository.getAllBikes().first()
            val localBikesByRef = localBikes.associateBy { it.firebaseRef }

            // Get remote bikes
            val remoteBikes = syncRepository.observeRemoteBikes().first()
            Napier.d("Pulling ${remoteBikes.size} bikes from cloud")

            for (remoteBike in remoteBikes) {
                val firebaseRef = remoteBike.firebaseRef ?: continue
                val existingBike = localBikesByRef[firebaseRef]

                if (existingBike == null) {
                    // Bike doesn't exist locally, add it
                    val newBikeId = bikeRepository.addBike(remoteBike.copy(id = 0))
                    Napier.d("Added bike from cloud: ${remoteBike.name} (id=$newBikeId)")

                    // Pull maintenances for this bike
                    pullMaintenancesForBike(firebaseRef, newBikeId)
                } else {
                    // Bike exists, pull maintenances
                    pullMaintenancesForBike(firebaseRef, existingBike.id)
                }
            }

            Napier.d("Cloud pull completed successfully")
        } catch (e: Exception) {
            Napier.e(e) { "Error pulling from cloud" }
            throw e
        }
    }

    private suspend fun pullMaintenancesForBike(bikeFirebaseRef: String, localBikeId: Long) {
        try {
            // Get current local maintenances for this bike
            val localMaintenances = maintenanceRepository.getMaintenancesByBikeId(localBikeId).first()
            val localMaintenancesByRef = localMaintenances.associateBy { it.firebaseRef }

            // Get remote maintenances
            val remoteMaintenances = syncRepository.observeRemoteMaintenances(bikeFirebaseRef).first()

            for (remoteMaintenance in remoteMaintenances) {
                val maintenanceRef = remoteMaintenance.firebaseRef ?: continue

                if (localMaintenancesByRef[maintenanceRef] == null) {
                    // Maintenance doesn't exist locally, add it
                    val newMaintenance = remoteMaintenance.copy(
                        id = 0,
                        bikeId = localBikeId
                    )
                    maintenanceRepository.addMaintenance(newMaintenance)
                    Napier.d("Added maintenance from cloud: ${remoteMaintenance.name}")
                }
            }
        } catch (e: Exception) {
            Napier.e(e) { "Error pulling maintenances for bike $bikeFirebaseRef" }
        }
    }
}
