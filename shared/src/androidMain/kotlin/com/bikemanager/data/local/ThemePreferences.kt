package com.bikemanager.data.local

import android.content.Context
import com.bikemanager.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android implementation of ThemePreferences.
 * Uses SharedPreferences to persist theme settings.
 */
actual class ThemePreferences(private val context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val themeModeFlow = MutableStateFlow(loadThemeMode())

    /**
     * Gets the current theme mode as a Flow for reactive updates.
     */
    actual fun getThemeMode(): Flow<ThemeMode> = themeModeFlow.asStateFlow()

    /**
     * Sets the theme mode preference.
     */
    actual suspend fun setThemeMode(mode: ThemeMode) {
        preferences.edit()
            .putString(KEY_THEME_MODE, mode.name)
            .apply()
        themeModeFlow.value = mode
    }

    private fun loadThemeMode(): ThemeMode {
        val savedMode = preferences.getString(KEY_THEME_MODE, null)
        return savedMode?.let {
            try {
                ThemeMode.valueOf(it)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        } ?: ThemeMode.SYSTEM
    }

    companion object {
        private const val PREFS_NAME = "theme_preferences"
        private const val KEY_THEME_MODE = "theme_mode"
    }
}
