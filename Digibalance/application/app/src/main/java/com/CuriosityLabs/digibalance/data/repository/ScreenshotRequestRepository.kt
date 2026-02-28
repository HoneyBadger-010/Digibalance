package com.CuriosityLabs.digibalance.data.repository

import com.CuriosityLabs.digibalance.DigiBalanceApplication
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File

@Serializable
data class ScreenshotRequest(
    val id: String,
    val parent_id: String,
    val student_id: String,
    val request_time: String,
    val status: String, // pending, captured, failed, expired
    val screenshot_url: String? = null,
    val captured_at: String? = null,
    val expires_at: String,
    val error_message: String? = null,
    val device_info: String? = null
)

class ScreenshotRequestRepository {
    
    private val supabase = DigiBalanceApplication.supabaseClient
    
    // Parent: Request a screenshot from student
    suspend fun createScreenshotRequest(
        parentId: String,
        studentId: String
    ): Result<ScreenshotRequest> {
        return try {
            val response = supabase.from("screenshot_requests").insert(
                buildJsonObject {
                    put("parent_id", parentId)
                    put("student_id", studentId)
                    put("status", "pending")
                }
            ) {
                select()
            }.decodeSingle<ScreenshotRequest>()
            
            Result.success(response)
        } catch (e: Exception) {
            android.util.Log.e("ScreenshotRepo", "Create request failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Student: Get pending screenshot requests
    suspend fun getPendingRequests(studentId: String): Result<List<ScreenshotRequest>> {
        return try {
            val response = supabase.from("screenshot_requests")
                .select {
                    filter {
                        eq("student_id", studentId)
                        eq("status", "pending")
                    }
                }
                .decodeList<ScreenshotRequest>()
            
            Result.success(response)
        } catch (e: Exception) {
            android.util.Log.e("ScreenshotRepo", "Get pending failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Student: Upload screenshot and update request
    // Note: Supabase Storage upload would go here - simplified for now
    suspend fun uploadScreenshot(
        requestId: String,
        screenshotFile: File,
        deviceInfo: String
    ): Result<String> {
        return try {
            // TODO: Implement actual Supabase Storage upload
            // For now, just mark as captured with a placeholder URL
            val placeholderUrl = "https://placeholder.com/screenshot_${requestId}.jpg"
            
            // Update request with screenshot URL
            supabase.from("screenshot_requests").update(
                buildJsonObject {
                    put("status", "captured")
                    put("screenshot_url", placeholderUrl)
                    put("captured_at", java.time.Instant.now().toString())
                    put("device_info", deviceInfo)
                }
            ) {
                filter {
                    eq("id", requestId)
                }
            }
            
            Result.success(placeholderUrl)
        } catch (e: Exception) {
            android.util.Log.e("ScreenshotRepo", "Upload failed: ${e.message}", e)
            
            // Mark request as failed
            try {
                supabase.from("screenshot_requests").update(
                    buildJsonObject {
                        put("status", "failed")
                        put("error_message", e.message ?: "Upload failed")
                    }
                ) {
                    filter {
                        eq("id", requestId)
                    }
                }
            } catch (updateError: Exception) {
                android.util.Log.e("ScreenshotRepo", "Failed to update error status", updateError)
            }
            
            Result.failure(e)
        }
    }
    
    // Parent: Get all screenshot requests for their students
    suspend fun getParentScreenshotRequests(
        parentId: String,
        limit: Int = 20
    ): Result<List<ScreenshotRequest>> {
        return try {
            val response = supabase.from("screenshot_requests")
                .select {
                    filter {
                        eq("parent_id", parentId)
                    }
                    order("created_at", order = Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<ScreenshotRequest>()
            
            Result.success(response)
        } catch (e: Exception) {
            android.util.Log.e("ScreenshotRepo", "Get parent requests failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Get specific screenshot request
    suspend fun getScreenshotRequest(requestId: String): Result<ScreenshotRequest> {
        return try {
            val response = supabase.from("screenshot_requests")
                .select {
                    filter {
                        eq("id", requestId)
                    }
                }
                .decodeSingle<ScreenshotRequest>()
            
            Result.success(response)
        } catch (e: Exception) {
            android.util.Log.e("ScreenshotRepo", "Get request failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // Delete old screenshot requests (cleanup)
    suspend fun deleteOldRequests(olderThanDays: Int = 7): Result<Unit> {
        return try {
            val cutoffDate = java.time.Instant.now()
                .minus(olderThanDays.toLong(), java.time.temporal.ChronoUnit.DAYS)
                .toString()
            
            supabase.from("screenshot_requests").delete {
                filter {
                    lt("created_at", cutoffDate)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("ScreenshotRepo", "Delete old requests failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
