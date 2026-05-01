package com.bartman79.elisa.ui.main

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bartman79.elisa.MoodWidgetProvider
import com.bartman79.elisa.data.repository.MoodRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(private val moodRepository: MoodRepository) : ViewModel() {
    val balance: StateFlow<Int> = moodRepository.getTodayBalanceFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    class Factory(private val moodRepository: MoodRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(moodRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}