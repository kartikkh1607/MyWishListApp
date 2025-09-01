package com.example.mywishlistapp.Data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
abstract class WishDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun addWish(wishEntity: Wish)

    // Optimized query with ordering by creation date (most recent first)
    @Query("SELECT * FROM `Wish-Table` ORDER BY id DESC")
    abstract fun getAllWishes(): Flow<List<Wish>>

    // Get wishes with pagination for large datasets
    @Query("SELECT * FROM `Wish-Table` ORDER BY id DESC LIMIT :limit OFFSET :offset")
    abstract suspend fun getWishesPaginated(limit: Int, offset: Int): List<Wish>

    // Optimized queries for filtering
    @Query("SELECT * FROM `Wish-Table` WHERE `wish-category` = :category ORDER BY id DESC")
    abstract fun getWishesByCategory(category: String): Flow<List<Wish>>

    @Query("SELECT * FROM `Wish-Table` WHERE `wish-priority` = :priority ORDER BY id DESC")
    abstract fun getWishesByPriority(priority: Priority): Flow<List<Wish>>

    @Query("SELECT * FROM `Wish-Table` WHERE `wish-is-completed` = :isCompleted ORDER BY id DESC")
    abstract fun getWishesByCompletionStatus(isCompleted: Boolean): Flow<List<Wish>>

    @Query("SELECT * FROM `Wish-Table` WHERE `wish-is-goal` = 1 ORDER BY id DESC")
    abstract fun getGoals(): Flow<List<Wish>>

    // Optimized search query with LIKE operator and indices
    @Query("SELECT * FROM `Wish-Table` WHERE `wish-title` LIKE '%' || :searchQuery || '%' OR `wish-description` LIKE '%' || :searchQuery || '%' OR `wish-category` LIKE '%' || :searchQuery || '%' ORDER BY id DESC")
    abstract fun searchWishes(searchQuery: String): Flow<List<Wish>>

    // Count queries for dashboard statistics (more efficient than loading all data)
    @Query("SELECT COUNT(*) FROM `Wish-Table`")
    abstract suspend fun getTotalWishCount(): Int

    @Query("SELECT COUNT(*) FROM `Wish-Table` WHERE `wish-is-completed` = 1")
    abstract suspend fun getCompletedWishCount(): Int

    @Query("SELECT COUNT(*) FROM `Wish-Table` WHERE `wish-priority` = 'HIGH'")
    abstract suspend fun getHighPriorityWishCount(): Int

    @Query("SELECT COUNT(*) FROM `Wish-Table` WHERE `wish-is-goal` = 1")
    abstract suspend fun getGoalCount(): Int

    // Get recent wishes (last 5) for dashboard
    @Query("SELECT * FROM `Wish-Table` ORDER BY id DESC LIMIT 5")
    abstract fun getRecentWishes(): Flow<List<Wish>>
    
    // New queries for Dashboard redesign
    @Query("SELECT * FROM `Wish-Table` WHERE `wish-target-date` > :currentDate AND `wish-is-completed` = 0 ORDER BY `wish-target-date` ASC LIMIT 5")
    abstract fun getUpcomingItems(currentDate: Long): Flow<List<Wish>>
    
    @Query("SELECT * FROM `Wish-Table` WHERE `wish-progress` > 0 AND `wish-progress` < 100 AND `wish-is-completed` = 0 ORDER BY `wish-progress` DESC")
    abstract fun getInProgressItems(): Flow<List<Wish>>
    
    // New query for Calendar/Journey View
    @Query("SELECT * FROM `Wish-Table` WHERE (`wish-created-date` BETWEEN :startDate AND :endDate) OR (`wish-target-date` BETWEEN :startDate AND :endDate) ORDER BY `wish-created-date` DESC")
    abstract fun getItemsByDate(startDate: Long, endDate: Long): Flow<List<Wish>>

    @Update
    abstract suspend fun updateAWish(wishEntity: Wish)

    @Delete
    abstract suspend fun deleteAWish(wishEntity: Wish)
    
    // Clear all data function for settings
    @Query("DELETE FROM `Wish-Table`")
    abstract suspend fun deleteAll()

    @Query("SELECT * FROM `Wish-Table` Where id = :id")
    abstract fun getAWishById(id: Long): Flow<Wish>

}
