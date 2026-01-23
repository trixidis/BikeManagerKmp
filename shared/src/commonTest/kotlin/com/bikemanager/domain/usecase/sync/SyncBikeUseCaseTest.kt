package com.bikemanager.domain.usecase.sync

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.usecase.bike.AddBikeUseCase
import com.bikemanager.fake.FakeBikeRepository
import com.bikemanager.fake.FakeSyncRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SyncBikeUseCaseTest {
    private lateinit var bikeRepository: FakeBikeRepository
    private lateinit var syncRepository: FakeSyncRepository
    private lateinit var addBikeUseCase: AddBikeUseCase

    @BeforeTest
    fun setup() {
        bikeRepository = FakeBikeRepository()
        syncRepository = FakeSyncRepository()
        addBikeUseCase = AddBikeUseCase(bikeRepository, syncRepository)
    }

    @Test
    fun `addBike syncs to firebase when connected`() = runTest {
        syncRepository.setConnected(true)
        val bike = Bike(name = "Test Bike", countingMethod = CountingMethod.KM)

        val bikeId = addBikeUseCase(bike)

        assertTrue(bikeId > 0)
        assertEquals(1, syncRepository.syncedBikes.size)
        assertEquals("Test Bike", syncRepository.syncedBikes[0].name)
    }

    @Test
    fun `addBike does not sync when not connected`() = runTest {
        syncRepository.setConnected(false)
        val bike = Bike(name = "Test Bike", countingMethod = CountingMethod.KM)

        val bikeId = addBikeUseCase(bike)

        assertTrue(bikeId > 0)
        assertTrue(syncRepository.syncedBikes.isEmpty())
    }

    @Test
    fun `addBike updates local bike with firebase reference after sync`() = runTest {
        syncRepository.setConnected(true)
        val bike = Bike(name = "Test Bike", countingMethod = CountingMethod.KM)

        addBikeUseCase(bike)

        val bikes = bikeRepository.getAllBikes().first()
        assertEquals(1, bikes.size)
        assertNotNull(bikes[0].firebaseRef)
        assertTrue(bikes[0].firebaseRef!!.startsWith("bike-ref-"))
    }

    @Test
    fun `addBike still succeeds when sync fails`() = runTest {
        // Bike is added but sync silently fails
        syncRepository.setConnected(true)
        // Disconnect right after check to simulate network failure during sync
        val useCase = AddBikeUseCase(bikeRepository, object : FakeSyncRepository() {
            init { setConnected(true) }
            override suspend fun syncBike(bike: Bike): String {
                throw RuntimeException("Network error")
            }
        })

        val bike = Bike(name = "Test Bike", countingMethod = CountingMethod.KM)
        val bikeId = useCase(bike)

        assertTrue(bikeId > 0)
        val bikes = bikeRepository.getAllBikes().first()
        assertEquals(1, bikes.size)
        // Firebase ref should be null since sync failed
        assertNull(bikes[0].firebaseRef)
    }

    @Test
    fun `addBike without syncRepository works normally`() = runTest {
        val useCaseWithoutSync = AddBikeUseCase(bikeRepository, null)
        val bike = Bike(name = "Test Bike", countingMethod = CountingMethod.KM)

        val bikeId = useCaseWithoutSync(bike)

        assertTrue(bikeId > 0)
        val bikes = bikeRepository.getAllBikes().first()
        assertEquals(1, bikes.size)
        assertEquals("Test Bike", bikes[0].name)
    }
}
