package com.example.mywishlistapp

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.Data.WishRepository
import com.example.mywishlistapp.Data.UserProfile
import com.example.mywishlistapp.Data.UserProfileRepository
import com.example.mywishlistapp.Data.AchievementSystem
import com.example.mywishlistapp.Data.Achievement
import com.example.mywishlistapp.models.NotificationItem
import com.example.mywishlistapp.models.NotificationType
import com.example.mywishlistapp.notifications.ReminderSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class WishViewModel(
    application: Application,
    private val wishRepository: WishRepository? = null,
    private val userProfileRepository: UserProfileRepository? = null
) : AndroidViewModel(application) {

    private val actualWishRepository: WishRepository by lazy {
        wishRepository ?: Graph.wishRepository
    }
    
    private val actualUserProfileRepository: UserProfileRepository by lazy {
        userProfileRepository ?: Graph.userProfileRepository
    }

    private val reminderSystem by lazy { ReminderSystem(getApplication()) }
    private val achievementSystem = AchievementSystem

    // Notification state management
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // User profile and achievements
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    // Consolidated wish state
    var wishState by mutableStateOf(Wish())

    init {
        try {
            android.util.Log.d("WishViewModel", "Initializing WishViewModel")
            loadUserProfile()
            loadAchievements()
            android.util.Log.d("WishViewModel", "WishViewModel initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("WishViewModel", "Error initializing WishViewModel", e)
            // Don't rethrow - let the ViewModel be created with minimal state
        }
    }

    // Legacy individual state properties for backward compatibility
    var wishTitleState by mutableStateOf("")
    var wishDescriptionState by mutableStateOf("")
    var wishCategoryState by mutableStateOf("")
    var wishTagsState by mutableStateOf("")
    var wishPriorityState by mutableStateOf(Priority.MEDIUM)
    var wishPriceState by mutableStateOf("")
    var wishImageUrlState by mutableStateOf("")

    fun onWishTitleChanged(newString: String) {
        wishTitleState = newString
        wishState = wishState.copy(title = newString)
    }

    fun onWishDescriptionChanged(newString: String) {
        wishDescriptionState = newString
        wishState = wishState.copy(description = newString)
    }

    fun onWishCategoryChanged(newString: String) {
        wishCategoryState = newString
        wishState = wishState.copy(category = newString)
    }

    fun onWishTagsChanged(newString: String) {
        wishTagsState = newString
        val tagsList = if (newString.isBlank()) {
            emptyList()
        } else {
            newString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
        wishState = wishState.copy(tags = tagsList)
    }

    fun onWishPriorityChanged(newPriority: Priority) {
        wishPriorityState = newPriority
        wishState = wishState.copy(priority = newPriority)
    }

    fun onWishPriceChanged(newString: String) {
        wishPriceState = newString
        wishState = wishState.copy(price = newString)
    }

    fun onWishImageUrlChanged(newString: String) {
        wishImageUrlState = newString
        wishState = wishState.copy(imageUrl = newString)
    }

    // Convert tags string to list
    fun getTagsList(): List<String> {
        return if (wishTagsState.isBlank()) {
            emptyList()
        } else {
            wishTagsState.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }

    val getAllWishes: Flow<List<Wish>> by lazy {
        try {
            android.util.Log.d("WishViewModel", "Accessing actualWishRepository.getWishes()")
            val flow = actualWishRepository.getWishes()
            android.util.Log.d("WishViewModel", "Successfully got wishes flow")
            flow
        } catch (e: Exception) {
            android.util.Log.e("WishViewModel", "Error getting wishes flow", e)
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }

    fun addWish(wish: Wish){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                actualWishRepository.addWish(wish = wish)

                // Schedule reminder for high-priority wishes
                if (wish.priority == Priority.HIGH) {
                    try {
                        reminderSystem.scheduleSmartReminder(wish)
                    } catch (e: Exception) {
                        // Log error but don't crash - reminder failure shouldn't block wish creation
                        android.util.Log.e("WishViewModel", "Failed to schedule reminder for wish: ${wish.id}", e)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("WishViewModel", "Failed to add wish: ${wish.title}", e)
                // Optionally show error to user via notification
                addNotification("Error", "Failed to add wish", NotificationType.SYSTEM)
            }
        }
    }

    fun getWishbyId(id: Long) : Flow<Wish>{
        return actualWishRepository.getWishById(id = id)
    }

    fun updateWish(wish: Wish) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                actualWishRepository.updateWish(wish = wish)

                // Update or schedule reminder based on priority
                try {
                    if (wish.priority == Priority.HIGH) {
                        reminderSystem.scheduleSmartReminder(wish)
                    } else {
                        // Cancel existing reminder if priority is no longer high
                        reminderSystem.cancelReminder(wish.id)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("WishViewModel", "Failed to update reminder for wish: ${wish.id}", e)
                }
            } catch (e: Exception) {
                android.util.Log.e("WishViewModel", "Failed to update wish: ${wish.title}", e)
                addNotification("Error", "Failed to update wish", NotificationType.SYSTEM)
            }
        }
    }

    fun deleteWish(wish: Wish) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                actualWishRepository.deleteWish(wish = wish)

                // Cancel any existing reminders for deleted wish
                try {
                    reminderSystem.cancelReminder(wish.id)
                } catch (e: Exception) {
                    android.util.Log.e("WishViewModel", "Failed to cancel reminder for deleted wish: ${wish.id}", e)
                }
            } catch (e: Exception) {
                android.util.Log.e("WishViewModel", "Failed to delete wish: ${wish.title}", e)
                addNotification("Error", "Failed to delete wish", NotificationType.SYSTEM)
            }
        }
    }

    // Calendar and scheduling related functions
    fun updateWishScheduledDate(wishId: Long, scheduledDate: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentWish = actualWishRepository.getWishById(wishId).first()
                val updatedWish = currentWish.copy(scheduledDate = scheduledDate)
                actualWishRepository.updateWish(updatedWish)

                // Update reminder if needed
                try {
                    if (updatedWish.reminderSet) {
                        if (scheduledDate != null) {
                            reminderSystem.scheduleSmartReminder(updatedWish)
                        } else {
                            reminderSystem.cancelReminder(wishId)
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("WishViewModel", "Failed to update reminder for scheduled wish: $wishId", e)
                }
            } catch (e: Exception) {
                android.util.Log.e("WishViewModel", "Failed to update wish scheduled date: $wishId", e)
                addNotification("Error", "Failed to update schedule", NotificationType.SYSTEM)
            }
        }
    }

    fun toggleWishReminder(wishId: Long, reminderSet: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentWish = actualWishRepository.getWishById(wishId).first()
                val updatedWish = currentWish.copy(reminderSet = reminderSet)
                actualWishRepository.updateWish(updatedWish)

                // Schedule or cancel reminder
                try {
                    if (reminderSet && updatedWish.scheduledDate != null) {
                        reminderSystem.scheduleSmartReminder(updatedWish)
                    } else {
                        reminderSystem.cancelReminder(wishId)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("WishViewModel", "Failed to toggle reminder for wish: $wishId", e)
                }
            } catch (e: Exception) {
                android.util.Log.e("WishViewModel", "Failed to toggle wish reminder: $wishId", e)
                addNotification("Error", "Failed to update reminder", NotificationType.SYSTEM)
            }
        }
    }

    // Get wishes scheduled for a specific date
    @RequiresApi(Build.VERSION_CODES.O)
    fun getWishesForDate(date: LocalDate): Flow<List<Wish>> {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return getAllWishes.map { wishes ->
            wishes.filter { it.scheduledDate == dateString }
        }
    }

    // Get all wishes with scheduled dates
    fun getScheduledWishes(): Flow<List<Wish>> {
        return getAllWishes.map { wishes ->
            wishes.filter { it.scheduledDate != null }
        }
    }

    // Get wishes with reminders enabled
    fun getWishesWithReminders(): Flow<List<Wish>> {
        return getAllWishes.map { wishes ->
            wishes.filter { it.reminderSet }
        }
    }

    // Notification management functions
    fun removeNotification(notificationId: Int) {
        viewModelScope.launch {
            _notifications.value = _notifications.value.filter { it.id != notificationId }
        }
    }

    fun markNotificationAsRead(notificationId: Int) {
        viewModelScope.launch {
            _notifications.value = _notifications.value.map { notification ->
                if (notification.id == notificationId) {
                    notification.copy(isRead = true)
                } else {
                    notification
                }
            }
        }
    }

    fun addNotification(title: String, message: String, type: NotificationType) {
        viewModelScope.launch {
            val newNotification = NotificationItem(
                id = (_notifications.value.maxOfOrNull { it.id } ?: 0) + 1,
                title = title,
                message = message,
                timestamp = Date(),
                type = type,
                isRead = false
            )
            _notifications.value = listOf(newNotification) + _notifications.value
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            _notifications.value = emptyList()
        }
    }

    fun getUnreadNotificationCount(): StateFlow<Int> {
        return _notifications.map { notifications ->
            notifications.count { !it.isRead }
        }.stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    }

    // Gamification methods
    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val existingProfile = actualUserProfileRepository.getUserProfile()
                if (existingProfile != null) {
                    _userProfile.value = existingProfile
                } else {
                    // Create default profile if none exists
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
                }
            } catch (e: Exception) {
                android.util.Log.e("WishViewModel", "Failed to load user profile", e)
                // Use default profile if loading fails
                _userProfile.value = UserProfile()
            }
        }
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            _achievements.value = achievementSystem.getAllAchievements()
        }
    }

    private suspend fun updateUserProfile(updater: (UserProfile) -> UserProfile) {
        try {
            val currentProfile = _userProfile.value
            val updatedProfile = updater(currentProfile)
            _userProfile.value = updatedProfile
            actualUserProfileRepository.saveUserProfile(updatedProfile)
        } catch (e: Exception) {
            android.util.Log.e("WishViewModel", "Failed to update user profile", e)
            // Don't crash on profile update failure, just log the error
        }
    }

    private suspend fun checkAndUnlockAchievements(profile: UserProfile) {
        val unlockedAchievements = achievementSystem.checkAchievements(profile)
        val newAchievements = unlockedAchievements.filter { !it.isUnlocked }

        if (newAchievements.isNotEmpty()) {
            // Mark achievements as unlocked
            achievementSystem.unlockAchievements(newAchievements.map { it.id })

            // Update achievements list
            _achievements.value = achievementSystem.getAllAchievements()

            // Notify user about new achievements
            newAchievements.forEach { achievement ->
                addNotification(
                    title = "Achievement Unlocked!",
                    message = "🏆 ${achievement.name}: ${achievement.description}",
                    type = NotificationType.ACHIEVEMENT
                )
            }
        }
    }

    private suspend fun onWishAdded() {
        updateUserProfile { profile ->
            profile.copy(
                totalWishes = profile.totalWishes + 1,
                experiencePoints = profile.experiencePoints + 10
            )
        }
        checkAndUnlockAchievements(_userProfile.value)
    }

    private suspend fun onWishCompleted() {
        updateUserProfile { profile ->
            profile.copy(
                completedWishes = profile.completedWishes + 1,
                experiencePoints = profile.experiencePoints + 25
            )
        }
        checkAndUnlockAchievements(_userProfile.value)
    }

    private suspend fun onWishDeleted() {
        updateUserProfile { profile ->
            profile.copy(
                totalWishes = maxOf(0, profile.totalWishes - 1)
            )
        }
    }

    // Update username method
    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            updateUserProfile { profile ->
                profile.copy(username = newUsername)
            }
        }
    }

    // Enhanced wish management with gamification
    fun addWishWithGamification(wish: Wish) {
        viewModelScope.launch(Dispatchers.IO) {
            actualWishRepository.addWish(wish = wish)

            // Schedule reminder for high-priority wishes
            if (wish.priority == Priority.HIGH && reminderSystem != null) {
                reminderSystem.scheduleSmartReminder(wish)
            }

            // Update gamification stats
            onWishAdded()
        }
    }

    fun completeWish(wish: Wish) {
        viewModelScope.launch(Dispatchers.IO) {
            val completedWish = wish.copy(isCompleted = true)
            actualWishRepository.updateWish(completedWish)

            // Cancel reminders for completed wishes
            if (reminderSystem != null) {
                reminderSystem.cancelReminder(wish.id)
            }

            // Update gamification stats
            onWishCompleted()

            // Add completion notification
            addNotification(
                title = "Wish Completed!",
                message = "🎉 Congratulations! You've completed '${wish.title}'",
                type = NotificationType.WISH_UPDATE
            )
        }
    }

    fun deleteWishWithGamification(wish: Wish) {
        viewModelScope.launch(Dispatchers.IO) {
            actualWishRepository.deleteWish(wish = wish)

            // Cancel any existing reminders for deleted wish
            if (reminderSystem != null) {
                reminderSystem.cancelReminder(wish.id)
            }

            // Update gamification stats
            onWishDeleted()
        }
    }

    // Get user level based on experience points
    fun getUserLevel(): StateFlow<Int> {
        return _userProfile.map { profile ->
            profile.experiencePoints / 100 + 1
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1
        )
    }

    // Get progress to next level
    fun getLevelProgress(): StateFlow<Float> {
        return _userProfile.map { profile ->
            val currentLevelXP = (profile.experiencePoints / 100) * 100
            val progressInLevel = profile.experiencePoints - currentLevelXP
            progressInLevel / 100f
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )
    }

    // Get recent achievements (last 5)
    fun getRecentAchievements(): StateFlow<List<Achievement>> {
        return _achievements.map { achievements ->
            achievements.filter { achievement -> achievement.isUnlocked }
                .sortedByDescending { achievement -> achievement.unlockedAt }
                .take(5)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
    
    // Saving Goals Feature
    fun addFundsToWish(wishId: Long, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentWish = actualWishRepository.getWishById(wishId).first()
                val updatedWish = currentWish.copy(savedAmount = currentWish.savedAmount + amount)
                actualWishRepository.updateWish(updatedWish)
                
                // Add notification for funds added
                addNotification(
                    title = "Funds Added! 💰",
                    message = "Added $${String.format("%.2f", amount)} to '${currentWish.title}'",
                    type = NotificationType.WISH_UPDATE
                )
                
                // Check if goal is reached
                val targetPrice = currentWish.price.toDoubleOrNull() ?: 0.0
                if (targetPrice > 0 && updatedWish.savedAmount >= targetPrice) {
                    addNotification(
                        title = "Goal Reached! 🎯",
                        message = "You've saved enough for '${currentWish.title}'!",
                        type = NotificationType.ACHIEVEMENT
                    )
                }
            } catch (e: Exception) {
                // Handle error - could add error notification here
            }
        }
    }
}
