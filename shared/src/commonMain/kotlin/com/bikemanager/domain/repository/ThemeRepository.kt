package com.bikemanager.domain.repository

import com.bikemanager.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for theme preference operations.
 * Handles persisting and retrieving user theme preferences.
 */
interface ThemeRepository {
    /**
     * Gets the current theme preference as a Flow (real-time updates).
     */
    fun getThemePreference(): Flow<ThemeMode>

    /**
     * Saves the theme preference.
     * @param theme The theme preference (LIGHT, DARK, or SYSTEM)
     */
    suspend fun saveThemePreference(theme: ThemeMode)
}
