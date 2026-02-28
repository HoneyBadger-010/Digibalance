package com.CuriosityLabs.digibalance.data.repository

import com.CuriosityLabs.digibalance.DigiBalanceApplication
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class Feedback(
    val id: Long? = null,
    val user_id: String? = null,
    val category: String,
    val message: String,
    val is_read: Boolean = false,
    val created_at: String? = null
)

class FeedbackRepository {
    
    private val supabase = DigiBalanceApplication.supabaseClient
    
    suspend fun submitFeedback(
        userId: String?,
        category: String,
        message: String
    ): Result<Unit> {
        return try {
            android.util.Log.d("FeedbackRepository", "Submitting feedback: category=$category, userId=$userId")
            
            supabase.from("feedback").insert(
                buildJsonObject {
                    userId?.let { put("user_id", it) }
                    put("category", category)
                    put("message", message)
                }
            )
            
            android.util.Log.d("FeedbackRepository", "Feedback submitted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("FeedbackRepository", "Failed to submit feedback: ${e.message}", e)
            Result.failure(e)
        }
    }
}
