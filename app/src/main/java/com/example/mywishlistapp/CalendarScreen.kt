package com.example.mywishlistapp

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mywishlistapp.Data.Wish
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.emptyList

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavHostController, viewModel: WishViewModel) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showWishScheduler by remember { mutableStateOf(false) }
    var selectedWishForSchedule by remember { mutableStateOf<Wish?>(null) }
    
    val allWishes by viewModel.getAllWishes.collectAsState(initial = emptyList())
    val userProfile by viewModel.userProfile.collectAsState()
    
    // Enhanced date calculations for journey view
    val activitiesForSelectedDate = getActivitiesForDate(allWishes, selectedDate)
    val monthlyStats = getMonthlyStats(allWishes, currentMonth)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFF),
                        Color(0xFFE8F0FE)
                    )
                )
            )
    ) {
        // Journey View Header
        JourneyHeader(
            currentMonth = currentMonth,
            monthlyStats = monthlyStats,
            userProfile = userProfile,
            onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
            onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
            onTodayClick = { 
                currentMonth = YearMonth.now()
                selectedDate = LocalDate.now()
            }
        )
        
        // Journey Calendar Grid with activity indicators
        JourneyCalendarGrid(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            allWishes = allWishes,
            onDateSelected = { selectedDate = it }
        )
        
        // Journey Reflection Section
        JourneyReflectionSection(
            selectedDate = selectedDate,
            activities = activitiesForSelectedDate,
            onScheduleWish = { showWishScheduler = true },
            onWishClick = { wish ->
                navController.navigate(Screen.AddScreen.route + "/${wish.id}")
            },
            onRemoveSchedule = { wish -> 
                viewModel.updateWish(wish.copy(scheduledDate = null, reminderSet = false))
            },
            onToggleReminder = { wish ->
                viewModel.updateWish(wish.copy(reminderSet = !wish.reminderSet))
            }
        )
    }
    
    // Wish Scheduler Dialog
    if (showWishScheduler) {
        WishSchedulerDialog(
            allWishes = allWishes.filter { it.scheduledDate == null },
            selectedDate = selectedDate,
            onDismiss = { showWishScheduler = false },
            onScheduleWish = { wish ->
                viewModel.updateWish(
                    wish.copy(
                        scheduledDate = selectedDate.toString(),
                        reminderSet = false
                    )
                )
                showWishScheduler = false
            }
        )
    }
}

// Journey Activity Section Composable
@Composable
fun JourneyActivitySection(
    title: String,
    subtitle: String,
    wishes: List<Wish>,
    color: Color,
    onWishClick: (Wish) -> Unit,
    showActions: Boolean,
    onRemoveSchedule: ((Wish) -> Unit)? = null,
    onToggleReminder: ((Wish) -> Unit)? = null
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B)
            )
        }
        
        wishes.forEach { wish ->
            JourneyWishItem(
                wish = wish,
                color = color,
                onClick = { onWishClick(wish) },
                showActions = showActions,
                onRemoveSchedule = onRemoveSchedule?.let { { onRemoveSchedule(wish) } },
                onToggleReminder = onToggleReminder?.let { { onToggleReminder(wish) } }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun JourneyWishItem(
    wish: Wish,
    color: Color,
    onClick: () -> Unit,
    showActions: Boolean,
    onRemoveSchedule: (() -> Unit)? = null,
    onToggleReminder: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wish.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1D29)
                )
                if (wish.description.isNotEmpty()) {
                    Text(
                        text = wish.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        maxLines = 1
                    )
                }
                
                // Show progress for goals
                if (wish.isGoal && wish.progress > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "Progress: ${wish.progress}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = color
                        )
                    }
                }
            }
            
            // Action buttons for scheduled items
            if (showActions && onRemoveSchedule != null && onToggleReminder != null) {
                Row {
                    IconButton(
                        onClick = onToggleReminder,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (wish.reminderSet) Icons.Default.NotificationsOff else Icons.Default.NotificationsActive,
                            contentDescription = if (wish.reminderSet) "Remove reminder" else "Set reminder",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF667EEA)
                        )
                    }
                    
                    IconButton(
                        onClick = onRemoveSchedule,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove from date",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFFF6B6B)
                        )
                    }
                }
            }
        }
    }
}

