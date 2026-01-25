package com.bikemanager.presentation.bikes

import app.cash.turbine.test
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.usecase.bike.AddBikeUseCase
import com.bikemanager.domain.usecase.bike.DeleteBikeUseCase
import com.bikemanager.domain.usecase.bike.GetBikesUseCase
import com.bikemanager.domain.usecase.bike.UpdateBikeUseCase
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
class BikesViewModelTest {
    private lateinit var repository: FakeBikeRepository
    private lateinit var maintenanceRepository: FakeMaintenanceRepository
    private lateinit var getBikesUseCase: GetBikesUseCase
    private lateinit var addBikeUseCase: AddBikeUseCase
    private lateinit var updateBikeUseCase: UpdateBikeUseCase
    private lateinit var deleteBikeUseCase: DeleteBikeUseCase
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeBikeRepository()
        maintenanceRepository = FakeMaintenanceRepository()
        getBikesUseCase = GetBikesUseCase(repository)
        addBikeUseCase = AddBikeUseCase(repository)
        updateBikeUseCase = UpdateBikeUseCase(repository)
        deleteBikeUseCase = DeleteBikeUseCase(repository, maintenanceRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading then Empty when no bikes`() = runTest {
        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase, deleteBikeUseCase, maintenanceRepository)

        viewModel.uiState.test {
            // Initial Loading state
            assertEquals(BikesUiState.Loading, awaitItem())

            // After loading, Empty state
            advanceUntilIdle()
            assertTrue(awaitItem() is BikesUiState.Empty)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state is Loading then Success when bikes exist`() = runTest {
        val bikes = listOf(Bike(id = "bike1", name = "Test Bike"))
        repository.setBikes(bikes)

        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase, deleteBikeUseCase, maintenanceRepository)

        viewModel.uiState.test {
            // Initial Loading state
            assertEquals(BikesUiState.Loading, awaitItem())

            // After loading, Success state with bikes
            advanceUntilIdle()
            val successState = awaitItem()
            assertTrue(successState is BikesUiState.Success)
            assertEquals(1, (successState as BikesUiState.Success).bikes.size)
            assertEquals("Test Bike", successState.bikes[0].name)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addBike adds bike to repository`() = runTest {
        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase, deleteBikeUseCase, maintenanceRepository)
        advanceUntilIdle()

        viewModel.addBike("New Bike")
        advanceUntilIdle()

        val bikes = repository.getCurrentBikes()
        assertEquals(1, bikes.size)
        assertEquals("New Bike", bikes[0].name)
    }

    @Test
    fun `addBike with blank name does nothing`() = runTest {
        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase, deleteBikeUseCase, maintenanceRepository)
        advanceUntilIdle()

        viewModel.addBike("")
        advanceUntilIdle()

        val bikes = repository.getCurrentBikes()
        assertTrue(bikes.isEmpty())
    }

    @Test
    fun `addBike with whitespace only does nothing`() = runTest {
        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase, deleteBikeUseCase, maintenanceRepository)
        advanceUntilIdle()

        viewModel.addBike("   ")
        advanceUntilIdle()

        val bikes = repository.getCurrentBikes()
        assertTrue(bikes.isEmpty())
    }

    @Test
    fun `updateBike updates bike in repository`() = runTest {
        val initialBike = Bike(id = "bike1", name = "Old Name")
        repository.setBikes(listOf(initialBike))

        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase, deleteBikeUseCase, maintenanceRepository)
        advanceUntilIdle()

        val updatedBike = initialBike.copy(name = "New Name")
        viewModel.updateBike(updatedBike)
        advanceUntilIdle()

        val bikes = repository.getCurrentBikes()
        assertEquals("New Name", bikes[0].name)
    }

    @Test
    fun `updateBikeName updates bike name when in Success state`() = runTest {
        val initialBike = Bike(id = "bike1", name = "Old Name")
        repository.setBikes(listOf(initialBike))

        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase, deleteBikeUseCase, maintenanceRepository)
        advanceUntilIdle()

        viewModel.updateBikeName("bike1", "New Name")
        advanceUntilIdle()

        val bikes = repository.getCurrentBikes()
        assertEquals("New Name", bikes[0].name)
    }

    @Test
    fun `updateBikeCountingMethod updates counting method when in Success state`() = runTest {
        val initialBike = Bike(id = "bike1", name = "Bike", countingMethod = CountingMethod.KM)
        repository.setBikes(listOf(initialBike))

        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase, deleteBikeUseCase, maintenanceRepository)
        advanceUntilIdle()

        viewModel.updateBikeCountingMethod("bike1", CountingMethod.HOURS)
        advanceUntilIdle()

        val bikes = repository.getCurrentBikes()
        assertEquals(CountingMethod.HOURS, bikes[0].countingMethod)
    }

    @Test
    fun `updateBikeName with invalid id does nothing`() = runTest {
        val initialBike = Bike(id = "bike1", name = "Original Name")
        repository.setBikes(listOf(initialBike))

        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase, deleteBikeUseCase, maintenanceRepository)
        advanceUntilIdle()

        viewModel.updateBikeName("nonexistent", "New Name")
        advanceUntilIdle()

        val bikes = repository.getCurrentBikes()
        assertEquals("Original Name", bikes[0].name)
    }

    @Test
    fun `deleteBike removes bike from state`() = runTest {
        // Given
        val bike = Bike(id = "bike1", name = "Test Bike")
        repository.setBikes(listOf(bike))
        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase, deleteBikeUseCase, maintenanceRepository)
        advanceUntilIdle()

        // When
        viewModel.deleteBike(bike)
        advanceUntilIdle()

        // Then
        val bikes = repository.getCurrentBikes()
        assertTrue(bikes.isEmpty())
    }

    @Test
    fun `undoDelete restores deleted bike`() = runTest {
        // Given
        val bike = Bike(id = "bike1", name = "Test Bike")
        repository.setBikes(listOf(bike))
        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase, deleteBikeUseCase, maintenanceRepository)
        advanceUntilIdle()

        // When
        viewModel.deleteBike(bike)
        advanceUntilIdle()
        viewModel.undoDelete()
        advanceUntilIdle()

        // Then
        val bikes = repository.getCurrentBikes()
        assertEquals(1, bikes.size)
        assertEquals("Test Bike", bikes[0].name)
    }

    @Test
    fun `undoDelete does nothing when no bike was deleted`() = runTest {
        // Given
        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase, deleteBikeUseCase, maintenanceRepository)
        advanceUntilIdle()

        // When
        viewModel.undoDelete()
        advanceUntilIdle()

        // Then - no crash, no changes
        val bikes = repository.getCurrentBikes()
        assertTrue(bikes.isEmpty())
    }

    @Test
    fun `second deleteBike clears previous undo buffer`() = runTest {
        // Given
        val bike1 = Bike(id = "bike1", name = "Bike 1")
        val bike2 = Bike(id = "bike2", name = "Bike 2")
        repository.setBikes(listOf(bike1, bike2))
        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase, deleteBikeUseCase, maintenanceRepository)
        advanceUntilIdle()

        // When
        viewModel.deleteBike(bike1)
        advanceUntilIdle()
        viewModel.deleteBike(bike2)
        advanceUntilIdle()
        viewModel.undoDelete()  // Should restore bike2, not bike1
        advanceUntilIdle()

        // Then
        val bikes = repository.getCurrentBikes()
        assertEquals(1, bikes.size)
        assertEquals("Bike 2", bikes[0].name)
    }

    @Test
    fun `undoDelete restores bike with maintenances`() = runTest {
        // Given - bike with maintenances
        val bike = Bike(id = "bike1", name = "Test Bike")
        repository.setBikes(listOf(bike))

        val maintenance1 = com.bikemanager.domain.model.Maintenance(
            id = "m1",
            name = "Oil Change",
            bikeId = "bike1",
            isDone = true
        )
        val maintenance2 = com.bikemanager.domain.model.Maintenance(
            id = "m2",
            name = "Tire Check",
            bikeId = "bike1",
            isDone = false
        )
        maintenanceRepository.setMaintenances(listOf(maintenance1, maintenance2))

        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase, deleteBikeUseCase, maintenanceRepository)
        advanceUntilIdle()

        // When - delete and undo
        viewModel.deleteBike(bike)
        advanceUntilIdle()

        // Verify bike and maintenances are deleted
        assertTrue(repository.getCurrentBikes().isEmpty())
        assertTrue(maintenanceRepository.getCurrentMaintenances("bike1").isEmpty())

        viewModel.undoDelete()
        advanceUntilIdle()

        // Then - bike restored with same ID
        val bikes = repository.getCurrentBikes()
        assertEquals(1, bikes.size)
        assertEquals("bike1", bikes[0].id)
        assertEquals("Test Bike", bikes[0].name)

        // And maintenances restored
        val maintenances = maintenanceRepository.getCurrentMaintenances("bike1")
        assertEquals(2, maintenances.size)
        assertEquals("Oil Change", maintenances.find { it.id == "m1" }?.name)
        assertEquals("Tire Check", maintenances.find { it.id == "m2" }?.name)
    }

    @Test
    fun `undoDelete restores bike with original ID not new ID`() = runTest {
        // Given
        val originalBike = Bike(id = "original-id-123", name = "Test Bike")
        repository.setBikes(listOf(originalBike))
        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase, deleteBikeUseCase, maintenanceRepository)
        advanceUntilIdle()

        // When - delete and undo
        viewModel.deleteBike(originalBike)
        advanceUntilIdle()
        viewModel.undoDelete()
        advanceUntilIdle()

        // Then - bike restored with SAME ID (not a new generated ID)
        val bikes = repository.getCurrentBikes()
        assertEquals(1, bikes.size)
        assertEquals("original-id-123", bikes[0].id, "Bike should be restored with original ID")
    }
}
