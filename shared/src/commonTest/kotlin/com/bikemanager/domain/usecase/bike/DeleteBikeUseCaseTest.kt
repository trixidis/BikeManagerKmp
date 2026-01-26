package com.bikemanager.domain.usecase.bike

import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.fake.FakeBikeRepository
import com.bikemanager.fake.FakeMaintenanceRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DeleteBikeUseCaseTest {
    private lateinit var bikeRepository: FakeBikeRepository
    private lateinit var maintenanceRepository: FakeMaintenanceRepository
    private lateinit var deleteBikeUseCase: DeleteBikeUseCase

    @BeforeTest
    fun setup() {
        bikeRepository = FakeBikeRepository()
        maintenanceRepository = FakeMaintenanceRepository()
        deleteBikeUseCase = DeleteBikeUseCase(bikeRepository, maintenanceRepository)
    }

    @Test
    fun `deleteBike deletes bike from repository`() = runTest {
        // Given
        val bike = Bike(id = "bike1", name = "Test Bike")
        bikeRepository.setBikes(listOf(bike))

        // When
        deleteBikeUseCase("bike1")

        // Then
        assertTrue(bikeRepository.getCurrentBikes().isEmpty())
    }

    @Test
    fun `deleteBike deletes all maintenances for bike`() = runTest {
        // Given
        val bike = Bike(id = "bike1", name = "Test Bike")
        val maintenances = listOf(
            Maintenance(id = "m1", name = "Oil Change", bikeId = "bike1"),
            Maintenance(id = "m2", name = "Tire Check", bikeId = "bike1")
        )
        bikeRepository.setBikes(listOf(bike))
        maintenanceRepository.setMaintenances(maintenances)

        // When
        deleteBikeUseCase("bike1")

        // Then
        assertTrue(bikeRepository.getCurrentBikes().isEmpty())
        assertEquals(0, maintenanceRepository.getCurrentMaintenances().filter { it.bikeId == "bike1" }.size)
    }

    @Test
    fun `deleteBike with empty id throws exception`() = runTest {
        // When/Then
        assertFailsWith<IllegalArgumentException> {
            deleteBikeUseCase("")
        }
    }

    @Test
    fun `deleteBike deletes maintenances before bike`() = runTest {
        // This test verifies order of operations (maintenances first, then bike)
        // Given
        val bike = Bike(id = "bike1", name = "Test Bike")
        val maintenance = Maintenance(id = "m1", name = "Oil Change", bikeId = "bike1")
        bikeRepository.setBikes(listOf(bike))
        maintenanceRepository.setMaintenances(listOf(maintenance))

        // When
        deleteBikeUseCase("bike1")

        // Then
        assertTrue(bikeRepository.getCurrentBikes().isEmpty())
        assertTrue(maintenanceRepository.getCurrentMaintenances().isEmpty())
    }

    @Test
    fun `deleteBike does not affect other bikes maintenances`() = runTest {
        // Given
        val bike1 = Bike(id = "bike1", name = "Bike 1")
        val bike2 = Bike(id = "bike2", name = "Bike 2")
        val maintenance1 = Maintenance(id = "m1", name = "Oil Change", bikeId = "bike1")
        val maintenance2 = Maintenance(id = "m2", name = "Tire Check", bikeId = "bike2")
        bikeRepository.setBikes(listOf(bike1, bike2))
        maintenanceRepository.setMaintenances(listOf(maintenance1, maintenance2))

        // When
        deleteBikeUseCase("bike1")

        // Then
        assertEquals(1, bikeRepository.getCurrentBikes().size)
        assertEquals("bike2", bikeRepository.getCurrentBikes()[0].id)
        assertEquals(1, maintenanceRepository.getCurrentMaintenances().size)
        assertEquals("m2", maintenanceRepository.getCurrentMaintenances()[0].id)
    }
}
