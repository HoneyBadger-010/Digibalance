package com.CuriosityLabs.digibalance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val usageTimeMillis: Long,
    val date: String, // YYYY-MM-DD format
    val isProductive: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
