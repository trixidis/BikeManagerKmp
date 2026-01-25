package com.bikemanager.domain.usecase.sync

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.repository.BikeRepository
import com.bikemanager.domain.repository.MaintenanceRepository
import com.bikemanager.domain.repository.SyncRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Result of a sync operation.
 */
sealed class SyncResult {
    data object Syncing : SyncResult()
    data object Success : SyncResult()
    data class Error(val message: String) : SyncResult()
    data object NotConnected : SyncResult()
}

/**
 * Use case for observing Firebase data changes in real-time and syncing them to local database.
 * This enables an offline-first approach where:
 * 1. Local data is always the source of truth for the UI
 * 2. Firebase changes are observed and merged into local database
 * 3. The UI automatically reflects changes via local database Flows
 */
class ObserveAndSyncFromCloudUseCase(
    private val syncRepository: SyncRepository,
    private val bikeRepository: BikeRepository,
    private val maintenanceRepository: MaintenanceRepository
) {
    /**
     * Observes bikes from Firebase and syncs them to local database.
     * Returns a Flow that emits sync status updates.
     */
    operator fun invoke(): Flow<SyncResult> = flow {
        if (!syncRepository.isUserConnected()) {
            Napier.d("User not connected, skipping cloud sync observation")
            emit(SyncResult.NotConnected)
            return@flow
        }

        emit(SyncResult.Syncing)

        syncRepository.observeRemoteBikes()
            .catch { e ->
                Napier.e(e) { "Error observing remote bikes" }
                emit(SyncResult.Error(e.message ?: "Error syncing from cloud"))
            }
            .collect { remoteBikes ->
                try {
                    syncBikesToLocal(remoteBikes)
                    emit(SyncResult.Success)
                } catch (e: Exception) {
                    Napier.e(e) { "Error syncing bikes to local database" }
                    emit(SyncResult.Error(e.message ?: "Error syncing to local database"))
                }
            }
    }

    /**
     * Syncs remote bikes to local database.
     * Only adds items that don't already exist locally (matched by firebaseRef).
     */
    private suspend fun syncBikesToLocal(remoteBikes: List<Bike>) {
        // Get current local bikes
        val localBikes = bikeRepository.getAllBikes().first()
        val localBikesByRef = localBikes.associateBy { it.firebaseRef }

        Napier.d("Syncing ${remoteBikes.size} remote bikes to local (${localBikes.size} local)")

        for (remoteBike in remoteBikes) {
            val firebaseRef = remoteBike.firebaseRef ?: continue
            val existingBike = localBikesByRef[firebaseRef]

            if (existingBike == null) {
                // Bike doesn't exist locally, add it
                val newBikeId = bikeRepository.addBike(remoteBike.copy(id = 0))
                Napier.d("Added bike from cloud: ${remoteBike.name} (id=$newBikeId)")

                // Pull maintenances for this new bike
                syncMaintenancesForBike(firebaseRef, newBikeId)
            } else {
                // Bike exists, sync its maintenances
                syncMaintenancesForBike(firebaseRef, existingBike.id)
            }
        }
    }

    /**
     * Syncs maintenances for a specific bike from Firebase to local.
     */
    private suspend fun syncMaintenancesForBike(bikeFirebaseRef: String, localBikeId: Long) {
        try {
            // Get current local maintenances for this bike
            val localMaintenances = maintenanceRepository.getMaintenancesByBikeId(localBikeId).first()
            val localMaintenancesByRef = localMaintenances.associateBy { it.firebaseRef }

            // Get remote maintenances (just the first emission for sync)
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
            Napier.e(e) { "Error syncing maintenances for bike $bikeFirebaseRef" }
        }
    }
}
