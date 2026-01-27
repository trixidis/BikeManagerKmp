package com.bikemanager.ui.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController

/**
 * CompositionLocal for providing NavHostController to composables.
 * Allows screens to access the NavController without passing it explicitly.
 *
 * Usage:
 * ```
 * val navController = LocalNavController.current
 * navController.navigate(Route.Bikes.route)
 * ```
 */
val LocalNavController = compositionLocalOf<NavHostController> {
    error("No NavController provided. Make sure to wrap your composable with CompositionLocalProvider.")
}
