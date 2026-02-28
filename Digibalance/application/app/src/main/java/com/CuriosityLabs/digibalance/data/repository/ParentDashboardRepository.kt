package com.CuriosityLabs.digibalance.data.repository

import com.CuriosityLabs.digibalance.DigiBalanceApplication
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class LinkedStudent(
    val id: String,
    val display_name: String?,
    val gamertag: String?,
    val email: String?,
    val phone: String?
)

@Serializable
data class StudentUsageStats(
    val student_id: String,
    val date: String,
    val total_screen_time_ms: Long,
    val productive_time_ms: Long,
    val distraction_time_ms: Long,
    val apps_opened_count: Int,
    val focus_sessions_count: Int,
    val alerts_triggered_count: Int
)

@Serializable
data class StudentAppUsage(
    val app_name: String,
    val package_name: String,
    val usage_time_ms: Long,
    val is_productive: Boolean
)

@Serializable
data class RuleCompliance(
    val rule_type: String,
    val followed_count: Int,
    val violated_count: Int,
    val most_violated_app: String?
)

class ParentDashboardRepository {
    
    private val supabase = DigiBalanceApplication.supabaseClient
    
    suspend fun getLinkedStudents(parentId: String): Result<List<LinkedStudent>> {
        return try {
            val response = supabase.from("users")
                .select(Columns.list("id", "display_name", "gamertag", "email", "phone")) {
                    filter {
                        eq("linked_parent_id", parentId)
                    }
                }
                .decodeList<LinkedStudent>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getStudentTodayStats(studentId: String): Result<StudentUsageStats> {
        return try {
            // Try to get from aggregated view first
            val response = supabase.from("daily_usage_summary")
                .select {
                    filter {
                        eq("user_id", studentId)
                        eq("date", getTodayDate())
                    }
                }
                .decodeSingleOrNull<StudentUsageStats>()
            
            // If no data in view, calculate from raw data
            if (response != null) {
                Result.success(response)
            } else {
                // Fallback: calculate from app_usage_stats
                val rawStats = supabase.from("app_usage_stats")
                    .select {
                        filter {
                            eq("user_id", studentId)
                            gte("timestamp", getTodayStartTimestamp())
                        }
                    }
                    .decodeList<RawUsageStats>()
                
                val aggregated = aggregateUsageStats(studentId, rawStats)
                Result.success(aggregated)
            }
        } catch (e: Exception) {
            // Return empty stats on error
            Result.success(StudentUsageStats(
                student_id = studentId,
                date = getTodayDate(),
                total_screen_time_ms = 0,
                productive_time_ms = 0,
                distraction_time_ms = 0,
                apps_opened_count = 0,
                focus_sessions_count = 0,
                alerts_triggered_count = 0
            ))
        }
    }
    
    private fun aggregateUsageStats(studentId: String, rawStats: List<RawUsageStats>): StudentUsageStats {
        var totalScreenTime = 0L
        var productiveTime = 0L
        var distractionTime = 0L
        var appsOpened = 0
        var focusSessions = 0
        var alertsTriggered = 0
        
        rawStats.forEach { stat ->
            totalScreenTime += stat.usage_time_ms
            if (stat.is_productive) {
                productiveTime += stat.usage_time_ms
            } else {
                distractionTime += stat.usage_time_ms
            }
            appsOpened++
        }
        
        return StudentUsageStats(
            student_id = studentId,
            date = getTodayDate(),
            total_screen_time_ms = totalScreenTime,
            productive_time_ms = productiveTime,
            distraction_time_ms = distractionTime,
            apps_opened_count = appsOpened,
            focus_sessions_count = focusSessions,
            alerts_triggered_count = alertsTriggered
        )
    }
    
    suspend fun getStudentWeeklyStats(studentId: String): Result<List<StudentUsageStats>> {
        return try {
            val response = supabase.from("app_usage_stats")
                .select {
                    filter {
                        eq("student_id", studentId)
                        gte("date", getWeekAgoDate())
                    }
                }
                .decodeList<StudentUsageStats>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getStudentTopApps(studentId: String, limit: Int = 5): Result<List<StudentAppUsage>> {
        return try {
            // This would call a Supabase RPC function or view
            val response = supabase.from("student_app_usage_today")
                .select {
                    filter {
                        eq("student_id", studentId)
                    }
                }
                .decodeList<StudentAppUsage>()
                .take(limit)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getStudentRuleCompliance(studentId: String): Result<List<RuleCompliance>> {
        return try {
            // This would call a Supabase RPC function
            val response = supabase.from("rule_compliance_stats")
                .select {
                    filter {
                        eq("student_id", studentId)
                    }
                }
                .decodeList<RuleCompliance>()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun unlinkStudent(parentId: String, studentId: String): Result<Unit> {
        return try {
            // Update student to remove parent link
            supabase.from("users").update(
                buildJsonObject {
                    put("linked_parent_id", null as String?)
                }
            ) {
                filter {
                    eq("id", studentId)
                    eq("linked_parent_id", parentId)
                }
            }
            
            // Delete all rules for this student
            supabase.from("parental_rules").delete {
                filter {
                    eq("student_id", studentId)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun generateLinkCode(parentId: String): Result<String> {
        return try {
            // Generate a temporary link code (valid for 5 minutes)
            val linkCode = generateRandomCode()
            val expiresAt = System.currentTimeMillis() + (5 * 60 * 1000)
            
            supabase.from("parent_link_codes").insert(
                buildJsonObject {
                    put("parent_id", parentId)
                    put("link_code", linkCode)
                    put("expires_at", expiresAt)
                }
            )
            
            Result.success(linkCode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun linkStudentWithCode(studentId: String, linkCode: String): Result<String> {
        return try {
            // Verify code and get parent ID
            val response = supabase.from("parent_link_codes")
                .select {
                    filter {
                        eq("link_code", linkCode)
                        gt("expires_at", System.currentTimeMillis())
                    }
                }
                .decodeSingleOrNull<ParentLinkCode>()
            
            if (response == null) {
                return Result.failure(Exception("Invalid or expired link code"))
            }
            
            // Link student to parent
            supabase.from("users").update(
                buildJsonObject {
                    put("linked_parent_id", response.parent_id)
                }
            ) {
                filter {
                    eq("id", studentId)
                }
            }
            
            // Delete used code
            supabase.from("parent_link_codes").delete {
                filter {
                    eq("link_code", linkCode)
                }
            }
            
            Result.success(response.parent_id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getTodayDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
    
    private fun getTodayStartTimestamp(): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return java.time.Instant.ofEpochMilli(calendar.timeInMillis).toString()
    }
    
    private fun getWeekAgoDate(): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -7)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(calendar.time)
    }
    
    suspend fun syncParentalRulesForStudent(studentId: String): Result<List<ParentalRule>> {
        return try {
            // Get student's parent ID
            val userRepo = UserRepository()
            val userProfile = userRepo.getUserProfile(studentId).getOrNull()
            val parentId = userProfile?.linked_parent_id
            
            if (parentId == null) {
                return Result.success(emptyList())
            }
            
            // Fetch parental rules for this student
            val rules = supabase.from("parental_rules")
                .select {
                    filter {
                        eq("parent_id", parentId)
                        eq("student_id", studentId)
                        eq("is_active", true)
                    }
                }
                .decodeList<ParentalRule>()
            
            Result.success(rules)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createParentalRule(
        parentId: String,
        studentId: String,
        ruleType: String,
        ruleValue: String,
        isActive: Boolean = true
    ): Result<Unit> {
        return try {
            supabase.from("parental_rules").insert(
                buildJsonObject {
                    put("parent_id", parentId)
                    put("student_id", studentId)
                    put("rule_type", ruleType)
                    put("rule_value", ruleValue)
                    put("is_active", isActive)
                }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateParentalRule(
        ruleId: Long,
        ruleValue: String? = null,
        isActive: Boolean? = null
    ): Result<Unit> {
        return try {
            val updates = buildJsonObject {
                ruleValue?.let { put("rule_value", it) }
                isActive?.let { put("is_active", it) }
            }
            
            supabase.from("parental_rules").update(updates) {
                filter {
                    eq("id", ruleId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteParentalRule(ruleId: Long): Result<Unit> {
        return try {
            supabase.from("parental_rules").delete {
                filter {
                    eq("id", ruleId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateRandomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
}

@Serializable
data class ParentalRule(
    val id: Long? = null,
    val parent_id: String,
    val student_id: String,
    val rule_type: String,
    val rule_value: String,
    val is_active: Boolean = true,
    val created_at: String? = null
)

@Serializable
data class ParentLinkCode(
    val parent_id: String,
    val link_code: String,
    val expires_at: Long
)

@Serializable
data class RawUsageStats(
    val user_id: String,
    val package_name: String,
    val usage_time_ms: Long,
    val is_productive: Boolean,
    val timestamp: String
)
