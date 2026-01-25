package com.bikemanager.presentation.theme

import com.bikemanager.domain.model.ThemeMode

/**
 * UI state for theme management.
 * Represents the current theme preference state.
 */
sealed class ThemeUiState {
    /**
     * Loading state while fetching theme preference.
     */
    data object Loading : ThemeUiState()

    /**
     * Success state with current theme mode.
     */
    data class Success(val themeMode: ThemeMode) : ThemeUiState()

    /**
     * Error state with error message.
     */
    data class Error(val message: String) : ThemeUiState()
}
