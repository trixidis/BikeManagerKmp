package com.bikemanager.data.repository

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.SyncRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.database.database
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

/**
 * Common implementation of SyncRepository using GitLive Firebase Database SDK.
 * Works on both Android and iOS.
 *
 * Database structure:
 * users/{userId}/bikes/{bikeRef}/
 *   - nameBike: String
 *   - countingMethod: "KM" | "HOURS"
 *   - maintenances/{maintenanceRef}/
 *       - nameMaintenance: String
 *       - nbHoursMaintenance: Float
 *       - dateMaintenance: Long
 *       - isDone: Boolean
 */
class SyncRepositoryImpl : SyncRepository {

    private val database = Firebase.database
    private val auth = Firebase.auth

    private val usersRef get() = database.reference("users")

    override fun isUserConnected(): Boolean {
        return auth.currentUser != null
    }

    override fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun syncBike(bike: Bike): String {
        val userId = getCurrentUserId()
            ?: throw IllegalStateException("User must be logged in to sync")

        val bikesRef = usersRef.child(userId).child("bikes")
        val bikeRef = if (bike.firebaseRef != null) {
            bikesRef.child(bike.firebaseRef)
        } else {
            bikesRef.push()
        }

        val bikeData = mapOf(
            "nameBike" to bike.name,
            "countingMethod" to bike.countingMethod.name
        )

        bikeRef.setValue(bikeData)
        val key = bikeRef.key ?: throw IllegalStateException("Failed to get Firebase key")
        Napier.d { "Synced bike to Firebase: $key" }
        return key
    }

    override suspend fun syncMaintenance(maintenance: Maintenance, bikeFirebaseRef: String): String {
        val userId = getCurrentUserId()
            ?: throw IllegalStateException("User must be logged in to sync")

        val maintenancesRef = usersRef.child(userId)
            .child("bikes")
            .child(bikeFirebaseRef)
            .child("maintenances")

        val maintenanceRef = if (maintenance.firebaseRef != null) {
            maintenancesRef.child(maintenance.firebaseRef)
        } else {
            maintenancesRef.push()
        }

        val maintenanceData = mapOf(
            "nameMaintenance" to maintenance.name,
            "nbHoursMaintenance" to maintenance.value,
            "dateMaintenance" to maintenance.date,
            "isDone" to maintenance.isDone
        )

        maintenanceRef.setValue(maintenanceData)
        val key = maintenanceRef.key ?: throw IllegalStateException("Failed to get Firebase key")
        Napier.d { "Synced maintenance to Firebase: $key" }
        return key
    }

    override suspend fun deleteBikeFromCloud(firebaseRef: String) {
        val userId = getCurrentUserId()
            ?: throw IllegalStateException("User must be logged in to sync")

        usersRef.child(userId).child("bikes").child(firebaseRef).removeValue()
        Napier.d { "Deleted bike from Firebase: $firebaseRef" }
    }

    override suspend fun deleteMaintenanceFromCloud(
        bikeFirebaseRef: String,
        maintenanceFirebaseRef: String
    ) {
        val userId = getCurrentUserId()
            ?: throw IllegalStateException("User must be logged in to sync")

        usersRef.child(userId)
            .child("bikes")
            .child(bikeFirebaseRef)
            .child("maintenances")
            .child(maintenanceFirebaseRef)
            .removeValue()
        Napier.d { "Deleted maintenance from Firebase: $maintenanceFirebaseRef" }
    }

    override fun observeRemoteBikes(): Flow<List<Bike>> {
        val userId = getCurrentUserId() ?: return emptyFlow()

        return usersRef.child(userId).child("bikes").valueEvents.map { snapshot ->
            snapshot.children.mapNotNull { bikeSnapshot ->
                try {
                    val firebaseRef = bikeSnapshot.key ?: return@mapNotNull null
                    val name = bikeSnapshot.child("nameBike").value<String?>() ?: return@mapNotNull null
                    val countingMethodStr = bikeSnapshot.child("countingMethod").value<String?>() ?: "KM"
                    val countingMethod = try {
                        CountingMethod.valueOf(countingMethodStr)
                    } catch (e: IllegalArgumentException) {
                        CountingMethod.KM
                    }

                    Bike(
                        id = 0, // Will be matched by firebaseRef
                        name = name,
                        countingMethod = countingMethod,
                        firebaseRef = firebaseRef
                    )
                } catch (e: Exception) {
                    Napier.e(e) { "Error parsing bike from Firebase" }
                    null
                }
            }
        }
    }

    override fun observeRemoteMaintenances(bikeFirebaseRef: String): Flow<List<Maintenance>> {
        val userId = getCurrentUserId() ?: return emptyFlow()

        return usersRef.child(userId)
            .child("bikes")
            .child(bikeFirebaseRef)
            .child("maintenances")
            .valueEvents
            .map { snapshot ->
                snapshot.children.mapNotNull { maintenanceSnapshot ->
                    try {
                        val firebaseRef = maintenanceSnapshot.key ?: return@mapNotNull null
                        val name = maintenanceSnapshot.child("nameMaintenance").value<String?>()
                            ?: return@mapNotNull null
                        val value = maintenanceSnapshot.child("nbHoursMaintenance").value<Double?>()?.toFloat() ?: -1f
                        val date = maintenanceSnapshot.child("dateMaintenance").value<Long?>() ?: 0L
                        val isDone = maintenanceSnapshot.child("isDone").value<Boolean?>() ?: false

                        Maintenance(
                            id = 0, // Will be matched by firebaseRef
                            name = name,
                            value = value,
                            date = date,
                            isDone = isDone,
                            bikeId = 0, // Will be resolved locally
                            firebaseRef = firebaseRef
                        )
                    } catch (e: Exception) {
                        Napier.e(e) { "Error parsing maintenance from Firebase" }
                        null
                    }
                }
            }
    }

    override suspend fun updateBikeInCloud(bike: Bike) {
        requireNotNull(bike.firebaseRef) { "Bike must have a Firebase reference to update" }
        syncBike(bike)
    }

    override suspend fun updateMaintenanceInCloud(maintenance: Maintenance, bikeFirebaseRef: String) {
        requireNotNull(maintenance.firebaseRef) { "Maintenance must have a Firebase reference to update" }
        syncMaintenance(maintenance, bikeFirebaseRef)
    }
}
