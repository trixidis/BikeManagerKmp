package com.bikemanager.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.ui.auth.LoginScreenContent
import com.bikemanager.ui.bikes.BikesScreenContent
import com.bikemanager.ui.maintenances.MaintenancesScreenContent

private const val ANIMATION_DURATION = 300

/**
 * Main navigation graph for BikeManager app.
 * Handles navigation between Login, Bikes, and Maintenances screens.
 *
 * @param navController The NavHostController to use for navigation
 * @param startDestination The initial destination route
 */
@Composable
fun BikeManagerNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: Any = Route.Login
) {
    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeIn(animationSpec = tween(ANIMATION_DURATION))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(ANIMATION_DURATION)
                ) + fadeOut(animationSpec = tween(ANIMATION_DURATION))
            }
        ) {
            // Login Screen
            composable<Route.Login> {
                LoginScreenContent(
                    onSignedIn = {
                        navController.navigate(Route.Bikes) {
                            popUpTo<Route.Login> { inclusive = true }
                        }
                    }
                )
            }

            // Bikes Screen
            composable<Route.Bikes> {
                BikesScreenContent()
            }

            // Maintenances Screen with parameters
            composable<Route.Maintenances> { backStackEntry ->
                val args = backStackEntry.toRoute<Route.Maintenances>()
                val countingMethod = try {
                    CountingMethod.valueOf(args.countingMethod)
                } catch (e: IllegalArgumentException) {
                    CountingMethod.KM
                }

                MaintenancesScreenContent(
                    bikeId = args.bikeId,
                    bikeName = args.bikeName,
                    countingMethod = countingMethod
                )
            }
        }
    }
}
