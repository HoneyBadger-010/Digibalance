package com.CuriosityLabs.digibalance.data.repository

import com.CuriosityLabs.digibalance.DigiBalanceApplication
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.Phone
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class User(
    val id: String,
    val email: String? = null,
    val phone: String? = null,
    val role: String? = null,
    val display_name: String? = null,
    val gamertag: String? = null,
    val gamertag_last_changed: String? = null,
    val linked_parent_id: String? = null
)

class UserRepository {
    
    private val supabase = DigiBalanceApplication.supabaseClient
    
    suspend fun signUpWithEmail(email: String, password: String): Result<String> {
        return try {
            android.util.Log.d("UserRepository", "Attempting signUpWithEmail: $email")
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            android.util.Log.d("UserRepository", "SignUp API call completed")
            
            // Try multiple ways to get the user ID
            val userId = supabase.auth.currentUserOrNull()?.id 
                ?: supabase.auth.currentSessionOrNull()?.user?.id
            
            android.util.Log.d("UserRepository", "UserId after signup: $userId")
            
            if (userId == null) {
                throw Exception("Email confirmation required. Please disable 'Confirm email' in Supabase Dashboard → Settings → Auth → Email Auth")
            }
            
            Result.success(userId)
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "SignUp failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun signUpWithPhone(phone: String, password: String): Result<String> {
        return try {
            supabase.auth.signUpWith(Phone) {
                this.phone = phone
                this.password = password
            }
            
            // Try multiple ways to get the user ID
            val userId = supabase.auth.currentUserOrNull()?.id 
                ?: supabase.auth.currentSessionOrNull()?.user?.id
            
            if (userId == null) {
                throw Exception("Phone confirmation required.")
            }
            
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signInWithEmail(email: String, password: String): Result<String> {
        return try {
            android.util.Log.d("UserRepository", "Attempting signInWithEmail: $email")
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            android.util.Log.d("UserRepository", "SignIn API call completed")
            val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("User ID not found")
            android.util.Log.d("UserRepository", "SignIn successful, userId: $userId")
            Result.success(userId)
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "SignIn failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun signInWithPhone(phone: String, password: String): Result<String> {
        return try {
            supabase.auth.signInWith(Phone) {
                this.phone = phone
                this.password = password
            }
            val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("User ID not found")
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createUserProfile(
        userId: String,
        email: String? = null,
        phone: String? = null,
        role: String? = null,
        displayName: String,
        gamertag: String? = null
    ): Result<Unit> {
        return try {
            supabase.from("users").insert(
                buildJsonObject {
                    put("id", userId)
                    email?.let { put("email", it) }
                    phone?.let { put("phone", it) }
                    role?.let { put("role", it) }
                    put("display_name", displayName)
                    gamertag?.let { put("gamertag", it) }
                }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUserRole(userId: String, role: String): Result<Unit> {
        return try {
            supabase.from("users").update(
                buildJsonObject {
                    put("role", role)
                }
            ) {
                filter {
                    eq("id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateGamertag(userId: String, gamertag: String): Result<Unit> {
        return try {
            supabase.from("users").update(
                buildJsonObject {
                    put("gamertag", gamertag)
                    // Use ISO 8601 timestamp format for PostgreSQL
                    put("gamertag_last_changed", java.time.Instant.now().toString())
                }
            ) {
                filter {
                    eq("id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            android.util.Log.d("UserRepository", "Querying profile for userId: $userId")
            val response = supabase.from("users")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<User>()
            android.util.Log.d("UserRepository", "Query successful! User: $response")
            Result.success(response)
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Query failed: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun linkStudentToParent(studentId: String, parentId: String): Result<Unit> {
        return try {
            supabase.from("users").update(
                buildJsonObject {
                    put("linked_parent_id", parentId)
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
    
    suspend fun unlinkStudent(studentId: String): Result<Unit> {
        return try {
            supabase.from("users").update(
                buildJsonObject {
                    put("linked_parent_id", null)
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
    
    // Session Management
    fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }
    
    fun isUserLoggedIn(): Boolean {
        return supabase.auth.currentSessionOrNull() != null
    }
    
    suspend fun signOut() {
        try {
            supabase.auth.signOut()
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Error signing out: ${e.message}")
        }
    }
    
    suspend fun getCurrentUserProfile(): Result<User> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("No user logged in")
            getUserProfile(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
