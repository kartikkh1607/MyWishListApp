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
    val wishesForSelectedDate = allWishes.filter { wish ->
        wish.scheduledDate?.let { scheduledDate ->
            val wishDate = LocalDate.parse(scheduledDate)
            wishDate.isEqual(selectedDate)
        } ?: false
    }
    
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
        // Top Header
        CalendarHeader(
            currentMonth = currentMonth,
            onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
            onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
            onTodayClick = { 
                currentMonth = YearMonth.now()
                selectedDate = LocalDate.now()
            }
        )
        
        // Calendar Grid
        CalendarGrid(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            wishesWithDates = allWishes.filter { it.scheduledDate != null },
            onDateSelected = { selectedDate = it }
        )
        
        // Selected Date Info & Actions
        SelectedDateSection(
            selectedDate = selectedDate,
            wishesForDate = wishesForSelectedDate,
            onScheduleWish = { showWishScheduler = true },
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarHeader(
    currentMonth: YearMonth,
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
                    text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1D29)
                )
                Text(
                    text = currentMonth.year.toString(),
                    style = MaterialTheme.typography.bodyMedium,
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
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    wishesWithDates: List<Wish>,
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
                
                // Days of the month
                items(daysInMonth) { day ->
                    val date = currentMonth.atDay(day + 1)
                    val isSelected = date.isEqual(selectedDate)
                    val isToday = date.isEqual(LocalDate.now())
                    val hasWishes = wishesWithDates.any { wish ->
                        wish.scheduledDate?.let { scheduledDate ->
                            LocalDate.parse(scheduledDate).isEqual(date)
                        } ?: false
                    }
                    
                    CalendarDayItem(
                        day = day + 1,
                        isSelected = isSelected,
                        isToday = isToday,
                        hasWishes = hasWishes,
                        onClick = { onDateSelected(date) }
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarDayItem(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasWishes: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable { onClick() }
            .background(
                color = when {
                    isSelected -> Color(0xFF667EEA)
                    isToday -> Color(0xFFE8F0FE)
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
            if (hasWishes) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            color = if (isSelected) Color.White else Color(0xFFFF6B6B),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SelectedDateSection(
    selectedDate: LocalDate,
    wishesForDate: List<Wish>,
    onScheduleWish: () -> Unit,
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
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1D29)
                )
                
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
                    Text("Schedule Wish")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (wishesForDate.isEmpty()) {
                Text(
                    text = "No wishes scheduled for this date",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn {
                    items(wishesForDate) { wish ->
                        ScheduledWishItem(
                            wish = wish,
                            onRemoveSchedule = { onRemoveSchedule(wish) },
                            onToggleReminder = { onToggleReminder(wish) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
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
