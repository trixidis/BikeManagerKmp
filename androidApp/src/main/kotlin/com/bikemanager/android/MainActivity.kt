package com.bikemanager.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.ui.App
import com.bikemanager.ui.auth.ActivityProvider
import com.bikemanager.ui.navigation.Route
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

class MainActivity : ComponentActivity() {
    private var deepLinkRoute by mutableStateOf<Route?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register this activity for Apple Sign In (and other features requiring Activity)
        ActivityProvider.currentActivity = this

        // Initialize Napier logging in debug mode
        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())
        }

        // Parse deep link from intent
        deepLinkRoute = parseDeepLink(intent)

        setContent {
            App(deepLinkRoute = deepLinkRoute)
        }
    }

    override fun onDestroy() {
        // Clear activity reference to avoid memory leaks
        if (ActivityProvider.currentActivity == this) {
            ActivityProvider.currentActivity = null
        }
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // Parse and navigate to deep link when app is already running
        val route = parseDeepLink(intent)
        if (route != null) {
            deepLinkRoute = route
            Napier.d { "Deep link reçu (onNewIntent) : $route" }
        }
    }

    private fun parseDeepLink(intent: Intent?): Route? {
        val uri = intent?.data ?: return null

        if (uri.scheme == "bikemanager" && uri.host == "maintenance") {
            val bikeId = uri.getQueryParameter("bikeId") ?: return null
            val bikeName = uri.getQueryParameter("bikeName") ?: return null
            val countingMethodStr = uri.getQueryParameter("countingMethod") ?: "KM"
            val openTab = uri.getQueryParameter("openTab")

            val countingMethod = try {
                CountingMethod.valueOf(countingMethodStr)
            } catch (e: IllegalArgumentException) {
                Napier.w { "Méthode de comptage invalide : $countingMethodStr, utilisation de KM par défaut" }
                CountingMethod.KM
            }

            val initialTab = if (openTab == "todo") 1 else 0

            Napier.d { "Deep link parsé : bikeId=$bikeId, bikeName=$bikeName, countingMethod=$countingMethod, initialTab=$initialTab" }

            return Route.Maintenances(
                bikeId = bikeId,
                bikeName = bikeName,
                countingMethodString = countingMethod.name,
                initialTab = initialTab
            )
        }

        return null
    }
}
