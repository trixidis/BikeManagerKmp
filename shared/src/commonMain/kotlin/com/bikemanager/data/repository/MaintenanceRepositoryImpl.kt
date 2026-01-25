package com.bikemanager.data.repository

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.ErrorHandler
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.AuthRepository
import com.bikemanager.domain.repository.MaintenanceRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.database.database
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Firebase implementation of MaintenanceRepository.
 * Uses Firebase Realtime Database with offline persistence.
 */
class MaintenanceRepositoryImpl(
    private val authRepository: AuthRepository
) : MaintenanceRepository {

    private val database = Firebase.database

    private fun maintenancesRef(bikeId: String) = authRepository.getCurrentUser()?.let { user ->
        database.reference("users")
            .child(user.uid)
            .child("bikes")
            .child(bikeId)
            .child("maintenances")
    }

    override fun getMaintenancesByBikeId(bikeId: String): Flow<Result<List<Maintenance>>> {
        val ref = maintenancesRef(bikeId)
        if (ref == null) {
            return flow {
                emit(Result.Failure(
                    AppError.AuthError("User not authenticated")
                ))
            }
        }

        return ref.valueEvents.map { snapshot ->
            try {
                val maintenances = snapshot.children.mapNotNull { maintenanceSnapshot ->
                    try {
                        val id = maintenanceSnapshot.key ?: return@mapNotNull null
                        val name = maintenanceSnapshot.child("nameMaintenance").value<String?>()
                            ?: return@mapNotNull null
                        val value = maintenanceSnapshot.child("nbHoursMaintenance").value<Double?>()?.toFloat() ?: -1f
                        val date = maintenanceSnapshot.child("dateMaintenance").value<Long?>() ?: 0L
                        val isDone = maintenanceSnapshot.child("isDone").value<Boolean?>() ?: false

                        Maintenance(
                            id = id,
                            name = name,
                            value = value,
                            date = date,
                            isDone = isDone,
                            bikeId = bikeId
                        )
                    } catch (e: Exception) {
                        Napier.e(e) { "Error parsing maintenance" }
                        null
                    }
                }.sortedByDescending { it.value }
                Result.Success(maintenances)
            } catch (e: Throwable) {
                Result.Failure(ErrorHandler.handle(e, "loading maintenances"))
            }
        }
    }

    override fun getDoneMaintenances(bikeId: String): Flow<Result<List<Maintenance>>> {
        val ref = maintenancesRef(bikeId)
        if (ref == null) {
            return flow {
                emit(Result.Failure(
                    AppError.AuthError("User not authenticated")
                ))
            }
        }

        return ref.valueEvents.map { snapshot ->
            try {
                val maintenances = snapshot.children.mapNotNull { maintenanceSnapshot ->
                    try {
                        val id = maintenanceSnapshot.key ?: return@mapNotNull null
                        val name = maintenanceSnapshot.child("nameMaintenance").value<String?>()
                            ?: return@mapNotNull null
                        val value = maintenanceSnapshot.child("nbHoursMaintenance").value<Double?>()?.toFloat() ?: -1f
                        val date = maintenanceSnapshot.child("dateMaintenance").value<Long?>() ?: 0L
                        val isDone = maintenanceSnapshot.child("isDone").value<Boolean?>() ?: false

                        Maintenance(
                            id = id,
                            name = name,
                            value = value,
                            date = date,
                            isDone = isDone,
                            bikeId = bikeId
                        )
                    } catch (e: Exception) {
                        Napier.e(e) { "Error parsing maintenance" }
                        null
                    }
                }.filter { it.isDone }.sortedByDescending { it.value }
                Result.Success(maintenances)
            } catch (e: Throwable) {
                Result.Failure(ErrorHandler.handle(e, "loading done maintenances"))
            }
        }
    }

    override fun getTodoMaintenances(bikeId: String): Flow<Result<List<Maintenance>>> {
        val ref = maintenancesRef(bikeId)
        if (ref == null) {
            return flow {
                emit(Result.Failure(IllegalStateException("User not authenticated")))
            }
        }

        return ref.valueEvents.map { snapshot ->
            com.bikemanager.domain.common.runCatching {
                snapshot.children.mapNotNull { maintenanceSnapshot ->
                    try {
                        val id = maintenanceSnapshot.key ?: return@mapNotNull null
                        val name = maintenanceSnapshot.child("nameMaintenance").value<String?>()
                            ?: return@mapNotNull null
                        val value = maintenanceSnapshot.child("nbHoursMaintenance").value<Double?>()?.toFloat() ?: -1f
                        val date = maintenanceSnapshot.child("dateMaintenance").value<Long?>() ?: 0L
                        val isDone = maintenanceSnapshot.child("isDone").value<Boolean?>() ?: false

                        Maintenance(
                            id = id,
                            name = name,
                            value = value,
                            date = date,
                            isDone = isDone,
                            bikeId = bikeId
                        )
                    } catch (e: Exception) {
                        Napier.e(e) { "Error parsing maintenance" }
                        null
                    }
                }.filter { !it.isDone }.sortedByDescending { it.id }
            }
        }
    }

    override suspend fun addMaintenance(maintenance: Maintenance): Result<String> {
        val ref = maintenancesRef(maintenance.bikeId)
        if (ref == null) {
            return Result.Failure(IllegalStateException("User not authenticated"))
        }

        return com.bikemanager.domain.common.runCatching {
            val newRef = ref.push()
            val data = mapOf(
                "nameMaintenance" to maintenance.name,
                "nbHoursMaintenance" to maintenance.value,
                "dateMaintenance" to maintenance.date,
                "isDone" to maintenance.isDone
            )
            newRef.setValue(data)

            val key = newRef.key ?: throw IllegalStateException("Failed to get Firebase key")
            Napier.d { "Maintenance added: ${maintenance.name} (id=$key)" }
            key
        }
    }

    override suspend fun updateMaintenance(maintenance: Maintenance): Result<Unit> {
        val ref = maintenancesRef(maintenance.bikeId)
        if (ref == null) {
            return Result.Failure(IllegalStateException("User not authenticated"))
        }

        if (maintenance.id.isEmpty()) {
            return Result.Failure(IllegalArgumentException("Maintenance id cannot be empty"))
        }

        return com.bikemanager.domain.common.runCatching {
            val data = mapOf(
                "nameMaintenance" to maintenance.name,
                "nbHoursMaintenance" to maintenance.value,
                "dateMaintenance" to maintenance.date,
                "isDone" to maintenance.isDone
            )
            ref.child(maintenance.id).setValue(data)
            Napier.d { "Maintenance updated: ${maintenance.name}" }
        }
    }

    override suspend fun markMaintenanceDone(id: String, bikeId: String, value: Float, date: Long): Result<Unit> {
        val ref = maintenancesRef(bikeId)
        if (ref == null) {
            return Result.Failure(IllegalStateException("User not authenticated"))
        }

        return com.bikemanager.domain.common.runCatching {
            val updates = mapOf(
                "isDone" to true,
                "nbHoursMaintenance" to value,
                "dateMaintenance" to date
            )
            ref.child(id).updateChildren(updates)
            Napier.d { "Maintenance marked done: $id" }
        }
    }

    override suspend fun deleteMaintenance(id: String, bikeId: String): Result<Unit> {
        val ref = maintenancesRef(bikeId)
        if (ref == null) {
            return Result.Failure(IllegalStateException("User not authenticated"))
        }

        return com.bikemanager.domain.common.runCatching {
            ref.child(id).removeValue()
            Napier.d { "Maintenance deleted: $id" }
        }
    }
}
