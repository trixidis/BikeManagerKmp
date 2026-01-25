package com.bikemanager.presentation.maintenances

import app.cash.turbine.test
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.usecase.maintenance.AddMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.DeleteMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.GetMaintenancesUseCase
import com.bikemanager.domain.usecase.maintenance.MarkMaintenanceDoneUseCase
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
    private lateinit var maintenanceRepository: FakeMaintenanceRepository
    private lateinit var getMaintenancesUseCase: GetMaintenancesUseCase
    private lateinit var addMaintenanceUseCase: AddMaintenanceUseCase
    private lateinit var markMaintenanceDoneUseCase: MarkMaintenanceDoneUseCase
    private lateinit var deleteMaintenanceUseCase: DeleteMaintenanceUseCase
    private val testDispatcher = StandardTestDispatcher()
    private val bikeId = "bike1"

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        maintenanceRepository = FakeMaintenanceRepository()
        getMaintenancesUseCase = GetMaintenancesUseCase(maintenanceRepository)
        addMaintenanceUseCase = AddMaintenanceUseCase(maintenanceRepository)
        markMaintenanceDoneUseCase = MarkMaintenanceDoneUseCase(maintenanceRepository)
        deleteMaintenanceUseCase = DeleteMaintenanceUseCase(maintenanceRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MaintenancesViewModel {
        return MaintenancesViewModel(
            bikeId = bikeId,
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
                Maintenance(id = "m1", name = "Done 1", isDone = true, bikeId = bikeId),
                Maintenance(id = "m2", name = "Todo 1", isDone = false, bikeId = bikeId)
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
            listOf(Maintenance(id = "m1", name = "Todo", isDone = false, bikeId = bikeId))
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.markMaintenanceDone(maintenanceId = "m1", value = 3000f)
        advanceUntilIdle()

        val maintenances = maintenanceRepository.getCurrentMaintenances()
        assertTrue(maintenances[0].isDone)
        assertEquals(3000f, maintenances[0].value)
    }

    @Test
    fun `markMaintenanceDone with negative value does nothing`() = runTest {
        maintenanceRepository.setMaintenances(
            listOf(Maintenance(id = "m1", name = "Todo", isDone = false, bikeId = bikeId))
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.markMaintenanceDone(maintenanceId = "m1", value = -1f)
        advanceUntilIdle()

        val maintenances = maintenanceRepository.getCurrentMaintenances()
        assertTrue(!maintenances[0].isDone)
    }

    @Test
    fun `deleteMaintenance removes maintenance from repository`() = runTest {
        val maintenance = Maintenance(id = "m1", name = "To Delete", isDone = true, bikeId = bikeId)
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
            id = "m1",
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
    fun `maintenances are filtered by bikeId`() = runTest {
        maintenanceRepository.setMaintenances(
            listOf(
                Maintenance(id = "m1", name = "Bike 1 Done", isDone = true, bikeId = "bike1"),
                Maintenance(id = "m2", name = "Bike 2 Done", isDone = true, bikeId = "bike2"),
                Maintenance(id = "m3", name = "Bike 1 Todo", isDone = false, bikeId = "bike1")
            )
        )

        val viewModel = createViewModel() // bikeId = "bike1"
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

    @Test
    fun `observeMaintenances with database error shows French error message`() = runTest {
        maintenanceRepository.setGetDoneFails(true, com.bikemanager.domain.common.AppError.DatabaseError("Database failed"))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(MaintenancesUiState.Loading, awaitItem())

            advanceUntilIdle()
            val errorState = awaitItem()
            assertTrue(errorState is MaintenancesUiState.Error)
            assertEquals("Erreur lors de la sauvegarde. Veuillez réessayer.", (errorState as MaintenancesUiState.Error).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addDoneMaintenance with database error shows French error message`() = runTest {
        maintenanceRepository.setAddFails(true, com.bikemanager.domain.common.AppError.DatabaseError("Add failed"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            // Skip initial state
            awaitItem()

            viewModel.addDoneMaintenance(name = "Oil Change", value = 5000f)
            advanceUntilIdle()

            val errorState = awaitItem()
            assertTrue(errorState is MaintenancesUiState.Error)
            assertEquals("Erreur lors de la sauvegarde. Veuillez réessayer.", (errorState as MaintenancesUiState.Error).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addTodoMaintenance with database error shows French error message`() = runTest {
        maintenanceRepository.setAddFails(true, com.bikemanager.domain.common.AppError.DatabaseError("Add failed"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            // Skip initial state
            awaitItem()

            viewModel.addTodoMaintenance(name = "Future Check")
            advanceUntilIdle()

            val errorState = awaitItem()
            assertTrue(errorState is MaintenancesUiState.Error)
            assertEquals("Erreur lors de la sauvegarde. Veuillez réessayer.", (errorState as MaintenancesUiState.Error).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `markMaintenanceDone with database error shows French error message`() = runTest {
        maintenanceRepository.setMaintenances(
            listOf(Maintenance(id = "m1", name = "Todo", isDone = false, bikeId = bikeId))
        )
        maintenanceRepository.setMarkDoneFails(true, com.bikemanager.domain.common.AppError.DatabaseError("Mark done failed"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            // Skip initial state
            awaitItem()

            viewModel.markMaintenanceDone(maintenanceId = "m1", value = 3000f)
            advanceUntilIdle()

            val errorState = awaitItem()
            assertTrue(errorState is MaintenancesUiState.Error)
            assertEquals("Erreur lors de la sauvegarde. Veuillez réessayer.", (errorState as MaintenancesUiState.Error).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteMaintenance with database error shows French error message`() = runTest {
        val maintenance = Maintenance(id = "m1", name = "To Delete", isDone = true, bikeId = bikeId)
        maintenanceRepository.setMaintenances(listOf(maintenance))
        maintenanceRepository.setDeleteFails(true, com.bikemanager.domain.common.AppError.DatabaseError("Delete failed"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.uiState.test {
            // Skip initial state
            awaitItem()

            viewModel.deleteMaintenance(maintenance)
            advanceUntilIdle()

            val errorState = awaitItem()
            assertTrue(errorState is MaintenancesUiState.Error)
            assertEquals("Erreur lors de la sauvegarde. Veuillez réessayer.", (errorState as MaintenancesUiState.Error).message)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
