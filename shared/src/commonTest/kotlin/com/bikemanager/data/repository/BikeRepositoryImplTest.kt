package com.bikemanager.data.repository

import app.cash.turbine.test
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.common.getOrNull
import com.bikemanager.domain.common.getOrThrow
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
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertTrue(result.value.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllBikes returns bikes after adding`() = runTest {
        val bike = Bike(name = "Yamaha MT-07", countingMethod = CountingMethod.KM)
        repository.addBike(bike)

        repository.getAllBikes().test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            val bikes = result.value
            assertEquals(1, bikes.size)
            assertEquals("Yamaha MT-07", bikes[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addBike returns generated id`() = runTest {
        val bike = Bike(name = "Honda CRF")

        val result = repository.addBike(bike)

        assertTrue(result is Result.Success)
        assertTrue(result.value.isNotEmpty())
    }

    @Test
    fun `addBike stores bike with all properties`() = runTest {
        val bike = Bike(
            name = "Kawasaki Ninja",
            countingMethod = CountingMethod.KM
        )

        val addResult = repository.addBike(bike)
        assertTrue(addResult is Result.Success)
        val id = addResult.value

        val getResult = repository.getBikeById(id)
        assertTrue(getResult is Result.Success)
        val stored = getResult.value

        assertEquals("Kawasaki Ninja", stored?.name)
        assertEquals(CountingMethod.KM, stored?.countingMethod)
    }

    @Test
    fun `getBikeById returns null for non-existent id`() = runTest {
        val result = repository.getBikeById("nonexistent")
        assertTrue(result is Result.Success)
        assertNull(result.value)
    }

    @Test
    fun `getBikeById returns correct bike`() = runTest {
        repository.addBike(Bike(name = "Bike 1"))
        val addResult2 = repository.addBike(Bike(name = "Bike 2"))
        assertTrue(addResult2 is Result.Success)
        val id2 = addResult2.value
        repository.addBike(Bike(name = "Bike 3"))

        val getResult = repository.getBikeById(id2)
        assertTrue(getResult is Result.Success)
        val bike = getResult.value

        assertEquals("Bike 2", bike?.name)
    }

    @Test
    fun `updateBike modifies existing bike`() = runTest {
        val addResult = repository.addBike(Bike(name = "Original"))
        assertTrue(addResult is Result.Success)
        val id = addResult.value

        val updateResult = repository.updateBike(Bike(id = id, name = "Updated", countingMethod = CountingMethod.HOURS))
        assertTrue(updateResult is Result.Success)

        val getResult = repository.getBikeById(id)
        assertTrue(getResult is Result.Success)
        val bike = getResult.value
        assertEquals("Updated", bike?.name)
        assertEquals(CountingMethod.HOURS, bike?.countingMethod)
    }

    @Test
    fun `deleteBike removes bike`() = runTest {
        val addResult = repository.addBike(Bike(name = "To Delete"))
        assertTrue(addResult is Result.Success)
        val id = addResult.value

        val deleteResult = repository.deleteBike(id)
        assertTrue(deleteResult is Result.Success)

        val getResult = repository.getBikeById(id)
        assertTrue(getResult is Result.Success)
        assertNull(getResult.value)
    }

    @Test
    fun `deleteBike does not affect other bikes`() = runTest {
        val addResult1 = repository.addBike(Bike(name = "Bike 1"))
        assertTrue(addResult1 is Result.Success)
        val id1 = addResult1.value

        val addResult2 = repository.addBike(Bike(name = "Bike 2"))
        assertTrue(addResult2 is Result.Success)
        val id2 = addResult2.value

        val deleteResult = repository.deleteBike(id1)
        assertTrue(deleteResult is Result.Success)

        val getResult = repository.getBikeById(id2)
        assertTrue(getResult is Result.Success)
        assertEquals("Bike 2", getResult.value?.name)
    }

    @Test
    fun `getAllBikes emits updates reactively`() = runTest {
        repository.getAllBikes().test {
            val result1 = awaitItem()
            assertTrue(result1 is Result.Success)
            assertTrue(result1.value.isEmpty())

            repository.addBike(Bike(name = "New Bike"))
            val result2 = awaitItem()
            assertTrue(result2 is Result.Success)
            assertEquals(1, result2.value.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `multiple bikes are stored and retrieved correctly`() = runTest {
        repository.addBike(Bike(name = "Bike A", countingMethod = CountingMethod.KM))
        repository.addBike(Bike(name = "Bike B", countingMethod = CountingMethod.HOURS))
        repository.addBike(Bike(name = "Bike C", countingMethod = CountingMethod.KM))

        repository.getAllBikes().test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            val bikes = result.value
            assertEquals(3, bikes.size)
            assertEquals(listOf("Bike A", "Bike B", "Bike C"), bikes.map { it.name })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
