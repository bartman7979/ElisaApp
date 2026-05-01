package com.bartman79.elisa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "photos",
    foreignKeys = [ForeignKey(
        entity = DiaryEntry::class,
        parentColumns = ["id"],
        childColumns = ["entryId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Photo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entryId: Long, // внешний ключ к DiaryEntry
    val filePath: String, // путь к файлу изображения
    val dateAdded: Long = System.currentTimeMillis()
)