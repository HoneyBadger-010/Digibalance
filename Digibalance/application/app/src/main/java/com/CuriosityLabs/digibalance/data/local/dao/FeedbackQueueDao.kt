package com.CuriosityLabs.digibalance.data.local.dao

import androidx.room.*
import com.CuriosityLabs.digibalance.data.local.entity.FeedbackQueueEntity

@Dao
interface FeedbackQueueDao {
    
    @Query("SELECT * FROM feedback_queue WHERE isSynced = 0 ORDER BY createdAt ASC")
    suspend fun getUnsyncedFeedback(): List<FeedbackQueueEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: FeedbackQueueEntity)
    
    @Update
    suspend fun updateFeedback(feedback: FeedbackQueueEntity)
    
    @Delete
    suspend fun deleteFeedback(feedback: FeedbackQueueEntity)
    
    @Query("DELETE FROM feedback_queue WHERE isSynced = 1 AND createdAt < :beforeTimestamp")
    suspend fun deleteOldSyncedFeedback(beforeTimestamp: Long)
}
