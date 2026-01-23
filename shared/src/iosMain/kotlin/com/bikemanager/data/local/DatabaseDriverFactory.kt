package com.bikemanager.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

/**
 * iOS implementation of DatabaseDriverFactory.
 * Uses NativeSqliteDriver to create the database driver.
 */
actual class DatabaseDriverFactory {
    /**
     * Creates a native SQLite driver for the BikeManager database on iOS.
     */
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(BikeManagerDatabase.Schema, "bikemanager.db")
    }
}
