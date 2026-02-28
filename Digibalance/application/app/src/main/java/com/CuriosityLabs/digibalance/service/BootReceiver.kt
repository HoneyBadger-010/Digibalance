package com.CuriosityLabs.digibalance.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs: SharedPreferences = context.getSharedPreferences("digibalance_prefs", Context.MODE_PRIVATE)
            val focusModeActive = prefs.getBoolean("focusModeActive", false)
            
            if (focusModeActive) {
                // Restart Focus Mode after boot
                val focusIntent = Intent(context, FocusLauncherActivity::class.java)
                focusIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(focusIntent)
            }
        }
    }
}
