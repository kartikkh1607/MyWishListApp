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
import com.example.mywishlistapp.Data.MilestoneRepository
import com.example.mywishlistapp.Data.Milestone
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
import java.util.concurrent.TimeUnit

class WishViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val wishRepository: WishRepository? = null
    private val userProfileRepository: UserProfileRepository? = null
    private val milestoneRepository: MilestoneRepository? = null
    
    private val actualWishRepository: WishRepository by lazy {
        wishRepository ?: Graph.wishRepository
    }
    
    private val actualUserProfileRepository: UserProfileRepository by lazy {
        userProfileRepository ?: Graph.userProfileRepository
    }
    
    private val actualMilestoneRepository: MilestoneRepository by lazy {
        milestoneRepository ?: Graph.milestoneRepository
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
            loadWishStats()
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
    
    // Personal Growth Companion state variables
    var wishIsGoalState by mutableStateOf(false)
    var wishTargetDateState by mutableStateOf<Long?>(null)
    var wishProgressState by mutableStateOf(0)

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
    
    // Personal Growth Companion handler functions
    fun onWishIsGoalChanged(isGoal: Boolean) {
        wishIsGoalState = isGoal
        wishState = wishState.copy(isGoal = isGoal)
    }
    
    fun onWishTargetDateChanged(targetDate: Long?) {
        wishTargetDateState = targetDate
        wishState = wishState.copy(targetDate = targetDate)
    }
    
    fun onWishProgressChanged(progress: Int) {
        wishProgressState = progress.coerceIn(0, 100)
        wishState = wishState.copy(progress = wishProgressState)
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
            val flow = actualWishRepository.getWishes().stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
            android.util.Log.d("WishViewModel", "Successfully got wishes flow")
            flow
        } catch (e: Exception) {
            android.util.Log.e("WishViewModel", "Error getting wishes flow", e)
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }
    
    // Optimized flows for specific use cases
    val recentWishes: StateFlow<List<Wish>> by lazy {
        actualWishRepository.getRecentWishes().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
    
    val goals: StateFlow<List<Wish>> by lazy {
        actualWishRepository.getGoals().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
    
    // Cached stats for dashboard
    private val _wishStats = MutableStateFlow<WishRepository.WishStats?>(null)
    val wishStats: StateFlow<WishRepository.WishStats?> = _wishStats.asStateFlow()
    
    // Dashboard StateFlows for upcoming and in-progress goals
    val upcomingGoals: StateFlow<List<Wish>> by lazy {
        actualWishRepository.getUpcomingGoals(System.currentTimeMillis()).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
    
    val inProgressGoals: StateFlow<List<Wish>> by lazy {
        actualWishRepository.getInProgressGoals().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
    
    // Calendar/Journey View StateFlow for monthly items
    fun getItemsForMonth(startDate: Long, endDate: Long): Flow<List<Wish>> {
        return actualWishRepository.getItemsForMonth(startDate, endDate)
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
    
    private fun loadWishStats() {
        viewModelScope.launch {
            try {
                _wishStats.value = actualWishRepository.getWishStats()
            } catch (e: Exception) {
                android.util.Log.e("WishViewModel", "Failed to load wish stats", e)
                // Use default stats if loading fails
                _wishStats.value = WishRepository.WishStats(0, 0, 0, 0)
            }
        }
    }
    
    fun refreshWishStats() {
        loadWishStats()
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
    
    // Save user name for onboarding/personalization
    fun saveUserName(name: String) {
        viewModelScope.launch {
            try {
                updateUserProfile { profile ->
                    profile.copy(name = name.trim())
                }
                android.util.Log.d("WishViewModel", "User name saved: $name")
            } catch (e: Exception) {
                android.util.Log.e("WishViewModel", "Failed to save user name", e)
            }
        }
    }
    
    // Theme management
    val currentTheme: StateFlow<String> = _userProfile.map { profile ->
        profile.theme
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "System"
    )
    
    fun setTheme(theme: String) {
        viewModelScope.launch {
            try {
                updateUserProfile { profile ->
                    profile.copy(theme = theme)
                }
                android.util.Log.d("WishViewModel", "Theme updated to: $theme")
            } catch (e: Exception) {
                android.util.Log.e("WishViewModel", "Failed to update theme", e)
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
    
    // ===== PERSONAL GROWTH COMPANION ANALYTICS =====
    
    // Data classes for analytics
    data class GoalAnalytics(
        val totalGoals: Int = 0,
        val completedGoals: Int = 0,
        val activeGoals: Int = 0,
        val averageProgress: Float = 0f,
        val goalsWithDeadlines: Int = 0,
        val overdue: Int = 0,
        val completionRate: Float = 0f
    )
    
    data class MotivationalInsight(
        val message: String,
        val type: InsightType,
        val actionSuggestion: String? = null,
        val emoji: String = "💪"
    )
    
    enum class InsightType {
        ENCOURAGEMENT, CELEBRATION, REMINDER, TIP, MILESTONE
    }
    
    data class UpcomingDeadline(
        val wish: Wish,
        val daysUntilDeadline: Long,
        val urgencyLevel: UrgencyLevel
    )
    
    enum class UrgencyLevel {
        OVERDUE, URGENT, SOON, NORMAL
    }
    
    // Analytics Functions
    
    fun getGoalAnalytics(): Flow<GoalAnalytics> {
        return getAllWishes.map { wishes ->
            val goals = wishes.filter { it.isGoal }
            val completed = goals.count { it.progress >= 100 }
            val active = goals.count { it.progress < 100 }
            val withDeadlines = goals.count { it.targetDate != null }
            val overdue = getOverdueGoalsCount(goals)
            val avgProgress = if (goals.isNotEmpty()) goals.map { it.progress }.average().toFloat() else 0f
            val completionRate = if (goals.isNotEmpty()) completed.toFloat() / goals.size.toFloat() else 0f
            
            GoalAnalytics(
                totalGoals = goals.size,
                completedGoals = completed,
                activeGoals = active,
                averageProgress = avgProgress,
                goalsWithDeadlines = withDeadlines,
                overdue = overdue,
                completionRate = completionRate
            )
        }
    }
    
    fun getActiveGoalsWithProgress(): Flow<List<Wish>> {
        return getAllWishes.map { wishes ->
            wishes.filter { it.isGoal && it.progress < 100 && !it.isCompleted }
                .sortedByDescending { it.progress }
        }
    }
    
    fun getUpcomingDeadlines(): Flow<List<UpcomingDeadline>> {
        return getAllWishes.map { wishes ->
            val now = System.currentTimeMillis()
            wishes.filter { it.isGoal && it.targetDate != null && it.progress < 100 }
                .mapNotNull { wish ->
                    wish.targetDate?.let { targetDate ->
                        val daysUntil = TimeUnit.MILLISECONDS.toDays(targetDate - now)
                        val urgency = when {
                            daysUntil < 0 -> UrgencyLevel.OVERDUE
                            daysUntil <= 3 -> UrgencyLevel.URGENT
                            daysUntil <= 7 -> UrgencyLevel.SOON
                            else -> UrgencyLevel.NORMAL
                        }
                        UpcomingDeadline(wish, daysUntil, urgency)
                    }
                }
                .sortedBy { it.daysUntilDeadline }
        }
    }
    
    fun getMotivationalInsights(): Flow<List<MotivationalInsight>> {
        return getAllWishes.map { wishes ->
            val insights = mutableListOf<MotivationalInsight>()
            val goals = wishes.filter { it.isGoal }
            val completed = goals.count { it.progress >= 100 }
            val totalGoals = goals.size
            
            // Completion rate insights
            when {
                totalGoals == 0 -> {
                    insights.add(
                        MotivationalInsight(
                            message = "Ready to start your growth journey?",
                            type = InsightType.ENCOURAGEMENT,
                            actionSuggestion = "Create your first goal to begin tracking your progress!",
                            emoji = "🌱"
                        )
                    )
                }
                completed == 0 && totalGoals > 0 -> {
                    insights.add(
                        MotivationalInsight(
                            message = "Every journey begins with a single step!",
                            type = InsightType.ENCOURAGEMENT,
                            actionSuggestion = "Update the progress on your goals to see how far you've come.",
                            emoji = "🚀"
                        )
                    )
                }
                completed.toFloat() / totalGoals >= 0.8f -> {
                    insights.add(
                        MotivationalInsight(
                            message = "Wow! You're absolutely crushing your goals!",
                            type = InsightType.CELEBRATION,
                            actionSuggestion = "Consider setting new challenging goals to keep growing.",
                            emoji = "🏆"
                        )
                    )
                }
                completed > 0 -> {
                    insights.add(
                        MotivationalInsight(
                            message = "Great progress! You've completed $completed ${if (completed == 1) "goal" else "goals"}.",
                            type = InsightType.MILESTONE,
                            emoji = "🎯"
                        )
                    )
                }
            }
            
            // Progress insights
            val highProgressGoals = goals.filter { it.progress >= 80 && it.progress < 100 }
            if (highProgressGoals.isNotEmpty()) {
                insights.add(
                    MotivationalInsight(
                        message = "You're so close to achieving ${highProgressGoals.size} ${if (highProgressGoals.size == 1) "goal" else "goals"}!",
                        type = InsightType.ENCOURAGEMENT,
                        actionSuggestion = "Push through the final stretch - you've got this!",
                        emoji = "💪"
                    )
                )
            }
            
            // Deadline insights
            val now = System.currentTimeMillis()
            val urgentGoals = goals.filter { 
                it.targetDate != null && 
                TimeUnit.MILLISECONDS.toDays(it.targetDate!! - now) <= 7 &&
                it.progress < 100
            }
            if (urgentGoals.isNotEmpty()) {
                insights.add(
                    MotivationalInsight(
                        message = "${urgentGoals.size} ${if (urgentGoals.size == 1) "goal has" else "goals have"} upcoming deadlines!",
                        type = InsightType.REMINDER,
                        actionSuggestion = "Focus your energy on these time-sensitive goals.",
                        emoji = "⏰"
                    )
                )
            }
            
            // Stagnant goals insight
            val stagnantGoals = goals.filter { it.progress == 0 && it.targetDate != null }
            if (stagnantGoals.size >= 3) {
                insights.add(
                    MotivationalInsight(
                        message = "Break down your goals into smaller, actionable steps.",
                        type = InsightType.TIP,
                        actionSuggestion = "Start with just 10-15 minutes of progress today!",
                        emoji = "🧩"
                    )
                )
            }
            
            insights.take(3) // Limit to 3 insights to avoid overwhelming the user
        }
    }
    
    fun getCompletionStats(): Flow<Pair<Int, Int>> {
        return getAllWishes.map { wishes ->
            val totalWishes = wishes.size
            val totalGoals = wishes.count { it.isGoal }
            Pair(totalWishes - totalGoals, totalGoals) // (wishes, goals)
        }
    }
    
    fun getAverageGoalProgress(): Flow<Float> {
        return getAllWishes.map { wishes ->
            val activeGoals = wishes.filter { it.isGoal && it.progress < 100 }
            if (activeGoals.isNotEmpty()) {
                activeGoals.map { it.progress }.average().toFloat()
            } else 0f
        }
    }
    
    fun getStreakData(): Flow<Int> {
        // For now, return current streak from user profile
        // In a real implementation, you'd calculate streaks based on goal completion dates
        return _userProfile.map { it.currentStreak }
    }
    
    private fun getOverdueGoalsCount(goals: List<Wish>): Int {
        val now = System.currentTimeMillis()
        return goals.count { goal ->
            goal.targetDate != null && goal.targetDate!! < now && goal.progress < 100
        }
    }
    
    // ===== MILESTONE MANAGEMENT =====
    
    // Get milestones for a specific goal
    fun getMilestonesForGoal(wishId: Long): Flow<List<Milestone>> {
        return actualMilestoneRepository.getMilestonesForGoal(wishId)
    }
    
    // Add a new milestone to a goal
    fun addMilestone(wishId: Long, title: String, description: String? = null, dueDate: Long? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val milestone = Milestone(
                    wishId = wishId,
                    title = title,
                    description = description ?: "",
                    targetDate = dueDate,
                    isCompleted = false,
                    completedDate = null,
                    createdDate = System.currentTimeMillis()
                )
                actualMilestoneRepository.insertMilestone(milestone)
                
                addNotification(
                    title = "Milestone Added! 🎯",
                    message = "Added milestone: $title",
                    type = NotificationType.WISH_UPDATE
                )
            } catch (e: Exception) {
                android.util.Log.e("WishViewModel", "Failed to add milestone", e)
                addNotification(
                    title = "Error",
                    message = "Failed to add milestone",
                    type = NotificationType.SYSTEM
                )
            }
        }
    }
    
    // Update an existing milestone
    fun updateMilestone(milestone: Milestone) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                actualMilestoneRepository.updateMilestone(milestone)
            } catch (e: Exception) {
                android.util.Log.e("WishViewModel", "Failed to update milestone", e)
            }
        }
    }
    
    // Delete a milestone
    fun deleteMilestone(milestone: Milestone) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                actualMilestoneRepository.deleteMilestone(milestone)
                
                addNotification(
                    title = "Milestone Removed",
                    message = "Milestone '${milestone.title}' has been deleted",
                    type = NotificationType.WISH_UPDATE
                )
            } catch (e: Exception) {
                android.util.Log.e("WishViewModel", "Failed to delete milestone", e)
            }
        }
    }
    
    // Complete a milestone
    fun completeMilestone(milestoneId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                actualMilestoneRepository.completeMilestone(milestoneId)
                
                // Get the milestone to show notification
                val milestone = actualMilestoneRepository.getMilestoneById(milestoneId)
                milestone?.let {
                    addNotification(
                        title = "Milestone Completed! 🎉",
                        message = "Great job completing: ${it.title}",
                        type = NotificationType.ACHIEVEMENT
                    )
                }
                
                // Update goal progress automatically based on milestone completion
                updateGoalProgressFromMilestones(milestone?.wishId ?: 0L)
            } catch (e: Exception) {
                android.util.Log.e("WishViewModel", "Failed to complete milestone", e)
            }
        }
    }
    
    // Uncomplete a milestone
    fun uncompleteMilestone(milestoneId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                actualMilestoneRepository.uncompleteMilestone(milestoneId)
                
                // Get the milestone to update goal progress
                val milestone = actualMilestoneRepository.getMilestoneById(milestoneId)
                updateGoalProgressFromMilestones(milestone?.wishId ?: 0L)
            } catch (e: Exception) {
                android.util.Log.e("WishViewModel", "Failed to uncomplete milestone", e)
            }
        }
    }
    
    // Automatically update goal progress based on milestone completion
    private suspend fun updateGoalProgressFromMilestones(wishId: Long) {
        try {
            val calculatedProgress = actualMilestoneRepository.calculateGoalProgress(wishId)
            val currentWish = actualWishRepository.getWishById(wishId).first()
            val updatedWish = currentWish.copy(progress = calculatedProgress)
            actualWishRepository.updateWish(updatedWish)
        } catch (e: Exception) {
            android.util.Log.e("WishViewModel", "Failed to update goal progress from milestones", e)
        }
    }
    
    // Get suggested milestones for a goal
    fun getSuggestedMilestones(goalTitle: String, goalCategory: String): List<String> {
        return actualMilestoneRepository.getSuggestedMilestones(goalTitle, goalCategory)
    }
    
    // Get overdue milestones
    fun getOverdueMilestones(): Flow<List<Milestone>> {
        return actualMilestoneRepository.getOverdueMilestones()
    }
    
    // Get upcoming milestones
    fun getUpcomingMilestones(): Flow<List<Milestone>> {
        return actualMilestoneRepository.getUpcomingMilestones()
    }
    
    // Clear all data function for settings
    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Clear all wishes
                actualWishRepository.deleteAllWishes()
                
                // Reset user profile to default state
                actualUserProfileRepository.resetProfile()
                
                // Update local profile state
                _userProfile.value = UserProfile(
                    id = 1,
                    username = "User",
                    name = "",
                    theme = "System"
                )
                
                // Clear notifications
                _notifications.value = emptyList()
                
                // Reset achievements
                _achievements.value = AchievementSystem.getAllAchievements()
                
                // Add confirmation notification
                addNotification(
                    title = "Data Cleared",
                    message = "All wishes and data have been successfully removed.",
                    type = NotificationType.SYSTEM
                )
                
                android.util.Log.d("WishViewModel", "All data cleared successfully")
            } catch (e: Exception) {
                android.util.Log.e("WishViewModel", "Failed to clear all data", e)
                addNotification(
                    title = "Error",
                    message = "Failed to clear data. Please try again.",
                    type = NotificationType.SYSTEM
                )
            }
        }
    }
}
