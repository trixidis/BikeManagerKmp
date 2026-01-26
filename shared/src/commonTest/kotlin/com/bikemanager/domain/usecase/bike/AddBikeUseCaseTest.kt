package com.bikemanager.domain.usecase.bike

import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.fake.FakeBikeRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddBikeUseCaseTest {
    private lateinit var repository: FakeBikeRepository
    private lateinit var useCase: AddBikeUseCase

    @BeforeTest
    fun setup() {
        repository = FakeBikeRepository()
        useCase = AddBikeUseCase(repository)
    }

    @Test
    fun `invoke adds bike to repository and returns id`() = runTest {
        val bike = Bike(name = "Yamaha MT-07")

        val result = useCase(bike)

        assertTrue(result is Result.Success)
        val id = result.value
        assertTrue(id.isNotEmpty())
        val bikes = repository.getCurrentBikes()
        assertEquals(1, bikes.size)
        assertEquals("Yamaha MT-07", bikes[0].name)
    }

    @Test
    fun `invoke returns failure when bike name is empty`() = runTest {
        val bike = Bike(name = "")

        val result = useCase(bike)

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `invoke returns failure when bike name is blank`() = runTest {
        val bike = Bike(name = "   ")

        val result = useCase(bike)

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `invoke preserves counting method`() = runTest {
        val bike = Bike(name = "Honda CRF 450", countingMethod = CountingMethod.HOURS)

        val result = useCase(bike)

        assertTrue(result is Result.Success)
        val bikes = repository.getCurrentBikes()
        assertEquals(CountingMethod.HOURS, bikes[0].countingMethod)
    }

    @Test
    fun `invoke assigns unique ids to multiple bikes`() = runTest {
        val bike1 = Bike(name = "Bike 1")
        val bike2 = Bike(name = "Bike 2")

        val result1 = useCase(bike1)
        val result2 = useCase(bike2)

        assertTrue(result1 is Result.Success)
        assertTrue(result2 is Result.Success)
        val id1 = result1.value
        val id2 = result2.value
        assertTrue(id1 != id2)
        assertEquals(2, repository.getCurrentBikes().size)
    }

    @Test
    fun `invoke defaults counting method to KM`() = runTest {
        val bike = Bike(name = "Default Method Bike")

        val result = useCase(bike)

        assertTrue(result is Result.Success)
        val bikes = repository.getCurrentBikes()
        assertEquals(CountingMethod.KM, bikes[0].countingMethod)
    }
}
