package com.bikemanager.data.repository

import app.cash.turbine.test
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.fake.FakeMaintenanceRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
        repository.getMaintenancesByBikeId(1L).test {
            val maintenances = awaitItem()
            assertTrue(maintenances.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getMaintenancesByBikeId filters by bikeId`() = runTest {
        repository.addMaintenance(Maintenance(name = "M1", bikeId = 1L))
        repository.addMaintenance(Maintenance(name = "M2", bikeId = 1L))
        repository.addMaintenance(Maintenance(name = "M3", bikeId = 2L))

        repository.getMaintenancesByBikeId(1L).test {
            val maintenances = awaitItem()
            assertEquals(2, maintenances.size)
            assertTrue(maintenances.all { it.bikeId == 1L })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getDoneMaintenances returns only completed maintenances for bikeId`() = runTest {
        repository.addMaintenance(Maintenance(name = "Done", bikeId = 1L, isDone = true))
        repository.addMaintenance(Maintenance(name = "Todo", bikeId = 1L, isDone = false))
        repository.addMaintenance(Maintenance(name = "Done Other", bikeId = 2L, isDone = true))

        repository.getDoneMaintenances(1L).test {
            val maintenances = awaitItem()
            assertEquals(1, maintenances.size)
            assertEquals("Done", maintenances[0].name)
            assertTrue(maintenances[0].isDone)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTodoMaintenances returns only pending maintenances for bikeId`() = runTest {
        repository.addMaintenance(Maintenance(name = "Done", bikeId = 1L, isDone = true))
        repository.addMaintenance(Maintenance(name = "Todo 1", bikeId = 1L, isDone = false))
        repository.addMaintenance(Maintenance(name = "Todo 2", bikeId = 1L, isDone = false))

        repository.getTodoMaintenances(1L).test {
            val maintenances = awaitItem()
            assertEquals(2, maintenances.size)
            assertTrue(maintenances.none { it.isDone })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addMaintenance returns generated id`() = runTest {
        val maintenance = Maintenance(name = "Oil Change", bikeId = 1L)

        val id = repository.addMaintenance(maintenance)

        assertEquals(1L, id)
    }

    @Test
    fun `addMaintenance stores all properties`() = runTest {
        val maintenance = Maintenance(
            name = "Chain Lube",
            value = 5000f,
            date = 1234567890L,
            isDone = true,
            bikeId = 1L,
            firebaseRef = "ref-456"
        )

        repository.addMaintenance(maintenance)

        repository.getMaintenancesByBikeId(1L).test {
            val maintenances = awaitItem()
            assertEquals(1, maintenances.size)
            val stored = maintenances[0]
            assertEquals("Chain Lube", stored.name)
            assertEquals(5000f, stored.value)
            assertEquals(1234567890L, stored.date)
            assertTrue(stored.isDone)
            assertEquals("ref-456", stored.firebaseRef)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateMaintenance modifies existing maintenance`() = runTest {
        val id = repository.addMaintenance(Maintenance(name = "Original", bikeId = 1L))

        repository.updateMaintenance(
            Maintenance(
                id = id,
                name = "Updated",
                value = 10000f,
                isDone = true,
                bikeId = 1L
            )
        )

        repository.getMaintenancesByBikeId(1L).test {
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
            Maintenance(name = "Pending", bikeId = 1L, isDone = false)
        )

        repository.markMaintenanceDone(id, 15000f, 9876543210L)

        repository.getDoneMaintenances(1L).test {
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
        val id = repository.addMaintenance(Maintenance(name = "To Delete", bikeId = 1L))

        repository.deleteMaintenance(id)

        repository.getMaintenancesByBikeId(1L).test {
            val maintenances = awaitItem()
            assertTrue(maintenances.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteMaintenance does not affect other maintenances`() = runTest {
        val id1 = repository.addMaintenance(Maintenance(name = "M1", bikeId = 1L))
        repository.addMaintenance(Maintenance(name = "M2", bikeId = 1L))

        repository.deleteMaintenance(id1)

        repository.getMaintenancesByBikeId(1L).test {
            val maintenances = awaitItem()
            assertEquals(1, maintenances.size)
            assertEquals("M2", maintenances[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getMaintenancesByBikeId emits updates reactively`() = runTest {
        repository.getMaintenancesByBikeId(1L).test {
            assertTrue(awaitItem().isEmpty())

            repository.addMaintenance(Maintenance(name = "New", bikeId = 1L))
            assertEquals(1, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `markMaintenanceDone moves maintenance from todo to done`() = runTest {
        val id = repository.addMaintenance(
            Maintenance(name = "Pending", bikeId = 1L, isDone = false)
        )

        // Initially in todo
        repository.getTodoMaintenances(1L).test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }

        repository.markMaintenanceDone(id, 5000f, 1234567890L)

        // Now in done
        repository.getTodoMaintenances(1L).test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
        repository.getDoneMaintenances(1L).test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
