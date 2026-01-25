package com.bikemanager.ui.maintenances

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.model.Maintenance
import com.bikemanager.presentation.maintenances.MaintenancesUiState
import com.bikemanager.presentation.maintenances.MaintenancesViewModel
import com.bikemanager.ui.Strings
import com.bikemanager.ui.theme.Indigo
import com.bikemanager.ui.theme.Teal
import com.bikemanager.ui.theme.White
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MaintenancesScreenContent(
    bikeId: String,
    bikeName: String,
    countingMethod: CountingMethod,
    viewModel: MaintenancesViewModel = koinInject { parametersOf(bikeId) }
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

    val tabs = listOf(Strings.TAB_DONE, Strings.TAB_TODO)

    val headerColor = if (pagerState.currentPage == 0) Indigo else Teal

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(bikeName) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                                tint = White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = headerColor,
                        titleContentColor = White
                    )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(headerColor)
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                FloatingActionButton(
                    onClick = {
                        if (pagerState.currentPage == 0) {
                            showAddDoneDialog = true
                        } else {
                            showAddTodoDialog = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = Strings.ADD,
                        tint = White
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

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
                                Text(
                                    text = Strings.NO_MAINTENANCE,
                                    modifier = Modifier.padding(16.dp)
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
                                ) {  maintenance ->

                                    MaintenanceItem(
                                        maintenance = maintenance,
                                        countingMethod = countingMethod,
                                        isDone = page == 0,
                                        onClick = {
                                            if (page == 1) {
                                                markDoneMaintenance = maintenance
                                            }
                                        },
                                        onDelete = {
                                            viewModel.deleteMaintenance(maintenance)
                                            scope.launch {
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
