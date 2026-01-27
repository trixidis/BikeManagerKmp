package com.bikemanager.ui.navigation

import com.bikemanager.domain.model.CountingMethod
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object Login : Route

    @Serializable
    data object Bikes : Route

    @Serializable
    data class Maintenances(
        val bikeId: String,
        val bikeName: String,
        val countingMethodString: String
    ) : Route {
        companion object {
            fun create(bikeId: String, bikeName: String, countingMethod: CountingMethod): Maintenances {
                return Maintenances(
                    bikeId = bikeId,
                    bikeName = bikeName,
                    countingMethodString = countingMethod.name
                )
            }
        }

        fun getCountingMethod(): CountingMethod {
            return CountingMethod.valueOf(countingMethodString)
        }
    }
}
