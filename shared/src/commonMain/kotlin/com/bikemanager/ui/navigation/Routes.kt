package com.bikemanager.ui.navigation

import com.bikemanager.domain.model.CountingMethod
import kotlinx.serialization.Serializable

/**
 * Type-safe route definitions for Navigation Compose using Kotlin serialization.
 * Each route is a serializable class that the NavController can navigate to.
 */
sealed interface Route {
    /**
     * Login screen route.
     */
    @Serializable
    data object Login : Route

    /**
     * Bikes list screen route.
     */
    @Serializable
    data object Bikes : Route

    /**
     * Maintenances screen route with parameters.
     *
     * @param bikeId The ID of the bike
     * @param bikeName The name of the bike
     * @param countingMethod The counting method as string (KM or HOURS)
     */
    @Serializable
    data class Maintenances(
        val bikeId: String,
        val bikeName: String,
        val countingMethod: String
    ) : Route {
        companion object {
            /**
             * Creates a Maintenances route from a CountingMethod enum.
             */
            fun create(
                bikeId: String,
                bikeName: String,
                countingMethod: CountingMethod
            ): Maintenances = Maintenances(
                bikeId = bikeId,
                bikeName = bikeName,
                countingMethod = countingMethod.name
            )
        }
    }
}
