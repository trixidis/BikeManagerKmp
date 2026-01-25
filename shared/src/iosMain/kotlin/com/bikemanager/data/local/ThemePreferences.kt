package com.bikemanager.data.local

import com.bikemanager.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of ThemePreferences.
 * Uses NSUserDefaults to persist theme settings.
 */
actual class ThemePreferences {

    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val themeModeFlow = MutableStateFlow(loadThemeMode())

    /**
     * Gets the current theme mode as a Flow for reactive updates.
     */
    actual fun getThemeMode(): Flow<ThemeMode> = themeModeFlow.asStateFlow()

    /**
     * Sets the theme mode preference.
     */
    actual suspend fun setThemeMode(mode: ThemeMode) {
        userDefaults.setObject(mode.name, KEY_THEME_MODE)
        userDefaults.synchronize()
        themeModeFlow.value = mode
    }

    private fun loadThemeMode(): ThemeMode {
        val savedMode = userDefaults.stringForKey(KEY_THEME_MODE)
        return savedMode?.let {
            try {
                ThemeMode.valueOf(it)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        } ?: ThemeMode.SYSTEM
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
