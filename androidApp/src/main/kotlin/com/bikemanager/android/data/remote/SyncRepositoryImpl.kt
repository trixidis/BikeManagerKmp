package com.bikemanager.android.data.remote

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.SyncRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase Realtime Database implementation of SyncRepository.
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
class SyncRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) : SyncRepository {

    private val usersRef get() = firebaseDatabase.getReference("users")

    override fun isUserConnected(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override suspend fun syncBike(bike: Bike): String {
        val userId = getCurrentUserId()
            ?: throw IllegalStateException("User must be logged in to sync")

        val existingRef = bike.firebaseRef
        val bikeRef = if (existingRef != null) {
            usersRef.child(userId).child("bikes").child(existingRef)
        } else {
            usersRef.child(userId).child("bikes").push()
        }

        val bikeData = mapOf(
            "nameBike" to bike.name,
            "countingMethod" to bike.countingMethod.name
        )

        bikeRef.setValue(bikeData).await()
        Napier.d("Synced bike to Firebase: ${bikeRef.key}")
        return bikeRef.key ?: throw IllegalStateException("Failed to get Firebase key")
    }

    override suspend fun syncMaintenance(maintenance: Maintenance, bikeFirebaseRef: String): String {
        val userId = getCurrentUserId()
            ?: throw IllegalStateException("User must be logged in to sync")

        val existingMaintenanceRef = maintenance.firebaseRef
        val maintenanceRef = if (existingMaintenanceRef != null) {
            usersRef.child(userId)
                .child("bikes")
                .child(bikeFirebaseRef)
                .child("maintenances")
                .child(existingMaintenanceRef)
        } else {
            usersRef.child(userId)
                .child("bikes")
                .child(bikeFirebaseRef)
                .child("maintenances")
                .push()
        }

        val maintenanceData = mapOf(
            "nameMaintenance" to maintenance.name,
            "nbHoursMaintenance" to maintenance.value,
            "dateMaintenance" to maintenance.date,
            "isDone" to maintenance.isDone
        )

        maintenanceRef.setValue(maintenanceData).await()
        Napier.d("Synced maintenance to Firebase: ${maintenanceRef.key}")
        return maintenanceRef.key ?: throw IllegalStateException("Failed to get Firebase key")
    }

    override suspend fun deleteBikeFromCloud(firebaseRef: String) {
        val userId = getCurrentUserId()
            ?: throw IllegalStateException("User must be logged in to sync")

        usersRef.child(userId).child("bikes").child(firebaseRef).removeValue().await()
        Napier.d("Deleted bike from Firebase: $firebaseRef")
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
            .await()
        Napier.d("Deleted maintenance from Firebase: $maintenanceFirebaseRef")
    }

    override fun observeRemoteBikes(): Flow<List<Bike>> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val bikesRef = usersRef.child(userId).child("bikes")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bikes = snapshot.children.mapNotNull { bikeSnapshot ->
                    try {
                        val firebaseRef = bikeSnapshot.key ?: return@mapNotNull null
                        val name = bikeSnapshot.child("nameBike").getValue(String::class.java)
                            ?: return@mapNotNull null
                        val countingMethodStr = bikeSnapshot.child("countingMethod")
                            .getValue(String::class.java) ?: "KM"
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
                        Napier.e("Error parsing bike from Firebase", e)
                        null
                    }
                }
                trySend(bikes)
            }

            override fun onCancelled(error: DatabaseError) {
                Napier.e("Firebase bikes listener cancelled: ${error.message}")
                close(error.toException())
            }
        }

        bikesRef.addValueEventListener(listener)
        awaitClose { bikesRef.removeEventListener(listener) }
    }

    override fun observeRemoteMaintenances(bikeFirebaseRef: String): Flow<List<Maintenance>> =
        callbackFlow {
            val userId = getCurrentUserId()
            if (userId == null) {
                trySend(emptyList())
                close()
                return@callbackFlow
            }

            val maintenancesRef = usersRef.child(userId)
                .child("bikes")
                .child(bikeFirebaseRef)
                .child("maintenances")

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val maintenances = snapshot.children.mapNotNull { maintenanceSnapshot ->
                        try {
                            val firebaseRef = maintenanceSnapshot.key ?: return@mapNotNull null
                            val name = maintenanceSnapshot.child("nameMaintenance")
                                .getValue(String::class.java) ?: return@mapNotNull null
                            val value = maintenanceSnapshot.child("nbHoursMaintenance")
                                .getValue(Float::class.java) ?: -1f
                            val date = maintenanceSnapshot.child("dateMaintenance")
                                .getValue(Long::class.java) ?: 0L
                            val isDone = maintenanceSnapshot.child("isDone")
                                .getValue(Boolean::class.java) ?: false

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
                            Napier.e("Error parsing maintenance from Firebase", e)
                            null
                        }
                    }
                    trySend(maintenances)
                }

                override fun onCancelled(error: DatabaseError) {
                    Napier.e("Firebase maintenances listener cancelled: ${error.message}")
                    close(error.toException())
                }
            }

            maintenancesRef.addValueEventListener(listener)
            awaitClose { maintenancesRef.removeEventListener(listener) }
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
