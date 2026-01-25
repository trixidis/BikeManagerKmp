package com.bikemanager.domain.usecase.bike

import app.cash.turbine.test
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.fake.FakeBikeRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetBikesUseCaseTest {
    private lateinit var repository: FakeBikeRepository
    private lateinit var useCase: GetBikesUseCase

    @BeforeTest
    fun setup() {
        repository = FakeBikeRepository()
        useCase = GetBikesUseCase(repository)
    }

    @Test
    fun `invoke returns empty list when no bikes exist`() = runTest {
        useCase().test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            val bikes = result.value
            assertTrue(bikes.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke returns bikes from repository`() = runTest {
        val expectedBikes = listOf(
            Bike(id = "bike1", name = "Yamaha MT-07", countingMethod = CountingMethod.KM),
            Bike(id = "bike2", name = "Honda CRF 450", countingMethod = CountingMethod.HOURS)
        )
        repository.setBikes(expectedBikes)

        useCase().test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            val bikes = result.value
            assertEquals(2, bikes.size)
            assertEquals("Yamaha MT-07", bikes[0].name)
            assertEquals("Honda CRF 450", bikes[1].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke emits updates when bikes change`() = runTest {
        useCase().test {
            // Initially empty
            val initialResult = awaitItem()
            assertTrue(initialResult is Result.Success)
            val initial = initialResult.value
            assertTrue(initial.isEmpty())

            // Add a bike
            repository.setBikes(listOf(Bike(id = "bike1", name = "Kawasaki Ninja")))
            val afterAddResult = awaitItem()
            assertTrue(afterAddResult is Result.Success)
            val afterAdd = afterAddResult.value
            assertEquals(1, afterAdd.size)
            assertEquals("Kawasaki Ninja", afterAdd[0].name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke returns bikes with correct counting methods`() = runTest {
        val bikes = listOf(
            Bike(id = "bike1", name = "Road Bike", countingMethod = CountingMethod.KM),
            Bike(id = "bike2", name = "Dirt Bike", countingMethod = CountingMethod.HOURS)
        )
        repository.setBikes(bikes)

        useCase().test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            val bikesResult = result.value
            assertEquals(CountingMethod.KM, bikesResult[0].countingMethod)
            assertEquals(CountingMethod.HOURS, bikesResult[1].countingMethod)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
