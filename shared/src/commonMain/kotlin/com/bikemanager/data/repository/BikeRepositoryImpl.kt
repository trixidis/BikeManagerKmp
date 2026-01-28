package com.bikemanager.data.repository

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.ErrorHandler
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.repository.AuthRepository
import com.bikemanager.domain.repository.BikeRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.database.database
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout

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

    /**
     * Parses a Firebase snapshot into a Bike object.
     * Returns null if parsing fails or required fields are missing.
     * Follows Single Responsibility Principle: dedicated to parsing logic.
     */
    private fun parseBikeSnapshot(bikeSnapshot: dev.gitlive.firebase.database.DataSnapshot): Bike? {
        return try {
            val id = bikeSnapshot.key ?: return null
            val name = bikeSnapshot.child("nameBike").value<String?>() ?: return null
            val countingMethodStr = bikeSnapshot.child("countingMethod").value<String?>() ?: "KM"
            val countingMethod = try {
                CountingMethod.valueOf(countingMethodStr)
            } catch (e: IllegalArgumentException) {
                Napier.w { "Invalid counting method '$countingMethodStr', defaulting to KM" }
                CountingMethod.KM
            }

            Bike(id = id, name = name, countingMethod = countingMethod)
        } catch (e: CancellationException) {
            // CRITICAL: Re-throw CancellationException to allow proper coroutine cancellation
            throw e
        } catch (e: Exception) {
            Napier.e(e) { "Error parsing bike" }
            null
        }
    }

    override fun getAllBikes(): Flow<Result<List<Bike>>> {
        val ref = bikesRef()
        if (ref == null) {
            return flow {
                emit(Result.Failure(
                    AppError.AuthError("User not authenticated")
                ))
            }
        }

        return ref.valueEvents.map { snapshot ->
            try {
                val bikes = snapshot.children
                    .mapNotNull { parseBikeSnapshot(it) }
                    .sortedByDescending { it.name }
                Result.Success(bikes)
            } catch (e: CancellationException) {
                // CRITICAL: Re-throw CancellationException to allow proper coroutine cancellation
                throw e
            } catch (e: Throwable) {
                Result.Failure(ErrorHandler.handle(e, "loading bikes"))
            }
        }
    }

    override suspend fun getBikeById(id: String): Result<Bike?> {
        val ref = bikesRef()
        if (ref == null) {
            return Result.Failure(
                AppError.AuthError("User not authenticated")
            )
        }

        return ErrorHandler.catching("getting bike by id") {
            // Use first() instead of collect to get only the first emission and complete
            // Add timeout protection to prevent indefinite waits
            withTimeout(10_000L) { // 10 second timeout
                val snapshot = ref.child(id).valueEvents.first()

                if (snapshot.exists) {
                    parseBikeSnapshot(snapshot)
                } else {
                    null
                }
            }
        }
    }

    override suspend fun addBike(bike: Bike): Result<String> {
        val ref = bikesRef()
        if (ref == null) {
            return Result.Failure(
                AppError.AuthError("User not authenticated")
            )
        }

        return ErrorHandler.catching("adding bike") {
            withTimeout(10_000L) { // 10 second timeout
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
    }

    override suspend fun updateBike(bike: Bike): Result<Unit> {
        val ref = bikesRef()
        if (ref == null) {
            return Result.Failure(
                AppError.AuthError("User not authenticated")
            )
        }

        if (bike.id.isEmpty()) {
            return Result.Failure(
                AppError.ValidationError(
                    errorMessage = "Bike id cannot be empty",
                    field = "id"
                )
            )
        }

        return ErrorHandler.catching("updating bike") {
            withTimeout(10_000L) { // 10 second timeout
                val bikeData = mapOf(
                    "nameBike" to bike.name,
                    "countingMethod" to bike.countingMethod.name
                )
                ref.child(bike.id).updateChildren(bikeData)
                Napier.d { "Bike updated: ${bike.name}" }
            }
        }
    }

    override suspend fun deleteBike(id: String): Result<Unit> {
        val ref = bikesRef()
        if (ref == null) {
            return Result.Failure(
                AppError.AuthError("User not authenticated")
            )
        }

        return ErrorHandler.catching("deleting bike") {
            withTimeout(10_000L) { // 10 second timeout
                ref.child(id).removeValue()
                Napier.d { "Bike deleted: $id" }
            }
        }
    }
}
