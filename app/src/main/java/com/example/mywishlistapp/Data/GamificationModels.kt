package com.example.mywishlistapp.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single user profile
    @ColumnInfo(name = "username") val username: String = "User",
    @ColumnInfo(name = "total_wishes") val totalWishes: Int = 0,
    @ColumnInfo(name = "completed_wishes") val completedWishes: Int = 0,
    @ColumnInfo(name = "high_priority_completed") val highPriorityCompleted: Int = 0,
    @ColumnInfo(name = "current_streak") val currentStreak: Int = 0,
    @ColumnInfo(name = "longest_streak") val longestStreak: Int = 0,
    @ColumnInfo(name = "total_money_saved") val totalMoneySaved: Double = 0.0,
    @ColumnInfo(name = "level") val level: Int = 1,
    @ColumnInfo(name = "experience_points") val experiencePoints: Int = 0,
    @ColumnInfo(name = "earned_badges") val earnedBadges: List<String> = emptyList(),
    @ColumnInfo(name = "profile_created_date") val profileCreatedDate: Long = System.currentTimeMillis()
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
    BEGINNER, COLLECTOR, ACHIEVER, SAVER, STREAKER, MASTER
}


object AchievementSystem {
    private var mutableAchievements = mutableListOf(
        Achievement("first_wish", "Dream Starter", "Add your first wish", "🌟", 1, AchievementCategory.BEGINNER, 10),
        Achievement("five_wishes", "Wishful Thinking", "Add 5 wishes", "💭", 5, AchievementCategory.COLLECTOR, 25),
        Achievement("first_complete", "First Victory", "Complete your first wish", "🎉", 1, AchievementCategory.ACHIEVER, 20),
        Achievement("ten_complete", "Goal Crusher", "Complete 10 wishes", "💪", 10, AchievementCategory.ACHIEVER, 50),
        Achievement("high_priority_master", "Priority Pro", "Complete 5 high-priority wishes", "🔥", 5, AchievementCategory.MASTER, 75),
        Achievement("streak_warrior", "Streak Warrior", "Maintain a 7-day completion streak", "⚡", 7, AchievementCategory.STREAKER, 100),
        Achievement("big_spender", "Big Dreams", "Add a wish worth $1000+", "💰", 1000, AchievementCategory.SAVER, 40),
        Achievement("collector", "Wish Collector", "Have 25 active wishes", "📚", 25, AchievementCategory.COLLECTOR, 60),
        Achievement("completionist", "Completionist", "Complete 50 wishes", "🏆", 50, AchievementCategory.MASTER, 200)
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
