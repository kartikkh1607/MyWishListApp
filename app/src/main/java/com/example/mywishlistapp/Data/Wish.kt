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

@Entity(tableName = "Wish-Table")
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
    val priority: Priority = Priority.MEDIUM
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
        )
    )
}
