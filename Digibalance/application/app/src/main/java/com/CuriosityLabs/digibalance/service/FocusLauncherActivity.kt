package com.CuriosityLabs.digibalance.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.CuriosityLabs.digibalance.data.local.AppDatabase
import com.CuriosityLabs.digibalance.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AllowedApp(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable
)

class FocusLauncherActivity : ComponentActivity() {
    
    private lateinit var prefs: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        prefs = getSharedPreferences("digibalance_prefs", Context.MODE_PRIVATE)
        
        // HIDE FROM RECENT APPS - user can't clear from recents
        setTaskDescription(android.app.ActivityManager.TaskDescription.Builder()
            .setLabel("Focus Mode")
            .build())
        
        // Request battery optimization exemption
        requestBatteryOptimizationExemption()
        
        // NO LOCK TASK MODE - causes severe lag
        // NO APP PINNING - causes severe lag
        // Instead rely on aggressive monitoring service
        
        // Start the hardcore focus monitoring service
        startFocusMonitoringService()
        
        // Enable Do Not Disturb
        enableDoNotDisturb()
        
        setContent {
            MyApplicationTheme {
                FocusLauncherScreen(
                    onExitFocusMode = { exitFocusMode() }
                )
            }
        }
    }
    
    private fun startFocusMonitoringService() {
        val serviceIntent = Intent(this, FocusMonitoringService::class.java)
        startForegroundService(serviceIntent)
    }
    
    override fun onResume() {
        super.onResume()
        // Check if focus mode is still active
        val isActive = prefs.getBoolean("focusModeActive", false)
        val endTime = prefs.getLong("focusModeEndTime", 0L)
        
        if (!isActive || System.currentTimeMillis() > endTime) {
            exitFocusMode()
        }
        // Monitoring service handles bringing us back - no need for aggressive resume
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Don't stop the monitoring service - it should continue running
        android.util.Log.d("FocusLauncher", "Activity destroyed but monitoring service continues")
    }
    
    private fun enableDoNotDisturb() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        }
    }
    
    private fun requestBatteryOptimizationExemption() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            val packageName = packageName
            
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = android.net.Uri.parse("package:$packageName")
                startActivity(intent)
                android.util.Log.d("FocusLauncher", "Requesting battery optimization exemption")
            } else {
                android.util.Log.d("FocusLauncher", "Already exempt from battery optimization")
            }
        } catch (e: Exception) {
            android.util.Log.e("FocusLauncher", "Failed to request battery exemption", e)
        }
    }
    
    private fun exitFocusMode() {
        // Stop the monitoring service
        val serviceIntent = Intent(this, FocusMonitoringService::class.java)
        stopService(serviceIntent)
        
        // Disable DND
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
        
        // Clear focus mode flag
        prefs.edit().putBoolean("focusModeActive", false).apply()
        
        // Return to main app
        finish()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent back button from exiting focus mode
        // Do nothing - completely block back button
    }
    
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // User is leaving - monitoring service will immediately check and bring them back if needed
        android.util.Log.d("FocusLauncher", "User leaving - monitoring service will enforce")
    }
    
    override fun onPause() {
        super.onPause()
        // Activity paused - monitoring service continues to enforce
        android.util.Log.d("FocusLauncher", "Activity paused - monitoring continues")
    }
    
    override fun onStop() {
        super.onStop()
        // Activity stopped - monitoring service will bring it back if user goes to wrong app
        android.util.Log.d("FocusLauncher", "Activity stopped - monitoring will restore if needed")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusLauncherScreen(
    onExitFocusMode: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var allowedApps by remember { mutableStateOf<List<AllowedApp>>(emptyList()) }
    var showExitDialog by remember { mutableStateOf(false) }
    var emergencyCode by remember { mutableStateOf("") }
    var timerEndTime by remember { mutableStateOf(0L) }
    var isLinkedToParent by remember { mutableStateOf<Boolean?>(null) }
    
    LaunchedEffect(Unit) {
        scope.launch {
            allowedApps = loadAllowedApps(context)
            timerEndTime = context.getSharedPreferences("digibalance_prefs", Context.MODE_PRIVATE)
                .getLong("focusModeEndTime", 0L)
            
            // Check if student is linked to parent
            val userRepository = com.CuriosityLabs.digibalance.data.repository.UserRepository()
            val result = userRepository.getCurrentUserProfile()
            result.onSuccess { user ->
                isLinkedToParent = user.linked_parent_id != null
                android.util.Log.d("FocusLauncher", "Student linked to parent: $isLinkedToParent")
            }.onFailure {
                isLinkedToParent = false
                android.util.Log.e("FocusLauncher", "Failed to check parent link status")
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎯 Focus Mode Active", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = { 
                        // Check if student is linked to parent
                        if (isLinkedToParent == true) {
                            // Linked to parent - require emergency code
                            showExitDialog = true
                        } else {
                            // Not linked to parent - allow free exit
                            onExitFocusMode()
                        }
                    }) {
                        Text("Exit", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EA),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE8EAF6),
                            Color(0xFFF3E5F5),
                            Color(0xFFE1F5FE)
                        )
                    )
                )
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Timer display card
            if (timerEndTime > System.currentTimeMillis()) {
                val remainingTime = (timerEndTime - System.currentTimeMillis()) / 1000 / 60
                androidx.compose.material3.Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = Color(0xFF6200EA).copy(alpha = 0.1f)
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏱️",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "$remainingTime minutes remaining",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6200EA)
                            )
                            Text(
                                text = "Stay focused!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }
            }
            
            Text(
                text = "📱 Allowed Apps",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp),
                color = Color(0xFF1A1A1A)
            )
            
            if (allowedApps.isEmpty()) {
                androidx.compose.material3.Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "🔒",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Text(
                            text = "No Apps Selected",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "You haven't selected any apps for Focus Mode. Exit and configure apps before starting.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(allowedApps) { app ->
                        AppIcon(
                            app = app,
                            onClick = { launchApp(context, app.packageName) }
                        )
                    }
                }
            }
        }
        }
    }
    
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Focus Mode") },
            text = {
                Column {
                    Text("Enter emergency code to exit:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = emergencyCode,
                        onValueChange = { emergencyCode = it },
                        label = { Text("Emergency Code") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val isValid = verifyEmergencyCode(context, emergencyCode)
                            if (isValid) {
                                onExitFocusMode()
                            } else {
                                // Show error
                                android.widget.Toast.makeText(
                                    context,
                                    "Invalid emergency code",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                ) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AppIcon(
    app: AllowedApp,
    onClick: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            val bitmap = app.icon.toBitmap()
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = app.appName,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = app.appName,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A)
            )
        }
    }
}

private suspend fun loadAllowedApps(context: Context): List<AllowedApp> {
    return withContext(Dispatchers.IO) {
        // Get selected apps from preferences
        val prefs = context.getSharedPreferences("digibalance_prefs", Context.MODE_PRIVATE)
        val selectedAppsSet = prefs.getStringSet("focus_mode_selected_apps", emptySet()) ?: emptySet()
        
        val allowedPackages = if (selectedAppsSet.isNotEmpty()) {
            selectedAppsSet.toList()
        } else {
            // Default apps if none selected
            listOf(
                "com.android.dialer",
                "com.android.calculator2",
                "com.android.camera2",
                "com.CuriosityLabs.digibalance"
            )
        }
        
        android.util.Log.d("FocusLauncher", "Loading ${allowedPackages.size} allowed apps: $allowedPackages")
        
        val pm = context.packageManager
        allowedPackages.mapNotNull { packageName ->
            try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                AllowedApp(
                    packageName = packageName,
                    appName = pm.getApplicationLabel(appInfo).toString(),
                    icon = pm.getApplicationIcon(appInfo)
                )
            } catch (e: Exception) {
                android.util.Log.e("FocusLauncher", "Failed to load app: $packageName", e)
                null
            }
        }
    }
}

