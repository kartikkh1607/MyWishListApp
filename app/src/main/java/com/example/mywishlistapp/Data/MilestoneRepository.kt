package com.example.mywishlistapp.Data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class MilestoneRepository(private val milestoneDao: MilestoneDao) {
    
    fun getMilestonesForGoal(wishId: Long): Flow<List<Milestone>> {
        return milestoneDao.getMilestonesForGoal(wishId)
    }
    
    suspend fun getMilestoneById(milestoneId: Long): Milestone? {
        return milestoneDao.getMilestoneById(milestoneId)
    }
    
    suspend fun insertMilestone(milestone: Milestone): Long {
        return milestoneDao.insertMilestone(milestone)
    }
    
    suspend fun updateMilestone(milestone: Milestone) {
        milestoneDao.updateMilestone(milestone)
    }
    
    suspend fun deleteMilestone(milestone: Milestone) {
        milestoneDao.deleteMilestone(milestone)
    }
    
    suspend fun deleteMilestonesForGoal(wishId: Long) {
        milestoneDao.deleteMilestonesForGoal(wishId)
    }
    
    suspend fun completeMilestone(milestoneId: Long) {
        milestoneDao.completeMilestone(milestoneId)
    }
    
    suspend fun uncompleteMilestone(milestoneId: Long) {
        milestoneDao.uncompleteMilestone(milestoneId)
    }
    
    suspend fun getMilestoneStats(wishId: Long): MilestoneStats? {
        return milestoneDao.getMilestoneStats(wishId)
    }
    
    fun getAllCompletedMilestones(): Flow<List<Milestone>> {
        return milestoneDao.getAllCompletedMilestones()
    }
    
    fun getOverdueMilestones(): Flow<List<Milestone>> {
        return milestoneDao.getOverdueMilestones()
    }
    
    fun getUpcomingMilestones(): Flow<List<Milestone>> {
        return milestoneDao.getUpcomingMilestones()
    }
    
    // Get goal with milestones and auto-calculated progress
    suspend fun getGoalWithMilestones(wish: Wish): GoalWithMilestones {
        val milestones = getMilestonesForGoal(wish.id).map { it }.toString() // Placeholder for now
        val stats = getMilestoneStats(wish.id)
        
        return GoalWithMilestones(
            goal = wish,
            milestones = emptyList(), // Will be populated by Flow
            completedMilestones = stats?.completed ?: 0,
            totalMilestones = stats?.total ?: 0,
            calculatedProgress = stats?.completionPercentage ?: wish.progress
        )
    }
    
    // Calculate automatic progress based on milestone completion
    suspend fun calculateGoalProgress(wishId: Long): Int {
        val stats = getMilestoneStats(wishId)
        return stats?.completionPercentage ?: 0
    }
    
    // Smart milestone suggestions based on goal type
    fun getSuggestedMilestones(goalTitle: String, goalCategory: String): List<String> {
        return when {
            goalTitle.contains("learn", ignoreCase = true) || goalCategory.equals("Education", ignoreCase = true) -> {
                listOf(
                    "Research and gather learning resources",
                    "Complete first learning module or chapter", 
                    "Practice what you've learned with a project",
                    "Take assessment or quiz to test knowledge",
                    "Apply knowledge in a real-world scenario"
                )
            }
            goalTitle.contains("fitness", ignoreCase = true) || goalTitle.contains("exercise", ignoreCase = true) || goalCategory.equals("Health", ignoreCase = true) -> {
                listOf(
                    "Create a workout plan",
                    "Complete first week of consistent exercise",
                    "Reach 25% of fitness goal",
                    "Reach 50% of fitness goal", 
                    "Reach 75% of fitness goal"
                )
            }
            goalTitle.contains("save", ignoreCase = true) || goalTitle.contains("money", ignoreCase = true) || goalCategory.equals("Financial", ignoreCase = true) -> {
                listOf(
                    "Set up automatic savings transfer",
                    "Save first $500",
                    "Reach 25% of savings goal",
                    "Reach 50% of savings goal",
                    "Reach 75% of savings goal"
                )
            }
            goalTitle.contains("read", ignoreCase = true) || goalTitle.contains("book", ignoreCase = true) -> {
                listOf(
                    "Choose your first book",
                    "Read 25% of your target",
                    "Read 50% of your target",
                    "Read 75% of your target",
                    "Complete reading goal"
                )
            }
            goalTitle.contains("project", ignoreCase = true) || goalTitle.contains("build", ignoreCase = true) -> {
                listOf(
                    "Plan and design the project",
                    "Set up development environment",
                    "Complete basic functionality",
                    "Add advanced features",
                    "Test and finalize project"
                )
            }
            else -> {
                listOf(
                    "Define specific action steps",
                    "Complete initial research/preparation",
                    "Reach 25% progress milestone",
                    "Reach 50% progress milestone",
                    "Final push to completion"
                )
            }
        }
    }
}
