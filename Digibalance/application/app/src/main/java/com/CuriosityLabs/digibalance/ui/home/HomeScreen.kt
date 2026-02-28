package com.CuriosityLabs.digibalance.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CuriosityLabs.digibalance.R

enum class UserRole {
    PARENT, STUDENT, PROFESSIONAL
}

enum class TabItem(val title: String, val iconRes: Int) {
    AWARENESS("Videos", R.drawable.awareness),
    LEADERBOARD("Rank", R.drawable.leaderboard),
    REPORT("Reports", R.drawable.report),
    FOCUS("Focus", R.drawable.focus),
    FEEDBACK("Feedback", R.drawable.review)
}

@Composable
fun HomeScreen(
    userRole: UserRole,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(TabItem.AWARENESS) }
    var showProfileMenu by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    // TODO-12: Removed showPersonalize state

    Scaffold(
        topBar = {
            HomeTopBar(
                userRole = userRole,
                onProfileClick = { showProfileMenu = true }
            )
        },
        bottomBar = {
            HomeBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8FAFC), // Clean light gray
                            Color(0xFFFFFFFF), // Pure white
                            Color(0xFFF1F5F9)  // Subtle blue tint
                        )
                    )
                )
        ) {
            // For parents, show dashboard instead of regular screens
            if (userRole == UserRole.PARENT && selectedTab == TabItem.REPORT) {
                com.CuriosityLabs.digibalance.ui.parent.ParentDashboardScreen()
            } else {
                when (selectedTab) {
                    TabItem.AWARENESS -> com.CuriosityLabs.digibalance.ui.awareness.AwarenessScreen()
                    TabItem.REPORT -> ReportScreen(userRole)
                    TabItem.FEEDBACK -> HelpScreen(userRole)
                    TabItem.LEADERBOARD -> RankScreen(userRole)
                    TabItem.FOCUS -> FocusScreen(userRole)
                }
            }
            
            // Profile menu modal
            if (showProfileMenu) {
                ProfileMenuModal(
                    userRole = userRole,
                    onDismiss = { showProfileMenu = false },
                    onSettings = {
                        showProfileMenu = false
                        showSettings = true
                    },
                    onAbout = {
                        showProfileMenu = false
                        showAbout = true
                    },
                    onLogout = {
                        showProfileMenu = false
                        onLogout()
                    }
                )
            }
            
            // Settings screen
            if (showSettings) {
                SimpleSettingsScreen(
                    userRole = userRole,
                    onDismiss = { showSettings = false }
                )
            }
            
            // About screen
            if (showAbout) {
                AboutScreen(onDismiss = { showAbout = false })
            }
            
            // TODO-12: Removed PersonalizeScreen
        }
    }
}

// TODO-4: Helper function to extract username from email
fun extractUsernameFromEmail(email: String?): String {
    if (email.isNullOrBlank()) return "User"
    val username = email.substringBefore("@")
    return username.replaceFirstChar { it.uppercase() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    userRole: UserRole,
    onProfileClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { com.CuriosityLabs.digibalance.data.PreferencesManager.getInstance(context) }
    val userEmail = prefs.currentUserEmail
    val userGamertag = prefs.currentUserGamertag
    val displayName = userGamertag ?: extractUsernameFromEmail(userEmail)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF667EEA),
                        Color(0xFF764BA2)
                    )
                )
            )
    ) {
        TopAppBar(
            title = {
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = "Hi, $displayName",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                    Text(
                        text = when (userRole) {
                            UserRole.PARENT -> "Parent Dashboard"
                            UserRole.STUDENT -> "Your Digital Wellness"
                            UserRole.PROFESSIONAL -> "Professional Dashboard"
                        },
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayName.take(1).uppercase(),
                                color = Color(0xFF667EEA),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White
            )
        )
    }
}

@Composable
fun HomeBottomNavigation(
    selectedTab: TabItem,
    onTabSelected: (TabItem) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        NavigationBar(
            containerColor = Color.White,
            contentColor = Color(0xFF1A1A1A),
            tonalElevation = 0.dp,
            modifier = Modifier.height(80.dp)
        ) {
            TabItem.entries.forEach { tab ->
                NavigationBarItem(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .padding(top = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = tab.iconRes),
                                contentDescription = tab.title,
                                modifier = Modifier.fillMaxSize(),
                                alpha = if (selectedTab == tab) 1f else 0.6f
                            )
                        }
                    },
                    label = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF3D5AFE),
                        selectedTextColor = Color(0xFF3D5AFE),
                        unselectedIconColor = Color(0xFF9E9E9E),
                        unselectedTextColor = Color(0xFF9E9E9E),
                        indicatorColor = Color(0xFFE8EAFF)
                    )
                )
            }
        }
    }
}

