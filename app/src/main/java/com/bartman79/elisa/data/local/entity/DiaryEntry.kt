package com.bartman79.elisa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long = System.currentTimeMillis(), // временная метка
    val content: String,
    val moodScore: Int? = null // опционально: настроение в момент записи (можем привязать к счетчику)
)