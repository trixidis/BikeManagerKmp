package com.bikemanager.data.repository

import com.bikemanager.data.local.ThemePreferences
import com.bikemanager.domain.model.ThemeMode
import com.bikemanager.domain.repository.ThemeRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of ThemeRepository.
 * Uses platform-specific preferences storage (SharedPreferences on Android, UserDefaults on iOS).
 */
class ThemeRepositoryImpl(
    private val themePreferences: ThemePreferences
) : ThemeRepository {

    override fun getThemePreference(): Flow<ThemeMode> {
        return themePreferences.getThemeMode()
    }

    override suspend fun saveThemePreference(theme: ThemeMode) {
        themePreferences.setThemeMode(theme)
        Napier.d { "Theme preference saved: $theme" }
    }
}
