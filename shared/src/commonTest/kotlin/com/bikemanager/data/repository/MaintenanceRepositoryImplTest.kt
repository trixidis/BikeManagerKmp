package com.bikemanager.data.repository

import app.cash.turbine.test
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.fake.FakeMaintenanceRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for MaintenanceRepositoryImpl to verify it correctly delegates to the data source.
 * Uses FakeMaintenanceRepository which mirrors the repository interface behavior.
 */
class MaintenanceRepositoryImplTest {
    private lateinit var repository: FakeMaintenanceRepository

    @BeforeTest
    fun setup() {
        repository = FakeMaintenanceRepository()
    }

    @Test
    fun `getMaintenancesByBikeId returns empty list initially`() = runTest {
        repository.getMaintenancesByBikeId("bike1").test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertTrue(result.value.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getMaintenancesByBikeId filters by bikeId`() = runTest {
        repository.addMaintenance(Maintenance(name = "M1", bikeId = "bike1"))
        repository.addMaintenance(Maintenance(name = "M2", bikeId = "bike1"))
        repository.addMaintenance(Maintenance(name = "M3", bikeId = "bike2"))

        repository.getMaintenancesByBikeId("bike1").test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            val maintenances = result.value
            assertEquals(2, maintenances.size)
            assertTrue(maintenances.all { it.bikeId == "bike1" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getDoneMaintenances returns only completed maintenances for bikeId`() = runTest {
        repository.addMaintenance(Maintenance(name = "Done", bikeId = "bike1", isDone = true))
        repository.addMaintenance(Maintenance(name = "Todo", bikeId = "bike1", isDone = false))
        repository.addMaintenance(Maintenance(name = "Done Other", bikeId = "bike2", isDone = true))

        repository.getDoneMaintenances("bike1").test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            val maintenances = result.value
            assertEquals(1, maintenances.size)
            assertEquals("Done", maintenances[0].name)
            assertTrue(maintenances[0].isDone)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTodoMaintenances returns only pending maintenances for bikeId`() = runTest {
        repository.addMaintenance(Maintenance(name = "Done", bikeId = "bike1", isDone = true))
        repository.addMaintenance(Maintenance(name = "Todo 1", bikeId = "bike1", isDone = false))
        repository.addMaintenance(Maintenance(name = "Todo 2", bikeId = "bike1", isDone = false))

        repository.getTodoMaintenances("bike1").test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            val maintenances = result.value
            assertEquals(2, maintenances.size)
            assertTrue(maintenances.none { it.isDone })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addMaintenance returns generated id`() = runTest {
        val maintenance = Maintenance(name = "Oil Change", bikeId = "bike1")

        val result = repository.addMaintenance(maintenance)

        assertTrue(result is Result.Success)
        assertTrue(result.value.isNotEmpty())
    }

    @Test
    fun `addMaintenance stores all properties`() = runTest {
        val maintenance = Maintenance(
            name = "Chain Lube",
            value = 5000f,
            date = 1234567890L,
            isDone = true,
            bikeId = "bike1"
        )

        repository.addMaintenance(maintenance)

        repository.getMaintenancesByBikeId("bike1").test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            val maintenances = result.value
            assertEquals(1, maintenances.size)
            val stored = maintenances[0]
            assertEquals("Chain Lube", stored.name)
            assertEquals(5000f, stored.value)
            assertEquals(1234567890L, stored.date)
            assertTrue(stored.isDone)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateMaintenance modifies existing maintenance`() = runTest {
        val addResult = repository.addMaintenance(Maintenance(name = "Original", bikeId = "bike1"))
        assertTrue(addResult is Result.Success)
        val id = addResult.value

        val updateResult = repository.updateMaintenance(
            Maintenance(
                id = id,
                name = "Updated",
                value = 10000f,
                isDone = true,
                bikeId = "bike1"
            )
        )
        assertTrue(updateResult is Result.Success)

        repository.getMaintenancesByBikeId("bike1").test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            val maintenances = result.value
            assertEquals(1, maintenances.size)
            assertEquals("Updated", maintenances[0].name)
            assertEquals(10000f, maintenances[0].value)
            assertTrue(maintenances[0].isDone)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `markMaintenanceDone updates isDone value and date`() = runTest {
        val addResult = repository.addMaintenance(
            Maintenance(name = "Pending", bikeId = "bike1", isDone = false)
        )
        assertTrue(addResult is Result.Success)
        val id = addResult.value

        val markResult = repository.markMaintenanceDone(id, "bike1", 15000f, 9876543210L)
        assertTrue(markResult is Result.Success)

        repository.getDoneMaintenances("bike1").test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            val maintenances = result.value
            assertEquals(1, maintenances.size)
            assertTrue(maintenances[0].isDone)
            assertEquals(15000f, maintenances[0].value)
            assertEquals(9876543210L, maintenances[0].date)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteMaintenance removes maintenance`() = runTest {
        val addResult = repository.addMaintenance(Maintenance(name = "To Delete", bikeId = "bike1"))
        assertTrue(addResult is Result.Success)
        val id = addResult.value

        val deleteResult = repository.deleteMaintenance(id, "bike1")
        assertTrue(deleteResult is Result.Success)

        repository.getMaintenancesByBikeId("bike1").test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertTrue(result.value.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteMaintenance does not affect other maintenances`() = runTest {
        val addResult1 = repository.addMaintenance(Maintenance(name = "M1", bikeId = "bike1"))
        assertTrue(addResult1 is Result.Success)
        val id1 = addResult1.value
        repository.addMaintenance(Maintenance(name = "M2", bikeId = "bike1"))

        val deleteResult = repository.deleteMaintenance(id1, "bike1")
        assertTrue(deleteResult is Result.Success)

        repository.getMaintenancesByBikeId("bike1").test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            val maintenances = result.value
            assertEquals(1, maintenances.size)
            assertEquals("M2", maintenances[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getMaintenancesByBikeId emits updates reactively`() = runTest {
        repository.getMaintenancesByBikeId("bike1").test {
            val result1 = awaitItem()
            assertTrue(result1 is Result.Success)
            assertTrue(result1.value.isEmpty())

            repository.addMaintenance(Maintenance(name = "New", bikeId = "bike1"))
            val result2 = awaitItem()
            assertTrue(result2 is Result.Success)
            assertEquals(1, result2.value.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `markMaintenanceDone moves maintenance from todo to done`() = runTest {
        val addResult = repository.addMaintenance(
            Maintenance(name = "Pending", bikeId = "bike1", isDone = false)
        )
        assertTrue(addResult is Result.Success)
        val id = addResult.value

        // Initially in todo
        repository.getTodoMaintenances("bike1").test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(1, result.value.size)
            cancelAndIgnoreRemainingEvents()
        }

        val markResult = repository.markMaintenanceDone(id, "bike1", 5000f, 1234567890L)
        assertTrue(markResult is Result.Success)

        // Now in done
        repository.getTodoMaintenances("bike1").test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertTrue(result.value.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
        repository.getDoneMaintenances("bike1").test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(1, result.value.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
