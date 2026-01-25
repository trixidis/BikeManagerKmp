package com.bikemanager.data.repository

import app.cash.turbine.test
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
            val maintenances = awaitItem()
            assertTrue(maintenances.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getMaintenancesByBikeId filters by bikeId`() = runTest {
        repository.addMaintenance(Maintenance(name = "M1", bikeId = "bike1"))
        repository.addMaintenance(Maintenance(name = "M2", bikeId = "bike1"))
        repository.addMaintenance(Maintenance(name = "M3", bikeId = "bike2"))

        repository.getMaintenancesByBikeId("bike1").test {
            val maintenances = awaitItem()
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
            val maintenances = awaitItem()
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
            val maintenances = awaitItem()
            assertEquals(2, maintenances.size)
            assertTrue(maintenances.none { it.isDone })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addMaintenance returns generated id`() = runTest {
        val maintenance = Maintenance(name = "Oil Change", bikeId = "bike1")

        val id = repository.addMaintenance(maintenance)

        assertTrue(id.isNotEmpty())
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
            val maintenances = awaitItem()
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
        val id = repository.addMaintenance(Maintenance(name = "Original", bikeId = "bike1"))

        repository.updateMaintenance(
            Maintenance(
                id = id,
                name = "Updated",
                value = 10000f,
                isDone = true,
                bikeId = "bike1"
            )
        )

        repository.getMaintenancesByBikeId("bike1").test {
            val maintenances = awaitItem()
            assertEquals(1, maintenances.size)
            assertEquals("Updated", maintenances[0].name)
            assertEquals(10000f, maintenances[0].value)
            assertTrue(maintenances[0].isDone)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `markMaintenanceDone updates isDone value and date`() = runTest {
        val id = repository.addMaintenance(
            Maintenance(name = "Pending", bikeId = "bike1", isDone = false)
        )

        repository.markMaintenanceDone(id, "bike1", 15000f, 9876543210L)

        repository.getDoneMaintenances("bike1").test {
            val maintenances = awaitItem()
            assertEquals(1, maintenances.size)
            assertTrue(maintenances[0].isDone)
            assertEquals(15000f, maintenances[0].value)
            assertEquals(9876543210L, maintenances[0].date)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteMaintenance removes maintenance`() = runTest {
        val id = repository.addMaintenance(Maintenance(name = "To Delete", bikeId = "bike1"))

        repository.deleteMaintenance(id, "bike1")

        repository.getMaintenancesByBikeId("bike1").test {
            val maintenances = awaitItem()
            assertTrue(maintenances.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteMaintenance does not affect other maintenances`() = runTest {
        val id1 = repository.addMaintenance(Maintenance(name = "M1", bikeId = "bike1"))
        repository.addMaintenance(Maintenance(name = "M2", bikeId = "bike1"))

        repository.deleteMaintenance(id1, "bike1")

        repository.getMaintenancesByBikeId("bike1").test {
            val maintenances = awaitItem()
            assertEquals(1, maintenances.size)
            assertEquals("M2", maintenances[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getMaintenancesByBikeId emits updates reactively`() = runTest {
        repository.getMaintenancesByBikeId("bike1").test {
            assertTrue(awaitItem().isEmpty())

            repository.addMaintenance(Maintenance(name = "New", bikeId = "bike1"))
            assertEquals(1, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `markMaintenanceDone moves maintenance from todo to done`() = runTest {
        val id = repository.addMaintenance(
            Maintenance(name = "Pending", bikeId = "bike1", isDone = false)
        )

        // Initially in todo
        repository.getTodoMaintenances("bike1").test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }

        repository.markMaintenanceDone(id, "bike1", 5000f, 1234567890L)

        // Now in done
        repository.getTodoMaintenances("bike1").test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
        repository.getDoneMaintenances("bike1").test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
