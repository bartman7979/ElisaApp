package com.bartman79.elisa.data.repository

import android.util.Log
import com.bartman79.elisa.data.local.dao.CycleDayDao
import com.bartman79.elisa.data.local.entity.CycleDay
import java.util.Calendar
import java.util.concurrent.TimeUnit

class CycleRepository(private val cycleDayDao: CycleDayDao) {

    private val DAY_IN_MS = TimeUnit.DAYS.toMillis(1)

    // Медицинские константы
    private val MIN_CYCLE_DAYS = 21 // Цикл короче 21 дня обычно считается ановуляторным
    private val MAX_CYCLE_DAYS = 45 // Цикл длиннее 45 дней часто указывает на сбой
    private val LUTEAL_PHASE = 14   // Стандартная длина второй фазы
    private val PERIOD_DURATION = 5  // Средняя длительность менструации для прогноза

    suspend fun markMenstruationDay(date: Long) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0);
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val cleanDate = cal.timeInMillis
        val existing = cycleDayDao.getDay(cleanDate)
        if (existing == null) {
            cycleDayDao.insert(CycleDay(date = cleanDate, type = 1))
        } else {
            cycleDayDao.delete(existing)
        }
    }

    suspend fun getDaysForMonth(start: Long, end: Long): List<CycleDay> {
        return cycleDayDao.getDaysBetween(start, end)
    }

    data class PredictionData(
        val menstruation: Set<Long> = emptySet(),
        val ovulation: Set<Long> = emptySet(),
        val fertile: Set<Long> = emptySet()
    )

    /**
     * Определяет даты начала циклов.
     * Теперь порог — 16 дней, чтобы не путать продолжение месячных с новым циклом.
     */
    private suspend fun getCycleStarts(): List<Long> {
        val allDays = cycleDayDao.getAllMenstruationDays().sortedBy { it.date }
        if (allDays.isEmpty()) return emptyList()

        val starts = mutableListOf<Long>()
        var lastTracked: Long = -1

        for (day in allDays) {
            // Если разрыв между отмеченными днями > 16 дней — это новый цикл
            if (lastTracked == -1L || day.date - lastTracked > 16 * DAY_IN_MS) {
                starts.add(day.date)
            }
            lastTracked = day.date
        }
        return starts
    }

    /**
     * Расчет среднего цикла с фильтрацией аномалий.
     */
    private fun calculateAvgCycleLength(starts: List<Long>): Long {
        if (starts.size < 2) return 28 * DAY_IN_MS

        val lengths = mutableListOf<Long>()
        for (i in 1 until starts.size) {
            val diffDays = (starts[i] - starts[i - 1]) / DAY_IN_MS
            // Учитываем только физиологически нормальные циклы (21-45 дней)
            if (diffDays in MIN_CYCLE_DAYS..MAX_CYCLE_DAYS) {
                lengths.add(diffDays)
            }
        }

        val avgDays = if (lengths.isEmpty()) 28 else lengths.average().toLong()
        return avgDays * DAY_IN_MS
    }

    suspend fun predictNextPeriod(): Long? {
        val starts = getCycleStarts()
        if (starts.isEmpty()) return null
        val avg = calculateAvgCycleLength(starts)
        return starts.last() + avg
    }

    suspend fun calculatePredictions(viewStart: Long, viewEnd: Long): PredictionData {
        val starts = getCycleStarts()
        if (starts.isEmpty()) return PredictionData()

        val avgCycle = calculateAvgCycleLength(starts)
        val predMenstruation = mutableSetOf<Long>()
        val predOvulation = mutableSetOf<Long>()
        val predFertile = mutableSetOf<Long>()

        var nextCycleStart = starts.last()

        // Прогнозируем на 6 месяцев вперед (оптимально для баланса точности и удобства)
        repeat(6) {
            nextCycleStart += avgCycle

            // Приводим к 00:00
            val cal = Calendar.getInstance().apply {
                timeInMillis = nextCycleStart
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val currentStart = cal.timeInMillis

            // 1. Прогноз менструации
            for (i in 0 until PERIOD_DURATION) {
                predMenstruation.add(currentStart + (i * DAY_IN_MS))
            }

            // 2. Овуляция (за 14 дней до следующего цикла)
            val ovulationDay = currentStart - (LUTEAL_PHASE * DAY_IN_MS)
            predOvulation.add(ovulationDay)

            // 3. Фертильное окно (5 дней до овуляции + день овуляции + 1 день после)
            // Итого 7 дней — стандарт в медицине для "опасных/благоприятных" дней
            for (i in -5..1) {
                predFertile.add(ovulationDay + (i * DAY_IN_MS))
            }
        }

        return PredictionData(predMenstruation, predOvulation, predFertile)
    }
}