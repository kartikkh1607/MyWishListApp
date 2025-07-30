package com.example.mywishlistapp.Data

import kotlinx.coroutines.flow.Flow

class UserProfileRepository(private val userProfileDao: UserProfileDao) {
    val userProfileFlow: Flow<UserProfile?> = userProfileDao.getUserProfile()
    
    suspend fun getUserProfile(): UserProfile? = userProfileDao.getUserProfileSync()
    suspend fun insertOrUpdate(userProfile: UserProfile) = userProfileDao.insertOrUpdateProfile(userProfile)
    suspend fun saveUserProfile(userProfile: UserProfile) = insertOrUpdate(userProfile)
}
