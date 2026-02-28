package com.CuriosityLabs.digibalance.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.CuriosityLabs.digibalance.data.repository.UninstallEventRepository
import com.CuriosityLabs.digibalance.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Broadcast receiver to detect when DigiBalance app is being uninstalled
 * Note: This receiver can detect other app uninstalls, but cannot detect its own uninstall
 * We use a workaround by monitoring app activity and notifying parent if student hasn't been active
 */
class AppUninstallReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        when (intent.action) {
            Intent.ACTION_PACKAGE_REMOVED -> {
                val packageName = intent.data?.schemeSpecificPart
                Log.d("AppUninstallReceiver", "Package removed: $packageName")
                
                // If it's our app being uninstalled (this won't actually trigger for our own app)
                if (packageName == context.packageName) {
                    notifyParentOfUninstall(context)
                }
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // App was updated, not uninstalled
                Log.d("AppUninstallReceiver", "App was updated")
            }
        }
    }
    
    private fun notifyParentOfUninstall(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userRepository = UserRepository()
                val uninstallRepository = UninstallEventRepository()
                
                val studentId = userRepository.getCurrentUserId()
                if (studentId != null) {
                    val userProfile = userRepository.getUserProfile(studentId).getOrNull()
                    val parentId = userProfile?.linked_parent_id
                    
                    if (parentId != null) {
                        uninstallRepository.recordUninstallEvent(studentId, parentId)
                        Log.d("AppUninstallReceiver", "Uninstall event recorded for student: $studentId")
                    }
                }
            } catch (e: Exception) {
                Log.e("AppUninstallReceiver", "Error recording uninstall event", e)
            }
        }
    }
}
