package com.example.mywishlistapp.Data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


class WishRepository(private val wishDao: WishDao) {
    
    // Cache for frequently accessed data
    private val cacheMutex = Mutex()
    private var statsCache: WishStats? = null
    private var statsCacheTime = 0L
    private val cacheValidityMs = 30_000L // 30 seconds
    
    data class WishStats(
        val totalWishes: Int,
        val completedWishes: Int,
        val highPriorityWishes: Int,
        val goalCount: Int
    )

    suspend fun addWish(wish: Wish){
        wishDao.addWish(wish)
        invalidateStatsCache()
    }

    fun getWishes(): Flow<List<Wish>> = wishDao.getAllWishes()
    
    // Optimized method for getting recent wishes (for dashboard)
    fun getRecentWishes(): Flow<List<Wish>> = wishDao.getRecentWishes()
    
    // Pagination support for large datasets
    suspend fun getWishesPaginated(page: Int, pageSize: Int): List<Wish> {
        val offset = page * pageSize
        return wishDao.getWishesPaginated(pageSize, offset)
    }
    
    // Optimized filtering methods
    fun getWishesByCategory(category: String): Flow<List<Wish>> = 
        wishDao.getWishesByCategory(category)
    
    fun getWishesByPriority(priority: Priority): Flow<List<Wish>> = 
        wishDao.getWishesByPriority(priority)
    
    fun getCompletedWishes(): Flow<List<Wish>> = 
        wishDao.getWishesByCompletionStatus(true)
    
    fun getPendingWishes(): Flow<List<Wish>> = 
        wishDao.getWishesByCompletionStatus(false)
    
    fun getGoals(): Flow<List<Wish>> = wishDao.getGoals()
    
    // Search functionality
    fun searchWishes(query: String): Flow<List<Wish>> = 
        wishDao.searchWishes(query)

    fun getWishById(id: Long): Flow<Wish>{
        return wishDao.getAWishById(id)
    }

    suspend fun updateWish(wish: Wish) {
        wishDao.updateAWish(wish)
        invalidateStatsCache()
    }

    suspend fun deleteWish(wish: Wish) {
        wishDao.deleteAWish(wish)
        invalidateStatsCache()
    }
    
    // Optimized stats methods with caching
    suspend fun getWishStats(): WishStats {
        cacheMutex.withLock {
            val currentTime = System.currentTimeMillis()
            if (statsCache != null && (currentTime - statsCacheTime) < cacheValidityMs) {
                return statsCache!!
            }
            
            // Refresh cache
            val stats = WishStats(
                totalWishes = wishDao.getTotalWishCount(),
                completedWishes = wishDao.getCompletedWishCount(),
                highPriorityWishes = wishDao.getHighPriorityWishCount(),
                goalCount = wishDao.getGoalCount()
            )
            
            statsCache = stats
            statsCacheTime = currentTime
            return stats
        }
    }
    
    private suspend fun invalidateStatsCache() {
        cacheMutex.withLock {
            statsCache = null
        }
    }
    
    // Utility methods for common calculations
    fun getWishesWithProgress(): Flow<List<Wish>> = 
        getGoals().map { wishes -> 
            wishes.filter { it.progress > 0 }
        }
    
    fun getOverdueGoals(): Flow<List<Wish>> = 
        getGoals().map { wishes ->
            val currentTime = System.currentTimeMillis()
            wishes.filter { 
                it.targetDate != null && 
                it.targetDate < currentTime && 
                !it.isCompleted
            }
        }
}
