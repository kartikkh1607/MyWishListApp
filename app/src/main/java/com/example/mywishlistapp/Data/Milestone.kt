package com.example.mywishlistapp.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "Milestone-Table",
    foreignKeys = [
        ForeignKey(
            entity = Wish::class,
            parentColumns = ["id"],
            childColumns = ["wish-id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["wish-id"])]
)
data class Milestone(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    
    @ColumnInfo(name = "wish-id")
    val wishId: Long,
    
    @ColumnInfo(name = "milestone-title")
    val title: String,
    
    @ColumnInfo(name = "milestone-description")
    val description: String = "",
    
    @ColumnInfo(name = "milestone-target-date")
    val targetDate: Long? = null,
    
    @ColumnInfo(name = "milestone-is-completed")
    val isCompleted: Boolean = false,
    
    @ColumnInfo(name = "milestone-completed-date")
    val completedDate: Long? = null,
    
    @ColumnInfo(name = "milestone-order")
    val order: Int = 0, // For ordering milestones within a goal
    
    @ColumnInfo(name = "milestone-weight")
    val weight: Int = 1, // How much this milestone contributes to overall progress (1-10)
    
    @ColumnInfo(name = "milestone-created-date")
    val createdDate: Long = System.currentTimeMillis()
)

// Data class for milestone with completion statistics
data class MilestoneWithStats(
    val milestone: Milestone,
    val progressContribution: Float // How much this milestone contributes to goal progress
)

// Goal with its milestones
data class GoalWithMilestones(
    val goal: Wish,
    val milestones: List<Milestone>,
    val completedMilestones: Int,
    val totalMilestones: Int,
    val calculatedProgress: Int // Auto-calculated based on milestone completion
)
