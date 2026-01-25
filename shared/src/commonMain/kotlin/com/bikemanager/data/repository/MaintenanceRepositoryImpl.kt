package com.bikemanager.data.repository

import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.repository.AuthRepository
import com.bikemanager.domain.repository.MaintenanceRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.database.database
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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

    private fun parseMaintenances(bikeId: String): Flow<List<Maintenance>> {
        val ref = maintenancesRef(bikeId) ?: return emptyFlow()

        return ref.valueEvents.map { snapshot ->
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
            }
        }
    }

    override fun getMaintenancesByBikeId(bikeId: String): Flow<List<Maintenance>> {
        return parseMaintenances(bikeId).map { maintenances ->
            maintenances.sortedByDescending { it.value }
        }
    }

    override fun getDoneMaintenances(bikeId: String): Flow<List<Maintenance>> {
        return parseMaintenances(bikeId).map { maintenances ->
            maintenances.filter { it.isDone }.sortedByDescending { it.value }
        }
    }

    override fun getTodoMaintenances(bikeId: String): Flow<List<Maintenance>> {
        return parseMaintenances(bikeId).map { maintenances ->
            maintenances.filter { !it.isDone }.sortedByDescending { it.id }
        }
    }

    override suspend fun addMaintenance(maintenance: Maintenance): String {
        val ref = maintenancesRef(maintenance.bikeId)
            ?: throw IllegalStateException("User not authenticated")

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
        return key
    }

    override suspend fun updateMaintenance(maintenance: Maintenance) {
        val ref = maintenancesRef(maintenance.bikeId)
            ?: throw IllegalStateException("User not authenticated")
        require(maintenance.id.isNotEmpty()) { "Maintenance id cannot be empty" }

        val data = mapOf(
            "nameMaintenance" to maintenance.name,
            "nbHoursMaintenance" to maintenance.value,
            "dateMaintenance" to maintenance.date,
            "isDone" to maintenance.isDone
        )
        ref.child(maintenance.id).setValue(data)
        Napier.d { "Maintenance updated: ${maintenance.name}" }
    }

    override suspend fun markMaintenanceDone(id: String, bikeId: String, value: Float, date: Long) {
        val ref = maintenancesRef(bikeId)
            ?: throw IllegalStateException("User not authenticated")

        val updates = mapOf(
            "isDone" to true,
            "nbHoursMaintenance" to value,
            "dateMaintenance" to date
        )
        ref.child(id).updateChildren(updates)
        Napier.d { "Maintenance marked done: $id" }
    }

    override suspend fun deleteMaintenance(id: String, bikeId: String) {
        val ref = maintenancesRef(bikeId)
            ?: throw IllegalStateException("User not authenticated")

        ref.child(id).removeValue()
        Napier.d { "Maintenance deleted: $id" }
    }

    override suspend fun deleteAllMaintenancesForBike(bikeId: String) {
        val ref = maintenancesRef(bikeId)
            ?: throw IllegalStateException("User not authenticated")

        ref.removeValue()
        Napier.d { "All maintenances deleted for bike: $bikeId" }
    }
}
