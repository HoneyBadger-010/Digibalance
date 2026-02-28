package com.CuriosityLabs.digibalance.ui.permissions

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi

object PermissionChecker {
    
    /**
     * Check if Usage Stats permission is granted
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        return try {
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOpsManager.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if Accessibility Service is enabled for this app
     */
    fun hasAccessibilityPermission(context: Context, packageName: String): Boolean {
        return try {
            val accessibilityEnabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            ) == 1
            
            if (!accessibilityEnabled) {
                return false
            }
            
            val settingValue = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            
            // Check if any accessibility service from our package is enabled
            settingValue.contains(packageName, ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if Draw Over Apps permission is granted
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun hasOverlayPermission(context: Context): Boolean {
        return try {
            Settings.canDrawOverlays(context)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if Notification Policy Access (DND) is granted
     */
    fun hasNotificationPolicyAccess(context: Context): Boolean {
        return try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.isNotificationPolicyAccessGranted
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if Battery Optimization is disabled for this app
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun isBatteryOptimizationDisabled(context: Context): Boolean {
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if app has boot receiver permission (always true if declared in manifest)
     */
    fun hasBootPermission(): Boolean {
        // This permission is automatically granted if declared in manifest
        // We just check if we're on a supported Android version
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }
}

