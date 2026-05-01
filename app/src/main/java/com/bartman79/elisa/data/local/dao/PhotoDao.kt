package com.bartman79.elisa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import com.bartman79.elisa.data.local.entity.Photo
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert
    suspend fun insert(photo: Photo)

    @Delete
    suspend fun delete(photo: Photo)

    @Query("SELECT * FROM photos WHERE entryId = :entryId ORDER BY dateAdded DESC")
    fun getPhotosForEntry(entryId: Long): Flow<List<Photo>>

    @Query("DELETE FROM photos WHERE entryId = :entryId")
    suspend fun deleteByEntryId(entryId: Long)
}