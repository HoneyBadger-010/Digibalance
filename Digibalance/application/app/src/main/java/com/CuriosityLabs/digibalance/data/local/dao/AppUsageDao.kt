package com.CuriosityLabs.digibalance.data.local.dao

import androidx.room.*
import com.CuriosityLabs.digibalance.data.local.entity.AppUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {
    
    @Query("SELECT * FROM app_usage WHERE date = :date ORDER BY usageTimeMillis DESC")
    fun getUsageForDate(date: String): Flow<List<AppUsageEntity>>
    
    @Query("SELECT * FROM app_usage WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, usageTimeMillis DESC")
    fun getUsageForDateRange(startDate: String, endDate: String): Flow<List<AppUsageEntity>>
    
    @Query("SELECT SUM(usageTimeMillis) FROM app_usage WHERE date = :date AND isProductive = 1")
    suspend fun getProductiveTimeForDate(date: String): Long?
    
    @Query("SELECT SUM(usageTimeMillis) FROM app_usage WHERE date = :date AND isProductive = 0")
    suspend fun getDistractionTimeForDate(date: String): Long?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsage(usage: AppUsageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsages(usages: List<AppUsageEntity>)
    
    @Query("DELETE FROM app_usage WHERE date < :beforeDate")
    suspend fun deleteOldUsage(beforeDate: String)
}
