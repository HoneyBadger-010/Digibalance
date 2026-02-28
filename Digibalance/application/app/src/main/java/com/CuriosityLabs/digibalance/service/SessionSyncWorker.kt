package com.CuriosityLabs.digibalance.service

import android.content.Context
import androidx.work.*
import com.CuriosityLabs.digibalance.data.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class SessionSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("SessionSyncWorker", "Syncing session to Room database...")
            
            val prefs = PreferencesManager.getInstance(applicationContext)
            
            // Check if sync is needed
            val needsSync = applicationContext.getSharedPreferences("digibalance_prefs", Context.MODE_PRIVATE)
                .getBoolean("needs_db_sync", false)
            
            if (needsSync || prefs.isLoggedIn) {
                // Save current session to Room database
                prefs.saveToDatabase(applicationContext)
                android.util.Log.d("SessionSyncWorker", "✅ Session synced to database")
            } else {
                android.util.Log.d("SessionSyncWorker", "No sync needed")
            }
            
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("SessionSyncWorker", "Failed to sync session", e)
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "session_sync_work"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false) // Run even on low battery
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SessionSyncWorker>(
                15, TimeUnit.MINUTES // Sync every 15 minutes
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
            
            android.util.Log.d("SessionSyncWorker", "Scheduled periodic session sync")
        }
        
        fun syncNow(context: Context) {
            val syncRequest = OneTimeWorkRequestBuilder<SessionSyncWorker>()
                .build()
            
            WorkManager.getInstance(context).enqueue(syncRequest)
            android.util.Log.d("SessionSyncWorker", "Triggered immediate session sync")
        }
    }
}
