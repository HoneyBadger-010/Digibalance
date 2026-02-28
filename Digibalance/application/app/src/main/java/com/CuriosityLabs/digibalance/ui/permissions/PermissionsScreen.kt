package com.CuriosityLabs.digibalance.ui.permissions

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CuriosityLabs.digibalance.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun PermissionsScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableIntStateOf(0) }
    
    // TODO-15: Fixed skip button to skip current permission, not all
    when (currentScreen) {
        0 -> PermissionScreen1(
            onNext = { currentScreen = 1 },
            onSkip = { currentScreen = 1 } // Skip to next permission
        )
        1 -> PermissionScreen2(
            onNext = { currentScreen = 2 },
            onBack = { currentScreen = 0 },
            onSkip = { currentScreen = 2 } // Skip to next permission
        )
        2 -> PermissionScreen2b(
            onNext = { currentScreen = 3 },
            onBack = { currentScreen = 1 },
            onSkip = { currentScreen = 3 } // Skip to next permission
        )
        3 -> PermissionScreen3(
            onNext = { currentScreen = 4 },
            onBack = { currentScreen = 2 },
            onSkip = { currentScreen = 4 } // Skip to next permission
        )
        4 -> PermissionScreen4(
            onComplete = onComplete,
            onBack = { currentScreen = 3 },
            context = context
        )
    }
}

@Composable
fun PermissionScreen1(
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    
    // Check permission status periodically
    LaunchedEffect(Unit) {
        while (true) {
            hasPermission = PermissionChecker.hasUsageStatsPermission(context)
            delay(500)
        }
    }
    
    PermissionScreenTemplate(
        screenNumber = 1,
        totalScreens = 5,
        headline = "Track Your Digital Habits",
        bodyText = "To help you understand your phone usage, we need access to see which apps you use and for how long.\n\nThis helps us create personalized insights and show you where your time goes each day.",
        icon = Icons.Default.Visibility,
        iconColor = DigiBalanceCyan,
        permissionName = "Usage Stats Access",
        permissionDescription = "This allows us to track your app usage and create usage reports.",
        buttonText = "Grant Usage Access",
        onButtonClick = {
            try {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback if settings unavailable
            }
        },
        onNext = onNext,
        onSkip = onSkip,
        hasPermission = hasPermission
    )
}

@Composable
fun PermissionScreen2(
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    var hasAccessibility by remember { mutableStateOf(false) }
    
    // Check permission periodically
    LaunchedEffect(Unit) {
        while (true) {
            hasAccessibility = PermissionChecker.hasAccessibilityPermission(
                context,
                "com.CuriosityLabs.digibalance"
            )
            delay(500)
        }
    }
    
    PermissionScreenTemplate(
        screenNumber = 2,
        totalScreens = 5,
        headline = "Enable Accessibility Service",
        bodyText = "We need to see which app you're currently using so we can help you stay focused.\n\nThe Accessibility Service allows us to monitor app usage and show you helpful reminders when you open distracting apps.",
        icon = Icons.Default.Notifications,
        iconColor = DigiBalanceOrange,
        permissionName = "Accessibility Service",
        permissionDescription = "This allows us to monitor which app you're using and help you stay focused.",
        buttonText = "Enable Accessibility",
        onButtonClick = {
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback
            }
        },
        onNext = onNext,
        onBack = onBack,
        onSkip = onSkip,
        hasPermission = hasAccessibility
    )
}

@Composable
fun PermissionScreen2b(
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    var hasOverlay by remember { mutableStateOf(false) }
    
    // Check permission periodically
    LaunchedEffect(Unit) {
        while (true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hasOverlay = PermissionChecker.hasOverlayPermission(context)
            } else {
                hasOverlay = true // Not required on older versions
            }
            delay(500)
        }
    }
    
    PermissionScreenTemplate(
        screenNumber = 3,
        totalScreens = 5,
        headline = "Allow Draw Over Apps",
        bodyText = "We'll show helpful alerts and reminders on your screen to keep you focused.\n\nThis permission allows us to display gentle reminders on top of other apps when you open distracting apps.",
        icon = Icons.Default.Dashboard,
        iconColor = DigiBalanceTeal,
        permissionName = "Draw Over Apps",
        permissionDescription = "This allows us to show helpful alerts on top of other apps.",
        buttonText = "Allow Overlay",
        onButtonClick = {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    context.startActivity(intent)
                }
            } catch (e: Exception) {
                // Fallback
            }
        },
        onNext = onNext,
        onBack = onBack,
        onSkip = onSkip,
        hasPermission = hasOverlay
    )
}

@Composable
fun PermissionScreen3(
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    var hasDND by remember { mutableStateOf(false) }
    var hasBoot by remember { mutableStateOf(false) }
    
    // Check permissions periodically
    LaunchedEffect(Unit) {
        while (true) {
            hasDND = PermissionChecker.hasNotificationPolicyAccess(context)
            hasBoot = PermissionChecker.hasBootPermission()
            delay(500)
        }
    }
    
    val hasPermission = hasDND && hasBoot
    
    PermissionScreenTemplate(
        screenNumber = 4,
        totalScreens = 5,
        headline = "Enable Focus Mode",
        bodyText = "Focus Mode helps you stay distraction-free by limiting interruptions during your focused time.\n\nWe need permission to silence notifications (Do Not Disturb) and to keep your focus settings active even after your phone restarts.",
        icon = Icons.Default.Lock,
        iconColor = DigiBalancePurple,
        permissionName = "Do Not Disturb + Boot Persistence",
        permissionDescription = "This allows us to silence notifications during focus time and maintain your settings after restart.",
        buttonText = "Enable Focus Mode",
        onButtonClick = {
            try {
                // Open DND settings
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback
            }
        },
        onNext = onNext,
        onBack = onBack,
        onSkip = onSkip,
        hasPermission = hasPermission,
        showSecondPermission = true,
        secondPermissionName = "Boot Persistence",
        secondPermissionDescription = "This ensures your focus settings remain active even after your device restarts.",
        hasSecondPermission = hasBoot,
        onSecondPermissionClick = {
            // Boot permission is typically granted automatically
            // or through special intent - this is informational
        }
    )
}

