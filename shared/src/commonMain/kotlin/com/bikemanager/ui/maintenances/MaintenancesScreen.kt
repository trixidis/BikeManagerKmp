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
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import com.bikemanager.ui.navigation.LocalNavController
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.presentation.maintenances.MaintenanceEvent
import com.bikemanager.presentation.maintenances.MaintenancesUiState
import com.bikemanager.presentation.maintenances.MaintenancesViewModelMvi
import bikemanager.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
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
import com.bikemanager.ui.core.rememberFabVisibility
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MaintenancesScreenContent(
    bikeId: String,
    bikeName: String,
    countingMethod: CountingMethod,
    initialTab: Int = 0,
    viewModel: MaintenancesViewModelMvi = koinInject { parametersOf(bikeId) }
) {
    val navController = LocalNavController.current
    val uiState by viewModel.uiState.collectAsState()
    val currentBike by viewModel.currentBike.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = initialTab.coerceIn(0, 1),
        pageCount = { 2 }
    )
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Use live countingMethod from ViewModel if available, fallback to parameter
    val activeCountingMethod = currentBike?.countingMethod ?: countingMethod

    var showAddDoneDialog by remember { mutableStateOf(false) }
    var showAddTodoDialog by remember { mutableStateOf(false) }
    var markDoneMaintenance by remember { mutableStateOf<Maintenance?>(null) }

    // Track restoration versions to force fresh composition after undo
    val restorationVersions = remember { mutableStateMapOf<String, Int>() }

    // Track previous maintenance IDs to detect restorations
    val previousMaintenanceIds = remember { mutableStateOf<Set<String>>(emptySet()) }

    // Detect when items are restored and increment their version
    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is MaintenancesUiState.Success) {
            val currentIds = (currentState.doneMaintenances + currentState.todoMaintenances).map { it.id }.toSet()
            val restoredIds = currentIds - previousMaintenanceIds.value

            // Increment version for restored items (items that reappeared)
            restoredIds.forEach { id ->
                if (previousMaintenanceIds.value.isNotEmpty()) { // Skip initial load
                    restorationVersions[id] = (restorationVersions[id] ?: 0) + 1
                }
            }

            previousMaintenanceIds.value = currentIds
        }
    }

    val doneListState = rememberLazyListState()
    val todoListState = rememberLazyListState()
    val currentListState = if (pagerState.currentPage == 0) doneListState else todoListState
    val fabVisible = rememberFabVisibility(currentListState)


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

    // Extract strings before entering LaunchedEffect
    val maintenanceDeletedText = stringResource(Res.string.maintenance_deleted)
    val undoText = stringResource(Res.string.undo)

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
                        message = maintenanceDeletedText,
                        actionLabel = undoText,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        // Process undo in a new coroutine to avoid blocking the event collection
                        scope.launch {
                            // Dismiss snackbar and wait for animation to complete
                            snackbarHostState.currentSnackbarData?.dismiss()
                            kotlinx.coroutines.delay(100)
                            viewModel.undoDelete()
                        }
                    }
                }
                MaintenanceEvent.BikeDeleted -> {
                    // Navigate back - bike was deleted
                    navController.popBackStack()
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                countingMethod = activeCountingMethod,
                activeTab = currentTabVariant,
                onBackClick = { navController.popBackStack() }
            )

            // Premium Tabs with overlapping layout
            Tabs(
                tabs = listOf(
                    TabItem(
                        label = stringResource(Res.string.tab_done),
                        count = doneCount,
                        variant = TabVariant.DONE
                    ),
                    TabItem(
                        label = stringResource(Res.string.tab_todo),
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
                                    message = stringResource(Res.string.no_maintenance)
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
                                    key = { maintenance ->
                                        // Use composite key: ID + restoration version
                                        // This forces new composition when item is restored
                                        "${maintenance.id}_${restorationVersions[maintenance.id] ?: 0}"
                                    }
                                ) { maintenance ->
                                    MaintenanceCard(
                                        maintenance = maintenance,
                                        countingMethod = activeCountingMethod,
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
            countingMethod = activeCountingMethod,
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
            countingMethod = activeCountingMethod,
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
            countingMethod = activeCountingMethod,
            onDismiss = { markDoneMaintenance = null },
            onConfirm = { value ->
                viewModel.markMaintenanceDone(maintenance.id, value)
                markDoneMaintenance = null
            }
        )
    }
}
