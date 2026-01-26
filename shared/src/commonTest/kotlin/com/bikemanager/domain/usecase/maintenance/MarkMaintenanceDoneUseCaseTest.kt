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
        val maintenance = Maintenance(id = "m1", name = "Oil Change", isDone = false, bikeId = "bike1")
        repository.setMaintenances(listOf(maintenance))

        val result = useCase(maintenanceId = "m1", bikeId = "bike1", value = 5000f)

        assertTrue(result is Result.Success)
        val maintenances = repository.getCurrentMaintenances()
        assertTrue(maintenances[0].isDone)
        assertEquals(5000f, maintenances[0].value)
        assertTrue(maintenances[0].date > 0)
    }

    @Test
    fun `invoke returns failure when value is negative`() = runTest {
        val maintenance = Maintenance(id = "m1", name = "Test", isDone = false, bikeId = "bike1")
        repository.setMaintenances(listOf(maintenance))

        val result = useCase(maintenanceId = "m1", bikeId = "bike1", value = -1f)

        assertTrue(result is Result.Failure)
    }

    @Test
    fun `invoke accepts zero value`() = runTest {
        val maintenance = Maintenance(id = "m1", name = "Test", isDone = false, bikeId = "bike1")
        repository.setMaintenances(listOf(maintenance))

        val result = useCase(maintenanceId = "m1", bikeId = "bike1", value = 0f)

        assertTrue(result is Result.Success)
        val maintenances = repository.getCurrentMaintenances()
        assertEquals(0f, maintenances[0].value)
    }

    @Test
    fun `invoke only updates specified maintenance`() = runTest {
        val maintenances = listOf(
            Maintenance(id = "m1", name = "Maintenance 1", isDone = false, bikeId = "bike1"),
            Maintenance(id = "m2", name = "Maintenance 2", isDone = false, bikeId = "bike1")
        )
        repository.setMaintenances(maintenances)

        val result = useCase(maintenanceId = "m1", bikeId = "bike1", value = 3000f)

        assertTrue(result is Result.Success)
        val maintenancesResult = repository.getCurrentMaintenances()
        assertTrue(maintenancesResult.find { it.id == "m1" }?.isDone == true)
        assertTrue(maintenancesResult.find { it.id == "m2" }?.isDone == false)
    }

    @Test
    fun `invoke sets current timestamp`() = runTest {
        val maintenance = Maintenance(id = "m1", name = "Test", isDone = false, bikeId = "bike1")
        repository.setMaintenances(listOf(maintenance))
        val beforeTime = currentTimeMillis()

        val result = useCase(maintenanceId = "m1", bikeId = "bike1", value = 1000f)

        assertTrue(result is Result.Success)
        val afterTime = currentTimeMillis()
        val maintenanceResult = repository.getCurrentMaintenances()[0]
        assertTrue(maintenanceResult.date >= beforeTime)
        assertTrue(maintenanceResult.date <= afterTime)
    }

    @Test
    fun `invoke preserves other maintenance properties`() = runTest {
        val maintenance = Maintenance(
            id = "m1",
            name = "Oil Change",
            value = -1f,
            date = 0,
            isDone = false,
            bikeId = "bike42"
        )
        repository.setMaintenances(listOf(maintenance))

        val result = useCase(maintenanceId = "m1", bikeId = "bike42", value = 5000f)

        assertTrue(result is Result.Success)
        val maintenanceResult = repository.getCurrentMaintenances()[0]
        assertEquals("Oil Change", maintenanceResult.name)
        assertEquals("bike42", maintenanceResult.bikeId)
    }

    @Test
    fun `invoke accepts large values`() = runTest {
        val maintenance = Maintenance(id = "m1", name = "Test", isDone = false, bikeId = "bike1")
        repository.setMaintenances(listOf(maintenance))

        val result = useCase(maintenanceId = "m1", bikeId = "bike1", value = 999999f)

        assertTrue(result is Result.Success)
        val maintenanceResult = repository.getCurrentMaintenances()[0]
        assertEquals(999999f, maintenanceResult.value)
    }

    @Test
    fun `invoke accepts decimal values`() = runTest {
        val maintenance = Maintenance(id = "m1", name = "Test", isDone = false, bikeId = "bike1")
        repository.setMaintenances(listOf(maintenance))

        val result = useCase(maintenanceId = "m1", bikeId = "bike1", value = 1234.5f)

        assertTrue(result is Result.Success)
        val maintenanceResult = repository.getCurrentMaintenances()[0]
        assertEquals(1234.5f, maintenanceResult.value)
    }
}
