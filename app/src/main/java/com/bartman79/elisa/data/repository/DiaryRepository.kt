package com.bartman79.elisa.data.repository

import android.util.Log
import com.bartman79.elisa.data.local.dao.DiaryEntryDao
import com.bartman79.elisa.data.local.dao.PhotoDao
import com.bartman79.elisa.data.local.entity.DiaryEntry
import com.bartman79.elisa.data.local.entity.Photo
import kotlinx.coroutines.flow.Flow

class DiaryRepository(
    private val diaryEntryDao: DiaryEntryDao,
    private val photoDao: PhotoDao
) {
    // DiaryEntry operations
    fun getAllEntries(): Flow<List<DiaryEntry>> = diaryEntryDao.getAllEntries()

    suspend fun getEntryById(id: Long): DiaryEntry? {
        Log.d("DiaryRepo", "getEntryById: $id")
        return try {
            val result = diaryEntryDao.getEntryById(id)
            Log.d("DiaryRepo", "getEntryById result: $result")
            result
        } catch (e: Exception) {
            Log.e("DiaryRepo", "Error getting entry", e)
            null
        }
    }


    suspend fun insertEntry(entry: DiaryEntry): Long = diaryEntryDao.insert(entry)

    suspend fun addPhoto(entryId: Long, path: String) {
        photoDao.insert(Photo(entryId = entryId, filePath = path))
    }

        suspend fun updateEntry(entry: DiaryEntry) {
        diaryEntryDao.update(entry)
    }

    suspend fun deleteEntry(entry: DiaryEntry) {
        diaryEntryDao.delete(entry)
    }

    // Photo operations
    fun getPhotosForEntry(entryId: Long): Flow<List<Photo>> = photoDao.getPhotosForEntry(entryId)

    suspend fun insertPhoto(photo: Photo) {
        photoDao.insert(photo)
    }

    suspend fun deletePhoto(photo: Photo) {
        photoDao.delete(photo)
    }

    suspend fun deletePhotosForEntry(entryId: Long) {
        photoDao.deleteByEntryId(entryId)
    }
}