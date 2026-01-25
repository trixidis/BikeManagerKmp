package com.bikemanager.data.repository

import com.bikemanager.domain.common.Result
import com.bikemanager.domain.common.runCatching
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.repository.AuthRepository
import com.bikemanager.domain.repository.BikeRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.database.database
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    override fun getAllBikes(): Flow<Result<List<Bike>>> {
        val ref = bikesRef()
        if (ref == null) {
            return flow {
                emit(Result.Failure(IllegalStateException("User not authenticated")))
            }
        }

        return ref.valueEvents.map { snapshot ->
            com.bikemanager.domain.common.runCatching {
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
    }

    override suspend fun getBikeById(id: String): Result<Bike?> {
        val ref = bikesRef()
        if (ref == null) {
            return Result.Failure(IllegalStateException("User not authenticated"))
        }

        return com.bikemanager.domain.common.runCatching {
            ref.child(id).valueEvents.map { it }.let { flow ->
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
        }
    }

    override suspend fun addBike(bike: Bike): Result<String> {
        val ref = bikesRef()
        if (ref == null) {
            return Result.Failure(IllegalStateException("User not authenticated"))
        }

        return com.bikemanager.domain.common.runCatching {
            val newRef = ref.push()
            val bikeData = mapOf(
                "nameBike" to bike.name,
                "countingMethod" to bike.countingMethod.name
            )
            newRef.setValue(bikeData)

            val key = newRef.key ?: throw IllegalStateException("Failed to get Firebase key")
            Napier.d { "Bike added: ${bike.name} (id=$key)" }
            key
        }
    }

    override suspend fun updateBike(bike: Bike): Result<Unit> {
        val ref = bikesRef()
        if (ref == null) {
            return Result.Failure(IllegalStateException("User not authenticated"))
        }

        if (bike.id.isEmpty()) {
            return Result.Failure(IllegalArgumentException("Bike id cannot be empty"))
        }

        return com.bikemanager.domain.common.runCatching {
            val bikeData = mapOf(
                "nameBike" to bike.name,
                "countingMethod" to bike.countingMethod.name
            )
            ref.child(bike.id).setValue(bikeData)
            Napier.d { "Bike updated: ${bike.name}" }
        }
    }

    override suspend fun deleteBike(id: String): Result<Unit> {
        val ref = bikesRef()
        if (ref == null) {
            return Result.Failure(IllegalStateException("User not authenticated"))
        }

        return com.bikemanager.domain.common.runCatching {
            ref.child(id).removeValue()
            Napier.d { "Bike deleted: $id" }
        }
    }
}
