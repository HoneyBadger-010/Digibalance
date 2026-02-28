package com.CuriosityLabs.digibalance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feedback_queue")
data class FeedbackQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val category: String,
    val message: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
