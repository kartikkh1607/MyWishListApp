package com.example.mywishlistapp.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }
    
    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }
    
    @TypeConverter
    fun toPriority(priorityString: String): Priority {
        return try {
            Priority.valueOf(priorityString)
        } catch (e: IllegalArgumentException) {
            Priority.MEDIUM // Default fallback
        }
    }
}

enum class Priority {
    LOW, MEDIUM, HIGH
}

@Entity(
    tableName = "Wish-Table",
    indices = [
        androidx.room.Index(value = ["wish-category"]),
        androidx.room.Index(value = ["wish-priority"]),
        androidx.room.Index(value = ["wish-is-completed"]),
        androidx.room.Index(value = ["wish-is-goal"]),
        androidx.room.Index(value = ["wish-created-date"]),
        androidx.room.Index(value = ["wish-title"]),
        androidx.room.Index(value = ["wish-target-date"])
    ]
)
@TypeConverters(Converters::class)
data class Wish(
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0L,
    @ColumnInfo(name = "wish-title")
    val title : String = "",
    @ColumnInfo(name = "wish-description")
    val description : String = "",
    @ColumnInfo(name = "wish-category")
    val category: String = "",
    @ColumnInfo(name = "wish-tags")
    val tags: List<String> = emptyList(),
    @ColumnInfo(name = "wish-priority")
    val priority: Priority = Priority.MEDIUM,
    // New fields for modern design
    @ColumnInfo(name = "wish-image-url")
    val imageUrl: String = "",
    @ColumnInfo(name = "wish-price")
    val price: String = "",
    @ColumnInfo(name = "wish-rating")
    val rating: Float = 0f,
    @ColumnInfo(name = "wish-is-completed")
    val isCompleted: Boolean = false,
    @ColumnInfo(name = "wish-due-date")
    val dueDate: Long? = null, // Timestamp
    @ColumnInfo(name = "wish-created-date")
    val createdDate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "wish-notes")
    val notes: String = "",
    // Calendar and reminder fields
    @ColumnInfo(name = "wish-scheduled-date")
    val scheduledDate: String? = null, // Format: yyyy-MM-dd
    @ColumnInfo(name = "wish-reminder-set")
    val reminderSet: Boolean = false,
    // Savings feature
    @ColumnInfo(name = "wish-saved-amount")
    val savedAmount: Double = 0.0,
    // Personal Growth Companion features
    @ColumnInfo(name = "wish-is-goal")
    val isGoal: Boolean = false,
    @ColumnInfo(name = "wish-target-date")
    val targetDate: Long? = null,
    @ColumnInfo(name = "wish-progress")
    val progress: Int = 0 // Range: 0-100
)

object DummyWish {
    val wishlist = listOf(
        Wish(
            title = "Android Phone",
            description = "Looking for a budget-friendly mid-range phone.",
            category = "Electronics",
            tags = listOf("smartphone", "android", "budget"),
            priority = Priority.MEDIUM
        ),
        Wish(
            title = "iPhone",
            description = "Want to try iOS experience, maybe iPhone 14 or newer.",
            category = "Electronics",
            tags = listOf("smartphone", "ios", "apple"),
            priority = Priority.HIGH
        ),
        Wish(
            title = "PS5",
            description = "For 4K gaming — God of War is a must play!",
            category = "Gaming",
            tags = listOf("console", "gaming", "entertainment"),
            priority = Priority.HIGH
        ),
        Wish(
            title = "MacBook",
            description = "For Android & iOS dev — ideal for working with Xcode.",
            category = "Electronics",
            tags = listOf("laptop", "development", "apple"),
            priority = Priority.HIGH
        ),
        Wish(
            title = "Trip to Europe",
            description = "Solo trip is needed",
            category = "Travel",
            tags = listOf("vacation", "solo", "adventure"),
            priority = Priority.LOW
        ),
        Wish(
            title = "New Headphones",
            description = "Noise cancelling, preferably Sony or Bose.",
            category = "Electronics",
            tags = listOf("audio", "noise-cancelling", "music"),
            priority = Priority.MEDIUM
        ),
        // Personal Growth Goals for demonstration
        Wish(
            title = "Learn Kotlin Multiplatform",
            description = "Master KMP development to build cross-platform apps for Android and iOS.",
            category = "Education",
            tags = listOf("learning", "development", "kotlin", "mobile"),
            priority = Priority.HIGH,
            isGoal = true,
            targetDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // 30 days from now
            progress = 35
        ),
        Wish(
            title = "Complete Marathon Training",
            description = "Train consistently for 6 months to complete my first full marathon.",
            category = "Health",
            tags = listOf("fitness", "running", "endurance", "health"),
            priority = Priority.HIGH,
            isGoal = true,
            targetDate = System.currentTimeMillis() + (180L * 24 * 60 * 60 * 1000), // 6 months from now
            progress = 65
        ),
        Wish(
            title = "Read 24 Books This Year",
            description = "Develop a consistent reading habit and expand knowledge across various topics.",
            category = "Education",
            tags = listOf("reading", "knowledge", "habit", "personal-growth"),
            priority = Priority.MEDIUM,
            isGoal = true,
            targetDate = System.currentTimeMillis() + (120L * 24 * 60 * 60 * 1000), // 4 months from now  
            progress = 75
        ),
        Wish(
            title = "Save $5000 for Emergency Fund",
            description = "Build a solid financial foundation with a proper emergency fund.",
            category = "Financial",
            tags = listOf("savings", "financial-security", "emergency-fund"),
            priority = Priority.HIGH,
            isGoal = true,
            targetDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000), // 1 year from now
            progress = 45
        )
    )
}
