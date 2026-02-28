package com.CuriosityLabs.digibalance.data.local.dao

import androidx.room.*
import com.CuriosityLabs.digibalance.data.local.entity.UserSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSessionDao {
    
    @Query("SELECT * FROM user_session WHERE id = 1 LIMIT 1")
    suspend fun getSession(): UserSessionEntity?
    
    @Query("SELECT * FROM user_session WHERE id = 1 LIMIT 1")
    fun getSessionFlow(): Flow<UserSessionEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: UserSessionEntity)
    
    @Query("UPDATE user_session SET isLoggedIn = :isLoggedIn WHERE id = 1")
    suspend fun updateLoginStatus(isLoggedIn: Boolean)
    
    @Query("UPDATE user_session SET userId = :userId, userRole = :role, gamertag = :gamertag, email = :email, phone = :phone WHERE id = 1")
    suspend fun updateUserInfo(userId: String?, role: String?, gamertag: String?, email: String?, phone: String?)
    
    @Query("UPDATE user_session SET hasCompletedOnboarding = :completed WHERE id = 1")
    suspend fun updateOnboardingStatus(completed: Boolean)
    
    @Query("UPDATE user_session SET hasCompletedSurvey = :completed WHERE id = 1")
    suspend fun updateSurveyStatus(completed: Boolean)
    
    @Query("UPDATE user_session SET hasSelectedRole = :selected WHERE id = 1")
    suspend fun updateRoleSelection(selected: Boolean)
    
    @Query("UPDATE user_session SET hasCreatedGamertag = :created WHERE id = 1")
    suspend fun updateGamertagCreation(created: Boolean)
    
    @Query("UPDATE user_session SET hasGrantedPermissions = :granted WHERE id = 1")
    suspend fun updatePermissionsStatus(granted: Boolean)
    
    @Query("UPDATE user_session SET lastScreen = :screen WHERE id = 1")
    suspend fun updateLastScreen(screen: String)
    
    @Query("DELETE FROM user_session")
    suspend fun clearSession()
}