private fun launchApp(context: Context, packageName: String) {
    try {
        android.util.Log.d("FocusLauncher", "Launching app: $packageName")
        
        // Get launch intent
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        
        if (intent != null) {
            // Add flags to ensure app launches properly
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            
            // Launch the app
            context.startActivity(intent)
            android.util.Log.d("FocusLauncher", "App launched successfully: $packageName")
        } else {
            android.util.Log.e("FocusLauncher", "No launch intent found for: $packageName")
            android.widget.Toast.makeText(
                context,
                "Cannot launch this app",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    } catch (e: Exception) {
        android.util.Log.e("FocusLauncher", "Failed to launch app: $packageName", e)
        android.widget.Toast.makeText(
            context,
            "Error launching app: ${e.message}",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}

private suspend fun verifyEmergencyCode(context: Context, code: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            // Get current user ID
            val userRepository = com.CuriosityLabs.digibalance.data.repository.UserRepository()
            val userId = userRepository.getCurrentUserId()
            
            if (userId == null) {
                android.util.Log.e("FocusLauncher", "No user ID found")
                return@withContext false
            }
            
            // Verify code using new emergency code system
            val emergencyRepo = com.CuriosityLabs.digibalance.data.repository.EmergencyCodeRepository()
            val result = emergencyRepo.verifyEmergencyCode(code, userId)
            
            result.getOrElse { 
                android.util.Log.e("FocusLauncher", "Error verifying emergency code", it)
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("FocusLauncher", "Error verifying code", e)
            false
        }
    }
}
