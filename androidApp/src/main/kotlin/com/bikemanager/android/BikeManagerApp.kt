package com.bikemanager.android

import android.app.Application
import com.bikemanager.data.local.DatabaseDriverFactory
import com.bikemanager.data.local.ThemePreferences
import com.bikemanager.di.sharedModule
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class BikeManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize KMPAuth Google Sign-In with build-specific Web Client ID
        // Debug → Dev Firebase, Release → Prod Firebase
        GoogleAuthProvider.create(credentials = GoogleAuthCredentials(serverId = BuildConfig.WEB_CLIENT_ID))

        startKoin {
            androidLogger()
            androidContext(this@BikeManagerApp)
            modules(
                androidModule,
                sharedModule
            )
        }
    }
}

/**
 * Android-specific Koin module.
 * Only provides platform-specific dependencies.
 * All Firebase/Auth logic is now in shared module using GitLive SDK.
 */
val androidModule = module {
    // Database Driver Factory (platform-specific)
    single { DatabaseDriverFactory(get()) }

    // Theme Preferences (platform-specific)
    single { ThemePreferences(get()) }
}
