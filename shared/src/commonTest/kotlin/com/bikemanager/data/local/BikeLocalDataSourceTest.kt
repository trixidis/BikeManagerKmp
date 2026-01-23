package com.bikemanager.data.local

import app.cash.turbine.test
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.fake.FakeBikeLocalDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for BikeLocalDataSource behavior using a fake implementation.
 */
class BikeLocalDataSourceTest {
    private lateinit var dataSource: FakeBikeLocalDataSource

    @BeforeTest
    fun setup() {
        dataSource = FakeBikeLocalDataSource()
    }

    @Test
    fun `getAllBikes returns empty flow when no bikes exist`() = runTest {
        dataSource.getAllBikes().test {
            val bikes = awaitItem()
            assertTrue(bikes.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAllBikes returns bikes after insertion`() = runTest {
        val bike = Bike(name = "Yamaha MT-07", countingMethod = CountingMethod.KM)
        dataSource.insertBike(bike)

        dataSource.getAllBikes().test {
            val bikes = awaitItem()
            assertEquals(1, bikes.size)
            assertEquals("Yamaha MT-07", bikes[0].name)
            assertEquals(CountingMethod.KM, bikes[0].countingMethod)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertBike returns new id and adds bike`() = runTest {
        val bike = Bike(name = "Honda CRF 450", countingMethod = CountingMethod.HOURS)

        val id = dataSource.insertBike(bike)

        assertEquals(1L, id)
        val retrieved = dataSource.getBikeById(id)
        assertEquals("Honda CRF 450", retrieved?.name)
        assertEquals(CountingMethod.HOURS, retrieved?.countingMethod)
    }

    @Test
    fun `getBikeById returns null for non-existent bike`() = runTest {
        val result = dataSource.getBikeById(999L)
        assertNull(result)
    }

    @Test
    fun `getBikeById returns correct bike`() = runTest {
        val bike1 = Bike(name = "Bike 1")
        val bike2 = Bike(name = "Bike 2")
        dataSource.insertBike(bike1)
        val id2 = dataSource.insertBike(bike2)

        val result = dataSource.getBikeById(id2)

        assertEquals("Bike 2", result?.name)
    }

    @Test
    fun `updateBike modifies existing bike`() = runTest {
        val bike = Bike(name = "Original Name", countingMethod = CountingMethod.KM)
        val id = dataSource.insertBike(bike)

        val updatedBike = Bike(
            id = id,
            name = "Updated Name",
            countingMethod = CountingMethod.HOURS
        )
        dataSource.updateBike(updatedBike)

        val result = dataSource.getBikeById(id)
        assertEquals("Updated Name", result?.name)
        assertEquals(CountingMethod.HOURS, result?.countingMethod)
    }

    @Test
    fun `deleteBike removes bike from data source`() = runTest {
        val bike = Bike(name = "To Delete")
        val id = dataSource.insertBike(bike)

        dataSource.deleteBike(id)

        val result = dataSource.getBikeById(id)
        assertNull(result)
    }

    @Test
    fun `getAllBikes emits updates when bikes change`() = runTest {
        dataSource.getAllBikes().test {
            // Initially empty
            assertTrue(awaitItem().isEmpty())

            // Add first bike
            dataSource.insertBike(Bike(name = "Bike 1"))
            assertEquals(1, awaitItem().size)

            // Add second bike
            dataSource.insertBike(Bike(name = "Bike 2"))
            assertEquals(2, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertBike preserves firebaseRef`() = runTest {
        val bike = Bike(
            name = "Synced Bike",
            countingMethod = CountingMethod.KM,
            firebaseRef = "firebase-ref-123"
        )

        val id = dataSource.insertBike(bike)

        val result = dataSource.getBikeById(id)
        assertEquals("firebase-ref-123", result?.firebaseRef)
    }
}
