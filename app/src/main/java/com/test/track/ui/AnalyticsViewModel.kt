package com.test.track.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.test.track.TrackerApplication
import com.test.track.data.AnalyticsRepository
import kotlinx.coroutines.launch

class AnalyticsViewModel(private val repository: AnalyticsRepository) : ViewModel() {
    val events = repository.events

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
                return AnalyticsViewModel(application.repository) as T
            }
        }
    }
}
