package com.CuriosityLabs.digibalance.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("digibalance_prefs", Context.MODE_PRIVATE)
    
    // Theme settings
    var themeMode: String
        get() = prefs.getString("theme_mode", "System") ?: "System"
        set(value) = prefs.edit().putString("theme_mode", value).apply()
    
    var accentColor: String
        get() = prefs.getString("accent_color", "Blue") ?: "Blue"
        set(value) = prefs.edit().putString("accent_color", value).apply()
    
    var chartType: String
        get() = prefs.getString("chart_type", "Pie") ?: "Pie"
        set(value) = prefs.edit().putString("chart_type", value).apply()
    
    // Display settings
    var showStatistics: Boolean
        get() = prefs.getBoolean("show_statistics", true)
        set(value) = prefs.edit().putBoolean("show_statistics", value).apply()
    
    var compactView: Boolean
        get() = prefs.getBoolean("compact_view", false)
        set(value) = prefs.edit().putBoolean("compact_view", value).apply()
    
    var showAnimations: Boolean
        get() = prefs.getBoolean("show_animations", true)
        set(value) = prefs.edit().putBoolean("show_animations", value).apply()
    
    // Notification settings
    var notificationsEnabled: Boolean
        get() = prefs.getBoolean("notifications_enabled", true)
        set(value) = prefs.edit().putBoolean("notifications_enabled", value).apply()
    
    var distractionAlertsEnabled: Boolean
        get() = prefs.getBoolean("distraction_alerts_enabled", true)
        set(value) = prefs.edit().putBoolean("distraction_alerts_enabled", value).apply()
    
    var focusRemindersEnabled: Boolean
        get() = prefs.getBoolean("focus_reminders_enabled", true)
        set(value) = prefs.edit().putBoolean("focus_reminders_enabled", value).apply()
    
    var leaderboardUpdatesEnabled: Boolean
        get() = prefs.getBoolean("leaderboard_updates_enabled", false)
        set(value) = prefs.edit().putBoolean("leaderboard_updates_enabled", value).apply()
    
    var parentNotificationsEnabled: Boolean
        get() = prefs.getBoolean("parent_notifications_enabled", true)
        set(value) = prefs.edit().putBoolean("parent_notifications_enabled", value).apply()
    
    // Distraction Alert Settings
    var distractionAlertTriggerTime: Int
        get() = prefs.getInt("distraction_alert_trigger_time", 5)
        set(value) = prefs.edit().putInt("distraction_alert_trigger_time", value).apply()
    
    var distractionAlertStyle: String
        get() = prefs.getString("distraction_alert_style", "OVERLAY") ?: "OVERLAY"
        set(value) = prefs.edit().putString("distraction_alert_style", value).apply()
    
    // Focus Mode Settings
    var defaultFocusDuration: Int
        get() = prefs.getInt("default_focus_duration", 60)
        set(value) = prefs.edit().putInt("default_focus_duration", value).apply()
    
    var focusEmergencyCode: String?
        get() = prefs.getString("focus_emergency_code", null)
        set(value) = prefs.edit().putString("focus_emergency_code", value).apply()
    
    // Sync settings
    var autoSync: Boolean
        get() = prefs.getBoolean("auto_sync", true)
        set(value) = prefs.edit().putBoolean("auto_sync", value).apply()
    
    var lastSyncTimestamp: Long
        get() = prefs.getLong("last_sync_timestamp", 0)
        set(value) = prefs.edit().putLong("last_sync_timestamp", value).apply()
    
    // Leaderboard Settings
    var showLeaderboardRank: Boolean
        get() = prefs.getBoolean("show_leaderboard_rank", true)
        set(value) = prefs.edit().putBoolean("show_leaderboard_rank", value).apply()
    
    // Gamertag
    var gamertagLastChanged: Long
        get() = prefs.getLong("gamertag_last_changed", 0)
        set(value) = prefs.edit().putLong("gamertag_last_changed", value).apply()
    
    // Developer Options
    var developerModeEnabled: Boolean
        get() = prefs.getBoolean("developer_mode_enabled", false)
        set(value) = prefs.edit().putBoolean("developer_mode_enabled", value).apply()
    
    var versionTapCount: Int
        get() = prefs.getInt("version_tap_count", 0)
        set(value) = prefs.edit().putInt("version_tap_count", value).apply()
    
    // Onboarding
    var hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean("has_completed_onboarding", false)
        set(value) = prefs.edit().putBoolean("has_completed_onboarding", value).apply()
    
    // User Session - Now synced with Room database for persistence
    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(value) {
            prefs.edit().putBoolean("is_logged_in", value).apply()
            // Also save to Room database for persistence
            syncToDatabase()
        }
    
    var currentUserId: String?
        get() = prefs.getString("current_user_id", null)
        set(value) {
            prefs.edit().putString("current_user_id", value).apply()
            syncToDatabase()
        }
    
    var currentUserRole: String?
        get() = prefs.getString("current_user_role", null)
        set(value) {
            prefs.edit().putString("current_user_role", value).apply()
            syncToDatabase()
        }
    
    var currentUserGamertag: String?
        get() = prefs.getString("current_user_gamertag", null)
        set(value) {
            prefs.edit().putString("current_user_gamertag", value).apply()
            syncToDatabase()
        }
    
    var currentUserEmail: String?
        get() = prefs.getString("current_user_email", null)
        set(value) {
            prefs.edit().putString("current_user_email", value).apply()
            syncToDatabase()
        }
    
    var currentUserPhone: String?
        get() = prefs.getString("current_user_phone", null)
        set(value) {
            prefs.edit().putString("current_user_phone", value).apply()
            syncToDatabase()
        }
    
    // Sync session to Room database immediately
    private fun syncToDatabase() {
        // Trigger immediate sync via WorkManager
        try {
            com.CuriosityLabs.digibalance.service.SessionSyncWorker.syncNow(context)
            android.util.Log.d("PreferencesManager", "Triggered database sync")
        } catch (e: Exception) {
            android.util.Log.e("PreferencesManager", "Failed to trigger sync", e)
        }
    }
    
    // Session State Tracking (TODO-1, TODO-2, TODO-3) - Synced with Room
    var lastScreen: String
        get() = prefs.getString("last_screen", "home") ?: "home"
        set(value) {
            prefs.edit().putString("last_screen", value).apply()
            syncToDatabase()
        }
    
    var onboardingCompleted: Boolean
        get() = prefs.getBoolean("onboarding_completed", false)
        set(value) {
            prefs.edit().putBoolean("onboarding_completed", value).apply()
            syncToDatabase()
        }
    
    var hasCompletedSurvey: Boolean
        get() = prefs.getBoolean("has_completed_survey", false)
        set(value) {
            prefs.edit().putBoolean("has_completed_survey", value).apply()
            syncToDatabase()
        }
    
    var hasSelectedRole: Boolean
        get() = prefs.getBoolean("has_selected_role", false)
        set(value) {
            prefs.edit().putBoolean("has_selected_role", value).apply()
            syncToDatabase()
        }
    
    var hasCreatedGamertag: Boolean
        get() = prefs.getBoolean("has_created_gamertag", false)
        set(value) {
            prefs.edit().putBoolean("has_created_gamertag", value).apply()
            syncToDatabase()
        }
    
    var hasGrantedPermissions: Boolean
        get() = prefs.getBoolean("has_granted_permissions", false)
        set(value) {
            prefs.edit().putBoolean("has_granted_permissions", value).apply()
            syncToDatabase()
        }
    
    var sessionTimestamp: Long
        get() = prefs.getLong("session_timestamp", 0)
        set(value) {
            prefs.edit().putLong("session_timestamp", value).apply()
            syncToDatabase()
        }
    
    // Restore session from Room database
    suspend fun restoreFromDatabase(context: Context) {
        try {
            val database = com.CuriosityLabs.digibalance.data.local.AppDatabase.getDatabase(context)
            val session = database.userSessionDao().getSession()
            
            if (session != null) {
                // Restore all session data from database
                prefs.edit().apply {
                    putBoolean("is_logged_in", session.isLoggedIn)
                    putString("current_user_id", session.userId)
                    putString("current_user_role", session.userRole)
                    putString("current_user_gamertag", session.gamertag)
                    putString("current_user_email", session.email)
                    putString("current_user_phone", session.phone)
                    putBoolean("onboarding_completed", session.hasCompletedOnboarding)
                    putBoolean("has_completed_survey", session.hasCompletedSurvey)
                    putBoolean("has_selected_role", session.hasSelectedRole)
                    putBoolean("has_created_gamertag", session.hasCreatedGamertag)
                    putBoolean("has_granted_permissions", session.hasGrantedPermissions)
                    putString("last_screen", session.lastScreen)
                    putLong("session_timestamp", session.sessionTimestamp)
                    apply()
                }
                android.util.Log.d("PreferencesManager", "Session restored from database")
            }
        } catch (e: Exception) {
            android.util.Log.e("PreferencesManager", "Failed to restore session from database", e)
        }
    }
    
    // Save current session to Room database
    suspend fun saveToDatabase(context: Context) {
        try {
            val database = com.CuriosityLabs.digibalance.data.local.AppDatabase.getDatabase(context)
            val session = com.CuriosityLabs.digibalance.data.local.entity.UserSessionEntity(
                id = 1,
                isLoggedIn = isLoggedIn,
                userId = currentUserId,
                userRole = currentUserRole,
                gamertag = currentUserGamertag,
                email = currentUserEmail,
                phone = currentUserPhone,
                hasCompletedOnboarding = onboardingCompleted,
                hasCompletedSurvey = hasCompletedSurvey,
                hasSelectedRole = hasSelectedRole,
                hasCreatedGamertag = hasCreatedGamertag,
                hasGrantedPermissions = hasGrantedPermissions,
                lastScreen = lastScreen,
                sessionTimestamp = sessionTimestamp
            )
            database.userSessionDao().saveSession(session)
            prefs.edit().putBoolean("needs_db_sync", false).apply()
            android.util.Log.d("PreferencesManager", "Session saved to database")
        } catch (e: Exception) {
            android.util.Log.e("PreferencesManager", "Failed to save session to database", e)
        }
    }
    
    // Focus Mode App Selection (TODO-16)
    var focusModeSelectedApps: Set<String>
        get() = prefs.getStringSet("focus_mode_selected_apps", emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet("focus_mode_selected_apps", value).apply()
    
    var parentLinkedStudentId: String?
        get() = prefs.getString("parent_linked_student_id", null)
        set(value) = prefs.edit().putString("parent_linked_student_id", value).apply()
    
    // Parent-Managed Settings (TODO-23)
    var parentManagedSettings: Boolean
        get() = prefs.getBoolean("parent_managed_settings", false)
        set(value) = prefs.edit().putBoolean("parent_managed_settings", value).apply()
    
    var parentScreenTimeLimit: Int
        get() = prefs.getInt("parent_screen_time_limit", 0)
        set(value) = prefs.edit().putInt("parent_screen_time_limit", value).apply()
    
    var parentFocusModeRequired: Boolean
        get() = prefs.getBoolean("parent_focus_mode_required", false)
        set(value) = prefs.edit().putBoolean("parent_focus_mode_required", value).apply()
    
    var parentBlockedApps: Set<String>
        get() = prefs.getStringSet("parent_blocked_apps", emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet("parent_blocked_apps", value).apply()
    
    var parentBedtimeHours: String?
        get() = prefs.getString("parent_bedtime_hours", null)
        set(value) = prefs.edit().putString("parent_bedtime_hours", value).apply()
    
    var parentProductiveAppsOnly: Boolean
        get() = prefs.getBoolean("parent_productive_apps_only", false)
        set(value) = prefs.edit().putBoolean("parent_productive_apps_only", value).apply()
    
    // Clear all preferences
    fun clearAll() {
        prefs.edit().clear().apply()
    }
    
    // Clear only session data (for logout)
    fun clearSession() {
        prefs.edit().apply {
            remove("is_logged_in")
            remove("current_user_id")
            remove("current_user_role")
            remove("current_user_gamertag")
            remove("current_user_email")
            remove("current_user_phone")
            apply()
        }
    }
    
    // Export settings as JSON
    fun exportSettings(): String {
        val allPrefs = prefs.all
        return allPrefs.entries.joinToString(",\n") { (key, value) ->
            "\"$key\": \"$value\""
        }.let { "{\n$it\n}" }
    }
    
    companion object {
        @Volatile
        private var instance: PreferencesManager? = null
        
        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
