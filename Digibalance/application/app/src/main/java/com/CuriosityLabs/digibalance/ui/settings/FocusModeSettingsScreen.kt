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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeSettingsScreen(
    navController: NavController,
    userRole: String
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
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (userRole == "Professional") {
                ExtendedFloatingActionButton(
                    onClick = { /* Start Focus Mode */ },
                    icon = { Icon(Icons.Default.PlayArrow, "Start") },
                    text = { Text("Start Focus Mode") },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }
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
            
            // Allowed Apps
            item {
                PreferenceSectionCard(
                    title = if (userRole == "Parent") "Student's Allowed Apps" else "Allowed Apps",
                    icon = Icons.Default.Apps,
                    description = if (userRole == "Parent") 
                        "Apps your student can use during Focus Mode"
                    else 
                        "Apps you can access during Focus Mode"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        allowedApps.forEach { app ->
                            AllowedAppItem(
                                appName = app,
                                onRemove = { allowedApps = allowedApps - app }
                            )
                        }
                        
                        OutlinedButton(
                            onClick = { /* Add app */ },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Add App")
                        }
                    }
                }
            }
            
            // Emergency Exit Code
            if (userRole != "Student") {
                item {
                    PreferenceSectionCard(
                        title = "Emergency Exit Code",
                        icon = Icons.Default.Lock,
                        description = if (userRole == "Parent")
                            "Set a code to remotely end Focus Mode"
                        else
                            "Set a code to exit Focus Mode early"
                    ) {
                        EmergencyCodeSection(
                            isSet = emergencyCodeSet,
                            onClick = { showEmergencyCodeDialog = true }
                        )
                    }
                }
            }
            
            // Auto-start Schedule
            if (userRole == "Professional") {
                item {
                    PreferenceSectionCard(
                        title = "Auto-Start Schedule",
                        icon = Icons.Default.Schedule,
                        description = "Automatically start Focus Mode at specific times"
                    ) {
                        SwitchPreference(
                            title = "Enable Auto-Start",
                            description = "Focus Mode will start automatically",
                            checked = autoStartEnabled,
                            onCheckedChange = { autoStartEnabled = it }
                        )
                        
                        AnimatedVisibility(visible = autoStartEnabled) {
                            Column(
                                modifier = Modifier.padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ScheduleItem(time = "9:00 AM", days = "Mon, Wed, Fri")
                                ScheduleItem(time = "2:00 PM", days = "Tue, Thu")
                                
                                OutlinedButton(
                                    onClick = { /* Add schedule */ },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Add Schedule")
                                }
                            }
                        }
                    }
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

@Composable
fun FocusModeStatusCard(isActive: Boolean, remainingTime: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isActive) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4CAF50).copy(alpha = 0.8f),
                                Color(0xFF8BC34A).copy(alpha = 0.6f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) Color.White.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.RemoveCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (isActive) Color.White else MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = if (isActive) "Focus Mode Active" else "Focus Mode Inactive",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface
                )
                
                if (isActive) {
                    Text(
                        text = "$remainingTime minutes remaining",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                } else {
                    Text(
                        text = "Start a session to boost your productivity",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}


@Composable
fun DurationSelector(
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit
) {
    val durations = listOf(15, 30, 45, 60, 90, 120)
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            durations.take(3).forEach { duration ->
                DurationChip(
                    duration = duration,
                    isSelected = selectedDuration == duration,
                    onClick = { onDurationSelected(duration) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            durations.drop(3).forEach { duration ->
                DurationChip(
                    duration = duration,
                    isSelected = selectedDuration == duration,
                    onClick = { onDurationSelected(duration) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Custom duration
        OutlinedButton(
            onClick = { /* Show custom duration dialog */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Custom Duration")
        }
    }
}

@Composable
fun DurationChip(
    duration: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleAnim by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Card(
        modifier = modifier
            .height(56.dp)
            .scale(scaleAnim)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$duration min",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimary 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AllowedAppItem(
    appName: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = appName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmergencyCodeSection(
    isSet: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSet) 
                Color(0xFF4CAF50).copy(alpha = 0.1f) 
            else 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isSet) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isSet) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = if (isSet) "Code Set" else "No Code Set",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isSet) "Tap to change" else "Tap to set up",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun ScheduleItem(time: String, days: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = days,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { /* Edit */ }) {
                    Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { /* Delete */ }) {
                    Icon(
                        Icons.Default.Delete, 
                        "Delete", 
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun FocusModeTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Pro Tips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            TipItem("Start with shorter sessions (15-30 min) and gradually increase")
            TipItem("Keep only essential apps in your allowed list")
            TipItem("Use Focus Mode during your most productive hours")
            TipItem("Set an emergency code you won't forget")
        }
    }
}

@Composable
fun TipItem(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .offset(y = 8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
fun EmergencyCodeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var confirmCode by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Set Emergency Code",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Create a 6-digit code to exit Focus Mode in emergencies",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                OutlinedTextField(
                    value = code,
                    onValueChange = { 
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            code = it
                            error = null
                        }
                    },
                    label = { Text("Enter Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = confirmCode,
                    onValueChange = { 
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            confirmCode = it
                            error = null
                        }
                    },
                    label = { Text("Confirm Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null
                )
                
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        code.length != 6 -> error = "Code must be 6 digits"
                        code != confirmCode -> error = "Codes don't match"
                        else -> onConfirm(code)
                    }
                }
            ) {
                Text("Set Code")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
