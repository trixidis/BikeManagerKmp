package com.bikemanager

import androidx.compose.ui.window.ComposeUIViewController
import com.bikemanager.di.initKoin
import com.bikemanager.ui.App
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import platform.UIKit.UIViewController

/**
 * Creates the main UIViewController for iOS.
 * This is the entry point for the Compose Multiplatform UI on iOS.
 */
fun MainViewController(): UIViewController {
    // Initialize logging
    Napier.base(DebugAntilog())

    // Initialize Koin DI
    initKoin()

    // Return the Compose UI wrapped in a UIViewController
    return ComposeUIViewController { App() }
}
