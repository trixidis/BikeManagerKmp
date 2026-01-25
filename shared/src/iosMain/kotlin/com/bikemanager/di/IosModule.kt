package com.bikemanager.di

import com.bikemanager.data.local.DatabaseDriverFactory
import com.bikemanager.data.local.ThemePreferences
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-specific Koin module.
 * Provides platform-specific implementations for iOS.
 *
 * Note: AuthRepository and SyncRepository are now in SharedModule
 * using GitLive Firebase SDK (works on both platforms).
 */
val iosModule: Module = module {
    // Database Driver Factory (platform-specific)
    single { DatabaseDriverFactory() }

    // Theme Preferences (platform-specific)
    single { ThemePreferences() }
}
