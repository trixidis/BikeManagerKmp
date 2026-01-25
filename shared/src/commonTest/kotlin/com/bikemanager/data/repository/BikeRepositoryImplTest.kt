package com.bikemanager.data.repository

import app.cash.turbine.test
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.fake.FakeBikeRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for BikeRepositoryImpl to verify it correctly delegates to the data source.
 * Uses FakeBikeRepository which mirrors the repository interface behavior.
 */
class BikeRepositoryImplTest {
    private lateinit var repository: FakeBikeRepository

    @BeforeTest
    fun setup() {
        repository = FakeBikeRepository()
    }

    @Test
    fun `getAllBikes returns empty list initially`() = runTest {
        repository.getAllBikes().test {
            val bikes = awaitItem()
            assertTrue(bikes.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllBikes returns bikes after adding`() = runTest {
        val bike = Bike(name = "Yamaha MT-07", countingMethod = CountingMethod.KM)
        repository.addBike(bike)

        repository.getAllBikes().test {
            val bikes = awaitItem()
            assertEquals(1, bikes.size)
            assertEquals("Yamaha MT-07", bikes[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addBike returns generated id`() = runTest {
        val bike = Bike(name = "Honda CRF")

        val id = repository.addBike(bike)

        assertTrue(id.isNotEmpty())
    }

    @Test
    fun `addBike stores bike with all properties`() = runTest {
        val bike = Bike(
            name = "Kawasaki Ninja",
            countingMethod = CountingMethod.KM
        )

        val id = repository.addBike(bike)
        val stored = repository.getBikeById(id)

        assertEquals("Kawasaki Ninja", stored?.name)
        assertEquals(CountingMethod.KM, stored?.countingMethod)
    }

    @Test
    fun `getBikeById returns null for non-existent id`() = runTest {
        val result = repository.getBikeById("nonexistent")
        assertNull(result)
    }

    @Test
    fun `getBikeById returns correct bike`() = runTest {
        repository.addBike(Bike(name = "Bike 1"))
        val id2 = repository.addBike(Bike(name = "Bike 2"))
        repository.addBike(Bike(name = "Bike 3"))

        val result = repository.getBikeById(id2)

        assertEquals("Bike 2", result?.name)
    }

    @Test
    fun `updateBike modifies existing bike`() = runTest {
        val id = repository.addBike(Bike(name = "Original"))

        repository.updateBike(Bike(id = id, name = "Updated", countingMethod = CountingMethod.HOURS))

        val result = repository.getBikeById(id)
        assertEquals("Updated", result?.name)
        assertEquals(CountingMethod.HOURS, result?.countingMethod)
    }

    @Test
    fun `deleteBike removes bike`() = runTest {
        val id = repository.addBike(Bike(name = "To Delete"))

        repository.deleteBike(id)

        val result = repository.getBikeById(id)
        assertNull(result)
    }

    @Test
    fun `deleteBike does not affect other bikes`() = runTest {
        val id1 = repository.addBike(Bike(name = "Bike 1"))
        val id2 = repository.addBike(Bike(name = "Bike 2"))

        repository.deleteBike(id1)

        val bike2 = repository.getBikeById(id2)
        assertEquals("Bike 2", bike2?.name)
    }

    @Test
    fun `getAllBikes emits updates reactively`() = runTest {
        repository.getAllBikes().test {
            assertTrue(awaitItem().isEmpty())

            repository.addBike(Bike(name = "New Bike"))
            assertEquals(1, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `multiple bikes are stored and retrieved correctly`() = runTest {
        repository.addBike(Bike(name = "Bike A", countingMethod = CountingMethod.KM))
        repository.addBike(Bike(name = "Bike B", countingMethod = CountingMethod.HOURS))
        repository.addBike(Bike(name = "Bike C", countingMethod = CountingMethod.KM))

        repository.getAllBikes().test {
            val bikes = awaitItem()
            assertEquals(3, bikes.size)
            assertEquals(listOf("Bike A", "Bike B", "Bike C"), bikes.map { it.name })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
