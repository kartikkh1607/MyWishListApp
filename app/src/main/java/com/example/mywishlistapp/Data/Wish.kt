package com.example.mywishlistapp.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

// Simple comma-based converter — no Gson needed
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString(",")

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split(",").map { it.trim() }

    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority =
        try { Priority.valueOf(value) } catch (e: IllegalArgumentException) { Priority.MEDIUM }
}

enum class Priority { LOW, MEDIUM, HIGH }

@Entity(tableName = "Wish-Table")
@TypeConverters(Converters::class)
data class Wish(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "wish-title")       val title: String = "",
    @ColumnInfo(name = "wish-description") val description: String = "",
    @ColumnInfo(name = "wish-category")    val category: String = "",
    @ColumnInfo(name = "wish-tags")        val tags: List<String> = emptyList(),
    @ColumnInfo(name = "wish-priority")    val priority: Priority = Priority.MEDIUM,
    @ColumnInfo(name = "wish-price")       val price: String = "",
    @ColumnInfo(name = "wish-is-completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "wish-created-date") val createdDate: Long = System.currentTimeMillis()
)
