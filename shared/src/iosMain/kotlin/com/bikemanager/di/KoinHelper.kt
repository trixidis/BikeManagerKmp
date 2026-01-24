package com.bikemanager.di

import org.koin.core.context.startKoin

/**
 * Helper object for initializing Koin from Swift.
 */
object KoinHelper {
    /**
     * Initializes Koin with the shared and iOS modules.
     * This should be called once from Swift on app startup.
     */
    fun initKoin() {
        startKoin {
            modules(sharedModule, iosModule)
        }
    }
}

/**
 * Helper function for Swift interoperability.
 */
fun initKoin() {
    KoinHelper.initKoin()
}
