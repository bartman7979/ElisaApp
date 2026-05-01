package com.bartman79.elisa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.bartman79.elisa.data.local.entity.CycleDay
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDayDao {

    @Insert
    suspend fun insert(cycleDay: CycleDay)

    @Update
    suspend fun update(cycleDay: CycleDay)

    @Delete
    suspend fun delete(cycleDay: CycleDay)

    // Получить все дни за определённый период
    @Query("SELECT * FROM cycle_days WHERE date BETWEEN :start AND :end ORDER BY date")
    suspend fun getDaysBetween(start: Long, end: Long): List<CycleDay>

    // Получить все дни менструации (type = 1) для анализа циклов
    @Query("SELECT * FROM cycle_days WHERE type = 1 ORDER BY date")
    suspend fun getAllMenstruationDays(): List<CycleDay>

    // Получить конкретный день по дате
    @Query("SELECT * FROM cycle_days WHERE date = :date LIMIT 1")
    suspend fun getDay(date: Long): CycleDay?
}