package com.bikemanager.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.bikemanager.domain.model.ThemeMode
import com.bikemanager.presentation.auth.AuthUiState
import com.bikemanager.presentation.auth.AuthViewModel
import com.bikemanager.presentation.theme.ThemeUiState
import com.bikemanager.presentation.theme.ThemeViewModel
import com.bikemanager.presentation.auth.AuthViewModelMvi
import com.bikemanager.ui.navigation.BikesScreenDestination
import com.bikemanager.ui.navigation.LoginScreenDestination
import com.bikemanager.ui.theme.BikeManagerTheme
import org.koin.compose.koinInject

/**
 * Main entry point for the BikeManager app UI.
 * This composable sets up the theme and navigation.
 */
@Composable
fun App() {
    val authViewModel: AuthViewModelMvi = koinInject()
    val authState by authViewModel.uiState.collectAsState()

    val themeViewModel: ThemeViewModel = koinInject()
    val themeState by themeViewModel.uiState.collectAsState()

    val systemInDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (themeState) {
        is ThemeUiState.Success -> {
            when ((themeState as ThemeUiState.Success).themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemInDarkTheme
            }
        }
        else -> systemInDarkTheme // Default to system theme while loading or on error
    }

    BikeManagerTheme(darkTheme = darkTheme) {
        val startScreen = when (authState) {
            is AuthUiState.Authenticated -> BikesScreenDestination
            is AuthUiState.Checking -> LoginScreenDestination // Show login while checking
            else -> LoginScreenDestination
        }

        Navigator(startScreen) { navigator ->
            SlideTransition(navigator)
        }
    }
}