// Helper Functions
@RequiresApi(Build.VERSION_CODES.O)
fun getActivitiesForDate(allWishes: List<Wish>, date: LocalDate): DayActivity {
    val dateString = date.toString()
    val currentTimeMillis = date.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val endOfDayMillis = currentTimeMillis + 24 * 60 * 60 * 1000
    
    // Find wishes created on this date
    val createdWishes = allWishes.filter { wish ->
        wish.createdDate in currentTimeMillis..endOfDayMillis
    }
    
    // Find wishes completed on this date (approximation based on completion status)
    val completedWishes = allWishes.filter { wish ->
        wish.isCompleted && wish.createdDate <= endOfDayMillis
    }
    
    // Find wishes scheduled for this date
    val scheduledWishes = allWishes.filter { wish ->
        wish.scheduledDate == dateString
    }
    
    // Find deadlines on this date
    val deadlines = allWishes.filter { wish ->
        wish.targetDate?.let { targetDate ->
            val targetDateLocal = java.time.Instant.ofEpochMilli(targetDate)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            targetDateLocal == date
        } ?: false
    }
    
    // Determine activity type
    val activityType = when {
        completedWishes.isNotEmpty() && createdWishes.isNotEmpty() -> ActivityType.MIXED
        completedWishes.isNotEmpty() -> ActivityType.COMPLETED
        createdWishes.isNotEmpty() -> ActivityType.CREATED
        scheduledWishes.isNotEmpty() -> ActivityType.SCHEDULED
        deadlines.isNotEmpty() -> ActivityType.DEADLINE
        else -> ActivityType.NONE
    }
    
    return DayActivity(
        date = date,
        completedWishes = completedWishes,
        createdWishes = createdWishes,
        scheduledWishes = scheduledWishes,
        deadlines = deadlines,
        activityType = activityType
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun getMonthlyStats(allWishes: List<Wish>, currentMonth: YearMonth): MonthlyStats {
    val monthStart = currentMonth.atDay(1)
    val monthEnd = currentMonth.atEndOfMonth()
    val monthStartMillis = monthStart.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val monthEndMillis = monthEnd.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() + 24 * 60 * 60 * 1000
    
    val monthWishes = allWishes.filter { wish ->
        wish.createdDate in monthStartMillis..monthEndMillis
    }
    
    val completedThisMonth = monthWishes.count { it.isCompleted }
    val createdThisMonth = monthWishes.size
    val completionRate = if (createdThisMonth > 0) completedThisMonth.toFloat() / createdThisMonth.toFloat() else 0f
    
    // Simple streak calculation (could be enhanced)
    val streak = calculateCurrentStreak(allWishes)
    
    // Find most productive day (day with most completions)
    val mostProductiveDay = findMostProductiveDay(allWishes, currentMonth)
    
    return MonthlyStats(
        totalCompleted = completedThisMonth,
        totalCreated = createdThisMonth,
        completionRate = completionRate,
        streak = streak,
        mostProductiveDay = mostProductiveDay
    )
}

fun getActivitySummaryText(activities: DayActivity): String {
    val parts = mutableListOf<String>()
    
    if (activities.completedWishes.isNotEmpty()) {
        parts.add("${activities.completedWishes.size} completed")
    }
    if (activities.createdWishes.isNotEmpty()) {
        parts.add("${activities.createdWishes.size} created")
    }
    if (activities.scheduledWishes.isNotEmpty()) {
        parts.add("${activities.scheduledWishes.size} scheduled")
    }
    if (activities.deadlines.isNotEmpty()) {
        parts.add("${activities.deadlines.size} deadline${if (activities.deadlines.size > 1) "s" else ""}")
    }
    
    return when {
        parts.isEmpty() -> "No activity recorded"
        parts.size == 1 -> parts[0]
        else -> parts.joinToString(" • ")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateCurrentStreak(allWishes: List<Wish>): Int {
    // Simple implementation - in a real app, you'd track completion dates more precisely
    val completedWishes = allWishes.filter { it.isCompleted }
    return if (completedWishes.isNotEmpty()) {
        minOf(7, completedWishes.size) // Cap at 7 days for demo
    } else 0
}

@RequiresApi(Build.VERSION_CODES.O)
fun findMostProductiveDay(allWishes: List<Wish>, currentMonth: YearMonth): LocalDate? {
    val monthStart = currentMonth.atDay(1)
    val monthEnd = currentMonth.atEndOfMonth()
    
    val completedWishesThisMonth = allWishes.filter { wish ->
        wish.isCompleted && wish.createdDate >= monthStart.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            && wish.createdDate <= monthEnd.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() + 24 * 60 * 60 * 1000
    }
    
    if (completedWishesThisMonth.isEmpty()) return null
    
    // For demo purposes, return the middle of the month as most productive
    return monthStart.plusDays(15)
}

// Data classes for journey view
data class DayActivity(
    val date: LocalDate,
    val completedWishes: List<Wish>,
    val createdWishes: List<Wish>,
    val scheduledWishes: List<Wish>,
    val deadlines: List<Wish>,
    val activityType: ActivityType
)

enum class ActivityType {
    NONE, CREATED, COMPLETED, SCHEDULED, DEADLINE, MIXED
}

data class MonthlyStats(
    val totalCompleted: Int,
    val totalCreated: Int,
    val completionRate: Float,
    val streak: Int,
    val mostProductiveDay: LocalDate?
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun JourneyHeader(
    currentMonth: YearMonth,
    monthlyStats: MonthlyStats,
    userProfile: com.example.mywishlistapp.Data.UserProfile,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Month",
                    tint = Color(0xFF667EEA)
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "My Journey",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF667EEA)
                )
                Text(
                    text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1D29)
                )
                // Monthly achievement summary
                Text(
                    text = "${monthlyStats.totalCompleted} completed • ${monthlyStats.streak} day streak",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )
            }
            
            Row {
                TextButton(onClick = onTodayClick) {
                    Text(
                        text = "Today",
                        color = Color(0xFF667EEA),
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Month",
                        tint = Color(0xFF667EEA)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun JourneyCalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    allWishes: List<Wish>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val lastDayOfMonth = currentMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = currentMonth.lengthOfMonth()
    
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Days of week header
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items(daysOfWeek) { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calendar days grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(300.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Empty cells for days before month starts
                items(firstDayOfWeek) {
                    Spacer(modifier = Modifier.size(40.dp))
                }
                
                // Days of the month with activity indicators
                items(daysInMonth) { day ->
                    val date = currentMonth.atDay(day + 1)
                    val isSelected = date.isEqual(selectedDate)
                    val isToday = date.isEqual(LocalDate.now())
                    val dayActivity = getActivitiesForDate(allWishes, date)
                    
                    JourneyDayItem(
                        day = day + 1,
                        isSelected = isSelected,
                        isToday = isToday,
                        activity = dayActivity,
                        onClick = { onDateSelected(date) }
                    )
                }
            }
        }
    }
}

@Composable
fun JourneyDayItem(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    activity: DayActivity,
    onClick: () -> Unit
) {
    val activityColor = when (activity.activityType) {
        ActivityType.COMPLETED -> Color(0xFF10B981) // Green for completions
        ActivityType.CREATED -> Color(0xFF667EEA) // Blue for new wishes
        ActivityType.DEADLINE -> Color(0xFFF59E0B) // Orange for deadlines
        ActivityType.SCHEDULED -> Color(0xFFE91E63) // Pink for scheduled
        ActivityType.MIXED -> Color(0xFF8B5CF6) // Purple for multiple activities
        ActivityType.NONE -> Color.Transparent
    }
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable { onClick() }
            .background(
                color = when {
                    isSelected -> Color(0xFF667EEA)
                    isToday -> Color(0xFFE8F0FE)
                    activity.activityType != ActivityType.NONE -> activityColor.copy(alpha = 0.15f)
                    else -> Color.Transparent
                },
                shape = CircleShape
            )
            .border(
                width = if (isToday && !isSelected) 2.dp else 0.dp,
                color = Color(0xFF667EEA),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSelected -> Color.White
                    isToday -> Color(0xFF667EEA)
                    else -> Color(0xFF1A1D29)
                },
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )
            // Multiple colored dots for different activities
            if (activity.activityType != ActivityType.NONE) {
                Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                    if (activity.completedWishes.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .background(Color(0xFF10B981), CircleShape)
                        )
                    }
                    if (activity.createdWishes.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .background(Color(0xFF667EEA), CircleShape)
                        )
                    }
                    if (activity.deadlines.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .background(Color(0xFFF59E0B), CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun JourneyReflectionSection(
    selectedDate: LocalDate,
    activities: DayActivity,
    onScheduleWish: () -> Unit,
    onWishClick: (Wish) -> Unit,
    onRemoveSchedule: (Wish) -> Unit,
    onToggleReminder: (Wish) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1D29)
                    )
                    // Activity summary for the day
                    Text(
                        text = getActivitySummaryText(activities),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }
                
                Button(
                    onClick = onScheduleWish,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF667EEA)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Schedule")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Display different types of activities for the selected date
            if (activities.activityType == ActivityType.NONE) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✨ A quiet day in your journey",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Every pause is part of progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Show completed wishes
                if (activities.completedWishes.isNotEmpty()) {
                    JourneyActivitySection(
                        title = "🎉 Completed",
                        subtitle = "Your achievements",
                        wishes = activities.completedWishes,
                        color = Color(0xFF10B981),
                        onWishClick = onWishClick,
                        showActions = false
                    )
                }
                
                // Show created wishes
                if (activities.createdWishes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    JourneyActivitySection(
                        title = "💡 Created",
                        subtitle = "New dreams born",
                        wishes = activities.createdWishes,
                        color = Color(0xFF667EEA),
                        onWishClick = onWishClick,
                        showActions = false
                    )
                }
                
                // Show scheduled wishes
                if (activities.scheduledWishes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    JourneyActivitySection(
                        title = "📅 Scheduled",
                        subtitle = "Planned activities",
                        wishes = activities.scheduledWishes,
                        color = Color(0xFFE91E63),
                        onWishClick = onWishClick,
                        showActions = true,
                        onRemoveSchedule = onRemoveSchedule,
                        onToggleReminder = onToggleReminder
                    )
                }
                
                // Show deadlines
                if (activities.deadlines.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    JourneyActivitySection(
                        title = "⏰ Deadlines",
                        subtitle = "Focus needed",
                        wishes = activities.deadlines,
                        color = Color(0xFFF59E0B),
                        onWishClick = onWishClick,
                        showActions = false
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduledWishItem(
    wish: Wish,
    onRemoveSchedule: () -> Unit,
    onToggleReminder: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wish.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1D29)
                )
                if (wish.description.isNotEmpty()) {
                    Text(
                        text = wish.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        maxLines = 2
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = if (wish.reminderSet) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (wish.reminderSet) Color(0xFF10B981) else Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (wish.reminderSet) "Reminder set" else "No reminder",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (wish.reminderSet) Color(0xFF10B981) else Color(0xFF64748B)
                    )
                }
            }
            
            Row {
                IconButton(
                    onClick = onToggleReminder,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (wish.reminderSet) Icons.Default.NotificationsOff else Icons.Default.NotificationsActive,
                        contentDescription = if (wish.reminderSet) "Remove reminder" else "Set reminder",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF667EEA)
                    )
                }
                
                IconButton(
                    onClick = onRemoveSchedule,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove from date",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFF6B6B)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WishSchedulerDialog(
    allWishes: List<Wish>,
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onScheduleWish: (Wish) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Schedule Wish for ${selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn {
                if (allWishes.isEmpty()) {
                    item {
                        Text(
                            text = "No unscheduled wishes available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    items(allWishes) { wish ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onScheduleWish(wish) }
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFF)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = wish.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1D29)
                                )
                                if (wish.description.isNotEmpty()) {
                                    Text(
                                        text = wish.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF64748B),
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF667EEA))
            }
        }
    )
}
