package com.CuriosityLabs.digibalance.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.CuriosityLabs.digibalance.R
import kotlinx.coroutines.*

class FocusMonitoringService : Service() {
    
    private lateinit var prefs: SharedPreferences
    private var monitoringJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "focus_monitoring_channel"
        private const val CHECK_INTERVAL = 500L // Check every 0.5 seconds - ultra-strict enforcement
    }
    
    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("digibalance_prefs", Context.MODE_PRIVATE)
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startMonitoring()
        
        // Request battery optimization exemption
        requestBatteryExemption()
        
        return START_STICKY // Restart if killed by system
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // User cleared app from RAM - relaunch Focus Mode immediately
        android.util.Log.d("FocusMonitor", "Task removed - relaunching Focus Mode")
        
        val isActive = prefs.getBoolean("focusModeActive", false)
        val endTime = prefs.getLong("focusModeEndTime", 0L)
        
        if (isActive && System.currentTimeMillis() < endTime) {
            // Focus Mode should still be active - relaunch it
            restartFocusLauncher()
            
            // Restart this service too
            val restartIntent = Intent(applicationContext, FocusMonitoringService::class.java)
            startForegroundService(restartIntent)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun startMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            var lastLauncherCheck = 0L
            
            while (isActive) {
                try {
                    val isActive = prefs.getBoolean("focusModeActive", false)
                    val endTime = prefs.getLong("focusModeEndTime", 0L)
                    val currentTime = System.currentTimeMillis()
                    
                    if (isActive && currentTime < endTime) {
                        // HARDCORE MODE: Focus mode should be active - check current app
                        val currentApp = getCurrentForegroundApp()
                        val allowedApps = getAllowedApps()
                        
                        android.util.Log.d("FocusMonitor", "Current app: $currentApp, Allowed: ${allowedApps.size}")
                        
                        // STRICT ENFORCEMENT: Only allow Focus Mode launcher OR explicitly allowed apps
                        // Block home screen, settings, everything else
                        val isFocusLauncher = currentApp == "com.CuriosityLabs.digibalance"
                        val isSystemUI = currentApp == "com.android.systemui" // Allow notification shade briefly
                        val isAllowedApp = allowedApps.contains(currentApp)
                        
                        if (currentApp != null && !isFocusLauncher && !isSystemUI && !isAllowedApp) {
                            // User is in non-allowed app (including home screen) - bring back Focus Mode immediately
                            android.util.Log.d("FocusMonitor", "BLOCKED: $currentApp - bringing back Focus Mode")
                            restartFocusLauncher()
                            lastLauncherCheck = currentTime
                        }
                        
                        // WATCHDOG: Every 5 seconds, verify Focus Mode launcher is still running
                        if (currentTime - lastLauncherCheck > 5000) {
                            if (!isFocusLauncher && !isAllowedApp) {
                                android.util.Log.d("FocusMonitor", "WATCHDOG: Focus Mode not visible - relaunching")
                                restartFocusLauncher()
                            }
                            lastLauncherCheck = currentTime
                        }
                    } else if (isActive && currentTime >= endTime) {
                        // Timer expired - end focus mode
                        android.util.Log.d("FocusMonitor", "Focus mode timer expired")
                        endFocusMode()
                        stopSelf()
                        return@launch
                    } else {
                        // Focus mode not active - stop monitoring
                        android.util.Log.d("FocusMonitor", "Focus mode not active - stopping service")
                        stopSelf()
                        return@launch
                    }
                    
                    delay(CHECK_INTERVAL)
                } catch (e: Exception) {
                    android.util.Log.e("FocusMonitor", "Error in monitoring loop", e)
                    delay(CHECK_INTERVAL)
                }
            }
        }
    }
    
    private fun getCurrentForegroundApp(): String? {
        return try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningApps = activityManager.runningAppProcesses
            
            // Get the app with highest importance (foreground)
            runningApps?.firstOrNull { 
                it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND 
            }?.processName
        } catch (e: Exception) {
            android.util.Log.e("FocusMonitor", "Error getting foreground app", e)
            null
        }
    }
    
    private fun getAllowedApps(): Set<String> {
        val selectedApps = prefs.getStringSet("focus_mode_selected_apps", emptySet()) ?: emptySet()
        
        // Always include DigiBalance itself
        return selectedApps + "com.CuriosityLabs.digibalance"
    }
    
    private fun restartFocusLauncher() {
        val intent = Intent(this, FocusLauncherActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or 
                     Intent.FLAG_ACTIVITY_CLEAR_TOP or
                     Intent.FLAG_ACTIVITY_SINGLE_TOP or
                     Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        startActivity(intent)
    }
    
    private fun endFocusMode() {
        // Disable DND
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
        
        // Clear focus mode flag
        prefs.edit().putBoolean("focusModeActive", false).apply()
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Focus Mode Monitoring",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Monitors Focus Mode to prevent bypassing"
            setShowBadge(false)
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun createNotification(): Notification {
        val remainingTime = prefs.getLong("focusModeEndTime", 0L) - System.currentTimeMillis()
        val minutes = (remainingTime / 60000).coerceAtLeast(0)
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus Mode Active")
            .setContentText("$minutes minutes remaining")
            .setSmallIcon(R.drawable.focus)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    private fun requestBatteryExemption() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            val packageName = packageName
            
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                android.util.Log.d("FocusMonitor", "App not exempt from battery optimization")
            } else {
                android.util.Log.d("FocusMonitor", "App exempt from battery optimization")
            }
        } catch (e: Exception) {
            android.util.Log.e("FocusMonitor", "Error checking battery optimization", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Check if Focus Mode should still be active
        val isActive = prefs.getBoolean("focusModeActive", false)
        val endTime = prefs.getLong("focusModeEndTime", 0L)
        
        if (isActive && System.currentTimeMillis() < endTime) {
            // Service destroyed but Focus Mode should be active - restart it
            android.util.Log.d("FocusMonitor", "Service destroyed - restarting")
            val restartIntent = Intent(applicationContext, FocusMonitoringService::class.java)
            startForegroundService(restartIntent)
        }
        
        monitoringJob?.cancel()
        serviceScope.cancel()
    }
}