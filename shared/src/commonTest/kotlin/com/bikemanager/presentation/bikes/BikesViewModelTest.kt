package com.bikemanager.presentation.bikes

import app.cash.turbine.test
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.usecase.bike.AddBikeUseCase
import com.bikemanager.domain.usecase.bike.GetBikesUseCase
import com.bikemanager.domain.usecase.bike.UpdateBikeUseCase
import com.bikemanager.fake.FakeBikeRepository
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
    private lateinit var getBikesUseCase: GetBikesUseCase
    private lateinit var addBikeUseCase: AddBikeUseCase
    private lateinit var updateBikeUseCase: UpdateBikeUseCase
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeBikeRepository()
        getBikesUseCase = GetBikesUseCase(repository)
        addBikeUseCase = AddBikeUseCase(repository)
        updateBikeUseCase = UpdateBikeUseCase(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading then Empty when no bikes`() = runTest {
        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase)

        viewModel.uiState.test {
            // Initial Loading state
            assertEquals(BikesUiState.Loading, awaitItem())

            // After loading, Empty state
            advanceUntilIdle()
            assertEquals(BikesUiState.Empty, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state is Loading then Success when bikes exist`() = runTest {
        val bikes = listOf(Bike(id = 1, name = "Test Bike"))
        repository.setBikes(bikes)

        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase)

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
        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase)
        advanceUntilIdle()

        viewModel.addBike("New Bike")
        advanceUntilIdle()

        val bikes = repository.getCurrentBikes()
        assertEquals(1, bikes.size)
        assertEquals("New Bike", bikes[0].name)
    }

    @Test
    fun `addBike with blank name does nothing`() = runTest {
        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase)
        advanceUntilIdle()

        viewModel.addBike("")
        advanceUntilIdle()

        val bikes = repository.getCurrentBikes()
        assertTrue(bikes.isEmpty())
    }

    @Test
    fun `addBike with whitespace only does nothing`() = runTest {
        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase)
        advanceUntilIdle()

        viewModel.addBike("   ")
        advanceUntilIdle()

        val bikes = repository.getCurrentBikes()
        assertTrue(bikes.isEmpty())
    }

    @Test
    fun `updateBike updates bike in repository`() = runTest {
        val initialBike = Bike(id = 1, name = "Old Name")
        repository.setBikes(listOf(initialBike))

        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase)
        advanceUntilIdle()

        val updatedBike = initialBike.copy(name = "New Name")
        viewModel.updateBike(updatedBike)
        advanceUntilIdle()

        val bikes = repository.getCurrentBikes()
        assertEquals("New Name", bikes[0].name)
    }

    @Test
    fun `updateBikeName updates bike name when in Success state`() = runTest {
        val initialBike = Bike(id = 1, name = "Old Name")
        repository.setBikes(listOf(initialBike))

        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase)
        advanceUntilIdle()

        viewModel.updateBikeName(1L, "New Name")
        advanceUntilIdle()

        val bikes = repository.getCurrentBikes()
        assertEquals("New Name", bikes[0].name)
    }

    @Test
    fun `updateBikeCountingMethod updates counting method when in Success state`() = runTest {
        val initialBike = Bike(id = 1, name = "Bike", countingMethod = CountingMethod.KM)
        repository.setBikes(listOf(initialBike))

        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase)
        advanceUntilIdle()

        viewModel.updateBikeCountingMethod(1L, CountingMethod.HOURS)
        advanceUntilIdle()

        val bikes = repository.getCurrentBikes()
        assertEquals(CountingMethod.HOURS, bikes[0].countingMethod)
    }

    @Test
    fun `updateBikeName with invalid id does nothing`() = runTest {
        val initialBike = Bike(id = 1, name = "Original Name")
        repository.setBikes(listOf(initialBike))

        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase)
        advanceUntilIdle()

        viewModel.updateBikeName(999L, "New Name")
        advanceUntilIdle()

        val bikes = repository.getCurrentBikes()
        assertEquals("Original Name", bikes[0].name)
    }

    @Test
    fun `loadBikes reloads bikes from repository`() = runTest {
        val viewModel = BikesViewModel(getBikesUseCase, addBikeUseCase, updateBikeUseCase)
        advanceUntilIdle()

        // Add a bike directly to repository
        repository.setBikes(listOf(Bike(id = 1, name = "New Bike")))

        // Reload
        viewModel.loadBikes()
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is BikesUiState.Success)
            assertEquals(1, (state as BikesUiState.Success).bikes.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
