package com.bikemanager.domain.usecase.maintenance

import com.bikemanager.domain.model.Maintenance
import com.bikemanager.fake.FakeMaintenanceRepository
import com.bikemanager.util.currentTimeMillis
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MarkMaintenanceDoneUseCaseTest {
    private lateinit var repository: FakeMaintenanceRepository
    private lateinit var useCase: MarkMaintenanceDoneUseCase

    @BeforeTest
    fun setup() {
        repository = FakeMaintenanceRepository()
        useCase = MarkMaintenanceDoneUseCase(repository)
    }

    @Test
    fun `invoke marks maintenance as done with value and timestamp`() = runTest {
        val maintenance = Maintenance(id = 1, name = "Oil Change", isDone = false, bikeId = 1L)
        repository.setMaintenances(listOf(maintenance))

        useCase(maintenanceId = 1L, value = 5000f)

        val maintenances = repository.getCurrentMaintenances()
        assertTrue(maintenances[0].isDone)
        assertEquals(5000f, maintenances[0].value)
        assertTrue(maintenances[0].date > 0)
    }

    @Test
    fun `invoke throws exception when value is negative`() = runTest {
        val maintenance = Maintenance(id = 1, name = "Test", isDone = false, bikeId = 1L)
        repository.setMaintenances(listOf(maintenance))

        assertFailsWith<IllegalArgumentException> {
            useCase(maintenanceId = 1L, value = -1f)
        }
    }

    @Test
    fun `invoke accepts zero value`() = runTest {
        val maintenance = Maintenance(id = 1, name = "Test", isDone = false, bikeId = 1L)
        repository.setMaintenances(listOf(maintenance))

        useCase(maintenanceId = 1L, value = 0f)

        val maintenances = repository.getCurrentMaintenances()
        assertEquals(0f, maintenances[0].value)
    }

    @Test
    fun `invoke only updates specified maintenance`() = runTest {
        val maintenances = listOf(
            Maintenance(id = 1, name = "Maintenance 1", isDone = false, bikeId = 1L),
            Maintenance(id = 2, name = "Maintenance 2", isDone = false, bikeId = 1L)
        )
        repository.setMaintenances(maintenances)

        useCase(maintenanceId = 1L, value = 3000f)

        val result = repository.getCurrentMaintenances()
        assertTrue(result.find { it.id == 1L }?.isDone == true)
        assertTrue(result.find { it.id == 2L }?.isDone == false)
    }

    @Test
    fun `invoke sets current timestamp`() = runTest {
        val maintenance = Maintenance(id = 1, name = "Test", isDone = false, bikeId = 1L)
        repository.setMaintenances(listOf(maintenance))
        val beforeTime = currentTimeMillis()

        useCase(maintenanceId = 1L, value = 1000f)

        val afterTime = currentTimeMillis()
        val result = repository.getCurrentMaintenances()[0]
        assertTrue(result.date >= beforeTime)
        assertTrue(result.date <= afterTime)
    }

    @Test
    fun `invoke preserves other maintenance properties`() = runTest {
        val maintenance = Maintenance(
            id = 1,
            name = "Oil Change",
            value = -1f,
            date = 0,
            isDone = false,
            bikeId = 42L,
            firebaseRef = "firebase-123"
        )
        repository.setMaintenances(listOf(maintenance))

        useCase(maintenanceId = 1L, value = 5000f)

        val result = repository.getCurrentMaintenances()[0]
        assertEquals("Oil Change", result.name)
        assertEquals(42L, result.bikeId)
        assertEquals("firebase-123", result.firebaseRef)
    }

    @Test
    fun `invoke accepts large values`() = runTest {
        val maintenance = Maintenance(id = 1, name = "Test", isDone = false, bikeId = 1L)
        repository.setMaintenances(listOf(maintenance))

        useCase(maintenanceId = 1L, value = 999999f)

        val result = repository.getCurrentMaintenances()[0]
        assertEquals(999999f, result.value)
    }

    @Test
    fun `invoke accepts decimal values`() = runTest {
        val maintenance = Maintenance(id = 1, name = "Test", isDone = false, bikeId = 1L)
        repository.setMaintenances(listOf(maintenance))

        useCase(maintenanceId = 1L, value = 1234.5f)

        val result = repository.getCurrentMaintenances()[0]
        assertEquals(1234.5f, result.value)
    }
}
