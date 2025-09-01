package com.example.mywishlistapp.Data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MilestoneDao {
    
    @Query("SELECT * FROM `Milestone-Table` WHERE `wish-id` = :wishId ORDER BY `milestone-order` ASC, `milestone-created-date` ASC")
    fun getMilestonesForGoal(wishId: Long): Flow<List<Milestone>>
    
    @Query("SELECT * FROM `Milestone-Table` WHERE id = :milestoneId")
    suspend fun getMilestoneById(milestoneId: Long): Milestone?
    
    @Insert
    suspend fun insertMilestone(milestone: Milestone): Long
    
    @Update
    suspend fun updateMilestone(milestone: Milestone)
    
    @Delete
    suspend fun deleteMilestone(milestone: Milestone)
    
    @Query("DELETE FROM `Milestone-Table` WHERE `wish-id` = :wishId")
    suspend fun deleteMilestonesForGoal(wishId: Long)
    
    // Mark milestone as completed
    @Query("UPDATE `Milestone-Table` SET `milestone-is-completed` = 1, `milestone-completed-date` = :completedDate WHERE id = :milestoneId")
    suspend fun completeMilestone(milestoneId: Long, completedDate: Long = System.currentTimeMillis())
    
    // Mark milestone as incomplete
    @Query("UPDATE `Milestone-Table` SET `milestone-is-completed` = 0, `milestone-completed-date` = NULL WHERE id = :milestoneId")
    suspend fun uncompleteMilestone(milestoneId: Long)
    
    // Get milestone statistics for a goal
    @Query("""
        SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN `milestone-is-completed` = 1 THEN 1 ELSE 0 END) as completed,
            SUM(CASE WHEN `milestone-is-completed` = 1 THEN `milestone-weight` ELSE 0 END) as completedWeight,
            SUM(`milestone-weight`) as totalWeight
        FROM `Milestone-Table` 
        WHERE `wish-id` = :wishId
    """)
    suspend fun getMilestoneStats(wishId: Long): MilestoneStats?
    
    // Get all completed milestones for analytics
    @Query("SELECT * FROM `Milestone-Table` WHERE `milestone-is-completed` = 1 ORDER BY `milestone-completed-date` DESC")
    fun getAllCompletedMilestones(): Flow<List<Milestone>>
    
    // Get overdue milestones
    @Query("""
        SELECT * FROM `Milestone-Table` 
        WHERE `milestone-target-date` < :currentTime 
        AND `milestone-is-completed` = 0 
        AND `milestone-target-date` IS NOT NULL
        ORDER BY `milestone-target-date` ASC
    """)
    fun getOverdueMilestones(currentTime: Long = System.currentTimeMillis()): Flow<List<Milestone>>
    
    // Get upcoming milestones (next 7 days)
    @Query("""
        SELECT * FROM `Milestone-Table` 
        WHERE `milestone-target-date` BETWEEN :currentTime AND :nextWeek
        AND `milestone-is-completed` = 0 
        AND `milestone-target-date` IS NOT NULL
        ORDER BY `milestone-target-date` ASC
    """)
    fun getUpcomingMilestones(
        currentTime: Long = System.currentTimeMillis(),
        nextWeek: Long = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000)
    ): Flow<List<Milestone>>
}

// Data class for milestone statistics
data class MilestoneStats(
    val total: Int,
    val completed: Int,
    val completedWeight: Int,
    val totalWeight: Int
) {
    val completionPercentage: Int
        get() = if (totalWeight > 0) (completedWeight * 100 / totalWeight) else 0
        
    val simpleCompletionPercentage: Int
        get() = if (total > 0) (completed * 100 / total) else 0
}
