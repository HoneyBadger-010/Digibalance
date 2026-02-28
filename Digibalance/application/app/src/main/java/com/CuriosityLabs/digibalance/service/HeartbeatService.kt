package com.CuriosityLabs.digibalance.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.CuriosityLabs.digibalance.data.repository.UninstallEventRepository
import com.CuriosityLabs.digibalance.data.repository.UserRepository
import kotlinx.coroutines.*

/**
 * Background service that sends periodic heartbeats to indicate the app is still installed
 * Parents can monitor these heartbeats to detect if the app has been uninstalled
 */
class HeartbeatService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var heartbeatJob: Job? = null
    
    companion object {
        private const val HEARTBEAT_INTERVAL_MS = 15 * 60 * 1000L // 15 minutes
        private const val TAG = "HeartbeatService"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "HeartbeatService created")
        startHeartbeat()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "HeartbeatService started")
        return START_STICKY // Restart service if killed
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "HeartbeatService destroyed")
        heartbeatJob?.cancel()
        serviceScope.cancel()
    }
    
    private fun startHeartbeat() {
        heartbeatJob = serviceScope.launch {
            while (isActive) {
                try {
                    sendHeartbeat()
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending heartbeat", e)
                }
                delay(HEARTBEAT_INTERVAL_MS)
            }
        }
    }
    
    private suspend fun sendHeartbeat() {
        val userRepository = UserRepository()
        val uninstallRepository = UninstallEventRepository()
        
        val studentId = userRepository.getCurrentUserId()
        if (studentId != null) {
            val userProfile = userRepository.getUserProfile(studentId).getOrNull()
            
            // Only send heartbeat if student is linked to a parent
            if (userProfile?.linked_parent_id != null) {
                uninstallRepository.recordHeartbeat(studentId).onSuccess {
                    Log.d(TAG, "Heartbeat sent successfully for student: $studentId")
                }.onFailure { error ->
                    Log.e(TAG, "Failed to send heartbeat: ${error.message}")
                }
            }
        }
    }
}
