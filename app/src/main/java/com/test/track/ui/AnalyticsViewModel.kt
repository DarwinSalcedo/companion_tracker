package com.test.track.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.test.track.TrackerApplication
import com.test.track.data.AnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.core.content.edit

class AnalyticsViewModel(
    private val repository: AnalyticsRepository,
    private val prefs: SharedPreferences
) : ViewModel() {
    
    val events = repository.events
    
    private val _isBubbleEnabled = MutableStateFlow(prefs.getBoolean("bubble_enabled", true))
    val isBubbleEnabled: StateFlow<Boolean> = _isBubbleEnabled.asStateFlow()

    private val _isHelpCardVisible = MutableStateFlow(prefs.getBoolean("help_card_visible", true))
    val isHelpCardVisible: StateFlow<Boolean> = _isHelpCardVisible.asStateFlow()

    fun toggleBubble(enabled: Boolean) {
        prefs.edit { putBoolean("bubble_enabled", enabled) }
        _isBubbleEnabled.value = enabled
    }

    fun hideHelpCard() {
        prefs.edit { putBoolean("help_card_visible", false) }
        _isHelpCardVisible.value = false
    }

    fun clearEvents() {
        viewModelScope.launch {
            repository.clearEvents()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as TrackerApplication
                val prefs = application.getSharedPreferences("CompanionPrefs", Context.MODE_PRIVATE)
                return AnalyticsViewModel(application.repository, prefs) as T
            }
        }
    }
}
