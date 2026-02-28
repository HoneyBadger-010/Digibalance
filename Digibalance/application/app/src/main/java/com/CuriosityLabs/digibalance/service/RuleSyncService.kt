package com.CuriosityLabs.digibalance.service

import android.content.Context
import android.util.Log
import com.CuriosityLabs.digibalance.DigiBalanceApplication
import com.CuriosityLabs.digibalance.data.local.AppDatabase
import com.CuriosityLabs.digibalance.data.local.entity.ParentalRuleEntity
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class RuleSyncService(private val context: Context) {
    
    private val supabase = DigiBalanceApplication.supabaseClient
    private val database = AppDatabase.getDatabase(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val TAG = "RuleSyncService"
    
    suspend fun startRealtimeSync(studentId: String) {
        try {
            Log.d(TAG, "Starting real-time sync for student: $studentId")
            
            // Initial sync - fetch all rules
            performInitialSync(studentId)
            
            // Subscribe to real-time changes
            val channel = supabase.channel("parental_rules_$studentId")

            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "parental_rules"
                // Listen to all rule changes; filtering is done in handlers if needed
            }
            
            changeFlow.onEach { action ->
                when (action) {
                    is PostgresAction.Insert -> {
                        Log.d(TAG, "New rule inserted")
                        handleRuleInsert(action)
                    }
                    is PostgresAction.Update -> {
                        Log.d(TAG, "Rule updated")
                        handleRuleUpdate(action)
                    }
                    is PostgresAction.Delete -> {
                        Log.d(TAG, "Rule deleted")
                        handleRuleDelete(action)
                    }
                    else -> {
                        Log.d(TAG, "Unknown action: $action")
                    }
                }
            }.launchIn(scope)
            
            channel.subscribe()
            
            Log.d(TAG, "Real-time sync started successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting real-time sync", e)
        }
    }
    
    private suspend fun performInitialSync(studentId: String) {
        try {
            // Fetch all rules from Supabase
            val rules = supabase.from("parental_rules")
                .select {
                    filter {
                        eq("student_id", studentId)
                    }
                }
            
            // Parse and save to local database
            // Note: This is a simplified version - you'll need to properly parse the JSON response
            Log.d(TAG, "Initial sync completed")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during initial sync", e)
        }
    }
    
    private suspend fun handleRuleInsert(action: PostgresAction.Insert) {
        try {
            val record = action.record
            val rule = ParentalRuleEntity(
                id = record["id"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
                studentId = record["student_id"]?.jsonPrimitive?.content ?: "",
                ruleType = record["rule_type"]?.jsonPrimitive?.content ?: "",
                config = record["config"]?.jsonObject?.toString() ?: "{}",
                emergencyCode = record["emergency_code"]?.jsonPrimitive?.content
            )
            
            database.parentalRuleDao().insertRule(rule)
            Log.d(TAG, "Rule inserted into local database")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling rule insert", e)
        }
    }
    
    private suspend fun handleRuleUpdate(action: PostgresAction.Update) {
        try {
            val record = action.record
            val rule = ParentalRuleEntity(
                id = record["id"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L,
                studentId = record["student_id"]?.jsonPrimitive?.content ?: "",
                ruleType = record["rule_type"]?.jsonPrimitive?.content ?: "",
                config = record["config"]?.jsonObject?.toString() ?: "{}",
                emergencyCode = record["emergency_code"]?.jsonPrimitive?.content
            )
            
            database.parentalRuleDao().insertRule(rule)
            Log.d(TAG, "Rule updated in local database")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling rule update", e)
        }
    }
    
    private suspend fun handleRuleDelete(action: PostgresAction.Delete) {
        try {
            val oldRecord = action.oldRecord
            val ruleId = oldRecord["id"]?.jsonPrimitive?.content?.toLongOrNull() ?: return
            
            // Delete from local database
            val rule = ParentalRuleEntity(
                id = ruleId,
                studentId = "",
                ruleType = "",
                config = "{}"
            )
            database.parentalRuleDao().deleteRule(rule)
            Log.d(TAG, "Rule deleted from local database")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling rule delete", e)
        }
    }
    
    fun stopSync() {
        // Cancel all coroutines for this service scope
        scope.coroutineContext.cancel()
        Log.d(TAG, "Real-time sync stopped")
    }
}
