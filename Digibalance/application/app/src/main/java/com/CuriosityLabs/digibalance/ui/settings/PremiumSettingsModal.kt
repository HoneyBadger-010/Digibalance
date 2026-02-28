package com.CuriosityLabs.digibalance.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.CuriosityLabs.digibalance.data.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PremiumSettingsScreenModal(
    preferencesManager: PreferencesManager,
    userRole: String,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var selectedSubScreen by remember { mutableStateOf<SettingsSubScreen?>(null) }
    
    // Full-screen dialog
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedSubScreen) {
                SettingsSubScreen.APPEARANCE -> {
                    AppearanceSettingsScreenStandalone(
                        preferencesManager = preferencesManager,
                        onBack = { selectedSubScreen = null }
                    )
                }
                SettingsSubScreen.FOCUS_MODE -> {
                    FocusModeSettingsScreenStandalone(
                        userRole = userRole,
                        onBack = { selectedSubScreen = null }
                    )
                }
                null -> {
                    // Main settings screen
                    val settingsSections = remember(userRole) {
                        getSettingsSections(userRole, onNavigate = { screen ->
                            selectedSubScreen = screen
                        })
                    }
                    
                    val filteredSections = remember(searchQuery, settingsSections) {
                        if (searchQuery.isEmpty()) {
                            settingsSections
                        } else {
                            settingsSections.map { section ->
                                section.copy(
                                    items = section.items.filter {
                                        it.title.contains(searchQuery, ignoreCase = true) ||
                                        it.subtitle?.contains(searchQuery, ignoreCase = true) == true
                                    }
                                )
                            }.filter { it.items.isNotEmpty() }
                        }
                    }

                    Scaffold(
                        topBar = {
                            PremiumTopBar(
                                showSearch = showSearch,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                onSearchToggle = { showSearch = !showSearch },
                                onBackClick = onDismiss
                            )
                        }
                    ) { padding ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .background(MaterialTheme.colorScheme.background),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // User Profile Card
                            item {
                                UserProfileCard(userRole = userRole, userData = null)
                            }
                            
                            // Settings Sections
                            filteredSections.forEach { section ->
                                item {
                                    SettingsSectionHeader(title = section.title, icon = section.icon)
                                }
                                
                                items(section.items) { item ->
                                    AnimatedSettingsItem(
                                        item = item,
                                        onClick = { item.onClick() }
                                    )
                                }
                            }
                            
                            // App Version Footer
                            item {
                                AppVersionFooter()
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class SettingsSubScreen {
    APPEARANCE,
    FOCUS_MODE
}

// Standalone versions without NavController
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreenStandalone(
    preferencesManager: PreferencesManager,
    onBack: () -> Unit
) {
    // TODO-14: Load and save theme preferences
    var selectedTheme by remember { mutableStateOf(preferencesManager.themeMode) }
    var selectedAccentColor by remember { mutableStateOf(preferencesManager.accentColor) }
    var selectedChartType by remember { mutableStateOf(preferencesManager.chartType) }
    var compactView by remember { mutableStateOf(preferencesManager.compactView) }
    var showAnimations by remember { mutableStateOf(preferencesManager.showAnimations) }
    
    // Save preferences when they change
    LaunchedEffect(selectedTheme) {
        preferencesManager.themeMode = selectedTheme
    }
    LaunchedEffect(selectedAccentColor) {
        preferencesManager.accentColor = selectedAccentColor
    }
    LaunchedEffect(selectedChartType) {
        preferencesManager.chartType = selectedChartType
    }
    LaunchedEffect(compactView) {
        preferencesManager.compactView = compactView
    }
    LaunchedEffect(showAnimations) {
        preferencesManager.showAnimations = showAnimations
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Appearance",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // DARK MODE REMOVED - Light Mode Only
            
            // Accent Color
            item {
                PreferenceSectionCard(
                    title = "Accent Color",
                    icon = Icons.Default.ColorLens,
                    description = "Personalize your app's look"
                ) {
                    AccentColorPicker(
                        selectedColor = selectedAccentColor,
                        onColorSelected = { selectedAccentColor = it }
                    )
                }
            }
            
            // Chart Type
            item {
                PreferenceSectionCard(
                    title = "Chart Style",
                    icon = Icons.Default.BarChart,
                    description = "Default visualization for reports"
                ) {
                    ChartTypePicker(
                        selectedType = selectedChartType,
                        onTypeSelected = { selectedChartType = it }
                    )
                }
            }
            
            // Display Options
            item {
                PreferenceSectionCard(
                    title = "Display Options",
                    icon = Icons.Default.DisplaySettings,
                    description = "Customize your viewing experience"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SwitchPreference(
                            title = "Compact View",
                            description = "Show more content on screen",
                            checked = compactView,
                            onCheckedChange = { compactView = it }
                        )
                        SwitchPreference(
                            title = "Animations",
                            description = "Enable smooth transitions",
                            checked = showAnimations,
                            onCheckedChange = { showAnimations = it }
                        )
                    }
                }
            }
            
            // Preview Card
            item {
                PreviewCard(
                    accentColor = selectedAccentColor,
                    chartType = selectedChartType
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeSettingsScreenStandalone(
    userRole: String,
    onBack: () -> Unit
) {
    var defaultDuration by remember { mutableStateOf(60) }
    var autoStartEnabled by remember { mutableStateOf(false) }
    var emergencyCodeSet by remember { mutableStateOf(false) }
    var showEmergencyCodeDialog by remember { mutableStateOf(false) }
    var allowedApps by remember { mutableStateOf(listOf("Phone", "Calculator", "Camera")) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Focus Mode",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Focus Mode Status Card
            item {
                FocusModeStatusCard(isActive = false, remainingTime = 0)
            }
            
            // Duration Selector
            item {
                PreferenceSectionCard(
                    title = "Default Duration",
                    icon = Icons.Default.Timer,
                    description = "How long should focus sessions last?"
                ) {
                    DurationSelector(
                        selectedDuration = defaultDuration,
                        onDurationSelected = { defaultDuration = it }
                    )
                }
            }
            
            // Focus Mode Tips
            item {
                FocusModeTipsCard()
            }
        }
    }
    
    if (showEmergencyCodeDialog) {
        EmergencyCodeDialog(
            onDismiss = { showEmergencyCodeDialog = false },
            onConfirm = { code ->
                emergencyCodeSet = true
                showEmergencyCodeDialog = false
            }
        )
    }
}

// Settings configuration with navigation
fun getSettingsSections(
    userRole: String,
    onNavigate: (SettingsSubScreen) -> Unit
): List<SettingsSection> {
    val sections = mutableListOf<SettingsSection>()
    
    // Account Settings (All Users)
    sections.add(
        SettingsSection(
            title = "Account",
            icon = Icons.Default.Person,
            items = listOf(
                SettingsItem(
                    title = "Profile Settings",
                    subtitle = "Edit your personal information",
                    icon = Icons.Default.Edit,
                    iconColor = Color(0xFF2196F3),
                    type = SettingsItemType.NAVIGATION,
                    onClick = { /* TODO: Navigate to profile */ }
                ),
                SettingsItem(
                    title = "Change Password",
                    subtitle = "Update your security credentials",
                    icon = Icons.Default.Lock,
                    iconColor = Color(0xFFFF9800),
                    type = SettingsItemType.NAVIGATION,
                    onClick = { /* TODO: Navigate to change password */ }
                )
            )
        )
    )
    
    // Appearance
    sections.add(
        SettingsSection(
            title = "Appearance",
            icon = Icons.Default.Palette,
            items = listOf(
                SettingsItem(
                    title = "Theme & Colors",
                    subtitle = "Customize your app's look",
                    icon = Icons.Default.ColorLens,
                    iconColor = Color(0xFF9C27B0),
                    type = SettingsItemType.NAVIGATION,
                    onClick = { onNavigate(SettingsSubScreen.APPEARANCE) }
                )
            )
        )
    )
    
    // Focus Mode
    if (userRole != "Parent") {
        sections.add(
            SettingsSection(
                title = "Focus Mode",
                icon = Icons.Default.Timer,
                items = listOf(
                    SettingsItem(
                        title = "Focus Settings",
                        subtitle = "Configure your focus sessions",
                        icon = Icons.Default.Settings,
                        iconColor = Color(0xFF4CAF50),
                        type = SettingsItemType.NAVIGATION,
                        onClick = { onNavigate(SettingsSubScreen.FOCUS_MODE) }
                    )
                )
            )
        )
    }
    
    // Permissions
    sections.add(
        SettingsSection(
            title = "Permissions",
            icon = Icons.Default.Security,
            items = listOf(
                SettingsItem(
                    title = "Usage Stats Access",
                    subtitle = "Required for reports",
                    icon = Icons.Default.BarChart,
                    iconColor = Color(0xFF4CAF50),
                    type = SettingsItemType.STATUS,
                    isEnabled = true
                ),
                SettingsItem(
                    title = "Accessibility Service",
                    subtitle = "For distraction alerts",
                    icon = Icons.Default.Accessibility,
                    iconColor = Color(0xFF2196F3),
                    type = SettingsItemType.STATUS,
                    isEnabled = false
                ),
                SettingsItem(
                    title = "Battery Optimization",
                    subtitle = "Unrestricted background",
                    icon = Icons.Default.BatteryFull,
                    iconColor = Color(0xFF4CAF50),
                    type = SettingsItemType.STATUS,
                    isEnabled = true
                )
            )
        )
    )
    
    // Notifications
    sections.add(
        SettingsSection(
            title = "Notifications",
            icon = Icons.Default.Notifications,
            items = listOf(
                SettingsItem(
                    title = "Distraction Alerts",
                    subtitle = "Warn when using distracting apps",
                    icon = Icons.Default.Warning,
                    iconColor = Color(0xFFFF9800),
                    type = SettingsItemType.TOGGLE,
                    isEnabled = true
                ),
                SettingsItem(
                    title = "Focus Mode Reminders",
                    subtitle = "Scheduled focus notifications",
                    icon = Icons.Default.Schedule,
                    iconColor = Color(0xFF2196F3),
                    type = SettingsItemType.TOGGLE,
                    isEnabled = true
                )
            )
        )
    )
    
    return sections
}
