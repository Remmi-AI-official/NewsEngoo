package com.example.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.EditorialApplication
import com.example.data.repository.AppPreferences
import com.example.data.repository.OverallLearningStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val preferences: AppPreferences = AppPreferences(),
    val overallStats: OverallLearningStats? = null,
    val isLoading: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as EditorialApplication
    private val prefsRepo = app.userPreferencesRepository
    private val progressRepo = app.progressRepository

    val uiState: StateFlow<SettingsUiState> = combine(
        prefsRepo.preferencesFlow,
        progressRepo.getOverallStats()
    ) { prefs, stats ->
        SettingsUiState(
            preferences = prefs,
            overallStats = stats,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState(isLoading = true))

    fun setThemeMode(mode: String) {
        viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    }

    fun setFontSize(size: Float) {
        viewModelScope.launch { prefsRepo.setReaderFontSize(size) }
    }

    fun setTestTimer(minutes: Int) {
        viewModelScope.launch { prefsRepo.setTestTimerDuration(minutes) }
    }

    fun resetProgress() {
        viewModelScope.launch {
            // Clears history
            app.contentImportExportRepository.clearHistory()
        }
    }
}
