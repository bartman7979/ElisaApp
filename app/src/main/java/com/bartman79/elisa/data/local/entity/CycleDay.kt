package com.bartman79.elisa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "cycle_days")
data class CycleDay(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long, // дата в миллисекундах (начало дня)
    val type: Int,  // 1 = менструация, 2 = овуляция, 3 = симптом, и т.д.
    val note: String? = null, // заметка (например, "головная боль")
    val intensity: Int? = null // для менструации: 1-3 (лёгкая, средняя, обильная)
)