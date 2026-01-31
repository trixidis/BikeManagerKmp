package com.bikemanager

import androidx.compose.ui.window.ComposeUIViewController
import com.bikemanager.di.initKoin
import com.bikemanager.ui.App
import com.bikemanager.ui.navigation.Route
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import platform.UIKit.UIViewController

/**
 * Creates the main UIViewController for iOS.
 * This is the entry point for the Compose Multiplatform UI on iOS.
 *
 * @param deepLinkRoute Optional deep link route to navigate to after authentication
 */
fun MainViewController(deepLinkRoute: Route? = null): UIViewController {
    // Initialize logging
    Napier.base(DebugAntilog())

    // Initialize Koin DI
    initKoin()

    if (deepLinkRoute != null) {
        Napier.d { "MainViewController créé avec deep link : $deepLinkRoute" }
    }

    // Return the Compose UI wrapped in a UIViewController
    return ComposeUIViewController { App(deepLinkRoute = deepLinkRoute) }
}
