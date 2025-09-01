package com.example.mywishlistapp.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

@Entity(tableName = "user_profile")
@TypeConverters(Converters::class)
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single user profile
    @ColumnInfo(name = "username") val username: String = "User",
    @ColumnInfo(name = "name") val name: String = "", // Personalized name for companion experience
    @ColumnInfo(name = "total_wishes") val totalWishes: Int = 0,
    @ColumnInfo(name = "completed_wishes") val completedWishes: Int = 0,
    @ColumnInfo(name = "high_priority_completed") val highPriorityCompleted: Int = 0,
    @ColumnInfo(name = "current_streak") val currentStreak: Int = 0,
    @ColumnInfo(name = "longest_streak") val longestStreak: Int = 0,
    @ColumnInfo(name = "total_money_saved") val totalMoneySaved: Double = 0.0,
    @ColumnInfo(name = "level") val level: Int = 1,
    @ColumnInfo(name = "experience_points") val experiencePoints: Int = 0,
    @ColumnInfo(name = "earned_badges") val earnedBadges: List<String> = emptyList(),
    @ColumnInfo(name = "profile_created_date") val profileCreatedDate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "daily_challenges_completed") val dailyChallengesCompleted: Int = 0,
    @ColumnInfo(name = "weekly_challenges_completed") val weeklyChallengesCompleted: Int = 0,
    @ColumnInfo(name = "last_login_date") val lastLoginDate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "login_streak") val loginStreak: Int = 0,
    @ColumnInfo(name = "total_categories_explored") val totalCategoriesExplored: Int = 0,
    @ColumnInfo(name = "wishes_shared") val wishesShared: Int = 0,
    @ColumnInfo(name = "personal_best_score") val personalBestScore: Int = 0,
    @ColumnInfo(name = "theme") val theme: String = "System"
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String, // Unicode emoji or icon name
    val requirement: Int,
    val category: AchievementCategory,
    val xpReward: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val name: String = title // Add name property for compatibility
)

enum class AchievementCategory {
    BEGINNER, COLLECTOR, ACHIEVER, SAVER, STREAKER, MASTER, SOCIAL, EXPLORER
}

@Entity(tableName = "daily_challenges")
data class DailyChallenge(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "icon") val icon: String,
    @ColumnInfo(name = "target_value") val targetValue: Int,
    @ColumnInfo(name = "current_progress") val currentProgress: Int = 0,
    @ColumnInfo(name = "xp_reward") val xpReward: Int,
    @ColumnInfo(name = "challenge_type") val challengeType: ChallengeType,
    @ColumnInfo(name = "date_created") val dateCreated: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "date_expires") val dateExpires: Long,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null
)

enum class ChallengeType {
    ADD_WISHES, COMPLETE_WISHES, SHARE_WISH, EXPLORE_CATEGORIES, LOGIN_STREAK, SAVE_MONEY
}

@Entity(tableName = "leaderboard_entries")
data class LeaderboardEntry(
    @PrimaryKey val userId: String,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "total_score") val totalScore: Int,
    @ColumnInfo(name = "level") val level: Int,
    @ColumnInfo(name = "completed_wishes") val completedWishes: Int,
    @ColumnInfo(name = "achievements_count") val achievementsCount: Int,
    @ColumnInfo(name = "current_streak") val currentStreak: Int,
    @ColumnInfo(name = "last_activity") val lastActivity: Long = System.currentTimeMillis()
)

data class WeeklyStats(
    val weekStart: Long,
    val wishesAdded: Int = 0,
    val wishesCompleted: Int = 0,
    val categoriesExplored: Int = 0,
    val challengesCompleted: Int = 0,
    val xpEarned: Int = 0
)


