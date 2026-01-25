package com.bikemanager.domain.usecase.bike

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.fake.FakeBikeRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdateBikeUseCaseTest {
    private lateinit var repository: FakeBikeRepository
    private lateinit var useCase: UpdateBikeUseCase

    @BeforeTest
    fun setup() {
        repository = FakeBikeRepository()
        useCase = UpdateBikeUseCase(repository)
    }

    @Test
    fun `invoke updates bike name in repository`() = runTest {
        val initialBike = Bike(id = "bike1", name = "Old Name")
        repository.setBikes(listOf(initialBike))

        val updatedBike = initialBike.copy(name = "New Name")
        useCase(updatedBike)

        val bikes = repository.getCurrentBikes()
        assertEquals("New Name", bikes[0].name)
    }

    @Test
    fun `invoke updates counting method`() = runTest {
        val initialBike = Bike(id = "bike1", name = "Bike", countingMethod = CountingMethod.KM)
        repository.setBikes(listOf(initialBike))

        val updatedBike = initialBike.copy(countingMethod = CountingMethod.HOURS)
        useCase(updatedBike)

        val bikes = repository.getCurrentBikes()
        assertEquals(CountingMethod.HOURS, bikes[0].countingMethod)
    }

    @Test
    fun `invoke throws exception when bike name is empty`() = runTest {
        val initialBike = Bike(id = "bike1", name = "Valid Name")
        repository.setBikes(listOf(initialBike))

        val invalidBike = initialBike.copy(name = "")

        assertFailsWith<IllegalArgumentException> {
            useCase(invalidBike)
        }
    }

    @Test
    fun `invoke throws exception when bike name is blank`() = runTest {
        val initialBike = Bike(id = "bike1", name = "Valid Name")
        repository.setBikes(listOf(initialBike))

        val invalidBike = initialBike.copy(name = "   ")

        assertFailsWith<IllegalArgumentException> {
            useCase(invalidBike)
        }
    }

    @Test
    fun `invoke only updates specified bike`() = runTest {
        val bike1 = Bike(id = "bike1", name = "Bike 1")
        val bike2 = Bike(id = "bike2", name = "Bike 2")
        repository.setBikes(listOf(bike1, bike2))

        val updatedBike1 = bike1.copy(name = "Updated Bike 1")
        useCase(updatedBike1)

        val bikes = repository.getCurrentBikes()
        assertEquals("Updated Bike 1", bikes.find { it.id == "bike1" }?.name)
        assertEquals("Bike 2", bikes.find { it.id == "bike2" }?.name)
    }

    @Test
    fun `invoke preserves id during update`() = runTest {
        val initialBike = Bike(id = "bike42", name = "Initial")
        repository.setBikes(listOf(initialBike))

        val updatedBike = initialBike.copy(name = "Updated")
        useCase(updatedBike)

        val bikes = repository.getCurrentBikes()
        assertEquals("bike42", bikes[0].id)
    }
}
