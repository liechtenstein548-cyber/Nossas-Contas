package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#2563EB",
    val docId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
