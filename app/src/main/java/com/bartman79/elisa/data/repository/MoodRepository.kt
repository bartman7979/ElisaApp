package com.bartman79.elisa.data.repository

import com.bartman79.elisa.data.local.dao.MoodEventDao
import com.bartman79.elisa.data.local.entity.MoodEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import java.util.Calendar

class MoodRepository(private val moodEventDao: MoodEventDao) {

    suspend fun addPositiveEvent(tag: String? = null) {
        moodEventDao.insert(MoodEvent(type = 1, tag = tag))
    }

    suspend fun addNegativeEvent(tag: String? = null) {
        moodEventDao.insert(MoodEvent(type = 0, tag = tag))
    }

    fun getTodayEvents(startOfDay: Long, endOfDay: Long): Flow<List<MoodEvent>> =
        moodEventDao.getEventsForDay(startOfDay, endOfDay)

    suspend fun getTodayBalance(startOfDay: Long, endOfDay: Long): Int {
        val positive = moodEventDao.countPositiveForDay(startOfDay, endOfDay)
        val negative = moodEventDao.countNegativeForDay(startOfDay, endOfDay)
        return (positive - negative) * 10
    }

    fun getTodayBalanceFlow(): Flow<Int> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return moodEventDao.getEventsForDay(startOfDay, endOfDay)
            .map { events ->
                (events.count { it.type == 1 } - events.count { it.type == 0 }) * 10
            }
            .flowOn(Dispatchers.IO)
    }
}