package com.example.alhikmah.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_progress")
data class ReadingProgress(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val bookId: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val pageNumber: Int,
    val percentage: Float,
    val durationSeconds: Int = 0 //duration at session
)