@Composable
fun ProfileMenuModal(
    userRole: UserRole,
    onDismiss: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    onLogout: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile avatar
                Surface(
                    shape = RoundedCornerShape(40.dp),
                    color = Color(0xFF2196F3),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = when (userRole) {
                                UserRole.PARENT -> "P"
                                UserRole.STUDENT -> "S"
                                UserRole.PROFESSIONAL -> "PR"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = when (userRole) {
                        UserRole.PARENT -> "Parent Account"
                        UserRole.STUDENT -> "Student Account"
                        UserRole.PROFESSIONAL -> "Professional Account"
                    },
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1A1A1A)
                )
            }
        },
        text = {
            // TODO-12: Removed "Personalize" option from profile menu
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                ProfileMenuItem(
                    icon = R.drawable.ic_settings,
                    title = "Settings",
                    onClick = onSettings
                )
                ProfileMenuItem(
                    icon = R.drawable.ic_about,
                    title = "About",
                    onClick = onAbout
                )
                ProfileMenuItem(
                    icon = R.drawable.report,
                    title = "Version 1.0.0",
                    onClick = { },
                    isDisabled = true
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileMenuItem(
                    icon = R.drawable.ic_logout,
                    title = "Logout",
                    onClick = onLogout,
                    isDestructive = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ProfileMenuItem(
    icon: Int,
    title: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    isDisabled: Boolean = false
) {
    Surface(
        onClick = if (!isDisabled) onClick else {{}},
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                alpha = if (isDisabled) 0.5f else 1f
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = when {
                    isDestructive -> Color(0xFFE53935)
                    isDisabled -> Color(0xFF9E9E9E)
                    else -> Color(0xFF1A1A1A)
                }
            )
        }
    }
}


@Composable
fun SettingsScreen(userRole: UserRole, onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { com.CuriosityLabs.digibalance.data.PreferencesManager.getInstance(context) }
    
    val roleString = when (userRole) {
        UserRole.STUDENT -> "Student"
        UserRole.PARENT -> "Parent"
        UserRole.PROFESSIONAL -> "Professional"
    }
    
    // Full-screen settings with premium UI
    com.CuriosityLabs.digibalance.ui.settings.PremiumSettingsScreenModal(
        preferencesManager = prefs,
        userRole = roleString,
        onDismiss = onDismiss
    )
}

// TODO-13: Professional About section without emojis
@Composable
fun AboutScreen(onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "About DigiBalance",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF1A1A1A)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App Description
                Text(
                    text = "DigiBalance is a comprehensive digital wellness platform designed to help individuals and families maintain a healthy relationship with technology.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF424242)
                )
                
                HorizontalDivider()
                
                // Features Section
                Text(
                    text = "Key Features",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AboutFeature("Screen Time Tracking", "Monitor and analyze your daily app usage patterns")
                    AboutFeature("Focus Mode", "Block distractions and maintain deep concentration")
                    AboutFeature("Leaderboards", "Compete with peers and track productivity goals")
                    AboutFeature("Parental Controls", "Comprehensive family safety and monitoring tools")
                    AboutFeature("Digital Wellness Education", "Learn best practices for healthy technology use")
                }
                
                HorizontalDivider()
                
                // Company Info
                Text(
                    text = "Developed by CuriosityLabs",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A)
                )
                
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
                
                Text(
                    text = "© 2024 CuriosityLabs. All rights reserved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Legal Links
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.clickable { /* TODO: Open privacy policy */ }
                    )
                    Text(
                        text = "Terms of Service",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.clickable { /* TODO: Open terms */ }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF2196F3))
            }
        }
    )
}

@Composable
fun AboutFeature(title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bullet point
        Box(
            modifier = Modifier
                .size(6.dp)
                .padding(top = 8.dp)
                .background(Color(0xFF2196F3), shape = CircleShape)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF757575)
            )
        }
    }
}

// TODO-12: PersonalizeScreen removed - functionality moved to Settings
