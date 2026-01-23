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
        val maintenance = Maintenance(id = 1, name = "Oil Change", bikeId = 1L)
        repository.setMaintenances(listOf(maintenance))

        useCase(maintenanceId = 1L)

        val maintenances = repository.getCurrentMaintenances()
        assertTrue(maintenances.isEmpty())
    }

    @Test
    fun `invoke only deletes specified maintenance`() = runTest {
        val maintenances = listOf(
            Maintenance(id = 1, name = "Maintenance 1", bikeId = 1L),
            Maintenance(id = 2, name = "Maintenance 2", bikeId = 1L),
            Maintenance(id = 3, name = "Maintenance 3", bikeId = 1L)
        )
        repository.setMaintenances(maintenances)

        useCase(maintenanceId = 2L)

        val result = repository.getCurrentMaintenances()
        assertEquals(2, result.size)
        assertTrue(result.none { it.id == 2L })
        assertTrue(result.any { it.id == 1L })
        assertTrue(result.any { it.id == 3L })
    }

    @Test
    fun `invoke with non-existent id does not throw`() = runTest {
        val maintenance = Maintenance(id = 1, name = "Test", bikeId = 1L)
        repository.setMaintenances(listOf(maintenance))

        // Should not throw
        useCase(maintenanceId = 999L)

        // Original maintenance should still exist
        val result = repository.getCurrentMaintenances()
        assertEquals(1, result.size)
    }

    @Test
    fun `invoke deletes done maintenance`() = runTest {
        val maintenance = Maintenance(
            id = 1,
            name = "Done Maintenance",
            isDone = true,
            value = 5000f,
            date = 1700000000000L,
            bikeId = 1L
        )
        repository.setMaintenances(listOf(maintenance))

        useCase(maintenanceId = 1L)

        assertTrue(repository.getCurrentMaintenances().isEmpty())
    }

    @Test
    fun `invoke deletes todo maintenance`() = runTest {
        val maintenance = Maintenance(
            id = 1,
            name = "Todo Maintenance",
            isDone = false,
            value = -1f,
            date = 0,
            bikeId = 1L
        )
        repository.setMaintenances(listOf(maintenance))

        useCase(maintenanceId = 1L)

        assertTrue(repository.getCurrentMaintenances().isEmpty())
    }

    @Test
    fun `invoke can delete multiple maintenances sequentially`() = runTest {
        val maintenances = listOf(
            Maintenance(id = 1, name = "Maintenance 1", bikeId = 1L),
            Maintenance(id = 2, name = "Maintenance 2", bikeId = 1L)
        )
        repository.setMaintenances(maintenances)

        useCase(maintenanceId = 1L)
        useCase(maintenanceId = 2L)

        assertTrue(repository.getCurrentMaintenances().isEmpty())
    }

    @Test
    fun `invoke deletes maintenance regardless of bike id`() = runTest {
        val maintenances = listOf(
            Maintenance(id = 1, name = "Bike 1 Maintenance", bikeId = 1L),
            Maintenance(id = 2, name = "Bike 2 Maintenance", bikeId = 2L)
        )
        repository.setMaintenances(maintenances)

        useCase(maintenanceId = 2L)

        val result = repository.getCurrentMaintenances()
        assertEquals(1, result.size)
        assertEquals(1L, result[0].bikeId)
    }
}
