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
        val countingMethodString: String,
        val initialTab: Int = 0 // 0 = Fait, 1 = À faire
    ) : Route {
        companion object {
            fun create(
                bikeId: String,
                bikeName: String,
                countingMethod: CountingMethod,
                initialTab: Int = 0
            ): Maintenances {
                return Maintenances(
                    bikeId = bikeId,
                    bikeName = bikeName,
                    countingMethodString = countingMethod.name,
                    initialTab = initialTab
                )
            }
        }

        fun getCountingMethod(): CountingMethod {
            return CountingMethod.valueOf(countingMethodString)
        }
    }
}
