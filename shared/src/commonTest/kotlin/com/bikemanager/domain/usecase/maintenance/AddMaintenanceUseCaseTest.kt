package com.bikemanager.domain.usecase.maintenance

import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.fake.FakeMaintenanceRepository
import com.bikemanager.util.currentTimeMillis
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
        val maintenance = Maintenance(name = "Oil Change", bikeId = "bike1")

        val result = useCase(maintenance)

        assertTrue(result is Result.Success)
        val id = result.value
        assertTrue(id.isNotEmpty())
        val maintenances = repository.getCurrentMaintenances()
        assertEquals(1, maintenances.size)
        assertEquals("Oil Change", maintenances[0].name)
    }

    @Test
    fun `invoke returns failure when maintenance name is empty`() = runTest {
        val maintenance = Maintenance(name = "", bikeId = "bike1")

        val result = useCase(maintenance)

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `invoke returns failure when maintenance name is blank`() = runTest {
        val maintenance = Maintenance(name = "   ", bikeId = "bike1")

        val result = useCase(maintenance)

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `invoke adds done maintenance with timestamp`() = runTest {
        val maintenance = Maintenance(
            name = "Tire Change",
            value = 10000f,
            date = currentTimeMillis(),
            isDone = true,
            bikeId = "bike1"
        )

        val result = useCase(maintenance)

        assertTrue(result is Result.Success)
        val id = result.value
        assertTrue(id.isNotEmpty())
        val maintenances = repository.getCurrentMaintenances()
        assertEquals(1, maintenances.size)
        assertEquals("Tire Change", maintenances[0].name)
        assertEquals(10000f, maintenances[0].value)
        assertTrue(maintenances[0].isDone)
        assertTrue(maintenances[0].date > 0)
    }

    @Test
    fun `invoke adds done maintenance with zero value`() = runTest {
        val maintenance = Maintenance(
            name = "Zero KM Check",
            value = 0f,
            date = currentTimeMillis(),
            isDone = true,
            bikeId = "bike1"
        )

        val result = useCase(maintenance)

        assertTrue(result is Result.Success)
        val id = result.value
        assertTrue(id.isNotEmpty())
        val maintenances = repository.getCurrentMaintenances()
        assertEquals(0f, maintenances[0].value)
    }

    @Test
    fun `invoke adds todo maintenance without value or date`() = runTest {
        val maintenance = Maintenance(
            name = "Future Maintenance",
            value = -1f,
            date = 0,
            isDone = false,
            bikeId = "bike1"
        )

        val result = useCase(maintenance)

        assertTrue(result is Result.Success)
        val id = result.value
        assertTrue(id.isNotEmpty())
        val maintenances = repository.getCurrentMaintenances()
        assertEquals(1, maintenances.size)
        assertEquals("Future Maintenance", maintenances[0].name)
        assertEquals(-1f, maintenances[0].value)
        assertEquals(0L, maintenances[0].date)
        assertTrue(!maintenances[0].isDone)
    }

    @Test
    fun `invoke preserves bikeId`() = runTest {
        val maintenance = Maintenance(name = "Test", bikeId = "bike42")

        val result = useCase(maintenance)

        assertTrue(result is Result.Success)
        val maintenances = repository.getCurrentMaintenances()
        assertEquals("bike42", maintenances[0].bikeId)
    }

    @Test
    fun `invoke creates multiple unique maintenances`() = runTest {
        val maintenance1 = Maintenance(
            name = "Maintenance 1",
            value = 1000f,
            date = currentTimeMillis(),
            isDone = true,
            bikeId = "bike1"
        )
        val maintenance2 = Maintenance(
            name = "Maintenance 2",
            value = 2000f,
            date = currentTimeMillis(),
            isDone = true,
            bikeId = "bike1"
        )

        val result1 = useCase(maintenance1)
        val result2 = useCase(maintenance2)

        assertTrue(result1 is Result.Success)
        assertTrue(result2 is Result.Success)
        val id1 = result1.value
        val id2 = result2.value
        assertTrue(id1 != id2)
        val maintenances = repository.getCurrentMaintenances()
        assertEquals(2, maintenances.size)
    }
}
