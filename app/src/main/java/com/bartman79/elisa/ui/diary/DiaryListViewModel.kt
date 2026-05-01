package com.bartman79.elisa.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bartman79.elisa.data.local.entity.DiaryEntry
import com.bartman79.elisa.data.repository.DiaryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DiaryListViewModel(
    private val repository: DiaryRepository
) : ViewModel() {

    val entries: StateFlow<List<DiaryEntry>> = repository.getAllEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    class Factory(private val repository: DiaryRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DiaryListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DiaryListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}