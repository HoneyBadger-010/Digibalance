package com.CuriosityLabs.digibalance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parental_rules")
data class ParentalRuleEntity(
    @PrimaryKey val id: Long,
    val studentId: String,
    val ruleType: String, // TOTAL_TIME, APP_TIME, PERSONAL_APPS, FOCUS_MODE
    val config: String, // JSON string
    val emergencyCode: String? = null,
    val lastSyncedAt: Long = System.currentTimeMillis()
)
