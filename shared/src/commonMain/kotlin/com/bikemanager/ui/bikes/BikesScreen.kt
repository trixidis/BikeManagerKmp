package com.bikemanager.ui.bikes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bikemanager.domain.model.Bike
import com.bikemanager.presentation.bikes.BikesUiState
import com.bikemanager.presentation.bikes.BikesViewModel
import com.bikemanager.ui.Strings
import com.bikemanager.ui.core.rememberFabVisibility
import com.bikemanager.ui.navigation.MaintenancesScreenDestination
import com.bikemanager.ui.navigation.SettingsScreenDestination
import com.bikemanager.ui.theme.White
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikesScreenContent(
    viewModel: BikesViewModel = koinInject()
) {
    val navigator = LocalNavigator.currentOrThrow
    val uiState by viewModel.uiState.collectAsState()
    val showAddDialog = remember { mutableStateOf(false) }
    val editingBike = remember { mutableStateOf<Bike?>(null) }
    val listState = rememberLazyListState()
    val fabVisible = rememberFabVisibility(listState)


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.APP_NAME) },
                actions = {
                    IconButton(onClick = { navigator.push(SettingsScreenDestination) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = Strings.SETTINGS,
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = White
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it *2}),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it *2})
            ) {
                FloatingActionButton(
                    onClick = { showAddDialog.value = true },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = Strings.ADD,
                        tint = White
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is BikesUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is BikesUiState.Empty -> {
                    Text(
                        text = Strings.NO_BIKES,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
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
                                onClick = {
                                    navigator.push(
                                        MaintenancesScreenDestination(
                                            bikeId = bike.id,
                                            bikeName = bike.name,
                                            countingMethod = bike.countingMethod
                                        )
                                    )
                                },
                                onEditClick = { editingBike.value = bike }
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
}