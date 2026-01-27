package com.bikemanager.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.bikemanager.presentation.auth.AuthUiState
import com.bikemanager.presentation.auth.AuthViewModelMvi
import com.bikemanager.ui.navigation.BikeManagerNavGraph
import com.bikemanager.ui.navigation.Route
import com.bikemanager.ui.theme.BikeManagerTheme
import org.koin.compose.koinInject

/**
 * Main entry point for the BikeManager app UI.
 * This composable sets up the theme and navigation using Jetpack Navigation Compose.
 */
@Composable
fun App() {
    val authViewModel: AuthViewModelMvi = koinInject()
    val authState by authViewModel.uiState.collectAsState()
    val navController = rememberNavController()

    BikeManagerTheme {
        // Determine start destination based on auth state
        val startDestination: Any = when (authState) {
            is AuthUiState.Authenticated -> Route.Bikes
            else -> Route.Login
        }

        BikeManagerNavGraph(
            navController = navController,
            startDestination = startDestination
        )

        // Handle auth state changes for navigation
        LaunchedEffect(authState) {
            when (authState) {
                is AuthUiState.Authenticated -> {
                    // Navigate to bikes if not already there
                    val currentRoute = navController.currentDestination?.route
                    if (currentRoute?.contains("Login") == true) {
                        navController.navigate(Route.Bikes) {
                            popUpTo<Route.Login> { inclusive = true }
                        }
                    }
                }
                is AuthUiState.NotAuthenticated -> {
                    // Navigate to login if user logged out
                    val currentRoute = navController.currentDestination?.route
                    if (currentRoute?.contains("Login") != true) {
                        navController.navigate(Route.Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                else -> {
                    // Loading/Checking states - do nothing
                }
            }
        }
    }
}
