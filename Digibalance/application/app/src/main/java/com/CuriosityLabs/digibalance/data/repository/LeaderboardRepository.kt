package com.CuriosityLabs.digibalance.data.repository

import com.CuriosityLabs.digibalance.DigiBalanceApplication
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// TODO-10: Fixed serialization by ensuring @Serializable annotation
@Serializable
data class LeaderboardEntry(
    val user_id: String,
    val gamertag: String,
    val productive_hours_weekly: Double,
    val focus_sessions_weekly: Int = 0,
    val distraction_alerts_weekly: Int = 0
)

class LeaderboardRepository {
    
    private val supabase = DigiBalanceApplication.supabaseClient
    
    suspend fun getWeeklyLeaderboard(limit: Int = 10): Result<List<LeaderboardEntry>> {
        return try {
            android.util.Log.d("LeaderboardRepository", "Fetching leaderboard data...")
            
            // Try to fetch from leaderboard_stats table
            val response = try {
                supabase.from("leaderboard_stats")
                    .select()
                    .decodeList<LeaderboardEntry>()
            } catch (e: Exception) {
                android.util.Log.e("LeaderboardRepository", "Error fetching from leaderboard_stats: ${e.message}")
                // If leaderboard_stats doesn't exist or has issues, return empty list
                emptyList()
            }
            
            val sorted = response
                .sortedByDescending { it.productive_hours_weekly }
                .take(limit)
            
            android.util.Log.d("LeaderboardRepository", "Successfully fetched ${sorted.size} entries")
            Result.success(sorted)
        } catch (e: Exception) {
            android.util.Log.e("LeaderboardRepository", "Error in getWeeklyLeaderboard", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateUserProductiveHours(userId: String, hours: Double): Result<Unit> {
        return try {
            supabase.from("leaderboard_stats").upsert(
                buildJsonObject {
                    put("user_id", userId)
                    put("productive_hours_weekly", hours)
                }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserRank(userId: String): Result<Int> {
        return try {
            val allEntries = supabase.from("leaderboard_stats")
                .select()
                .decodeList<LeaderboardEntry>()
            
            val rank = allEntries
                .sortedByDescending { it.productive_hours_weekly }
                .indexOfFirst { it.user_id == userId } + 1
            
            Result.success(if (rank > 0) rank else -1)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
