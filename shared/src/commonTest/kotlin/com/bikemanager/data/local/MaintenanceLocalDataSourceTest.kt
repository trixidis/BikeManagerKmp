package com.bikemanager.data.local

import app.cash.turbine.test
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.fake.FakeMaintenanceLocalDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for MaintenanceLocalDataSource behavior using a fake implementation.
 */
class MaintenanceLocalDataSourceTest {
    private lateinit var dataSource: FakeMaintenanceLocalDataSource

    @BeforeTest
    fun setup() {
        dataSource = FakeMaintenanceLocalDataSource()
    }

    @Test
    fun `getMaintenancesByBikeId returns empty flow when no maintenances exist`() = runTest {
        dataSource.getMaintenancesByBikeId(1L).test {
            val maintenances = awaitItem()
            assertTrue(maintenances.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getMaintenancesByBikeId filters by bikeId`() = runTest {
        dataSource.insertMaintenance(Maintenance(name = "Oil Change", bikeId = 1L))
        dataSource.insertMaintenance(Maintenance(name = "Chain Lube", bikeId = 1L))
        dataSource.insertMaintenance(Maintenance(name = "Tire Change", bikeId = 2L))

        dataSource.getMaintenancesByBikeId(1L).test {
            val maintenances = awaitItem()
            assertEquals(2, maintenances.size)
            assertTrue(maintenances.all { it.bikeId == 1L })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getDoneMaintenances returns only completed maintenances`() = runTest {
        dataSource.insertMaintenance(Maintenance(name = "Done 1", bikeId = 1L, isDone = true))
        dataSource.insertMaintenance(Maintenance(name = "Todo 1", bikeId = 1L, isDone = false))
        dataSource.insertMaintenance(Maintenance(name = "Done 2", bikeId = 1L, isDone = true))

        dataSource.getDoneMaintenances(1L).test {
            val maintenances = awaitItem()
            assertEquals(2, maintenances.size)
            assertTrue(maintenances.all { it.isDone })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getTodoMaintenances returns only pending maintenances`() = runTest {
        dataSource.insertMaintenance(Maintenance(name = "Done 1", bikeId = 1L, isDone = true))
        dataSource.insertMaintenance(Maintenance(name = "Todo 1", bikeId = 1L, isDone = false))
        dataSource.insertMaintenance(Maintenance(name = "Todo 2", bikeId = 1L, isDone = false))

        dataSource.getTodoMaintenances(1L).test {
            val maintenances = awaitItem()
            assertEquals(2, maintenances.size)
            assertTrue(maintenances.none { it.isDone })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertMaintenance returns new id and adds maintenance`() = runTest {
        val maintenance = Maintenance(
            name = "Oil Change",
            value = 5000f,
            date = 1234567890L,
            isDone = false,
            bikeId = 1L
        )

        val id = dataSource.insertMaintenance(maintenance)

        assertEquals(1L, id)
        dataSource.getMaintenancesByBikeId(1L).test {
            val maintenances = awaitItem()
            assertEquals(1, maintenances.size)
            assertEquals("Oil Change", maintenances[0].name)
            assertEquals(5000f, maintenances[0].value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateMaintenance modifies existing maintenance`() = runTest {
        val maintenance = Maintenance(name = "Original", bikeId = 1L)
        val id = dataSource.insertMaintenance(maintenance)

        val updated = Maintenance(
            id = id,
            name = "Updated",
            value = 10000f,
            date = 9999999L,
            isDone = true,
            bikeId = 1L
        )
        dataSource.updateMaintenance(updated)

        dataSource.getMaintenancesByBikeId(1L).test {
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
        val maintenance = Maintenance(name = "Pending", bikeId = 1L, isDone = false)
        val id = dataSource.insertMaintenance(maintenance)

        dataSource.markMaintenanceDone(id, 15000f, 1234567890L)

        dataSource.getDoneMaintenances(1L).test {
            val maintenances = awaitItem()
            assertEquals(1, maintenances.size)
            assertTrue(maintenances[0].isDone)
            assertEquals(15000f, maintenances[0].value)
            assertEquals(1234567890L, maintenances[0].date)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteMaintenance removes maintenance`() = runTest {
        val maintenance = Maintenance(name = "To Delete", bikeId = 1L)
        val id = dataSource.insertMaintenance(maintenance)

        dataSource.deleteMaintenance(id)

        dataSource.getMaintenancesByBikeId(1L).test {
            val maintenances = awaitItem()
            assertTrue(maintenances.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertMaintenance preserves firebaseRef`() = runTest {
        val maintenance = Maintenance(
            name = "Synced Maintenance",
            bikeId = 1L,
            firebaseRef = "firebase-ref-456"
        )

        dataSource.insertMaintenance(maintenance)

        dataSource.getMaintenancesByBikeId(1L).test {
            val maintenances = awaitItem()
            assertEquals("firebase-ref-456", maintenances[0].firebaseRef)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getDoneMaintenances filters by bikeId and isDone`() = runTest {
        // Bike 1: 1 done, 1 todo
        dataSource.insertMaintenance(Maintenance(name = "B1 Done", bikeId = 1L, isDone = true))
        dataSource.insertMaintenance(Maintenance(name = "B1 Todo", bikeId = 1L, isDone = false))
        // Bike 2: 1 done
        dataSource.insertMaintenance(Maintenance(name = "B2 Done", bikeId = 2L, isDone = true))

        dataSource.getDoneMaintenances(1L).test {
            val maintenances = awaitItem()
            assertEquals(1, maintenances.size)
            assertEquals("B1 Done", maintenances[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
