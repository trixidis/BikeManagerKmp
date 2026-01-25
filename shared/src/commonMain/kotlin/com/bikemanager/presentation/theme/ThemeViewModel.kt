package com.bikemanager.presentation.theme

import com.bikemanager.domain.model.ThemeMode
import com.bikemanager.domain.usecase.theme.GetThemeModeUseCase
import com.bikemanager.domain.usecase.theme.SetThemeModeUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for managing theme preferences.
 * Observes theme mode changes and provides methods to update theme preference.
 */
class ThemeViewModel(
    private val getThemeModeUseCase: GetThemeModeUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow<ThemeUiState>(ThemeUiState.Loading)
    val uiState: StateFlow<ThemeUiState> = _uiState.asStateFlow()

    init {
        observeThemeMode()
    }

    /**
     * Observes theme mode from preferences (real-time updates).
     */
    private fun observeThemeMode() {
        viewModelScope.launch {
            getThemeModeUseCase()
                .catch { throwable ->
                    Napier.e(throwable) { "Error observing theme mode" }
                    _uiState.value = ThemeUiState.Error(
                        throwable.message ?: "Unknown error"
                    )
                }
                .collect { themeMode ->
                    _uiState.value = ThemeUiState.Success(themeMode)
                }
        }
    }

    /**
     * Sets the theme mode preference.
     */
    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            try {
                setThemeModeUseCase(themeMode)
            } catch (e: Exception) {
                Napier.e(e) { "Error setting theme mode" }
                _uiState.value = ThemeUiState.Error(e.message ?: "Error setting theme mode")
            }
        }
    }
}
