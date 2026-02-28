package com.CuriosityLabs.digibalance.data.repository

import com.CuriosityLabs.digibalance.DigiBalanceApplication
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class UninstallEvent(
    val id: Long? = null,
    val student_id: String,
    val parent_id: String,
    val detected_at: String,
    val notified: Boolean = false
)

class UninstallEventRepository {
    
    private val supabase = DigiBalanceApplication.supabaseClient
    
    suspend fun recordUninstallEvent(studentId: String, parentId: String): Result<Unit> {
        return try {
            supabase.from("uninstall_events").insert(
                buildJsonObject {
                    put("student_id", studentId)
                    put("parent_id", parentId)
                    put("detected_at", getCurrentTimestamp())
                    put("notified", false)
                }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUninstallEventsForParent(parentId: String): Result<List<UninstallEvent>> {
        return try {
            val response = supabase.from("uninstall_events")
                .select {
                    filter {
                        eq("parent_id", parentId)
                    }
                }
                .decodeList<UninstallEvent>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun markAsNotified(eventId: Long): Result<Unit> {
        return try {
            supabase.from("uninstall_events").update(
                buildJsonObject {
                    put("notified", true)
                }
            ) {
                filter {
                    eq("id", eventId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun recordHeartbeat(studentId: String): Result<Unit> {
        return try {
            // Update last_seen timestamp in users table
            supabase.from("users").update(
                buildJsonObject {
                    put("last_seen", getCurrentTimestamp())
                }
            ) {
                filter {
                    eq("id", studentId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getCurrentTimestamp(): String {
        return java.time.Instant.now().toString()
    }
}
