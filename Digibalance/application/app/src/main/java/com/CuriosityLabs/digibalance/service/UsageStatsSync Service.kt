package com.CuriosityLabs.digibalance.service

import android.content.Context
import androidx.work.*
import com.CuriosityLabs.digibalance.DigiBalanceApplication
import com.CuriosityLabs.digibalance.data.local.AppDatabase
import com.CuriosityLabs.digibalance.data.repository.UserRepository
import com.CuriosityLabs.digibalance.util.UsageStatsHelper
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.concurrent.TimeUnit

class UsageStatsSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            syncUsageStats()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
    
    private suspend fun syncUsageStats() {
        val supabase = DigiBalanceApplication.supabaseClient
        val userRepository = UserRepository()
        val userId = userRepository.getCurrentUserId() ?: return
        
        // Get user role
        val userProfile = userRepository.getUserProfile(userId).getOrNull() ?: return
        
        // Only sync for students
        if (userProfile.role != "Student") return
        
        // Get today's usage stats
        val usageList = UsageStatsHelper.getTodayUsageStats(applicationContext)
        val database = AppDatabase.getDatabase(applicationContext)
        
        // Get productive apps from local database
        val rules = database.parentalRuleDao().getAllRules()
        val productivePackages = rules
            .filter { it.ruleType == "PERSONAL_APPS" }
            .flatMap { rule ->
                // Parse JSON config to extract productive apps
                // Simplified - in production, use proper JSON parsing
                emptyList<String>()
            }
            .toSet()
        
        // Calculate stats
        val totalScreenTime = usageList.sumOf { it.usageTimeMillis }
        val productiveTime = UsageStatsHelper.calculateProductiveTime(usageList, productivePackages)
        val distractionTime = UsageStatsHelper.calculateDistractionTime(usageList, productivePackages)
        val appsOpenedCount = usageList.size
        
        // Get focus sessions count from SharedPreferences
        val prefs = applicationContext.getSharedPreferences("digibalance_prefs", Context.MODE_PRIVATE)
        val focusSessionsCount = prefs.getInt("focus_sessions_today", 0)
        val alertsCount = prefs.getInt("alerts_triggered_today", 0)
        
        // Sync daily stats
        supabase.postgrest.rpc("update_daily_stats", buildJsonObject {
            put("p_student_id", userId)
            put("p_date", UsageStatsHelper.getTodayDateString())
            put("p_total_screen_time_ms", totalScreenTime)
            put("p_productive_time_ms", productiveTime)
            put("p_distraction_time_ms", distractionTime)
            put("p_apps_opened_count", appsOpenedCount)
            put("p_focus_sessions_count", focusSessionsCount)
            put("p_alerts_triggered_count", alertsCount)
        })
        
        // Sync individual app usage
        usageList.take(10).forEach { app ->
            val isProductive = app.packageName in productivePackages
            
            supabase.postgrest.rpc("update_app_usage", buildJsonObject {
                put("p_student_id", userId)
                put("p_date", UsageStatsHelper.getTodayDateString())
                put("p_app_name", app.appName)
                put("p_package_name", app.packageName)
                put("p_usage_time_ms", app.usageTimeMillis)
                put("p_is_productive", isProductive)
            })
        }
    }
    
    companion object {
        private const val WORK_NAME = "usage_stats_sync"
        
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val syncRequest = PeriodicWorkRequestBuilder<UsageStatsSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
        
        fun cancelSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
