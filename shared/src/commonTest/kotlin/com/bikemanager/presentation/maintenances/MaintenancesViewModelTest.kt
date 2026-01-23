package com.bikemanager.presentation.maintenances

import app.cash.turbine.test
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.usecase.maintenance.AddMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.DeleteMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.GetMaintenancesUseCase
import com.bikemanager.domain.usecase.maintenance.MarkMaintenanceDoneUseCase
import com.bikemanager.fake.FakeBikeRepository
import com.bikemanager.fake.FakeMaintenanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MaintenancesViewModelTest {
    private lateinit var bikeRepository: FakeBikeRepository
    private lateinit var maintenanceRepository: FakeMaintenanceRepository
    private lateinit var getMaintenancesUseCase: GetMaintenancesUseCase
    private lateinit var addMaintenanceUseCase: AddMaintenanceUseCase
    private lateinit var markMaintenanceDoneUseCase: MarkMaintenanceDoneUseCase
    private lateinit var deleteMaintenanceUseCase: DeleteMaintenanceUseCase
    private val testDispatcher = StandardTestDispatcher()
    private val bikeId = 1L

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        bikeRepository = FakeBikeRepository()
        maintenanceRepository = FakeMaintenanceRepository()
        getMaintenancesUseCase = GetMaintenancesUseCase(maintenanceRepository)
        addMaintenanceUseCase = AddMaintenanceUseCase(maintenanceRepository)
        markMaintenanceDoneUseCase = MarkMaintenanceDoneUseCase(maintenanceRepository)
        deleteMaintenanceUseCase = DeleteMaintenanceUseCase(maintenanceRepository)

        // Setup a default bike
        bikeRepository.setBikes(listOf(Bike(id = bikeId, name = "Test Bike")))
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MaintenancesViewModel {
        return MaintenancesViewModel(
            bikeId = bikeId,
            bikeRepository = bikeRepository,
            getMaintenancesUseCase = getMaintenancesUseCase,
            addMaintenanceUseCase = addMaintenanceUseCase,
            markMaintenanceDoneUseCase = markMaintenanceDoneUseCase,
            deleteMaintenanceUseCase = deleteMaintenanceUseCase
        )
    }

    @Test
    fun `initial state is Loading then Success with empty lists`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(MaintenancesUiState.Loading, awaitItem())

            advanceUntilIdle()
            val successState = awaitItem()
            assertTrue(successState is MaintenancesUiState.Success)
            val success = successState as MaintenancesUiState.Success
            assertTrue(success.doneMaintenances.isEmpty())
            assertTrue(success.todoMaintenances.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state shows done and todo maintenances`() = runTest {
        maintenanceRepository.setMaintenances(
            listOf(
                Maintenance(id = 1, name = "Done 1", isDone = true, bikeId = bikeId),
                Maintenance(id = 2, name = "Todo 1", isDone = false, bikeId = bikeId)
            )
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(MaintenancesUiState.Loading, awaitItem())

            advanceUntilIdle()
            val successState = awaitItem()
            assertTrue(successState is MaintenancesUiState.Success)
            val success = successState as MaintenancesUiState.Success
            assertEquals(1, success.doneMaintenances.size)
            assertEquals(1, success.todoMaintenances.size)
            assertEquals("Done 1", success.doneMaintenances[0].name)
            assertEquals("Todo 1", success.todoMaintenances[0].name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addDoneMaintenance adds maintenance to repository`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.addDoneMaintenance(name = "Oil Change", value = 5000f)
        advanceUntilIdle()

        val maintenances = maintenanceRepository.getCurrentMaintenances()
        assertEquals(1, maintenances.size)
        assertEquals("Oil Change", maintenances[0].name)
        assertEquals(5000f, maintenances[0].value)
        assertTrue(maintenances[0].isDone)
    }

    @Test
    fun `addDoneMaintenance with blank name does nothing`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.addDoneMaintenance(name = "", value = 5000f)
        advanceUntilIdle()

        assertTrue(maintenanceRepository.getCurrentMaintenances().isEmpty())
    }

    @Test
    fun `addDoneMaintenance with negative value does nothing`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.addDoneMaintenance(name = "Test", value = -1f)
        advanceUntilIdle()

        assertTrue(maintenanceRepository.getCurrentMaintenances().isEmpty())
    }

    @Test
    fun `addTodoMaintenance adds todo maintenance to repository`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.addTodoMaintenance(name = "Future Check")
        advanceUntilIdle()

        val maintenances = maintenanceRepository.getCurrentMaintenances()
        assertEquals(1, maintenances.size)
        assertEquals("Future Check", maintenances[0].name)
        assertTrue(!maintenances[0].isDone)
    }

    @Test
    fun `addTodoMaintenance with blank name does nothing`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.addTodoMaintenance(name = "   ")
        advanceUntilIdle()

        assertTrue(maintenanceRepository.getCurrentMaintenances().isEmpty())
    }

    @Test
    fun `markMaintenanceDone marks maintenance as done`() = runTest {
        maintenanceRepository.setMaintenances(
            listOf(Maintenance(id = 1, name = "Todo", isDone = false, bikeId = bikeId))
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.markMaintenanceDone(maintenanceId = 1L, value = 3000f)
        advanceUntilIdle()

        val maintenances = maintenanceRepository.getCurrentMaintenances()
        assertTrue(maintenances[0].isDone)
        assertEquals(3000f, maintenances[0].value)
    }

    @Test
    fun `markMaintenanceDone with negative value does nothing`() = runTest {
        maintenanceRepository.setMaintenances(
            listOf(Maintenance(id = 1, name = "Todo", isDone = false, bikeId = bikeId))
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.markMaintenanceDone(maintenanceId = 1L, value = -1f)
        advanceUntilIdle()

        val maintenances = maintenanceRepository.getCurrentMaintenances()
        assertTrue(!maintenances[0].isDone)
    }

    @Test
    fun `deleteMaintenance removes maintenance from repository`() = runTest {
        val maintenance = Maintenance(id = 1, name = "To Delete", isDone = true, bikeId = bikeId)
        maintenanceRepository.setMaintenances(listOf(maintenance))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteMaintenance(maintenance)
        advanceUntilIdle()

        assertTrue(maintenanceRepository.getCurrentMaintenances().isEmpty())
    }

    @Test
    fun `undoDelete restores deleted maintenance`() = runTest {
        val maintenance = Maintenance(
            id = 1,
            name = "To Delete",
            value = 5000f,
            date = 1700000000000L,
            isDone = true,
            bikeId = bikeId
        )
        maintenanceRepository.setMaintenances(listOf(maintenance))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.deleteMaintenance(maintenance)
        advanceUntilIdle()
        assertTrue(maintenanceRepository.getCurrentMaintenances().isEmpty())

        viewModel.undoDelete()
        advanceUntilIdle()

        val restored = maintenanceRepository.getCurrentMaintenances()
        assertEquals(1, restored.size)
        assertEquals("To Delete", restored[0].name)
        assertEquals(5000f, restored[0].value)
    }

    @Test
    fun `undoDelete does nothing when no maintenance was deleted`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Should not throw
        viewModel.undoDelete()
        advanceUntilIdle()

        assertTrue(maintenanceRepository.getCurrentMaintenances().isEmpty())
    }

    @Test
    fun `loadMaintenances reloads from repository`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Add a maintenance directly to repository
        maintenanceRepository.setMaintenances(
            listOf(Maintenance(id = 1, name = "New", isDone = true, bikeId = bikeId))
        )

        viewModel.loadMaintenances()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is MaintenancesUiState.Success)
            assertEquals(1, (state as MaintenancesUiState.Success).doneMaintenances.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `maintenances are filtered by bikeId`() = runTest {
        maintenanceRepository.setMaintenances(
            listOf(
                Maintenance(id = 1, name = "Bike 1 Done", isDone = true, bikeId = 1L),
                Maintenance(id = 2, name = "Bike 2 Done", isDone = true, bikeId = 2L),
                Maintenance(id = 3, name = "Bike 1 Todo", isDone = false, bikeId = 1L)
            )
        )

        val viewModel = createViewModel() // bikeId = 1L
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is MaintenancesUiState.Success)
            val success = state as MaintenancesUiState.Success
            assertEquals(1, success.doneMaintenances.size)
            assertEquals(1, success.todoMaintenances.size)
            assertEquals("Bike 1 Done", success.doneMaintenances[0].name)
            assertEquals("Bike 1 Todo", success.todoMaintenances[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
