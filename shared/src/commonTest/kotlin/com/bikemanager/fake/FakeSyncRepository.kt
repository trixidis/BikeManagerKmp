package com.bikemanager.fake

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake implementation of SyncRepository for testing.
 */
open class FakeSyncRepository : SyncRepository {
    private var isConnected = false
    private var userId: String? = null
    private val remoteBikes = MutableStateFlow<List<Bike>>(emptyList())
    private val remoteMaintenances = mutableMapOf<String, MutableStateFlow<List<Maintenance>>>()

    private var bikeRefCounter = 0
    private var maintenanceRefCounter = 0

    val syncedBikes = mutableListOf<Bike>()
    val syncedMaintenances = mutableListOf<Pair<Maintenance, String>>()
    val deletedBikeRefs = mutableListOf<String>()
    val deletedMaintenanceRefs = mutableListOf<Pair<String, String>>()

    fun setConnected(connected: Boolean, userId: String? = "test-user") {
        this.isConnected = connected
        this.userId = if (connected) userId else null
    }

    fun setRemoteBikes(bikes: List<Bike>) {
        remoteBikes.value = bikes
    }

    fun setRemoteMaintenances(bikeFirebaseRef: String, maintenances: List<Maintenance>) {
        remoteMaintenances.getOrPut(bikeFirebaseRef) { MutableStateFlow(emptyList()) }.value =
            maintenances
    }

    override fun isUserConnected(): Boolean = isConnected

    override fun getCurrentUserId(): String? = userId

    override suspend fun syncBike(bike: Bike): String {
        if (!isConnected) throw IllegalStateException("Not connected")
        syncedBikes.add(bike)
        return bike.firebaseRef ?: "bike-ref-${++bikeRefCounter}"
    }

    override suspend fun syncMaintenance(maintenance: Maintenance, bikeFirebaseRef: String): String {
        if (!isConnected) throw IllegalStateException("Not connected")
        syncedMaintenances.add(maintenance to bikeFirebaseRef)
        return maintenance.firebaseRef ?: "maintenance-ref-${++maintenanceRefCounter}"
    }

    override suspend fun deleteBikeFromCloud(firebaseRef: String) {
        if (!isConnected) throw IllegalStateException("Not connected")
        deletedBikeRefs.add(firebaseRef)
    }

    override suspend fun deleteMaintenanceFromCloud(
        bikeFirebaseRef: String,
        maintenanceFirebaseRef: String
    ) {
        if (!isConnected) throw IllegalStateException("Not connected")
        deletedMaintenanceRefs.add(bikeFirebaseRef to maintenanceFirebaseRef)
    }

    override fun observeRemoteBikes(): Flow<List<Bike>> = remoteBikes

    override fun observeRemoteMaintenances(bikeFirebaseRef: String): Flow<List<Maintenance>> {
        return remoteMaintenances.getOrPut(bikeFirebaseRef) { MutableStateFlow(emptyList()) }
    }

    override suspend fun updateBikeInCloud(bike: Bike) {
        if (!isConnected) throw IllegalStateException("Not connected")
        syncedBikes.add(bike)
    }

    override suspend fun updateMaintenanceInCloud(maintenance: Maintenance, bikeFirebaseRef: String) {
        if (!isConnected) throw IllegalStateException("Not connected")
        syncedMaintenances.add(maintenance to bikeFirebaseRef)
    }

    fun reset() {
        isConnected = false
        userId = null
        remoteBikes.value = emptyList()
        remoteMaintenances.clear()
        bikeRefCounter = 0
        maintenanceRefCounter = 0
        syncedBikes.clear()
        syncedMaintenances.clear()
        deletedBikeRefs.clear()
        deletedMaintenanceRefs.clear()
    }
}
