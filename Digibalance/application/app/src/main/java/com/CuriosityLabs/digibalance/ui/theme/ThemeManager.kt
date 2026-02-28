package com.CuriosityLabs.digibalance.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.CuriosityLabs.digibalance.data.PreferencesManager

class ThemeManager(private val preferencesManager: PreferencesManager) {
    var themeMode by mutableStateOf(preferencesManager.themeMode)
        private set
    
    fun updateThemeMode(mode: String) {
        themeMode = mode
        preferencesManager.themeMode = mode
    }
    
    fun refreshThemeMode() {
        themeMode = preferencesManager.themeMode
    }
}
