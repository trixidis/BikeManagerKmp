package com.bikemanager.domain.usecase.maintenance

import app.cash.turbine.test
import com.bikemanager.domain.common.Result
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
        useCase(bikeId = "bike1").test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            val (done, todo) = result.value
            assertTrue(done.isEmpty())
            assertTrue(todo.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke returns done and todo maintenances separately`() = runTest {
        val maintenances = listOf(
            Maintenance(id = "m1", name = "Done 1", isDone = true, bikeId = "bike1"),
            Maintenance(id = "m2", name = "Done 2", isDone = true, bikeId = "bike1"),
            Maintenance(id = "m3", name = "Todo 1", isDone = false, bikeId = "bike1")
        )
        repository.setMaintenances(maintenances)

        useCase(bikeId = "bike1").test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            val (done, todo) = result.value
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
            Maintenance(id = "m1", name = "Bike 1 Maintenance", isDone = true, bikeId = "bike1"),
            Maintenance(id = "m2", name = "Bike 2 Maintenance", isDone = true, bikeId = "bike2")
        )
        repository.setMaintenances(maintenances)

        useCase(bikeId = "bike1").test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            val (done, _) = result.value
            assertEquals(1, done.size)
            assertEquals("Bike 1 Maintenance", done[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke emits updates when maintenances change`() = runTest {
        // First, set up data before collecting
        repository.setMaintenances(emptyList())

        useCase(bikeId = "bike1").test {
            // Initially empty
            val initialResult = awaitItem()
            assertTrue(initialResult is Result.Success)
            val (done1, todo1) = initialResult.value
            assertTrue(done1.isEmpty())
            assertTrue(todo1.isEmpty())

            // Add maintenances - the flow should emit
            repository.setMaintenances(
                listOf(
                    Maintenance(id = "m1", name = "Done", isDone = true, bikeId = "bike1"),
                    Maintenance(id = "m2", name = "Todo", isDone = false, bikeId = "bike1")
                )
            )

            // Wait for any emissions (could be one or two depending on combine timing)
            val finalResult = expectMostRecentItem()
            assertTrue(finalResult is Result.Success)
            val result = finalResult.value
            assertEquals(1, result.first.size)
            assertEquals(1, result.second.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke includes maintenance details like value and date`() = runTest {
        val maintenances = listOf(
            Maintenance(
                id = "m1",
                name = "Oil Change",
                value = 5000f,
                date = 1700000000000L,
                isDone = true,
                bikeId = "bike1"
            )
        )
        repository.setMaintenances(maintenances)

        useCase(bikeId = "bike1").test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            val (done, _) = result.value
            assertEquals(5000f, done[0].value)
            assertEquals(1700000000000L, done[0].date)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
