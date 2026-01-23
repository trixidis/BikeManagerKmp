package com.bikemanager.android.ui.bikes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bikemanager.android.R
import com.bikemanager.android.ui.theme.White
import com.bikemanager.domain.model.Bike
import com.bikemanager.presentation.bikes.BikesUiState
import com.bikemanager.presentation.bikes.BikesViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikesScreen(
    onBikeClick: (Bike) -> Unit,
    viewModel: BikesViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingBike by remember { mutableStateOf<Bike?>(null) }
    val listState = rememberLazyListState()

    // FAB visibility based on scroll direction
    val fabVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 || !listState.isScrollInProgress
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = White
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add),
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
                        text = stringResource(R.string.no_bikes),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is BikesUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState
                    ) {
                        itemsIndexed(
                            items = state.bikes,
                            key = { _, bike -> bike.id }
                        ) { index, bike ->
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(bike.id) {
                                visible = true
                            }
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = index * 50)) +
                                        slideInVertically(
                                            animationSpec = tween(durationMillis = 300, delayMillis = index * 50),
                                            initialOffsetY = { it / 2 }
                                        )
                            ) {
                                BikeItem(
                                    bike = bike,
                                    onClick = { onBikeClick(bike) },
                                    onEditClick = { editingBike = bike }
                                )
                            }
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

    if (showAddDialog) {
        AddBikeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.addBike(name)
                showAddDialog = false
            }
        )
    }

    editingBike?.let { bike ->
        EditBikeDialog(
            bike = bike,
            onDismiss = { editingBike = null },
            onConfirm = { updatedBike ->
                viewModel.updateBike(updatedBike)
                editingBike = null
            }
        )
    }
}
