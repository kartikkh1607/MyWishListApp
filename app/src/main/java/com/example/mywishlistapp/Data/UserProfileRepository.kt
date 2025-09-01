package com.example.mywishlistapp.Data

import kotlinx.coroutines.flow.Flow

class UserProfileRepository(private val userProfileDao: UserProfileDao) {
    val userProfileFlow: Flow<UserProfile?> = userProfileDao.getUserProfile()
    
    suspend fun getUserProfile(): UserProfile? = userProfileDao.getUserProfileSync()
    suspend fun insertOrUpdate(userProfile: UserProfile) = userProfileDao.insertOrUpdateProfile(userProfile)
    suspend fun saveUserProfile(userProfile: UserProfile) = insertOrUpdate(userProfile)
    
    suspend fun resetProfile() {
        val defaultProfile = UserProfile(
            id = 1,
            username = "User",
            name = "",
            totalWishes = 0,
            completedWishes = 0,
            highPriorityCompleted = 0,
            currentStreak = 0,
            longestStreak = 0,
            totalMoneySaved = 0.0,
            level = 1,
            experiencePoints = 0,
            earnedBadges = emptyList(),
            theme = "System"
        )
        insertOrUpdate(defaultProfile)
    }
}
