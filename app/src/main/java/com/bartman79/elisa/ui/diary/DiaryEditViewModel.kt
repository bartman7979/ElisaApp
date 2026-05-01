package com.bartman79.elisa.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bartman79.elisa.data.local.entity.DiaryEntry
import com.bartman79.elisa.data.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DiaryEditViewModel(
    private val repository: DiaryRepository
) : ViewModel() {

    private val _photoPaths = MutableStateFlow<List<String>>(emptyList())
    val photoPaths: StateFlow<List<String>> = _photoPaths

    fun setPhotoPaths(paths: List<String>) {
        _photoPaths.value = paths
    }

    suspend fun saveEntry(content: String): Long {
        val entry = DiaryEntry(content = content)
        val entryId = repository.insertEntry(entry)
        _photoPaths.value.forEach { path ->
            repository.addPhoto(entryId, path)
        }
        return entryId
    }

    suspend fun updateEntry(entryId: Long, content: String) {
        val entry = DiaryEntry(id = entryId, content = content)
        repository.updateEntry(entry)
        repository.deletePhotosForEntry(entryId)
        _photoPaths.value.forEach { path ->
            repository.addPhoto(entryId, path)
        }
    }

    class Factory(private val repository: DiaryRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DiaryEditViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DiaryEditViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}