package com.bikemanager.data.repository

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.repository.AuthRepository
import com.bikemanager.domain.repository.BikeRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.database.database
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

/**
 * Firebase implementation of BikeRepository.
 * Uses Firebase Realtime Database with offline persistence.
 */
class BikeRepositoryImpl(
    private val authRepository: AuthRepository
) : BikeRepository {

    private val database = Firebase.database

    private fun bikesRef() = authRepository.getCurrentUser()?.let { user ->
        database.reference("users").child(user.uid).child("bikes")
    }

    override fun getAllBikes(): Flow<List<Bike>> {
        val ref = bikesRef() ?: return emptyFlow()

        return ref.valueEvents.map { snapshot ->
            snapshot.children.mapNotNull { bikeSnapshot ->
                try {
                    val id = bikeSnapshot.key ?: return@mapNotNull null
                    val name = bikeSnapshot.child("nameBike").value<String?>() ?: return@mapNotNull null
                    val countingMethodStr = bikeSnapshot.child("countingMethod").value<String?>() ?: "KM"
                    val countingMethod = try {
                        CountingMethod.valueOf(countingMethodStr)
                    } catch (e: IllegalArgumentException) {
                        CountingMethod.KM
                    }

                    Bike(id = id, name = name, countingMethod = countingMethod)
                } catch (e: Exception) {
                    Napier.e(e) { "Error parsing bike" }
                    null
                }
            }.sortedByDescending { it.name }
        }
    }

    override suspend fun getBikeById(id: String): Bike? {
        val ref = bikesRef() ?: return null

        return try {
            val snapshot = ref.child(id).valueEvents.map { it }.let { flow ->
                var result: Bike? = null
                flow.collect { snap ->
                    if (snap.exists) {
                        val name = snap.child("nameBike").value<String?>() ?: return@collect
                        val countingMethodStr = snap.child("countingMethod").value<String?>() ?: "KM"
                        val countingMethod = try {
                            CountingMethod.valueOf(countingMethodStr)
                        } catch (e: IllegalArgumentException) {
                            CountingMethod.KM
                        }
                        result = Bike(id = id, name = name, countingMethod = countingMethod)
                    }
                    return@collect
                }
                result
            }
            snapshot
        } catch (e: Exception) {
            Napier.e(e) { "Error getting bike by id" }
            null
        }
    }

    override suspend fun addBike(bike: Bike): String {
        val ref = bikesRef() ?: throw IllegalStateException("User not authenticated")

        val newRef = ref.push()
        val bikeData = mapOf(
            "nameBike" to bike.name,
            "countingMethod" to bike.countingMethod.name
        )
        newRef.setValue(bikeData)

        val key = newRef.key ?: throw IllegalStateException("Failed to get Firebase key")
        Napier.d { "Bike added: ${bike.name} (id=$key)" }
        return key
    }

    override suspend fun updateBike(bike: Bike) {
        val ref = bikesRef() ?: throw IllegalStateException("User not authenticated")
        require(bike.id.isNotEmpty()) { "Bike id cannot be empty" }

        val bikeData = mapOf(
            "nameBike" to bike.name,
            "countingMethod" to bike.countingMethod.name
        )
        ref.child(bike.id).setValue(bikeData)
        Napier.d { "Bike updated: ${bike.name}" }
    }

    override suspend fun deleteBike(id: String) {
        val ref = bikesRef() ?: throw IllegalStateException("User not authenticated")

        ref.child(id).removeValue()
        Napier.d { "Bike deleted: $id" }
    }
}
