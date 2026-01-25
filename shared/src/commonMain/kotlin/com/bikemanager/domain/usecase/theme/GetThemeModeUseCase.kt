package com.bikemanager.domain.usecase.theme

import com.bikemanager.domain.model.ThemeMode
import com.bikemanager.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting the current theme mode preference.
 */
class GetThemeModeUseCase(private val repository: ThemeRepository) {
    /**
     * Gets the current theme mode as a Flow.
     */
    operator fun invoke(): Flow<ThemeMode> {
        return repository.getThemePreference()
    }
}
