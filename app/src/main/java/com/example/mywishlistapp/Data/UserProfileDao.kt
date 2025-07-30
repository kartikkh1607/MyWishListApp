package com.example.mywishlistapp.Data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>
    
    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileSync(): UserProfile?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)
    
    @Update
    suspend fun updateProfile(profile: UserProfile)
    
    @Query("UPDATE user_profile SET total_wishes = :count WHERE id = 1")
    suspend fun updateTotalWishes(count: Int)
    
    @Query("UPDATE user_profile SET completed_wishes = :count WHERE id = 1")
    suspend fun updateCompletedWishes(count: Int)
    
    @Query("UPDATE user_profile SET high_priority_completed = :count WHERE id = 1")
    suspend fun updateHighPriorityCompleted(count: Int)
    
    @Query("UPDATE user_profile SET current_streak = :streak WHERE id = 1")
    suspend fun updateCurrentStreak(streak: Int)
    
    @Query("UPDATE user_profile SET longest_streak = :streak WHERE id = 1")
    suspend fun updateLongestStreak(streak: Int)
    
    @Query("UPDATE user_profile SET experience_points = :xp, level = :level WHERE id = 1")
    suspend fun updateXpAndLevel(xp: Int, level: Int)
    
    @Query("UPDATE user_profile SET earned_badges = :badges WHERE id = 1")
    suspend fun updateEarnedBadges(badges: List<String>)
    
    @Query("UPDATE user_profile SET total_money_saved = :amount WHERE id = 1")
    suspend fun updateTotalMoneySaved(amount: Double)
}