object AchievementSystem {
    private var mutableAchievements = mutableListOf(
        // Beginner Achievements
        Achievement("first_wish", "Dream Starter", "Add your first wish", "🌟", 1, AchievementCategory.BEGINNER, 10),
        Achievement("first_complete", "First Victory", "Complete your first wish", "🎉", 1, AchievementCategory.BEGINNER, 20),
        Achievement("first_week", "Steady Start", "Use the app for 7 consecutive days", "📅", 7, AchievementCategory.BEGINNER, 30),
        
        // Collector Achievements
        Achievement("five_wishes", "Wishful Thinking", "Add 5 wishes", "💭", 5, AchievementCategory.COLLECTOR, 25),
        Achievement("collector", "Wish Collector", "Have 25 active wishes", "📚", 25, AchievementCategory.COLLECTOR, 60),
        Achievement("mega_collector", "Wish Hoarder", "Have 100 total wishes", "🏪", 100, AchievementCategory.COLLECTOR, 150),
        
        // Achiever Achievements
        Achievement("ten_complete", "Goal Crusher", "Complete 10 wishes", "💪", 10, AchievementCategory.ACHIEVER, 50),
        Achievement("completionist", "Completionist", "Complete 50 wishes", "🏆", 50, AchievementCategory.ACHIEVER, 200),
        Achievement("legendary", "Legendary Achiever", "Complete 100 wishes", "👑", 100, AchievementCategory.MASTER, 500),
        
        // Saver Achievements
        Achievement("big_spender", "Big Dreams", "Add a wish worth $1000+", "💰", 1000, AchievementCategory.SAVER, 40),
        Achievement("money_saver", "Smart Saver", "Save $500 towards wishes", "💎", 500, AchievementCategory.SAVER, 80),
        Achievement("financial_guru", "Financial Guru", "Save $5000 towards wishes", "🏦", 5000, AchievementCategory.SAVER, 300),
        
        // Streaker Achievements
        Achievement("streak_warrior", "Streak Warrior", "Maintain a 7-day completion streak", "⚡", 7, AchievementCategory.STREAKER, 100),
        Achievement("streak_master", "Streak Master", "Maintain a 30-day streak", "🔥", 30, AchievementCategory.STREAKER, 250),
        Achievement("unstoppable", "Unstoppable", "Maintain a 100-day streak", "⚡", 100, AchievementCategory.STREAKER, 500),
        
        // Master Achievements
        Achievement("high_priority_master", "Priority Pro", "Complete 5 high-priority wishes", "🔥", 5, AchievementCategory.MASTER, 75),
        Achievement("efficiency_expert", "Efficiency Expert", "Complete 20 wishes in a single month", "⚡", 20, AchievementCategory.MASTER, 200),
        Achievement("wish_master", "Wish Master", "Reach level 10", "🎖️", 10, AchievementCategory.MASTER, 400),
        
        // Social Achievements
        Achievement("first_share", "Social Butterfly", "Share your first wish", "🦋", 1, AchievementCategory.SOCIAL, 15),
        Achievement("sharing_expert", "Sharing Expert", "Share 10 wishes", "📢", 10, AchievementCategory.SOCIAL, 75),
        Achievement("influencer", "Wish Influencer", "Share 50 wishes", "🌐", 50, AchievementCategory.SOCIAL, 200),
        
        // Explorer Achievements
        Achievement("category_explorer", "Category Explorer", "Add wishes in 5 different categories", "🗺️", 5, AchievementCategory.EXPLORER, 40),
        Achievement("world_traveler", "World Traveler", "Explore all available categories", "🌍", 10, AchievementCategory.EXPLORER, 100),
        Achievement("challenge_champion", "Challenge Champion", "Complete 25 daily challenges", "🏅", 25, AchievementCategory.EXPLORER, 150)
    )
    
    val achievements: List<Achievement>
        get() = mutableAchievements.toList()
    
    fun calculateLevel(xp: Int): Int {
        return when {
            xp < 100 -> 1
            xp < 300 -> 2
            xp < 600 -> 3
            xp < 1000 -> 4
            xp < 1500 -> 5
            else -> 6 + (xp - 1500) / 500
        }
    }
    
    fun getXpForNextLevel(currentLevel: Int): Int {
        return when (currentLevel) {
            1 -> 100
            2 -> 300
            3 -> 600
            4 -> 1000
            5 -> 1500
            else -> 1500 + (currentLevel - 5) * 500
        }
    }
    
    fun getAllAchievements(): List<Achievement> {
        return mutableAchievements.toList()
    }
    
    fun checkAchievements(userProfile: UserProfile): List<Achievement> {
        return mutableAchievements.filter { achievement ->
            when (achievement.id) {
                "first_wish" -> userProfile.totalWishes >= 1
                "five_wishes" -> userProfile.totalWishes >= 5
                "first_complete" -> userProfile.completedWishes >= 1
                "ten_complete" -> userProfile.completedWishes >= 10
                "high_priority_master" -> userProfile.highPriorityCompleted >= 5
                "streak_warrior" -> userProfile.currentStreak >= 7
                "big_spender" -> userProfile.totalMoneySaved >= 1000.0
                "collector" -> userProfile.totalWishes >= 25
                "completionist" -> userProfile.completedWishes >= 50
                else -> false
            } && !achievement.isUnlocked
        }
    }
    
    fun unlockAchievements(achievementIds: List<String>) {
        mutableAchievements = mutableAchievements.map { achievement ->
            if (achievement.id in achievementIds) {
                achievement.copy(
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis()
                )
            } else {
                achievement
            }
        }.toMutableList()
    }
}
