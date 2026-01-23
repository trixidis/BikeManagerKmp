package com.bikemanager.domain.usecase.maintenance

import com.bikemanager.domain.model.Maintenance
import com.bikemanager.fake.FakeMaintenanceRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AddMaintenanceUseCaseTest {
    private lateinit var repository: FakeMaintenanceRepository
    private lateinit var useCase: AddMaintenanceUseCase

    @BeforeTest
    fun setup() {
        repository = FakeMaintenanceRepository()
        useCase = AddMaintenanceUseCase(repository)
    }

    @Test
    fun `invoke adds maintenance to repository and returns id`() = runTest {
        val maintenance = Maintenance(name = "Oil Change", bikeId = 1L)

        val id = useCase(maintenance)

        assertTrue(id > 0)
        val maintenances = repository.getCurrentMaintenances()
        assertEquals(1, maintenances.size)
        assertEquals("Oil Change", maintenances[0].name)
    }

    @Test
    fun `invoke throws exception when maintenance name is empty`() = runTest {
        val maintenance = Maintenance(name = "", bikeId = 1L)

        assertFailsWith<IllegalArgumentException> {
            useCase(maintenance)
        }
    }

    @Test
    fun `invoke throws exception when maintenance name is blank`() = runTest {
        val maintenance = Maintenance(name = "   ", bikeId = 1L)

        assertFailsWith<IllegalArgumentException> {
            useCase(maintenance)
        }
    }

    @Test
    fun `addDone creates done maintenance with timestamp`() = runTest {
        val id = useCase.addDone(name = "Tire Change", value = 10000f, bikeId = 1L)

        assertTrue(id > 0)
        val maintenances = repository.getCurrentMaintenances()
        assertEquals(1, maintenances.size)
        assertEquals("Tire Change", maintenances[0].name)
        assertEquals(10000f, maintenances[0].value)
        assertTrue(maintenances[0].isDone)
        assertTrue(maintenances[0].date > 0)
    }

    @Test
    fun `addDone throws exception when name is empty`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase.addDone(name = "", value = 5000f, bikeId = 1L)
        }
    }

    @Test
    fun `addDone throws exception when value is negative`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase.addDone(name = "Test", value = -1f, bikeId = 1L)
        }
    }

    @Test
    fun `addDone accepts zero value`() = runTest {
        val id = useCase.addDone(name = "Zero KM Check", value = 0f, bikeId = 1L)

        assertTrue(id > 0)
        val maintenances = repository.getCurrentMaintenances()
        assertEquals(0f, maintenances[0].value)
    }

    @Test
    fun `addTodo creates todo maintenance without value or date`() = runTest {
        val id = useCase.addTodo(name = "Future Maintenance", bikeId = 1L)

        assertTrue(id > 0)
        val maintenances = repository.getCurrentMaintenances()
        assertEquals(1, maintenances.size)
        assertEquals("Future Maintenance", maintenances[0].name)
        assertEquals(-1f, maintenances[0].value)
        assertEquals(0L, maintenances[0].date)
        assertTrue(!maintenances[0].isDone)
    }

    @Test
    fun `addTodo throws exception when name is empty`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase.addTodo(name = "", bikeId = 1L)
        }
    }

    @Test
    fun `addTodo throws exception when name is blank`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase.addTodo(name = "   ", bikeId = 1L)
        }
    }

    @Test
    fun `invoke preserves bikeId`() = runTest {
        val maintenance = Maintenance(name = "Test", bikeId = 42L)

        useCase(maintenance)

        val maintenances = repository.getCurrentMaintenances()
        assertEquals(42L, maintenances[0].bikeId)
    }

    @Test
    fun `invoke preserves firebase reference`() = runTest {
        val maintenance = Maintenance(name = "Test", bikeId = 1L, firebaseRef = "firebase-123")

        useCase(maintenance)

        val maintenances = repository.getCurrentMaintenances()
        assertEquals("firebase-123", maintenances[0].firebaseRef)
    }

    @Test
    fun `multiple addDone creates unique maintenances`() = runTest {
        val id1 = useCase.addDone(name = "Maintenance 1", value = 1000f, bikeId = 1L)
        val id2 = useCase.addDone(name = "Maintenance 2", value = 2000f, bikeId = 1L)

        assertTrue(id1 != id2)
        val maintenances = repository.getCurrentMaintenances()
        assertEquals(2, maintenances.size)
    }
}
