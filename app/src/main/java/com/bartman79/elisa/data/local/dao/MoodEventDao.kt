package com.bartman79.elisa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import com.bartman79.elisa.data.local.entity.MoodEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodEventDao {

    @Insert
    suspend fun insert(event: MoodEvent)

    @Delete
    suspend fun delete(event: MoodEvent)

    @Query("SELECT * FROM mood_events WHERE date BETWEEN :startOfDay AND :endOfDay ORDER BY date DESC")
    fun getEventsForDay(startOfDay: Long, endOfDay: Long): Flow<List<MoodEvent>>

    @Query("SELECT * FROM mood_events ORDER BY date DESC")
    fun getAllEvents(): Flow<List<MoodEvent>>

    // Для подсчёта баланса за день:
    @Query("SELECT COUNT(*) FROM mood_events WHERE type = 1 AND date BETWEEN :startOfDay AND :endOfDay")
    suspend fun countPositiveForDay(startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT COUNT(*) FROM mood_events WHERE type = 0 AND date BETWEEN :startOfDay AND :endOfDay")
    suspend fun countNegativeForDay(startOfDay: Long, endOfDay: Long): Int
}