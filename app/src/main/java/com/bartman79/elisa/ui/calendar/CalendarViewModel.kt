package com.bartman79.elisa.ui.calendar

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bartman79.elisa.R
import com.bartman79.elisa.data.repository.CycleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CalendarViewModel(
    private val context: Context, // добавлен для локализации
    private val cycleRepository: CycleRepository
) : ViewModel() {

    private val _prediction = MutableStateFlow(context.getString(R.string.calendar_loading))
    val prediction: StateFlow<String> = _prediction

    private val _monthTitle = MutableStateFlow("")
    val monthTitle: StateFlow<String> = _monthTitle

    private val baseCalendar = Calendar.getInstance()

    private val _daysState = MutableStateFlow<Map<Int, List<DayItem>>>(emptyMap())
    val daysState: StateFlow<Map<Int, List<DayItem>>> = _daysState

    init {
        loadPrediction()
        updateMonthTitle(0)
    }

    fun loadDaysForMonth(offset: Int) {
        viewModelScope.launch {
            Log.d("CalendarVM", "loadDaysForMonth: offset=$offset")
            val cal = (baseCalendar.clone() as Calendar).apply { add(Calendar.MONTH, offset) }
            val start = getStartOfMonth(cal).timeInMillis
            val end = getEndOfMonth(cal).timeInMillis
            Log.d("CalendarVM", "Month start=$start, end=$end")

            val markedDates = cycleRepository.getDaysForMonth(start, end).map { it.date }
            Log.d("CalendarVM", "markedDates: $markedDates")

            val predictions = cycleRepository.calculatePredictions(start, end)

            val days = generateDaysForMonth(cal, markedDates, predictions)
            _daysState.value = _daysState.value + (offset to days)
            Log.d("CalendarVM", "days generated for offset $offset, count=${days.size}")
        }
    }

    fun getDaysForMonthSync(offset: Int, onResult: (List<DayItem>) -> Unit) {
        viewModelScope.launch {
            val cal = (baseCalendar.clone() as Calendar).apply { add(Calendar.MONTH, offset) }
            val start = getStartOfMonth(cal).timeInMillis
            val end = getEndOfMonth(cal).timeInMillis

            val markedDates = cycleRepository.getDaysForMonth(start, end).map { it.date }
            val predictions = cycleRepository.calculatePredictions(start, end)

            val days = generateDaysForMonth(cal, markedDates, predictions)
            onResult(days)
        }
    }

    fun updateMonthTitle(offset: Int) {
        val cal = (baseCalendar.clone() as Calendar).apply { add(Calendar.MONTH, offset) }
        val format = SimpleDateFormat("LLLL yyyy", Locale.getDefault())
        _monthTitle.value = format.format(cal.time).replaceFirstChar { it.uppercase() }
    }

    private fun loadPrediction() {
        viewModelScope.launch {
            val nextPeriod = cycleRepository.predictNextPeriod()
            _prediction.value = nextPeriod?.let {
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                context.getString(R.string.calendar_prediction_text, dateFormat.format(it))
            } ?: context.getString(R.string.calendar_no_data)
        }
    }

    fun toggleDay(date: Long, offset: Int) {
        viewModelScope.launch {
            cycleRepository.markMenstruationDay(date)
            loadPrediction()
            loadDaysForMonth(offset)
        }
    }

    private fun generateDaysForMonth(
        calendar: Calendar,
        markedDates: List<Long>,
        predictions: CycleRepository.PredictionData
    ): List<DayItem> {
        val days = mutableListOf<DayItem>()
        val clone = (calendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val firstDayOfMonth = clone.get(Calendar.DAY_OF_WEEK)
        val startOffset = if (firstDayOfMonth == Calendar.SUNDAY) 6 else firstDayOfMonth - 2
        clone.add(Calendar.DAY_OF_MONTH, -startOffset)

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        for (i in 0 until 42) {
            val date = clone.timeInMillis
            val isMarked = markedDates.any { Math.abs(it - date) < 3600000 }
            val isPredicted = predictions.menstruation.contains(date)
            val isOvulation = predictions.ovulation.contains(date)
            val isFertile = predictions.fertile.contains(date)

            days.add(DayItem(
                date = date,
                dayOfMonth = clone.get(Calendar.DAY_OF_MONTH),
                isMenstruation = isMarked,
                isPredicted = isPredicted,
                isOvulation = isOvulation,
                isFertile = isFertile,
                isCurrentMonth = clone.get(Calendar.MONTH) == calendar.get(Calendar.MONTH),
                isToday = Math.abs(date - today) < 3600000
            ))
            clone.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }

    private fun getStartOfMonth(calendar: Calendar): Calendar {
        return (calendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun getEndOfMonth(calendar: Calendar): Calendar {
        return (calendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
    }

    class Factory(
        private val context: Context,
        private val cycleRepository: CycleRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CalendarViewModel(context, cycleRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}