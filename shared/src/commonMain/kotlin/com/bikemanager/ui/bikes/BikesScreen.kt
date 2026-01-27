package com.bikemanager.ui.bikes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.domain.usecase.maintenance.GetMaintenancesUseCase
import com.bikemanager.presentation.bikes.BikeEvent
import com.bikemanager.presentation.bikes.BikesUiState
import com.bikemanager.presentation.bikes.BikesViewModelMvi
import bikemanager.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.bikemanager.ui.components.EmptyState
import com.bikemanager.ui.components.Fab
import com.bikemanager.ui.components.FabVariant
import com.bikemanager.ui.components.PremiumSnackbarHost
import com.bikemanager.ui.components.SnackbarType
import com.bikemanager.ui.core.rememberFabVisibility
import com.bikemanager.ui.navigation.MaintenancesScreenDestination
import com.bikemanager.ui.theme.AccentOrange
import com.bikemanager.ui.theme.BgPrimary
import com.bikemanager.ui.theme.Dimens
import com.bikemanager.ui.theme.TextPrimary
import kotlinx.coroutines.flow.firstOrNull
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikesScreenContent(
    viewModel: BikesViewModelMvi = koinInject(),
    getMaintenancesUseCase: GetMaintenancesUseCase = koinInject()
) {
    val navigator = LocalNavigator.currentOrThrow
    val uiState by viewModel.uiState.collectAsState()
    val showAddDialog = remember { mutableStateOf(false) }
    val editingBike = remember { mutableStateOf<Bike?>(null) }
    val deletingBike = remember { mutableStateOf<Bike?>(null) }
    val listState = rememberLazyListState()
    val fabVisible = rememberFabVisibility(listState)
    val snackbarHostState = remember { SnackbarHostState() }

    // Calculate total km/hours for each bike
    val bikeTotals = remember { mutableStateMapOf<String, Float>() }

    LaunchedEffect(uiState) {
        if (uiState is BikesUiState.Success) {
            val bikes = (uiState as BikesUiState.Success).bikes
            bikes.forEach { currentBike ->
                val result = getMaintenancesUseCase(currentBike.id).firstOrNull()
                if (result != null) {
                    when (result) {
                        is Result.Success -> {
                            val doneList = result.value.first
                            val todoList = result.value.second
                            // Get max value from all maintenances (done + todo)
                            val allMaintenances: List<Maintenance> = doneList + todoList
                            val values = allMaintenances.mapNotNull { maintenance ->
                                maintenance.value
                            }
                            val maxValue = if (values.isNotEmpty()) {
                                values.max()
                            } else {
                                0f
                            }
                            bikeTotals[currentBike.id] = maxValue
                        }
                        is Result.Failure -> {
                            // Ignore errors, keep default 0
                        }
                    }
                }
            }
        }
    }

    // Collect events from ViewModel (MVI pattern)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BikeEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is BikeEvent.ShowSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = BgPrimary,
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it * 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it * 2 })
            ) {
                Fab(
                    onClick = { showAddDialog.value = true },
                    variant = FabVariant.ORANGE
                )
            }
        },
        snackbarHost = {
            PremiumSnackbarHost(
                hostState = snackbarHostState,
                getSnackbarType = { message ->
                    when {
                        message.contains("ajouté", ignoreCase = true) ||
                        message.contains("modifié", ignoreCase = true) ||
                        message.contains("supprimé", ignoreCase = true) -> SnackbarType.SUCCESS
                        message.contains("erreur", ignoreCase = true) -> SnackbarType.ERROR
                        else -> SnackbarType.INFO
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Premium Header with orange dot
            Row(
                modifier = Modifier.padding(
                    horizontal = Dimens.Space2xl,
                    vertical = Dimens.Space3xl
                )
            ) {
                Text(
                    text = stringResource(Res.string.my_maintenances),
                    style = MaterialTheme.typography.displayLarge,
                    color = TextPrimary
                )
                Text(
                    text = ".",
                    style = MaterialTheme.typography.displayLarge,
                    color = AccentOrange
                )
            }

            Spacer(modifier = Modifier.height(Dimens.SpaceMd))

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is BikesUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is BikesUiState.Empty -> {
                        EmptyState(
                            message = stringResource(Res.string.no_bikes),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is BikesUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            state = listState
                        ) {
                            items(
                                items = state.bikes,
                                key = { bike -> bike.id }
                            ) { bike ->
                                BikeItem(
                                    bike = bike,
                                    totalKmOrHours = bikeTotals[bike.id] ?: 0f,
                                    onClick = {
                                        navigator.push(
                                            MaintenancesScreenDestination(
                                                bikeId = bike.id,
                                                bikeName = bike.name,
                                                countingMethod = bike.countingMethod
                                            )
                                        )
                                    },
                                    onEditClick = { editingBike.value = bike },
                                    onLongPress = { deletingBike.value = bike }
                                )
                            }
                        }
                    }

                    is BikesUiState.Error -> {
                        Text(
                            text = state.message,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog.value) {
        AddBikeDialog(
            onDismiss = { showAddDialog.value = false },
            onConfirm = { name ->
                viewModel.addBike(name)
                showAddDialog.value = false
            }
        )
    }

    editingBike.value?.let { bike ->
        EditBikeDialog(
            bike = bike,
            onDismiss = { editingBike.value = null },
            onConfirm = { updatedBike ->
                viewModel.updateBike(updatedBike)
                editingBike.value = null
            }
        )
    }

    deletingBike.value?.let { bike ->
        DeleteBikeConfirmationDialog(
            bike = bike,
            onDismiss = { deletingBike.value = null },
            onConfirm = {
                viewModel.deleteBike(bike.id)
                deletingBike.value = null
            }
        )
    }
}