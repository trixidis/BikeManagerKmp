package com.bikemanager.ui.maintenances

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.presentation.maintenances.MaintenanceEvent
import com.bikemanager.presentation.maintenances.MaintenancesUiState
import com.bikemanager.presentation.maintenances.MaintenancesViewModelMvi
import com.bikemanager.ui.Strings
import com.bikemanager.ui.components.EmptyState
import com.bikemanager.ui.components.Fab
import com.bikemanager.ui.components.FabVariant
import com.bikemanager.ui.components.MaintenanceCard
import com.bikemanager.ui.components.ParallaxHeader
import com.bikemanager.ui.components.PremiumSnackbarHost
import com.bikemanager.ui.components.SnackbarType
import com.bikemanager.ui.components.TabItem
import com.bikemanager.ui.components.TabVariant
import com.bikemanager.ui.components.Tabs
import com.bikemanager.ui.theme.BgPrimary
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MaintenancesScreenContent(
    bikeId: String,
    bikeName: String,
    countingMethod: CountingMethod,
    viewModel: MaintenancesViewModelMvi = koinInject { parametersOf(bikeId) }
) {
    val navigator = LocalNavigator.currentOrThrow
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDoneDialog by remember { mutableStateOf(false) }
    var showAddTodoDialog by remember { mutableStateOf(false) }
    var markDoneMaintenance by remember { mutableStateOf<Maintenance?>(null) }

    val doneListState = rememberLazyListState()
    val todoListState = rememberLazyListState()

    val fabVisible by remember {
        derivedStateOf {
            val currentListState = if (pagerState.currentPage == 0) doneListState else todoListState
            currentListState.firstVisibleItemIndex == 0 || !currentListState.isScrollInProgress
        }
    }

    // Calculate total km/hours from done maintenances
    val totalValue = when (val state = uiState) {
        is MaintenancesUiState.Success -> {
            state.doneMaintenances
                .mapNotNull { it.value }
                .maxOrNull() ?: 0f
        }
        else -> 0f
    }

    // Tab variant based on current page
    val currentTabVariant = if (pagerState.currentPage == 0) TabVariant.DONE else TabVariant.TODO

    // Count of maintenances for each tab
    val (doneCount, todoCount) = when (val state = uiState) {
        is MaintenancesUiState.Success -> {
            Pair(state.doneMaintenances.size, state.todoMaintenances.size)
        }
        else -> Pair(0, 0)
    }

    // Collect events from ViewModel (MVI pattern)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MaintenanceEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is MaintenanceEvent.ShowSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is MaintenanceEvent.ShowUndoSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = Strings.MAINTENANCE_DELETED,
                        actionLabel = Strings.UNDO,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete()
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = BgPrimary,
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                Fab(
                    onClick = {
                        if (pagerState.currentPage == 0) {
                            showAddDoneDialog = true
                        } else {
                            showAddTodoDialog = true
                        }
                    },
                    variant = if (pagerState.currentPage == 0) FabVariant.ORANGE else FabVariant.TEAL
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
                        message.contains("validé", ignoreCase = true) -> SnackbarType.SUCCESS
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
            // ParallaxHeader with gradient animation
            ParallaxHeader(
                bikeName = bikeName,
                totalValue = totalValue,
                countingMethod = countingMethod,
                activeTab = currentTabVariant,
                onBackClick = { navigator.pop() }
            )

            // Premium Tabs with overlapping layout
            Tabs(
                tabs = listOf(
                    TabItem(
                        label = Strings.TAB_DONE,
                        count = doneCount,
                        variant = TabVariant.DONE
                    ),
                    TabItem(
                        label = Strings.TAB_TODO,
                        count = todoCount,
                        variant = TabVariant.TODO
                    )
                ),
                selectedIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                modifier = Modifier.layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(0, -24.dp.roundToPx())
                    }
                }
            )

            when (val state = uiState) {
                is MaintenancesUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is MaintenancesUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                is MaintenancesUiState.Success -> {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val maintenances = if (page == 0) {
                            state.doneMaintenances
                        } else {
                            state.todoMaintenances
                        }

                        if (maintenances.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                EmptyState(
                                    message = Strings.NO_MAINTENANCE
                                )
                            }
                        } else {
                            val listState = if (page == 0) doneListState else todoListState
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = listState
                            ) {
                                items(
                                    items = maintenances,
                                    key = { maintenance -> maintenance.id }
                                ) { maintenance ->
                                    MaintenanceCard(
                                        maintenance = maintenance,
                                        countingMethod = countingMethod,
                                        onMarkDoneClick = {
                                            if (page == 1) {
                                                markDoneMaintenance = maintenance
                                            }
                                        },
                                        onDeleteSwipe = {
                                            viewModel.deleteMaintenance(maintenance)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDoneDialog) {
        AddMaintenanceDialog(
            isDone = true,
            countingMethod = countingMethod,
            onDismiss = { showAddDoneDialog = false },
            onConfirm = { name, value ->
                viewModel.addDoneMaintenance(name, value)
                showAddDoneDialog = false
            }
        )
    }

    if (showAddTodoDialog) {
        AddMaintenanceDialog(
            isDone = false,
            countingMethod = countingMethod,
            onDismiss = { showAddTodoDialog = false },
            onConfirm = { name, _ ->
                viewModel.addTodoMaintenance(name)
                showAddTodoDialog = false
            }
        )
    }

    markDoneMaintenance?.let { maintenance ->
        MarkDoneDialog(
            maintenance = maintenance,
            countingMethod = countingMethod,
            onDismiss = { markDoneMaintenance = null },
            onConfirm = { value ->
                viewModel.markMaintenanceDone(maintenance.id, value)
                markDoneMaintenance = null
            }
        )
    }
}
