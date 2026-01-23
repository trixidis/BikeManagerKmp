package com.bikemanager.domain.repository

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.Maintenance
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Firebase sync operations.
 * Handles cloud synchronization for bikes and maintenances.
 */
interface SyncRepository {
    /**
     * Checks if the user is currently connected to Firebase.
     */
    fun isUserConnected(): Boolean

    /**
     * Gets the current user's ID for Firebase path construction.
     */
    fun getCurrentUserId(): String?

    /**
     * Syncs a bike to Firebase and returns the firebase reference.
     */
    suspend fun syncBike(bike: Bike): String

    /**
     * Syncs a maintenance to Firebase and returns the firebase reference.
     */
    suspend fun syncMaintenance(maintenance: Maintenance, bikeFirebaseRef: String): String

    /**
     * Deletes a bike from Firebase.
     */
    suspend fun deleteBikeFromCloud(firebaseRef: String)

    /**
     * Deletes a maintenance from Firebase.
     */
    suspend fun deleteMaintenanceFromCloud(bikeFirebaseRef: String, maintenanceFirebaseRef: String)

    /**
     * Observes bikes from Firebase for the current user.
     * Emits updates when remote data changes.
     */
    fun observeRemoteBikes(): Flow<List<Bike>>

    /**
     * Observes maintenances from Firebase for a specific bike.
     */
    fun observeRemoteMaintenances(bikeFirebaseRef: String): Flow<List<Maintenance>>

    /**
     * Updates a bike in Firebase.
     */
    suspend fun updateBikeInCloud(bike: Bike)

    /**
     * Updates a maintenance in Firebase.
     */
    suspend fun updateMaintenanceInCloud(maintenance: Maintenance, bikeFirebaseRef: String)
}
