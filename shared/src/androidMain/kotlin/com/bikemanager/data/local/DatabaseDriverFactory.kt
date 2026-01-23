package com.bikemanager.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Android implementation of DatabaseDriverFactory.
 * Uses AndroidSqliteDriver to create the database driver.
 */
actual class DatabaseDriverFactory(private val context: Context) {
    /**
     * Creates an Android SQLite driver for the BikeManager database.
     */
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(BikeManagerDatabase.Schema, context, "bikemanager.db")
    }
}
