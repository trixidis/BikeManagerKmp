package com.bikemanager.domain.usecase.theme

import com.bikemanager.domain.model.ThemeMode
import com.bikemanager.domain.repository.ThemeRepository

/**
 * Use case for setting the theme mode preference.
 */
class SetThemeModeUseCase(
    private val repository: ThemeRepository
) {
    /**
     * Sets the theme mode preference.
     */
    suspend operator fun invoke(themeMode: ThemeMode) {
        repository.saveThemePreference(themeMode)
    }
}
