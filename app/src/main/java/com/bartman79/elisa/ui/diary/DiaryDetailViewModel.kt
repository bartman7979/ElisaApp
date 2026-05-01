package com.bartman79.elisa.ui.diary

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bartman79.elisa.data.local.entity.DiaryEntry
import com.bartman79.elisa.data.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DiaryDetailViewModel(
    private val repository: DiaryRepository,
    private val entryId: Long
) : ViewModel() {

    private val _entry = MutableStateFlow<DiaryEntry?>(null)
    val entry: StateFlow<DiaryEntry?> = _entry

    init {
        Log.d("DiaryDetailVM", "init, entryId = $entryId")
        loadEntry()
    }

    private fun loadEntry() {
        viewModelScope.launch {
            Log.d("DiaryDetailVM", "loading entry $entryId")
            val entry = repository.getEntryById(entryId)
            Log.d("DiaryDetailVM", "entry loaded: $entry")
            _entry.value = entry
            Log.d("DiaryDetailVM", "_entry set to $entry")
        }
    }

    suspend fun deleteEntry() {
        _entry.value?.let { repository.deleteEntry(it) }
    }

    class Factory(
        private val repository: DiaryRepository,
        private val entryId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DiaryDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DiaryDetailViewModel(repository, entryId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}