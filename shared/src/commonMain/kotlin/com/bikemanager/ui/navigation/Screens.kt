package com.bikemanager.ui.navigation

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.bikemanager.domain.model.CountingMethod

/**
 * Voyager Screen for Login.
 */
data object LoginScreenDestination : Screen {
    @androidx.compose.runtime.Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        com.bikemanager.ui.auth.LoginScreenContent(
            onSignedIn = {
                navigator.replaceAll(BikesScreenDestination)
            }
        )
    }
}

/**
 * Voyager Screen for the Bikes list.
 */
data object BikesScreenDestination : Screen {
    @androidx.compose.runtime.Composable
    override fun Content() {
        com.bikemanager.ui.bikes.BikesScreenContent()
    }
}

/**
 * Voyager Screen for Maintenances of a specific bike.
 */
data class MaintenancesScreenDestination(
    val bikeId: String,
    val bikeName: String,
    val countingMethod: CountingMethod
) : Screen {
    @androidx.compose.runtime.Composable
    override fun Content() {
        com.bikemanager.ui.maintenances.MaintenancesScreenContent(
            bikeId = bikeId,
            bikeName = bikeName,
            countingMethod = countingMethod
        )
    }
}
