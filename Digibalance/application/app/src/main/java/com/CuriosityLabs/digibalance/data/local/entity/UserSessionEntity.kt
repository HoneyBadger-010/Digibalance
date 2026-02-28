package com.CuriosityLabs.digibalance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey
    val id: Int = 1, // Always 1 - single session
    val isLoggedIn: Boolean = false,
    val userId: String? = null,
    val userRole: String? = null,
    val gamertag: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val hasCompletedOnboarding: Boolean = false,
    val hasCompletedSurvey: Boolean = false,
    val hasSelectedRole: Boolean = false,
    val hasCreatedGamertag: Boolean = false,
    val hasGrantedPermissions: Boolean = false,
    val lastScreen: String = "home",
    val sessionTimestamp: Long = System.currentTimeMillis()
)
