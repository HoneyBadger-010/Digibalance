package com.CuriosityLabs.digibalance.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.CuriosityLabs.digibalance.data.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val userRole: String = "Student",
    val displayName: String = "John Doe",
    val email: String? = null,
    val phone: String? = null,
    val gamertag: String? = null,
    val gamertagLastChanged: Long = 0,
    val linkedParentId: String? = null,
    
    // Permissions
    val hasUsageStatsPermission: Boolean = false,
    val hasAccessibilityPermission: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val hasDndPermission: Boolean = false,
    val hasBootPermission: Boolean = false,
    val isBatteryOptimized: Boolean = true,
    
    // Preferences
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.BLUE,
    val chartType: ChartType = ChartType.PIE,
    val compactView: Boolean = false,
    val showAnimations: Boolean = true,
    
    // Notifications
    val distractionAlertsEnabled: Boolean = true,
    val focusRemindersEnabled: Boolean = true,
    val leaderboardUpdatesEnabled: Boolean = false,
    val parentNotificationsEnabled: Boolean = true,
    
    // Focus Mode
    val defaultFocusDuration: Int = 60, // minutes
    val focusEmergencyCode: String? = null,
    val allowedFocusApps: List<String> = emptyList(),
    
    // Distraction Settings
    val distractionAlertTriggerTime: Int = 5, // minutes
    val distractionAlertStyle: AlertStyle = AlertStyle.OVERLAY,
    
    // Sync
    val lastSyncTimestamp: Long = 0,
    val syncStatus: SyncStatus = SyncStatus.CONNECTED,
    val autoSyncEnabled: Boolean = true,
    
    // Loading states
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class AccentColor {
    BLUE, PURPLE, GREEN, ORANGE, TEAL, PINK
}

enum class ChartType {
    PIE, BAR, LINE
}

enum class AlertStyle {
    OVERLAY, NOTIFICATION
}

enum class SyncStatus {
    CONNECTED, PENDING, FAILED, OFFLINE
}

class SettingsViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                themeMode = when (preferencesManager.themeMode) {
                    "Light" -> ThemeMode.LIGHT
                    "Dark" -> ThemeMode.DARK
                    else -> ThemeMode.SYSTEM
                },
                accentColor = AccentColor.valueOf(preferencesManager.accentColor.uppercase()),
                chartType = ChartType.valueOf(preferencesManager.chartType.uppercase()),
                compactView = preferencesManager.compactView,
                showAnimations = preferencesManager.showAnimations,
                distractionAlertsEnabled = preferencesManager.distractionAlertsEnabled,
                focusRemindersEnabled = preferencesManager.focusRemindersEnabled,
                leaderboardUpdatesEnabled = preferencesManager.leaderboardUpdatesEnabled,
                autoSyncEnabled = preferencesManager.autoSync
            )
        }
    }
    
    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.themeMode = mode.name.lowercase().replaceFirstChar { it.uppercase() }
            _uiState.value = _uiState.value.copy(themeMode = mode)
        }
    }
    
    fun updateAccentColor(color: AccentColor) {
        viewModelScope.launch {
            preferencesManager.accentColor = color.name.lowercase().replaceFirstChar { it.uppercase() }
            _uiState.value = _uiState.value.copy(accentColor = color)
        }
    }
    
    fun updateChartType(type: ChartType) {
        viewModelScope.launch {
            preferencesManager.chartType = type.name.lowercase().replaceFirstChar { it.uppercase() }
            _uiState.value = _uiState.value.copy(chartType = type)
        }
    }
    
    fun updateCompactView(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.compactView = enabled
            _uiState.value = _uiState.value.copy(compactView = enabled)
        }
    }
    
    fun updateShowAnimations(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.showAnimations = enabled
            _uiState.value = _uiState.value.copy(showAnimations = enabled)
        }
    }
    
    fun updateDistractionAlerts(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.distractionAlertsEnabled = enabled
            _uiState.value = _uiState.value.copy(distractionAlertsEnabled = enabled)
        }
    }
    
    fun updateFocusReminders(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.focusRemindersEnabled = enabled
            _uiState.value = _uiState.value.copy(focusRemindersEnabled = enabled)
        }
    }
    
    fun updateLeaderboardUpdates(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.leaderboardUpdatesEnabled = enabled
            _uiState.value = _uiState.value.copy(leaderboardUpdatesEnabled = enabled)
        }
    }
    
    fun updateAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.autoSync = enabled
            _uiState.value = _uiState.value.copy(autoSyncEnabled = enabled)
        }
    }
    
    fun updateDistractionAlertTriggerTime(minutes: Int) {
        viewModelScope.launch {
            preferencesManager.distractionAlertTriggerTime = minutes
            _uiState.value = _uiState.value.copy(distractionAlertTriggerTime = minutes)
        }
    }
    
    fun updateDistractionAlertStyle(style: AlertStyle) {
        viewModelScope.launch {
            preferencesManager.distractionAlertStyle = style.name
            _uiState.value = _uiState.value.copy(distractionAlertStyle = style)
        }
    }
    
    fun updateDefaultFocusDuration(minutes: Int) {
        viewModelScope.launch {
            preferencesManager.defaultFocusDuration = minutes
            _uiState.value = _uiState.value.copy(defaultFocusDuration = minutes)
        }
    }
    
    fun updateGamertag(newGamertag: String) {
        viewModelScope.launch {
            // TODO: Validate gamertag availability via API
            // TODO: Check if 30 days have passed since last change
            _uiState.value = _uiState.value.copy(
                gamertag = newGamertag,
                gamertagLastChanged = System.currentTimeMillis()
            )
        }
    }
    
    fun checkPermissions() {
        viewModelScope.launch {
            // TODO: Implement actual permission checks
            _uiState.value = _uiState.value.copy(
                hasUsageStatsPermission = true,
                hasAccessibilityPermission = false,
                hasOverlayPermission = true,
                hasDndPermission = true,
                hasBootPermission = true,
                isBatteryOptimized = false
            )
        }
    }
    
    fun syncSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                syncStatus = SyncStatus.PENDING,
                isLoading = true
            )
            
            try {
                // TODO: Implement actual sync with Supabase
                kotlinx.coroutines.delay(2000) // Simulate network call
                
                _uiState.value = _uiState.value.copy(
                    syncStatus = SyncStatus.CONNECTED,
                    lastSyncTimestamp = System.currentTimeMillis(),
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    syncStatus = SyncStatus.FAILED,
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
