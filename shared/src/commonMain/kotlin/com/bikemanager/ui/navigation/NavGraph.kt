package com.bikemanager.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

@Composable
fun BikeManagerNavGraph(
    navController: NavHostController,
    startDestination: Any
) {
    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 2 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 2 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable<Route.Login> {
                com.bikemanager.ui.auth.LoginScreenContent(
                    onSignedIn = {
                        navController.navigate(Route.Bikes) {
                            popUpTo<Route.Login> { inclusive = true }
                        }
                    }
                )
            }

            composable<Route.Bikes> {
                com.bikemanager.ui.bikes.BikesScreenContent()
            }

            composable<Route.Maintenances> { backStackEntry ->
                val route = backStackEntry.toRoute<Route.Maintenances>()
                com.bikemanager.ui.maintenances.MaintenancesScreenContent(
                    bikeId = route.bikeId,
                    bikeName = route.bikeName,
                    countingMethod = route.getCountingMethod(),
                    initialTab = route.initialTab
                )
            }
        }
    }
}
