package com.CuriosityLabs.digibalance.data.local.dao

import androidx.room.*
import com.CuriosityLabs.digibalance.data.local.entity.ParentalRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParentalRuleDao {
    
    @Query("SELECT * FROM parental_rules WHERE studentId = :studentId")
    fun getRulesForStudent(studentId: String): Flow<List<ParentalRuleEntity>>
    
    @Query("SELECT * FROM parental_rules WHERE studentId = :studentId AND ruleType = :ruleType")
    suspend fun getRulesByType(studentId: String, ruleType: String): List<ParentalRuleEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: ParentalRuleEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<ParentalRuleEntity>)
    
    @Delete
    suspend fun deleteRule(rule: ParentalRuleEntity)
    
    @Query("DELETE FROM parental_rules WHERE studentId = :studentId")
    suspend fun deleteAllRulesForStudent(studentId: String)
    
    @Query("SELECT * FROM parental_rules")
    suspend fun getAllRules(): List<ParentalRuleEntity>
}
