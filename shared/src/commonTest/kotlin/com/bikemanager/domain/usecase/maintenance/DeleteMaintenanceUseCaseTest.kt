package com.bikemanager.domain.usecase.maintenance

import com.bikemanager.domain.model.Maintenance
import com.bikemanager.fake.FakeMaintenanceRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeleteMaintenanceUseCaseTest {
    private lateinit var repository: FakeMaintenanceRepository
    private lateinit var useCase: DeleteMaintenanceUseCase

    @BeforeTest
    fun setup() {
        repository = FakeMaintenanceRepository()
        useCase = DeleteMaintenanceUseCase(repository)
    }

    @Test
    fun `invoke deletes maintenance from repository`() = runTest {
        val maintenance = Maintenance(id = "m1", name = "Oil Change", bikeId = "bike1")
        repository.setMaintenances(listOf(maintenance))

        useCase(maintenanceId = "m1", bikeId = "bike1")

        val maintenances = repository.getCurrentMaintenances()
        assertTrue(maintenances.isEmpty())
    }

    @Test
    fun `invoke only deletes specified maintenance`() = runTest {
        val maintenances = listOf(
            Maintenance(id = "m1", name = "Maintenance 1", bikeId = "bike1"),
            Maintenance(id = "m2", name = "Maintenance 2", bikeId = "bike1"),
            Maintenance(id = "m3", name = "Maintenance 3", bikeId = "bike1")
        )
        repository.setMaintenances(maintenances)

        useCase(maintenanceId = "m2", bikeId = "bike1")

        val result = repository.getCurrentMaintenances()
        assertEquals(2, result.size)
        assertTrue(result.none { it.id == "m2" })
        assertTrue(result.any { it.id == "m1" })
        assertTrue(result.any { it.id == "m3" })
    }

    @Test
    fun `invoke with non-existent id does not throw`() = runTest {
        val maintenance = Maintenance(id = "m1", name = "Test", bikeId = "bike1")
        repository.setMaintenances(listOf(maintenance))

        // Should not throw
        useCase(maintenanceId = "nonexistent", bikeId = "bike1")

        // Original maintenance should still exist
        val result = repository.getCurrentMaintenances()
        assertEquals(1, result.size)
    }

    @Test
    fun `invoke deletes done maintenance`() = runTest {
        val maintenance = Maintenance(
            id = "m1",
            name = "Done Maintenance",
            isDone = true,
            value = 5000f,
            date = 1700000000000L,
            bikeId = "bike1"
        )
        repository.setMaintenances(listOf(maintenance))

        useCase(maintenanceId = "m1", bikeId = "bike1")

        assertTrue(repository.getCurrentMaintenances().isEmpty())
    }

    @Test
    fun `invoke deletes todo maintenance`() = runTest {
        val maintenance = Maintenance(
            id = "m1",
            name = "Todo Maintenance",
            isDone = false,
            value = -1f,
            date = 0,
            bikeId = "bike1"
        )
        repository.setMaintenances(listOf(maintenance))

        useCase(maintenanceId = "m1", bikeId = "bike1")

        assertTrue(repository.getCurrentMaintenances().isEmpty())
    }

    @Test
    fun `invoke can delete multiple maintenances sequentially`() = runTest {
        val maintenances = listOf(
            Maintenance(id = "m1", name = "Maintenance 1", bikeId = "bike1"),
            Maintenance(id = "m2", name = "Maintenance 2", bikeId = "bike1")
        )
        repository.setMaintenances(maintenances)

        useCase(maintenanceId = "m1", bikeId = "bike1")
        useCase(maintenanceId = "m2", bikeId = "bike1")

        assertTrue(repository.getCurrentMaintenances().isEmpty())
    }

    @Test
    fun `invoke requires bikeId match`() = runTest {
        val maintenances = listOf(
            Maintenance(id = "m1", name = "Bike 1 Maintenance", bikeId = "bike1"),
            Maintenance(id = "m2", name = "Bike 2 Maintenance", bikeId = "bike2")
        )
        repository.setMaintenances(maintenances)

        useCase(maintenanceId = "m2", bikeId = "bike2")

        val result = repository.getCurrentMaintenances()
        assertEquals(1, result.size)
        assertEquals("bike1", result[0].bikeId)
    }
}
