package com.example.mywishlistapp

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.Data.WishRepository
import com.example.mywishlistapp.Data.UserProfileRepository
import com.example.mywishlistapp.Data.UserProfile
import com.example.mywishlistapp.Data.AchievementSystem
import com.example.mywishlistapp.Data.Achievement
import com.example.mywishlistapp.models.NotificationItem
import com.example.mywishlistapp.models.NotificationType
import com.example.mywishlistapp.notifications.ReminderSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class MinimalWishViewModel(application: Application) : AndroidViewModel(application) {
    
    // Test lazy repository initialization
    private val actualWishRepository: WishRepository by lazy {
        Log.d("MinimalWishViewModel", "Initializing actualWishRepository")
        Graph.wishRepository
    }
    
    private val actualUserProfileRepository: UserProfileRepository by lazy {
        Log.d("MinimalWishViewModel", "Initializing actualUserProfileRepository")
        Graph.userProfileRepository
    }

    private val achievementSystem = AchievementSystem
    
    // Test ReminderSystem initialization
    private val reminderSystem by lazy { ReminderSystem(getApplication()) }

    // User profile and achievements state
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()
    
    // Test Notification StateFlows
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    init {
        Log.d("MinimalWishViewModel", "Creating MinimalWishViewModel")
        try {
            Log.d("MinimalWishViewModel", "Graph object exists: ${Graph}")
            Log.d("MinimalWishViewModel", "Application: $application")
            // Test accessing repositories
            Log.d("MinimalWishViewModel", "Testing repository access...")
            val repo = actualWishRepository
            Log.d("MinimalWishViewModel", "WishRepository accessed successfully: $repo")
            val userRepo = actualUserProfileRepository
            Log.d("MinimalWishViewModel", "UserProfileRepository accessed successfully: $userRepo")
            
            // Test the problematic initialization calls
            Log.d("MinimalWishViewModel", "Testing loadUserProfile...")
            loadUserProfile()
            Log.d("MinimalWishViewModel", "loadUserProfile successful")
            
            Log.d("MinimalWishViewModel", "Testing loadAchievements...")
            loadAchievements()
            Log.d("MinimalWishViewModel", "loadAchievements successful")
            
            // Test ReminderSystem initialization
            Log.d("MinimalWishViewModel", "Testing ReminderSystem...")
            val reminder = reminderSystem
            Log.d("MinimalWishViewModel", "ReminderSystem created successfully: $reminder")
            
        } catch (e: Exception) {
            Log.e("MinimalWishViewModel", "Error in init", e)
            throw e // Re-throw to see what fails
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                Log.d("MinimalWishViewModel", "Loading user profile...")
                val existingProfile = actualUserProfileRepository.getUserProfile()
                if (existingProfile != null) {
                    _userProfile.value = existingProfile
                    Log.d("MinimalWishViewModel", "Loaded existing profile: ${existingProfile.username}")
                } else {
                    Log.d("MinimalWishViewModel", "Creating default profile...")
                    val defaultProfile = UserProfile(
                        id = 1,
                        username = "Player",
                        level = 1,
                        experiencePoints = 0,
                        totalWishes = 0,
                        completedWishes = 0,
                        currentStreak = 0,
                        longestStreak = 0
                    )
                    actualUserProfileRepository.saveUserProfile(defaultProfile)
                    _userProfile.value = defaultProfile
                    Log.d("MinimalWishViewModel", "Created and saved default profile")
                }
            } catch (e: Exception) {
                Log.e("MinimalWishViewModel", "Failed to load user profile", e)
                // Use default profile if loading fails
                _userProfile.value = UserProfile()
            }
        }
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            try {
                Log.d("MinimalWishViewModel", "Loading achievements...")
                _achievements.value = achievementSystem.getAllAchievements()
                Log.d("MinimalWishViewModel", "Loaded ${_achievements.value.size} achievements")
            } catch (e: Exception) {
                Log.e("MinimalWishViewModel", "Failed to load achievements", e)
                _achievements.value = emptyList()
            }
        }
    }

    // Basic state
    var wishTitleState by mutableStateOf("")
    var wishDescriptionState by mutableStateOf("")
    var wishCategoryState by mutableStateOf("")
    var wishTagsState by mutableStateOf("")
    var wishPriorityState by mutableStateOf(Priority.MEDIUM)
    var wishPriceState by mutableStateOf("")
    var wishImageUrlState by mutableStateOf("")

    // Simple getter for testing
    val getAllWishes: Flow<List<Wish>> = flowOf(emptyList())

    fun onWishTitleChanged(newString: String) {
        wishTitleState = newString
    }

    fun onWishDescriptionChanged(newString: String) {
        wishDescriptionState = newString
    }

    fun onWishCategoryChanged(newString: String) {
        wishCategoryState = newString
    }

    fun onWishTagsChanged(newString: String) {
        wishTagsState = newString
    }

    fun onWishPriorityChanged(newPriority: Priority) {
        wishPriorityState = newPriority
    }

    fun onWishPriceChanged(newString: String) {
        wishPriceState = newString
    }

    fun onWishImageUrlChanged(newString: String) {
        wishImageUrlState = newString
    }

    fun getTagsList(): List<String> {
        return if (wishTagsState.isBlank()) {
            emptyList()
        } else {
            wishTagsState.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }

    fun addWish(wish: Wish) {
        Log.d("MinimalWishViewModel", "AddWish called with: ${wish.title}")
    }

    fun getWishbyId(id: Long): Flow<Wish> {
        return flowOf(Wish())
    }

    fun updateWish(wish: Wish) {
        Log.d("MinimalWishViewModel", "UpdateWish called with: ${wish.title}")
    }

    fun deleteWish(wish: Wish) {
        Log.d("MinimalWishViewModel", "DeleteWish called with: ${wish.title}")
    }
}
