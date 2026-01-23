package com.bikemanager.domain.usecase.sync

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.usecase.maintenance.AddMaintenanceUseCase
import com.bikemanager.fake.FakeBikeRepository
import com.bikemanager.fake.FakeMaintenanceRepository
import com.bikemanager.fake.FakeSyncRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SyncMaintenanceUseCaseTest {
    private lateinit var bikeRepository: FakeBikeRepository
    private lateinit var maintenanceRepository: FakeMaintenanceRepository
    private lateinit var syncRepository: FakeSyncRepository
    private lateinit var addMaintenanceUseCase: AddMaintenanceUseCase

    @BeforeTest
    fun setup() {
        bikeRepository = FakeBikeRepository()
        maintenanceRepository = FakeMaintenanceRepository()
        syncRepository = FakeSyncRepository()
        addMaintenanceUseCase = AddMaintenanceUseCase(
            maintenanceRepository,
            bikeRepository,
            syncRepository
        )
    }

    @Test
    fun `addMaintenance syncs when connected and bike has firebase ref`() = runTest {
        syncRepository.setConnected(true)
        // Add a bike with firebase ref
        val bike = Bike(id = 1, name = "Test Bike", firebaseRef = "bike-ref-123")
        bikeRepository.setBikes(listOf(bike))

        val maintenanceId = addMaintenanceUseCase.addDone("Oil Change", 5000f, 1)

        assertTrue(maintenanceId > 0)
        assertEquals(1, syncRepository.syncedMaintenances.size)
        assertEquals("Oil Change", syncRepository.syncedMaintenances[0].first.name)
        assertEquals("bike-ref-123", syncRepository.syncedMaintenances[0].second)
    }

    @Test
    fun `addMaintenance does not sync when not connected`() = runTest {
        syncRepository.setConnected(false)
        val bike = Bike(id = 1, name = "Test Bike", firebaseRef = "bike-ref-123")
        bikeRepository.setBikes(listOf(bike))

        val maintenanceId = addMaintenanceUseCase.addDone("Oil Change", 5000f, 1)

        assertTrue(maintenanceId > 0)
        assertTrue(syncRepository.syncedMaintenances.isEmpty())
    }

    @Test
    fun `addMaintenance does not sync when bike has no firebase ref`() = runTest {
        syncRepository.setConnected(true)
        // Bike without firebase ref
        val bike = Bike(id = 1, name = "Test Bike", firebaseRef = null)
        bikeRepository.setBikes(listOf(bike))

        val maintenanceId = addMaintenanceUseCase.addDone("Oil Change", 5000f, 1)

        assertTrue(maintenanceId > 0)
        assertTrue(syncRepository.syncedMaintenances.isEmpty())
    }

    @Test
    fun `addTodo maintenance syncs correctly`() = runTest {
        syncRepository.setConnected(true)
        val bike = Bike(id = 1, name = "Test Bike", firebaseRef = "bike-ref-123")
        bikeRepository.setBikes(listOf(bike))

        val maintenanceId = addMaintenanceUseCase.addTodo("Future Service", 1)

        assertTrue(maintenanceId > 0)
        assertEquals(1, syncRepository.syncedMaintenances.size)
        assertEquals("Future Service", syncRepository.syncedMaintenances[0].first.name)
    }

    @Test
    fun `addMaintenance without sync repository works normally`() = runTest {
        val useCaseWithoutSync = AddMaintenanceUseCase(maintenanceRepository, null, null)
        val bike = Bike(id = 1, name = "Test Bike")
        bikeRepository.setBikes(listOf(bike))

        val maintenanceId = useCaseWithoutSync.addDone("Oil Change", 5000f, 1)

        assertTrue(maintenanceId > 0)
        val maintenances = maintenanceRepository.getMaintenancesByBikeId(1).first()
        assertEquals(1, maintenances.size)
    }

    @Test
    fun `addMaintenance updates local maintenance with firebase reference`() = runTest {
        syncRepository.setConnected(true)
        val bike = Bike(id = 1, name = "Test Bike", firebaseRef = "bike-ref-123")
        bikeRepository.setBikes(listOf(bike))

        addMaintenanceUseCase.addDone("Oil Change", 5000f, 1)

        val maintenances = maintenanceRepository.getMaintenancesByBikeId(1).first()
        assertEquals(1, maintenances.size)
        assertNotNull(maintenances[0].firebaseRef)
        assertTrue(maintenances[0].firebaseRef!!.startsWith("maintenance-ref-"))
    }
}
