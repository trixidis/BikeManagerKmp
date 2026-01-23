package com.bikemanager.data.local

import app.cash.sqldelight.db.SqlDriver

/**
 * Factory interface for creating SQLite database drivers.
 * Platform-specific implementations are provided in androidMain and iosMain.
 */
expect class DatabaseDriverFactory {
    /**
     * Creates a SqlDriver instance for the BikeManager database.
     */
    fun createDriver(): SqlDriver
}
