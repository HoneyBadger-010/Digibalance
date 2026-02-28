package com.CuriosityLabs.digibalance.data.repository

import com.CuriosityLabs.digibalance.DigiBalanceApplication
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import java.security.MessageDigest
import kotlin.random.Random

@Serializable
data class EmergencyCode(
    val id: String? = null,
    val parent_id: String,
    val student_id: String? = null,
    val code: String,
    val code_hash: String,
    val is_active: Boolean = true,
    val usage_count: Int = 0,
    val max_usage: Int = 1,
    val expires_at: String? = null,
    val created_at: String? = null,
    val used_at: String? = null
)

class EmergencyCodeRepository {
    private val supabase = DigiBalanceApplication.supabaseClient
    
    /**
     * Generate a new emergency code for a parent
     */
    suspend fun generateEmergencyCode(
        parentId: String,
        studentId: String? = null,
        maxUsage: Int = 1,
        expiresInHours: Int = 24
    ): Result<String> {
        return try {
            // Generate random 6-digit alphanumeric code
            val code = generateRandomCode()
            val codeHash = hashCode(code)
            
            // Calculate expiration time
            val expiresAt = if (expiresInHours > 0) {
                java.time.Instant.now()
                    .plus(expiresInHours.toLong(), java.time.temporal.ChronoUnit.HOURS)
                    .toString()
            } else null
            
            // Deactivate any existing active codes for this parent
            supabase.from("emergency_codes")
                .update(mapOf("is_active" to false)) {
                    filter {
                        eq("parent_id", parentId)
                        eq("is_active", true)
                    }
                }
            
            // Insert new code
            val emergencyCode = EmergencyCode(
                parent_id = parentId,
                student_id = studentId,
                code = code,
                code_hash = codeHash,
                max_usage = maxUsage,
                expires_at = expiresAt
            )
            
            supabase.from("emergency_codes")
                .insert(emergencyCode)
            
            android.util.Log.d("EmergencyCode", "Generated code: $code for parent: $parentId")
            Result.success(code)
        } catch (e: Exception) {
            android.util.Log.e("EmergencyCode", "Failed to generate emergency code", e)
            Result.failure(e)
        }
    }
    
    /**
     * Verify an emergency code for a user
     */
    suspend fun verifyEmergencyCode(
        code: String,
        userId: String
    ): Result<Boolean> {
        return try {
            val codeHash = hashCode(code)
            
            // Get user info to find parent
            val userResult = supabase.from("users")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<User>()
            
            val parentId = userResult.linked_parent_id ?: userId
            
            // Find active code
            val codes = supabase.from("emergency_codes")
                .select {
                    filter {
                        eq("code_hash", codeHash)
                        eq("is_active", true)
                        eq("parent_id", parentId)
                    }
                }
                .decodeList<EmergencyCode>()
            
            val activeCode = codes.firstOrNull { emergencyCode ->
                // Check expiration
                val isNotExpired = emergencyCode.expires_at?.let { expiresAt ->
                    java.time.Instant.parse(expiresAt).isAfter(java.time.Instant.now())
                } ?: true
                
                // Check usage limit
                val hasUsagesLeft = emergencyCode.max_usage <= 0 || 
                    emergencyCode.usage_count < emergencyCode.max_usage
                
                isNotExpired && hasUsagesLeft
            }
            
            if (activeCode != null) {
                // Update usage count
                val newUsageCount = activeCode.usage_count + 1
                val shouldDeactivate = activeCode.max_usage > 0 && 
                    newUsageCount >= activeCode.max_usage
                
                supabase.from("emergency_codes")
                    .update(mapOf(
                        "usage_count" to newUsageCount,
                        "used_at" to java.time.Instant.now().toString(),
                        "is_active" to !shouldDeactivate
                    )) {
                        filter {
                            eq("id", activeCode.id!!)
                        }
                    }
                
                android.util.Log.d("EmergencyCode", "Code verified successfully for user: $userId")
                Result.success(true)
            } else {
                android.util.Log.d("EmergencyCode", "Invalid or expired code for user: $userId")
                Result.success(false)
            }
        } catch (e: Exception) {
            android.util.Log.e("EmergencyCode", "Failed to verify emergency code", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get active emergency codes for a parent
     */
    suspend fun getActiveEmergencyCodes(parentId: String): Result<List<EmergencyCode>> {
        return try {
            val codes = supabase.from("emergency_codes")
                .select {
                    filter {
                        eq("parent_id", parentId)
                        eq("is_active", true)
                    }
                }
                .decodeList<EmergencyCode>()
            
            Result.success(codes)
        } catch (e: Exception) {
            android.util.Log.e("EmergencyCode", "Failed to get emergency codes", e)
            Result.failure(e)
        }
    }
    
    /**
     * Deactivate an emergency code
     */
    suspend fun deactivateEmergencyCode(codeId: String): Result<Unit> {
        return try {
            supabase.from("emergency_codes")
                .update(mapOf("is_active" to false)) {
                    filter {
                        eq("id", codeId)
                    }
                }
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("EmergencyCode", "Failed to deactivate emergency code", e)
            Result.failure(e)
        }
    }
    
    /**
     * Generate a random 6-digit alphanumeric code
     */
    private fun generateRandomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }
    
    /**
     * Hash a code for secure storage
     */
    private fun hashCode(code: String): String {
        val salt = "digibalance_salt"
        val input = code + salt
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}