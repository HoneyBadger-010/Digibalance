package com.CuriosityLabs.digibalance.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CuriosityLabs.digibalance.data.PreferencesManager
import com.CuriosityLabs.digibalance.data.repository.UserRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleSettingsScreen(
    userRole: UserRole,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { PreferencesManager.getInstance(context) }
    val userRepository = remember { UserRepository() }
    
    var showQRScanner by remember { mutableStateOf(false) }
    var showQRDisplay by remember { mutableStateOf(false) }
    var showEmergencyCode by remember { mutableStateOf(false) }
    var userData by remember { mutableStateOf<com.CuriosityLabs.digibalance.data.repository.User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load user data
    LaunchedEffect(Unit) {
        scope.launch {
            val result = userRepository.getCurrentUserProfile()
            result.onSuccess { user ->
                userData = user
                isLoading = false
            }.onFailure {
                isLoading = false
            }
        }
    }
    
    // Show QR Scanner for students
    if (showQRScanner) {
        com.CuriosityLabs.digibalance.ui.student.QRScannerScreen(
            onLinkSuccess = {
                showQRScanner = false
                // Refresh user data
                scope.launch {
                    val result = userRepository.getCurrentUserProfile()
                    result.onSuccess { user -> userData = user }
                }
            },
            onBack = { showQRScanner = false }
        )
        return
    }
    
    // Show QR Display for parents
    if (showQRDisplay) {
        com.CuriosityLabs.digibalance.ui.parent.QRCodeDisplayScreen(
            onBack = { showQRDisplay = false }
        )
        return
    }
    
    // Show Emergency Code screen for parents
    if (showEmergencyCode) {
        com.CuriosityLabs.digibalance.ui.parent.EmergencyCodeScreen(
            onBack = { showEmergencyCode = false }
        )
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings", 
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8FAFC),
                            Color(0xFFFFFFFF),
                            Color(0xFFF1F5F9)
                        )
                    )
                ),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Profile Section
            item {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    UserProfileCard(
                        userRole = userRole,
                        userData = userData
                    )
                }
            }
            
            // Parent-Student Linking Section
            if (userRole == UserRole.PARENT) {
                item {
                    SettingsSection(title = "Family Controls") {
                        SettingsItem(
                            title = "Link Student Device",
                            subtitle = "Generate QR code for student to scan",
                            icon = Icons.Default.QrCode,
                            iconColor = Color(0xFF2196F3),
                            onClick = { showQRDisplay = true }
                        )
                        
                        SettingsItem(
                            title = "Generate Emergency Code",
                            subtitle = "Create exit code for student",
                            icon = Icons.Default.Key,
                            iconColor = Color(0xFFFF9800),
                            onClick = { showEmergencyCode = true }
                        )
                        
                        SettingsItem(
                            title = "Manage Students",
                            subtitle = "View and manage linked devices",
                            icon = Icons.Default.Devices,
                            iconColor = Color(0xFF4CAF50),
                            onClick = {
                                // Navigate to parent dashboard
                            }
                        )
                    }
                }
            } else if (userRole == UserRole.STUDENT) {
                item {
                    val isLinked = userData?.linked_parent_id != null
                    SettingsSection(title = "Parental Controls") {
                        SettingsItem(
                            title = "Connect to Parent",
                            subtitle = "Scan parent's QR code to link",
                            icon = Icons.Default.QrCodeScanner,
                            iconColor = Color(0xFF2196F3),
                            onClick = { showQRScanner = true }
                        )
                        
                        SettingsItem(
                            title = "Connection Status",
                            subtitle = if (isLinked) "Linked to parent" else "Not connected",
                            icon = Icons.Default.Link,
                            iconColor = if (isLinked) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            onClick = { }
                        )
                    }
                }
            }
            
            // Account Settings
            item {
                SettingsSection(title = "Account") {
                    SettingsItem(
                        title = "Profile Settings",
                        subtitle = "Edit your personal information",
                        icon = Icons.Default.Person,
                        iconColor = Color(0xFF2196F3),
                        onClick = { }
                    )
                    
                    SettingsItem(
                        title = "Change Password",
                        subtitle = "Update your password",
                        icon = Icons.Default.Lock,
                        iconColor = Color(0xFFFF9800),
                        onClick = { }
                    )
                }
            }
            
            // App Settings
            item {
                SettingsSection(title = "App Settings") {
                    SettingsItem(
                        title = "Theme & Colors",
                        subtitle = "Customize appearance",
                        icon = Icons.Default.Palette,
                        iconColor = Color(0xFF9C27B0),
                        onClick = { }
                    )
                    
                    SettingsItem(
                        title = "Focus Settings",
                        subtitle = "Configure focus mode",
                        icon = Icons.Default.Psychology,
                        iconColor = Color(0xFF2196F3),
                        onClick = { }
                    )
                }
            }
            
            // Permissions
            item {
                SettingsSection(title = "Permissions") {
                    SettingsItem(
                        title = "Usage Stats Access",
                        subtitle = "Required for app reports",
                        icon = Icons.Default.BarChart,
                        iconColor = Color(0xFF4CAF50),
                        onClick = { }
                    )
                    
                    SettingsItem(
                        title = "Accessibility Service",
                        subtitle = "For distraction alerts",
                        icon = Icons.Default.Accessibility,
                        iconColor = Color(0xFF2196F3),
                        onClick = { }
                    )
                    
                    SettingsItem(
                        title = "Battery Optimization",
                        subtitle = "Prevent app from being killed",
                        icon = Icons.Default.BatteryFull,
                        iconColor = Color(0xFF4CAF50),
                        onClick = { }
                    )
                }
            }
            
            // Notifications
            item {
                SettingsSection(title = "Notifications") {
                    SettingsToggleItem(
                        title = "Distraction Alerts",
                        subtitle = "Warn about distracting apps",
                        icon = Icons.Default.Warning,
                        iconColor = Color(0xFFFF9800),
                        isEnabled = prefs.distractionAlertsEnabled,
                        onToggle = { prefs.distractionAlertsEnabled = it }
                    )
                    
                    SettingsToggleItem(
                        title = "Focus Reminders",
                        subtitle = "Scheduled focus notifications",
                        icon = Icons.Default.Schedule,
                        iconColor = Color(0xFF2196F3),
                        isEnabled = prefs.focusRemindersEnabled,
                        onToggle = { prefs.focusRemindersEnabled = it }
                    )
                }
            }
            
            // Sign Out
            item {
                SettingsSection(title = "Session") {
                    SettingsItem(
                        title = "Sign Out",
                        subtitle = "Log out of your account",
                        icon = Icons.Default.Logout,
                        iconColor = Color(0xFFFF5252),
                        onClick = {
                            scope.launch {
                                userRepository.signOut()
                                prefs.clearSession()
                                onDismiss()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UserProfileCard(
    userRole: UserRole,
    userData: com.CuriosityLabs.digibalance.data.repository.User?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Card(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (userRole) {
                            UserRole.PARENT -> Icons.Default.Person
                            UserRole.PROFESSIONAL -> Icons.Default.Work
                            else -> Icons.Default.School
                        },
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
            }
            
            // User Info
            Column {
                Text(
                    text = userData?.display_name ?: "User",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = userRole.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575)
                )
                userData?.email?.let { email ->
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2196F3),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon
        Card(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = iconColor.copy(alpha = 0.1f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = iconColor
                )
            }
        }
        
        // Text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF757575)
            )
        }
        
        // Arrow
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF757575)
        )
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon
        Card(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = iconColor.copy(alpha = 0.1f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = iconColor
                )
            }
        }
        
        // Text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF757575)
            )
        }
        
        // Toggle
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF4CAF50),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFBDBDBD)
            )
        )
    }
}