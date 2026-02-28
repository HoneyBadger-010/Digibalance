package com.CuriosityLabs.digibalance.util

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import java.text.SimpleDateFormat
import java.util.*

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val usageTimeMillis: Long,
    val lastTimeUsed: Long
)

object UsageStatsHelper {
    
    fun getTodayUsageStats(context: Context): List<AppUsageInfo> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()
        
        return getUsageStats(context, startTime, endTime)
    }
    
    fun getWeeklyUsageStats(context: Context): List<AppUsageInfo> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()
        
        return getUsageStats(context, startTime, endTime)
    }
    
    fun getMonthlyUsageStats(context: Context): List<AppUsageInfo> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()
        
        return getUsageStats(context, startTime, endTime)
    }
    
    private fun getUsageStats(context: Context, startTime: Long, endTime: Long): List<AppUsageInfo> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
        
        val pm = context.packageManager
        val appUsageList = mutableListOf<AppUsageInfo>()
        
        usageStatsList?.forEach { usageStats ->
            if (usageStats.totalTimeInForeground > 0) {
                try {
                    val appInfo = pm.getApplicationInfo(usageStats.packageName, 0)
                    val appName = pm.getApplicationLabel(appInfo).toString()
                    
                    appUsageList.add(
                        AppUsageInfo(
                            packageName = usageStats.packageName,
                            appName = appName,
                            usageTimeMillis = usageStats.totalTimeInForeground,
                            lastTimeUsed = usageStats.lastTimeUsed
                        )
                    )
                } catch (e: PackageManager.NameNotFoundException) {
                    // App not found, skip
                }
            }
        }
        
        return appUsageList.sortedByDescending { it.usageTimeMillis }
    }
    
    fun formatDuration(millis: Long): String {
        val hours = millis / (1000 * 60 * 60)
        val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "< 1m"
        }
    }
    
    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
    
    fun calculateProductiveTime(
        usageList: List<AppUsageInfo>,
        productivePackages: Set<String>
    ): Long {
        return usageList
            .filter { it.packageName in productivePackages }
            .sumOf { it.usageTimeMillis }
    }
    
    fun calculateDistractionTime(
        usageList: List<AppUsageInfo>,
        productivePackages: Set<String>
    ): Long {
        return usageList
            .filter { it.packageName !in productivePackages }
            .sumOf { it.usageTimeMillis }
    }
}
