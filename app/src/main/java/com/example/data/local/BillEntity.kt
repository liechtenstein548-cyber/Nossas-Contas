package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bills",
    indices = [Index(value = ["monthYear"])]
)
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val dueDate: String, // ISO format: yyyy-MM-dd
    val category: String,
    val isPaid: Boolean = false,
    val monthYear: String, // format: yyyy-MM
    val profileName: String = "Conjunto", // Assigned profile
    val isRecurring: Boolean = false, // Recurring bill flag
    val docId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
