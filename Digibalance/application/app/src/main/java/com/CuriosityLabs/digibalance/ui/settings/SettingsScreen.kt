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
import androidx.navigation.NavController
import com.CuriosityLabs.digibalance.data.PreferencesManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    preferencesManager: PreferencesManager,
    userRole: String // "Student", "Parent", or "Professional"
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    
    // Fetch real user data from database
    var userData by remember { mutableStateOf<com.CuriosityLabs.digibalance.data.repository.User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        val userRepository = com.CuriosityLabs.digibalance.data.repository.UserRepository()
        val result = userRepository.getCurrentUserProfile()
        result.onSuccess { user ->
            userData = user
            isLoading = false
        }.onFailure {
            isLoading = false
        }
    }
    
    val settingsSections = remember(userRole, userData) {
        getSettingsSections(userRole, userData, navController, preferencesManager)
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
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8F9FA),
                            Color(0xFFFFFFFF),
                            Color(0xFFF0F4F8)
                        )
                    )
                ),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // User Profile Card
            item {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally))
                } else {
                    UserProfileCard(userRole = userRole, userData = userData)
                }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun PremiumTopBar(
    showSearch: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column {
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = showSearch,
                        transitionSpec = {
                            fadeIn() + slideInVertically() with fadeOut() + slideOutVertically()
                        },
                        label = "search_animation"
                    ) { isSearching ->
                        if (isSearching) {
                            TextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                placeholder = { Text("Search settings...") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                        } else {
                            Text(
                                "Settings",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onSearchToggle) {
                        Icon(
                            if (showSearch) Icons.Default.Close else Icons.Default.Search,
                            "Search"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}


@Composable
fun UserProfileCard(userRole: String, userData: com.CuriosityLabs.digibalance.data.repository.User?) {
    var isExpanded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    val displayName = userData?.display_name ?: "User"
    val email = userData?.email ?: "Not set"
    val phone = userData?.phone ?: "Not set"
    val gamertag = userData?.gamertag ?: "Not set"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (userRole) {
                                "Parent" -> Icons.Default.Person
                                "Professional" -> Icons.Default.Work
                                else -> Icons.Default.School
                            },
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Column {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = userRole,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileInfoRow(icon = Icons.Default.Email, label = "Email", value = email)
                ProfileInfoRow(icon = Icons.Default.Phone, label = "Phone", value = phone)
                if (userRole != "Parent") {
                    ProfileInfoRow(icon = Icons.Default.Star, label = "Gamertag", value = gamertag)
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        thickness = 2.dp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    )
}

@Composable
fun AnimatedSettingsItem(
    item: SettingsItem,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press_scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon with gradient background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    item.iconColor.copy(alpha = 0.2f),
                                    item.iconColor.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = item.iconColor
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    item.subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            // Status indicator or action
            when (item.type) {
                SettingsItemType.TOGGLE -> {
                    Switch(
                        checked = item.isEnabled ?: false,
                        onCheckedChange = { item.onToggle?.invoke(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFBDBDBD),
                            uncheckedBorderColor = Color(0xFF9E9E9E)
                        )
                    )
                }
                SettingsItemType.NAVIGATION -> {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
                SettingsItemType.STATUS -> {
                    StatusBadge(isActive = item.isEnabled ?: false)
                }
            }
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}


@Composable
fun StatusBadge(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(
                if (isActive) Color(0xFF4CAF50).copy(alpha = alpha)
                else Color(0xFFFF5252)
            )
    )
}

@Composable
fun AppVersionFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "DigiBalance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Version 1.2.0 (Build 120)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = "Made by CuriosityLabs",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

// Data Models
data class SettingsSection(
    val title: String,
    val icon: ImageVector,
    val items: List<SettingsItem>
)

data class SettingsItem(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val iconColor: Color,
    val type: SettingsItemType,
    val isEnabled: Boolean? = null,
    val onClick: () -> Unit = {},
    val onToggle: ((Boolean) -> Unit)? = null
)

enum class SettingsItemType {
    NAVIGATION,
    TOGGLE,
    STATUS
}

// Settings Configuration
fun getSettingsSections(
    userRole: String, 
    userData: com.CuriosityLabs.digibalance.data.repository.User?,
    navController: NavController,
    preferencesManager: PreferencesManager
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
                    onClick = {
                        // Navigate to profile edit screen
                        android.util.Log.d("Settings", "Profile Settings clicked")
                    }
                ),
                SettingsItem(
                    title = "Change Password",
                    subtitle = "Update your security credentials",
                    icon = Icons.Default.Lock,
                    iconColor = Color(0xFFFF9800),
                    type = SettingsItemType.NAVIGATION,
                    onClick = {
                        // Navigate to change password screen
                        android.util.Log.d("Settings", "Change Password clicked")
                    }
                ),
                SettingsItem(
                    title = "Delete Account",
                    subtitle = "Permanently remove your account",
                    icon = Icons.Default.Delete,
                    iconColor = Color(0xFFFF5252),
                    type = SettingsItemType.NAVIGATION,
                    onClick = {
                        // Show delete confirmation dialog
                        android.util.Log.d("Settings", "Delete Account clicked")
                    }
                )
            )
        )
    )
    
    // Parent-Student Linking
    if (userRole == "Parent") {
        sections.add(
            SettingsSection(
                title = "Family Controls",
                icon = Icons.Default.FamilyRestroom,
                items = listOf(
                    SettingsItem(
                        title = "Link Student Device",
                        subtitle = "Connect your child's device via QR code",
                        icon = Icons.Default.QrCode,
                        iconColor = Color(0xFF2196F3),
                        type = SettingsItemType.NAVIGATION,
                        onClick = {
                            navController.navigate("qr_code_display")
                        }
                    ),
                    SettingsItem(
                        title = "Manage Linked Students",
                        subtitle = "View and manage connected devices",
                        icon = Icons.Default.Devices,
                        iconColor = Color(0xFF4CAF50),
                        type = SettingsItemType.NAVIGATION,
                        onClick = {
                            navController.navigate("parent_dashboard")
                        }
                    )
                )
            )
        )
    } else if (userRole == "Student") {
        val isLinked = userData?.linked_parent_id != null
        sections.add(
            SettingsSection(
                title = "Parental Controls",
                icon = Icons.Default.FamilyRestroom,
                items = listOf(
                    SettingsItem(
                        title = "Link to Parent",
                        subtitle = "Scan parent's QR code to connect",
                        icon = Icons.Default.QrCodeScanner,
                        iconColor = Color(0xFF2196F3),
                        type = SettingsItemType.NAVIGATION,
                        onClick = {
                            navController.navigate("qr_scanner")
                        }
                    ),
                    SettingsItem(
                        title = "Parent Connection Status",
                        subtitle = if (isLinked) "Linked to parent" else "Not linked",
                        icon = Icons.Default.Link,
                        iconColor = if (isLinked) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        type = SettingsItemType.STATUS,
                        isEnabled = isLinked
                    )
                )
            )
        )
    }
    
    // Gamertag (Students & Professionals only)
    if (userRole != "Parent") {
        val gamertag = userData?.gamertag ?: "Not set"
        sections.add(
            SettingsSection(
                title = "Gaming Profile",
                icon = Icons.Default.Star,
                items = listOf(
                    SettingsItem(
                        title = "Gamertag",
                        subtitle = "$gamertag • Tap to change",
                        icon = Icons.Default.Badge,
                        iconColor = Color(0xFF9C27B0),
                        type = SettingsItemType.NAVIGATION,
                        onClick = {
                            navController.navigate("gamertag")
                        }
                    ),
                    SettingsItem(
                        title = "Leaderboard Visibility",
                        subtitle = "Show your rank publicly",
                        icon = Icons.Default.Visibility,
                        iconColor = Color(0xFF00BCD4),
                        type = SettingsItemType.TOGGLE,
                        isEnabled = preferencesManager.showLeaderboardRank,
                        onToggle = { enabled ->
                            preferencesManager.showLeaderboardRank = enabled
                            android.util.Log.d("Settings", "Leaderboard visibility: $enabled")
                        }
                    )
                )
            )
        )
    }
    
    // Permissions (All Users)
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
                    isEnabled = true,
                    onClick = {
                        android.util.Log.d("Settings", "Usage Stats clicked")
                    }
                ),
                SettingsItem(
                    title = "Accessibility Service",
                    subtitle = "For distraction alerts",
                    icon = Icons.Default.Accessibility,
                    iconColor = Color(0xFF2196F3),
                    type = SettingsItemType.STATUS,
                    isEnabled = false,
                    onClick = {
                        android.util.Log.d("Settings", "Accessibility clicked")
                    }
                ),
                SettingsItem(
                    title = "Draw Over Apps",
                    subtitle = "Show overlay alerts",
                    icon = Icons.Default.Layers,
                    iconColor = Color(0xFFFF9800),
                    type = SettingsItemType.STATUS,
                    isEnabled = true,
                    onClick = {
                        android.util.Log.d("Settings", "Draw Over Apps clicked")
                    }
                ),
                SettingsItem(
                    title = "Do Not Disturb",
                    subtitle = "Silent Focus Mode",
                    icon = Icons.Default.NotificationsOff,
                    iconColor = Color(0xFF9C27B0),
                    type = SettingsItemType.STATUS,
                    isEnabled = true,
                    onClick = {
                        android.util.Log.d("Settings", "DND clicked")
                    }
                ),
                SettingsItem(
                    title = "Battery Optimization",
                    subtitle = "Unrestricted background",
                    icon = Icons.Default.BatteryFull,
                    iconColor = Color(0xFF4CAF50),
                    type = SettingsItemType.STATUS,
                    isEnabled = true,
                    onClick = {
                        android.util.Log.d("Settings", "Battery clicked")
                    }
                ),
                SettingsItem(
                    title = "Fix All Permissions",
                    subtitle = "Grant all required permissions",
                    icon = Icons.Default.Build,
                    iconColor = Color(0xFFFF5252),
                    type = SettingsItemType.NAVIGATION,
                    onClick = {
                        navController.navigate("permissions")
                    }
                )
            )
        )
    )
    
    // App Settings
    sections.add(
        SettingsSection(
            title = "App Settings",
            icon = Icons.Default.Settings,
            items = listOf(
                SettingsItem(
                    title = "Appearance",
                    subtitle = "Customize colors and charts",
                    icon = Icons.Default.Palette,
                    iconColor = Color(0xFF9C27B0),
                    type = SettingsItemType.NAVIGATION,
                    onClick = {
                        navController.navigate("appearance_settings")
                    }
                ),
                SettingsItem(
                    title = "Auto Sync",
                    subtitle = "Sync data automatically",
                    icon = Icons.Default.Sync,
                    iconColor = Color(0xFF2196F3),
                    type = SettingsItemType.TOGGLE,
                    isEnabled = preferencesManager.autoSync,
                    onToggle = { enabled ->
                        preferencesManager.autoSync = enabled
                    }
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
                    isEnabled = preferencesManager.distractionAlertsEnabled,
                    onToggle = { enabled ->
                        preferencesManager.distractionAlertsEnabled = enabled
                        android.util.Log.d("Settings", "Distraction Alerts: $enabled")
                    }
                ),
                SettingsItem(
                    title = "Focus Mode Reminders",
                    subtitle = "Scheduled focus notifications",
                    icon = Icons.Default.Schedule,
                    iconColor = Color(0xFF2196F3),
                    type = SettingsItemType.TOGGLE,
                    isEnabled = preferencesManager.focusRemindersEnabled,
                    onToggle = { enabled ->
                        preferencesManager.focusRemindersEnabled = enabled
                        android.util.Log.d("Settings", "Focus Reminders: $enabled")
                    }
                ),
                SettingsItem(
                    title = "Leaderboard Updates",
                    subtitle = "Weekly rank changes",
                    icon = Icons.Default.TrendingUp,
                    iconColor = Color(0xFF4CAF50),
                    type = SettingsItemType.TOGGLE,
                    isEnabled = preferencesManager.leaderboardUpdatesEnabled,
                    onToggle = { enabled ->
                        preferencesManager.leaderboardUpdatesEnabled = enabled
                        android.util.Log.d("Settings", "Leaderboard Updates: $enabled")
                    }
                )
            )
        )
    )
    
    // About & Support
    sections.add(
        SettingsSection(
            title = "About & Support",
            icon = Icons.Default.Info,
            items = listOf(
                SettingsItem(
                    title = "Help & FAQ",
                    subtitle = "Get help and answers",
                    icon = Icons.Default.Help,
                    iconColor = Color(0xFF2196F3),
                    type = SettingsItemType.NAVIGATION,
                    onClick = {
                        android.util.Log.d("Settings", "Help clicked")
                    }
                ),
                SettingsItem(
                    title = "Privacy Policy",
                    subtitle = "View our privacy policy",
                    icon = Icons.Default.PrivacyTip,
                    iconColor = Color(0xFF4CAF50),
                    type = SettingsItemType.NAVIGATION,
                    onClick = {
                        android.util.Log.d("Settings", "Privacy Policy clicked")
                    }
                ),
                SettingsItem(
                    title = "Terms of Service",
                    subtitle = "View terms and conditions",
                    icon = Icons.Default.Description,
                    iconColor = Color(0xFFFF9800),
                    type = SettingsItemType.NAVIGATION,
                    onClick = {
                        android.util.Log.d("Settings", "Terms clicked")
                    }
                )
            )
        )
    )
    
    // Sign Out
    sections.add(
        SettingsSection(
            title = "Session",
            icon = Icons.Default.ExitToApp,
            items = listOf(
                SettingsItem(
                    title = "Sign Out",
                    subtitle = "Log out of your account",
                    icon = Icons.Default.Logout,
                    iconColor = Color(0xFFFF5252),
                    type = SettingsItemType.NAVIGATION,
                    onClick = {
                        kotlinx.coroutines.GlobalScope.launch {
                            val userRepository = com.CuriosityLabs.digibalance.data.repository.UserRepository()
                            userRepository.signOut()
                            preferencesManager.clearSession()
                            navController.navigate("auth") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                )
            )
        )
    )
    
    return sections
}
