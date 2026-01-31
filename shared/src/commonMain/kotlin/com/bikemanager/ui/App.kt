package com.bikemanager.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 *
 * @param deepLinkRoute Optional deep link route to navigate to after authentication
 */
@Composable
fun App(deepLinkRoute: Route? = null) {
    val authViewModel: AuthViewModelMvi = koinInject()
    val authState by authViewModel.uiState.collectAsState()
    val navController = rememberNavController()
    var lastNavigatedDeepLink by remember { mutableStateOf<Route?>(null) }

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
        LaunchedEffect(authState, deepLinkRoute) {
            when (authState) {
                is AuthUiState.Authenticated -> {
                    val currentRoute = navController.currentDestination?.route

                    // Navigate to deep link if provided and different from last navigated
                    if (deepLinkRoute != null && deepLinkRoute != lastNavigatedDeepLink) {
                        navController.navigate(deepLinkRoute) {
                            if (currentRoute?.contains("Login") == true) {
                                popUpTo<Route.Login> { inclusive = true }
                            }
                        }
                        lastNavigatedDeepLink = deepLinkRoute
                    } else if (currentRoute?.contains("Login") == true) {
                        // Navigate to bikes if not already there
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