@Composable
fun PermissionScreen4(
    onComplete: () -> Unit,
    onBack: () -> Unit,
    context: android.content.Context
) {
    var hasPermission by remember { mutableStateOf(false) }
    
    // Check permission periodically
    LaunchedEffect(Unit) {
        while (true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hasPermission = PermissionChecker.isBatteryOptimizationDisabled(context)
            } else {
                hasPermission = true // Not required on older versions
            }
            delay(500)
        }
    }
    
    PermissionScreenTemplate(
        screenNumber = 5,
        totalScreens = 5,
        headline = "Keep DigiBalance Running",
        bodyText = "To ensure DigiBalance works reliably, we need permission to run in the background.\n\nAndroid may limit background apps to save battery, but we need to stay active to monitor your usage and enforce your focus settings. Don't worry - we're designed to be battery-efficient!",
        icon = Icons.Default.Battery4Bar,
        iconColor = DigiBalanceGreen,
        permissionName = "Disable Battery Optimization",
        permissionDescription = "This ensures DigiBalance can monitor your usage and enforce your settings reliably.",
        buttonText = "Allow Background Access",
        onButtonClick = {
            try {
                // Open battery optimization settings
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                context.startActivity(intent)
            } catch (e: Exception) {
                try {
                    // Alternative for some devices
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                } catch (e2: Exception) {
                    // Final fallback
                }
            }
        },
        onNext = onComplete,
        onBack = onBack,
        isLastScreen = true,
        hasPermission = hasPermission
    )
}

@Composable
fun PermissionScreenTemplate(
    screenNumber: Int,
    totalScreens: Int,
    headline: String,
    bodyText: String,
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    permissionName: String,
    permissionDescription: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    onNext: () -> Unit,
    onBack: (() -> Unit)? = null,
    onSkip: (() -> Unit)? = null,
    hasPermission: Boolean = false,
    showSecondPermission: Boolean = false,
    secondPermissionName: String = "",
    secondPermissionDescription: String = "",
    hasSecondPermission: Boolean = false,
    onSecondPermissionClick: (() -> Unit)? = null,
    isLastScreen: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Fixed header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Progress dots
                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(totalScreens) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .height(4.dp)
                                    .width(if (index == screenNumber - 1) 32.dp else 16.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (index == screenNumber - 1) 
                                            Color(0xFF3D5AFE) 
                                        else 
                                            Color(0xFFBDBDBD)
                                    )
                            )
                        }
                    }
                    
                    // Skip button (only on first 3 screens)
                    if (onSkip != null && !isLastScreen) {
                        TextButton(onClick = onSkip) {
                            Text(
                                text = "Skip",
                                color = Color(0xFF757575),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Scrollable content area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Headline
                Text(
                    text = headline,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Body text
                Text(
                    text = bodyText,
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Permission card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = permissionName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1A1A),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            // Permission status indicator
                            if (hasPermission) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Granted",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Granted",
                                        fontSize = 11.sp,
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        Text(
                            text = permissionDescription,
                            fontSize = 13.sp,
                            color = Color(0xFF757575),
                            lineHeight = 18.sp
                        )

                        // Second permission if needed
                        if (showSecondPermission && secondPermissionName.isNotEmpty()) {
                            Divider(
                                color = DigiBalanceWhite.copy(alpha = 0.2f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = secondPermissionName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DigiBalanceWhite,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                // Second permission status indicator
                                if (hasSecondPermission) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Granted",
                                            tint = DigiBalanceGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Granted",
                                            fontSize = 11.sp,
                                            color = DigiBalanceGreen,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            
                            Text(
                                text = secondPermissionDescription,
                                fontSize = 13.sp,
                                color = DigiBalanceWhite.copy(alpha = 0.7f),
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Fixed footer with navigation buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, top = 16.dp)
            ) {
                // Permission button
                Button(
                    onClick = onButtonClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = iconColor
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                // Next/Continue button (only enabled when permission is granted)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (onBack != null) 
                        Arrangement.SpaceBetween 
                    else 
                        Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        // Previous button
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier
                                .height(52.dp)
                                .weight(1f)
                                .padding(end = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF757575)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Back",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Next button
                    Button(
                        onClick = onNext,
                        enabled = hasPermission,
                        modifier = Modifier
                            .height(52.dp)
                            .then(if (onBack == null) Modifier.fillMaxWidth() else Modifier.weight(1f).padding(start = 8.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3D5AFE),
                            disabledContainerColor = Color(0xFFBDBDBD)
                        )
                    ) {
                        if (!hasPermission && !isLastScreen) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Grant to Continue",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = if (isLastScreen) "Complete" else "Next",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (!isLastScreen) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

