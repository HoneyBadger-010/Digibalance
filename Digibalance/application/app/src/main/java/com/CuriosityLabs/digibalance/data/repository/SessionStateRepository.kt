package com.CuriosityLabs.digibalance.data.repository

import com.CuriosityLabs.digibalance.DigiBalanceApplication
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class SessionState(
    val user_id: String,
    val last_screen: String,
    val onboarding_completed: Boolean = false,
    val has_completed_survey: Boolean = false,
    val has_selected_role: Boolean = false,
    val has_created_gamertag: Boolean = false,
    val has_granted_permissions: Boolean = false,
    val session_data: String = "{}"
)

class SessionStateRepository {
    
    private val supabase = DigiBalanceApplication.supabaseClient
    
    suspend fun getSessionState(userId: String): Result<SessionState> {
        return try {
            val response = supabase.from("user_session_state")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingle<SessionState>()
            Result.success(response)
        } catch (e: Exception) {
            android.util.Log.e("SessionStateRepo", "Failed to get session state: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun updateSessionState(
        userId: String,
        lastScreen: String,
        onboardingCompleted: Boolean = false,
        hasCompletedSurvey: Boolean = false,
        hasSelectedRole: Boolean = false,
        hasCreatedGamertag: Boolean = false,
        hasGrantedPermissions: Boolean = false
    ): Result<Unit> {
        return try {
            supabase.from("user_session_state").upsert(
                buildJsonObject {
                    put("user_id", userId)
                    put("last_screen", lastScreen)
                    put("onboarding_completed", onboardingCompleted)
                    put("has_completed_survey", hasCompletedSurvey)
                    put("has_selected_role", hasSelectedRole)
                    put("has_created_gamertag", hasCreatedGamertag)
                    put("has_granted_permissions", hasGrantedPermissions)
                }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("SessionStateRepo", "Failed to update session state: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun markOnboardingComplete(userId: String): Result<Unit> {
        return try {
            supabase.from("user_session_state").upsert(
                buildJsonObject {
                    put("user_id", userId)
                    put("last_screen", "home")
                    put("onboarding_completed", true)
                    put("has_completed_survey", true)
                    put("has_selected_role", true)
                    put("has_created_gamertag", true)
                    put("has_granted_permissions", true)
                }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
