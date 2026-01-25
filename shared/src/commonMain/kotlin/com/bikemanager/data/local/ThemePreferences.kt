package com.bikemanager.data.local

import com.bikemanager.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Preference storage for theme settings.
 * Platform-specific implementations are provided in androidMain (SharedPreferences)
 * and iosMain (UserDefaults).
 */
expect class ThemePreferences {
    /**
     * Gets the current theme mode as a Flow for reactive updates.
     */
    fun getThemeMode(): Flow<ThemeMode>

    /**
     * Sets the theme mode preference.
     */
    suspend fun setThemeMode(mode: ThemeMode)
}
