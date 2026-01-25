package com.bikemanager.domain.usecase.sync

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.repository.BikeRepository
import com.bikemanager.domain.repository.MaintenanceRepository
import com.bikemanager.domain.repository.SyncRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * Result of a sync operation.
 */
sealed interface SyncResult {
    data object Syncing : SyncResult
    data object Success : SyncResult
    data class Error(val message: String) : SyncResult
    data object NotConnected : SyncResult
}

class ObserveAndSyncFromCloudUseCase(
    private val syncRepository: SyncRepository,
    private val bikeRepository: BikeRepository,
    private val maintenanceRepository: MaintenanceRepository
) {
    operator fun invoke(): Flow<SyncResult> = flow {
        if (!syncRepository.isUserConnected()) {
            Napier.d("User not connected, skipping cloud sync observation")
            emit(SyncResult.NotConnected)
            return@flow
        }

        emit(SyncResult.Syncing)

        syncRepository.observeRemoteBikes()
            .catch { e ->
                if (e is CancellationException) throw e
                Napier.e(e) { "Error observing remote bikes" }
                emit(SyncResult.Error(e.message ?: "Error syncing from cloud"))
            }
            .collect { remoteBikes ->
                try {
                    syncBikesToLocal(remoteBikes)
                    emit(SyncResult.Success)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Napier.e(e) { "Error syncing bikes to local database" }
                    emit(SyncResult.Error(e.message ?: "Error syncing to local database"))
                }
            }
    }

    private suspend fun syncBikesToLocal(remoteBikes: List<Bike>) {
        val localBikes = bikeRepository.getAllBikes().first()
        val localBikesByRef = localBikes.associateBy { it.firebaseRef }

        Napier.d("Syncing ${remoteBikes.size} remote bikes to local (${localBikes.size} local)")

        coroutineScope {
            remoteBikes.forEach { remoteBike ->
                launch {
                    syncSingleBike(remoteBike, localBikesByRef)
                }
            }
        }
    }

    private suspend fun syncSingleBike(
        remoteBike: Bike,
        localBikesByRef: Map<String?, Bike>
    ) {
        val firebaseRef = remoteBike.firebaseRef ?: return
        val existingBike = localBikesByRef[firebaseRef]

        val localBikeId = existingBike?.id
            ?: bikeRepository.addBike(remoteBike.copy(id = 0)).also {
                Napier.d("Added bike from cloud (id=$it)")
            }

        syncMaintenancesForBike(firebaseRef, localBikeId)
    }

    private suspend fun syncMaintenancesForBike(bikeFirebaseRef: String, localBikeId: Long) {
        try {
            val localMaintenances =
                maintenanceRepository.getMaintenancesByBikeId(localBikeId).first()
            val localMaintenancesByRef = localMaintenances.associateBy { it.firebaseRef }

            val remoteMaintenances =
                syncRepository.observeRemoteMaintenances(bikeFirebaseRef).first()

            remoteMaintenances
                .filter { it.firebaseRef != null && localMaintenancesByRef[it.firebaseRef] == null }
                .forEach { remoteMaintenance ->
                    val newMaintenance = remoteMaintenance.copy(id = 0, bikeId = localBikeId)
                    maintenanceRepository.addMaintenance(newMaintenance)
                    Napier.d("Added maintenance from cloud: ${remoteMaintenance.firebaseRef}")
                }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Napier.e(e) { "Error syncing maintenances for bike $bikeFirebaseRef" }
        }
    }
}