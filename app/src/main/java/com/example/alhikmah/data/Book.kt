package com.example.alhikmah.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val author: String = "Unknown",
    val filePath: String,
    val fileType: String, // "PDF OR EPUB"
    val coverPath: String? = null,
    val totalPages: Int = 0,
    val currentPage: Int = 0,
    val lastReadTime: Long = System.currentTimeMillis(),
    val addedTime: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,

    val syncedWithServer: Boolean = false,
    val serverId: String? = null
)