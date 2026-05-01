package com.bartman79.elisa.ui.calendar
data class DayItem(
    val date: Long,
    val dayOfMonth: Int,
    val isMenstruation: Boolean = false,
    val isPredicted: Boolean = false,
    val isOvulation: Boolean = false,
    val isFertile: Boolean = false,
    val isCurrentMonth: Boolean = true,
    val isToday: Boolean = false
)