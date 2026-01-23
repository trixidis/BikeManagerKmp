package com.bikemanager.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bikemanager.android.ui.auth.SignInScreen
import com.bikemanager.android.ui.bikes.BikesScreen
import com.bikemanager.android.ui.maintenances.MaintenancesScreen
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.presentation.auth.AuthUiState
import com.bikemanager.presentation.auth.AuthViewModel
import org.koin.compose.koinInject

object NavRoutes {
    const val SIGN_IN = "sign_in"
    const val BIKES = "bikes"
    const val MAINTENANCES = "maintenances/{bikeId}/{bikeName}/{countingMethod}"

    fun maintenances(bikeId: Long, bikeName: String, countingMethod: CountingMethod): String {
        return "maintenances/$bikeId/$bikeName/${countingMethod.name}"
    }
}

@Composable
fun NavGraph(
    authViewModel: AuthViewModel = koinInject()
) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()

    // Handle auth state changes with navigation
    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.Authenticated -> {
                // Navigate to bikes if not already there
                if (navController.currentDestination?.route == NavRoutes.SIGN_IN) {
                    navController.navigate(NavRoutes.BIKES) {
                        popUpTo(NavRoutes.SIGN_IN) { inclusive = true }
                    }
                }
            }
            is AuthUiState.NotAuthenticated -> {
                // Navigate to sign in if not already there
                if (navController.currentDestination?.route != NavRoutes.SIGN_IN) {
                    navController.navigate(NavRoutes.SIGN_IN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> { /* Checking or Loading - do nothing */ }
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SIGN_IN
    ) {
        composable(NavRoutes.SIGN_IN) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(NavRoutes.BIKES) {
                        popUpTo(NavRoutes.SIGN_IN) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.BIKES) {
            BikesScreen(
                onBikeClick = { bike ->
                    navController.navigate(
                        NavRoutes.maintenances(bike.id, bike.name, bike.countingMethod)
                    )
                }
            )
        }

        composable(
            route = NavRoutes.MAINTENANCES,
            arguments = listOf(
                navArgument("bikeId") { type = NavType.LongType },
                navArgument("bikeName") { type = NavType.StringType },
                navArgument("countingMethod") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bikeId = backStackEntry.arguments?.getLong("bikeId") ?: return@composable
            val bikeName = backStackEntry.arguments?.getString("bikeName") ?: return@composable
            val countingMethodStr = backStackEntry.arguments?.getString("countingMethod") ?: return@composable
            val countingMethod = CountingMethod.valueOf(countingMethodStr)

            MaintenancesScreen(
                bikeId = bikeId,
                bikeName = bikeName,
                countingMethod = countingMethod,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
