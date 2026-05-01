package com.bartman79.elisa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_events")
data class MoodEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: Int, // 0 - отрицательное, 1 - положительное
    val date: Long = System.currentTimeMillis(),
    val tag: String? = null // например "кофе", "автобус"
)