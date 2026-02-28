package com.CuriosityLabs.digibalance.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.TextView
import com.CuriosityLabs.digibalance.R
import com.CuriosityLabs.digibalance.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DigiBalanceAccessibilityService : AccessibilityService() {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var currentPackage: String? = null
    private var distractionTimer: Handler? = null
    private var overlayView: android.view.View? = null
    private var windowManager: WindowManager? = null
    
    companion object {
        private const val DISTRACTION_THRESHOLD_MS = 5 * 60 * 1000L // 5 minutes
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                val packageName = event.packageName?.toString() ?: return
                
                // Ignore our own package
                if (packageName == this.packageName) return
                
                // If package changed, handle the transition
                if (packageName != currentPackage) {
                    handlePackageChange(packageName)
                    currentPackage = packageName
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AccessibilityService", "Error in onAccessibilityEvent: ${e.message}", e)
        }
    }
    
    private fun handlePackageChange(packageName: String) {
        // Check if Focus Mode is active
        val prefs = getSharedPreferences("digibalance_prefs", Context.MODE_PRIVATE)
        val isFocusModeActive = prefs.getBoolean("focusModeActive", false)
        val focusModeEndTime = prefs.getLong("focusModeEndTime", 0L)
        
        if (isFocusModeActive && System.currentTimeMillis() < focusModeEndTime) {
            // Get allowed apps
            val allowedApps = prefs.getStringSet("focus_mode_selected_apps", emptySet()) ?: emptySet()
            
            // Check if current app is allowed
            val isAllowed = allowedApps.contains(packageName) || 
                           packageName == "com.CuriosityLabs.digibalance" ||
                           packageName == "com.android.systemui" ||
                           packageName.startsWith("com.android.launcher")
            
            if (!isAllowed) {
                // Block this app and return to Focus Mode launcher
                android.util.Log.d("AccessibilityService", "Blocking non-allowed app: $packageName")
                val intent = Intent(this, FocusLauncherActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                return
            }
        }
        
        // Cancel any existing timer
        distractionTimer?.removeCallbacksAndMessages(null)
        removeOverlay()
        
        // Check if this app is categorized as a distraction
        serviceScope.launch(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(applicationContext)
            val rules = database.parentalRuleDao().getAllRules()
            
            // Check if app is in distraction list
            val isDistraction = rules.any { rule ->
                rule.ruleType == "PERSONAL_APPS" && 
                rule.config.contains(packageName) &&
                rule.config.contains("\"isProductive\":false")
            }
            
            if (isDistraction) {
                startDistractionTimer(packageName)
            }
        }
    }
    
    private fun startDistractionTimer(packageName: String) {
        distractionTimer = Handler(Looper.getMainLooper())
        distractionTimer?.postDelayed({
            showDistractionOverlay(packageName)
        }, DISTRACTION_THRESHOLD_MS)
    }
    
    private fun showDistractionOverlay(packageName: String) {
        try {
            if (overlayView != null) return
            
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            overlayView = inflater.inflate(R.layout.overlay_distraction_alert, null)
            
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.CENTER
            
            overlayView?.findViewById<TextView>(R.id.tvMessage)?.text = 
                "You have been on this app for 5 minutes. You are not using your time productively."
            
            overlayView?.findViewById<Button>(R.id.btnDismiss)?.setOnClickListener {
                removeOverlay()
            }
            
            windowManager?.addView(overlayView, params)
            
            // Auto-dismiss after 10 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                removeOverlay()
            }, 10000)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun removeOverlay() {
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayView = null
        }
    }
    
    override fun onInterrupt() {
        distractionTimer?.removeCallbacksAndMessages(null)
        removeOverlay()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        distractionTimer?.removeCallbacksAndMessages(null)
        removeOverlay()
    }
}
