package com.CuriosityLabs.digibalance.ui.home

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.CuriosityLabs.digibalance.R
import com.CuriosityLabs.digibalance.service.FocusLauncherActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO-16: Data class for app information
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(userRole: UserRole) {
    val context = LocalContext.current
    val prefs = remember { com.CuriosityLabs.digibalance.data.PreferencesManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    var showBottomSheet by remember { mutableStateOf(false) }
    var showAppSelection by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // TODO-16: App selection state
    var selectedApps by remember { mutableStateOf<Set<String>>(emptySet()) }
    var availableApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoadingApps by remember { mutableStateOf(false) }
    var isParentLinked by remember { mutableStateOf(false) }
    
    // Load installed apps and check parent linking
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            // Check if parent is linked
            isParentLinked = prefs.parentLinkedStudentId != null
            
            // Load previously selected apps
            val savedApps = prefs.focusModeSelectedApps
            withContext(Dispatchers.Main) {
                selectedApps = savedApps
            }
            
            // Load installed apps
            isLoadingApps = true
            val packageManager = context.packageManager
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 } // Only user apps
                .map { appInfo ->
                    AppInfo(
                        packageName = appInfo.packageName,
                        appName = packageManager.getApplicationLabel(appInfo).toString(),
                        icon = try { packageManager.getApplicationIcon(appInfo) } catch (e: Exception) { null }
                    )
                }
                .sortedBy { it.appName }
            
            withContext(Dispatchers.Main) {
                availableApps = installedApps
                isLoadingApps = false
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize().background(
        androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(
                Color(0xFFEBF4FF), // Light blue tint
                Color(0xFFFFFFFF), // Pure white
                Color(0xFFF0F9FF), // Very light blue
                Color(0xFFFEF7FF)  // Very light purple
            )
        )
    )) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp), // Space for sticky button
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
        // Header Section
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Focus Mode",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Block distractions and boost productivity",
                        fontSize = 15.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
        
        // Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Enter Deep Focus",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Block distracting apps and stay focused on what matters. Your focus session will continue even if you restart your phone.",
                        fontSize = 15.sp,
                        color = Color(0xFF757575),
                        lineHeight = 22.sp
                    )
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(24.dp)) }
        
        // Features Section
        item {
            Text(
                text = "Features",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        item {
            ModernFeatureCard(
                title = "Blocks Distractions",
                description = "Only allowed apps will be accessible",
                color = Color(0xFF3D5AFE)
            )
        }
        
        item {
            ModernFeatureCard(
                title = "Silences Notifications",
                description = "Do Not Disturb mode automatically enabled",
                color = Color(0xFF4CAF50)
            )
        }
        
        item {
            ModernFeatureCard(
                title = "Time-Based Sessions",
                description = "Set duration and stay focused until timer ends",
                color = Color(0xFFFF9800)
            )
        }
        
        item {
            ModernFeatureCard(
                title = "Unbreakable Lock",
                description = "Survives phone restarts and app switches",
                color = Color(0xFFFF5252)
            )
        }
        
        // TODO-16: App Selection Card
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Selected Apps:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        }
        
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAppSelection = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = if (selectedApps.isEmpty()) "Select Apps" else "${selectedApps.size} Apps Selected",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (selectedApps.isEmpty()) 
                                    "Choose which apps to allow during focus" 
                                else 
                                    "Tap to change selection",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                    Icon(
                        imageVector = if (selectedApps.isEmpty()) Icons.Default.Apps else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (selectedApps.isEmpty()) Color(0xFF757575) else Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        }
        
        // Sticky Start Button at bottom
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Button(
                onClick = { showBottomSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🎯",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Start Focus Mode",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
    
    if (showBottomSheet) {
        FocusModeBottomSheet(
            onDismiss = { showBottomSheet = false },
            onStart = { duration, apps, notificationControl, preventSwitching ->
                // Save selected apps before starting
                prefs.focusModeSelectedApps = selectedApps
                startFocusMode(context, duration)
                showBottomSheet = false
            },
            selectedAppsCount = selectedApps.size
        )
    }
    
    // TODO-16: App Selection Modal
    if (showAppSelection) {
        AppSelectionModal(
            availableApps = availableApps,
            selectedApps = selectedApps,
            isLoading = isLoadingApps,
            isParentLinked = isParentLinked,
            onDismiss = { showAppSelection = false },
            onConfirm = { newSelection ->
                selectedApps = newSelection
                prefs.focusModeSelectedApps = newSelection
                showAppSelection = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeBottomSheet(
    onDismiss: () -> Unit,
    onStart: (duration: Int, apps: List<String>, notificationControl: Boolean, preventSwitching: Boolean) -> Unit,
    selectedAppsCount: Int = 0
) {
    var focusDuration by remember { mutableStateOf(30) }
    var notificationControl by remember { mutableStateOf(true) }
    var preventSwitching by remember { mutableStateOf(true) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header
            Text(
                text = "Configure Focus Mode",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Duration Selection
            Text(
                text = "Focus Duration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(15, 30, 60, 120).forEach { duration ->
                    FilterChip(
                        selected = focusDuration == duration,
                        onClick = { focusDuration = duration },
                        label = { 
                            Text(
                                "${duration}m",
                                fontWeight = if (focusDuration == duration) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // TODO-8: Fixed toggle visibility with proper colors
            // Notification Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Silence Notifications", fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                    Text("Enable Do Not Disturb", style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575))
                }
                Switch(
                    checked = notificationControl,
                    onCheckedChange = { notificationControl = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4CAF50),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFBDBDBD),
                        uncheckedBorderColor = Color(0xFF9E9E9E)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Prevent App Switching
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Prevent App Switching", fontWeight = FontWeight.SemiBold, color = Color(0xFF1A1A1A))
                    Text("Block access to other apps", style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575))
                }
                Switch(
                    checked = preventSwitching,
                    onCheckedChange = { preventSwitching = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF2196F3),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFBDBDBD),
                        uncheckedBorderColor = Color(0xFF9E9E9E)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // TODO-16: App selection warning
            if (selectedAppsCount == 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Please select apps before starting Focus Mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF424242)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$selectedAppsCount apps will be allowed during Focus Mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF424242)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFF5252),
                        containerColor = Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF5252))
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                }
                
                Button(
                    onClick = { onStart(focusDuration, emptyList(), notificationControl, preventSwitching) },
                    enabled = selectedAppsCount > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedAppsCount > 0) Color(0xFF4CAF50) else Color(0xFFBDBDBD),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFBDBDBD),
                        disabledContentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Start", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ModernFeatureCard(
    title: String,
    description: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// TODO-16: App Selection Modal
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionModal(
    availableApps: List<AppInfo>,
    selectedApps: Set<String>,
    isLoading: Boolean,
    isParentLinked: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    var currentSelection by remember { mutableStateOf(selectedApps) }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredApps = remember(availableApps, searchQuery) {
        if (searchQuery.isBlank()) {
            availableApps
        } else {
            availableApps.filter { 
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Apps for Focus Mode",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF757575)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Info text
            Text(
                text = if (isParentLinked) 
                    "Only parent-approved apps are available" 
                else 
                    "Select apps you want to access during Focus Mode",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF757575)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search apps...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF757575)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Selection count
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentSelection.isEmpty()) 
                        Color(0xFFFFF3E0) 
                    else 
                        Color(0xFFE3F2FD)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (currentSelection.isEmpty()) Icons.Default.Apps else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (currentSelection.isEmpty()) Color(0xFFFF9800) else Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentSelection.isEmpty()) 
                            "No apps selected" 
                        else 
                            "${currentSelection.size} apps selected",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A1A1A)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // App list
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF2196F3))
                }
            } else if (filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "No apps available" else "No apps found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF757575)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps) { app ->
                        AppSelectionItem(
                            app = app,
                            isSelected = currentSelection.contains(app.packageName),
                            onToggle = {
                                currentSelection = if (currentSelection.contains(app.packageName)) {
                                    currentSelection - app.packageName
                                } else {
                                    currentSelection + app.packageName
                                }
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = { onConfirm(currentSelection) },
                    enabled = currentSelection.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentSelection.isNotEmpty()) Color(0xFF2196F3) else Color(0xFFBDBDBD)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun AppSelectionItem(
    app: AppInfo,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF2196F3)) 
        else 
            null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // App icon
                app.icon?.let { drawable ->
                    val bitmap = try {
                        drawable.toBitmap()
                    } catch (e: Exception) {
                        null
                    }
                    
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = app.appName,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                tint = Color(0xFF757575),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                } ?: Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A1A1A),
                        maxLines = 1
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575),
                        maxLines = 1
                    )
                }
            }
            
            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF2196F3),
                    uncheckedColor = Color(0xFF757575)
                )
            )
        }
    }
}

private fun startFocusMode(context: Context, durationMinutes: Int) {
    val prefs = context.getSharedPreferences("digibalance_prefs", Context.MODE_PRIVATE)
    val endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000)
    
    prefs.edit()
        .putBoolean("focusModeActive", true)
        .putLong("focusModeEndTime", endTime)
        .apply()
    
    val intent = Intent(context, FocusLauncherActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}
