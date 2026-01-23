package com.bikemanager.domain.usecase.maintenance

import app.cash.turbine.test
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.fake.FakeMaintenanceRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetMaintenancesUseCaseTest {
    private lateinit var repository: FakeMaintenanceRepository
    private lateinit var useCase: GetMaintenancesUseCase

    @BeforeTest
    fun setup() {
        repository = FakeMaintenanceRepository()
        useCase = GetMaintenancesUseCase(repository)
    }

    @Test
    fun `invoke returns empty lists when no maintenances exist`() = runTest {
        useCase(bikeId = 1L).test {
            val (done, todo) = awaitItem()
            assertTrue(done.isEmpty())
            assertTrue(todo.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke returns done and todo maintenances separately`() = runTest {
        val maintenances = listOf(
            Maintenance(id = 1, name = "Done 1", isDone = true, bikeId = 1L),
            Maintenance(id = 2, name = "Done 2", isDone = true, bikeId = 1L),
            Maintenance(id = 3, name = "Todo 1", isDone = false, bikeId = 1L)
        )
        repository.setMaintenances(maintenances)

        useCase(bikeId = 1L).test {
            val (done, todo) = awaitItem()
            assertEquals(2, done.size)
            assertEquals(1, todo.size)
            assertEquals("Done 1", done[0].name)
            assertEquals("Done 2", done[1].name)
            assertEquals("Todo 1", todo[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke filters by bike id`() = runTest {
        val maintenances = listOf(
            Maintenance(id = 1, name = "Bike 1 Maintenance", isDone = true, bikeId = 1L),
            Maintenance(id = 2, name = "Bike 2 Maintenance", isDone = true, bikeId = 2L)
        )
        repository.setMaintenances(maintenances)

        useCase(bikeId = 1L).test {
            val (done, _) = awaitItem()
            assertEquals(1, done.size)
            assertEquals("Bike 1 Maintenance", done[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getDone returns only done maintenances`() = runTest {
        val maintenances = listOf(
            Maintenance(id = 1, name = "Done", isDone = true, bikeId = 1L),
            Maintenance(id = 2, name = "Todo", isDone = false, bikeId = 1L)
        )
        repository.setMaintenances(maintenances)

        useCase.getDone(bikeId = 1L).test {
            val done = awaitItem()
            assertEquals(1, done.size)
            assertEquals("Done", done[0].name)
            assertTrue(done[0].isDone)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTodo returns only todo maintenances`() = runTest {
        val maintenances = listOf(
            Maintenance(id = 1, name = "Done", isDone = true, bikeId = 1L),
            Maintenance(id = 2, name = "Todo", isDone = false, bikeId = 1L)
        )
        repository.setMaintenances(maintenances)

        useCase.getTodo(bikeId = 1L).test {
            val todo = awaitItem()
            assertEquals(1, todo.size)
            assertEquals("Todo", todo[0].name)
            assertTrue(!todo[0].isDone)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke emits updates when maintenances change`() = runTest {
        // First, set up data before collecting
        repository.setMaintenances(emptyList())

        useCase(bikeId = 1L).test {
            // Initially empty
            val (done1, todo1) = awaitItem()
            assertTrue(done1.isEmpty())
            assertTrue(todo1.isEmpty())

            // Add maintenances - the flow should emit
            repository.setMaintenances(
                listOf(
                    Maintenance(id = 1, name = "Done", isDone = true, bikeId = 1L),
                    Maintenance(id = 2, name = "Todo", isDone = false, bikeId = 1L)
                )
            )

            // Wait for any emissions (could be one or two depending on combine timing)
            val result = expectMostRecentItem()
            assertEquals(1, result.first.size)
            assertEquals(1, result.second.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke includes maintenance details like value and date`() = runTest {
        val maintenances = listOf(
            Maintenance(
                id = 1,
                name = "Oil Change",
                value = 5000f,
                date = 1700000000000L,
                isDone = true,
                bikeId = 1L
            )
        )
        repository.setMaintenances(maintenances)

        useCase(bikeId = 1L).test {
            val (done, _) = awaitItem()
            assertEquals(5000f, done[0].value)
            assertEquals(1700000000000L, done[0].date)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
