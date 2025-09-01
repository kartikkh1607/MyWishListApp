package com.example.mywishlistapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.R
import com.example.mywishlistapp.WishViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GoalProgressAnalyticsCard(
    activeGoals: List<Wish>,
    onGoalClick: (Wish) -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        EnhancedAnalyticsCard(
            title = "🎯 Active Goals",
            subtitle = "${activeGoals.size} goals in progress",
            modifier = modifier
        ) {
            if (activeGoals.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🌱",
                        fontSize = 48.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = "No active goals yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Create your first goal to start tracking progress!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(activeGoals.take(5)) { goal ->
                        GoalProgressItem(
                            goal = goal,
                            onClick = { onGoalClick(goal) }
                        )
                    }
                    if (activeGoals.size > 5) {
                        item {
                            MoreGoalsIndicator(count = activeGoals.size - 5)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoalProgressItem(
    goal: Wish,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "goal_item_scale"
    )
    
    val progressColor = when {
        goal.progress >= 80 -> Color(0xFF10B981)
        goal.progress >= 50 -> Color(0xFFF59E0B)
        else -> Color(0xFF667EEA)
    }
    
    Card(
        modifier = modifier
            .width(200.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable { 
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Goal title and type indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1D29),
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(
                    shape = CircleShape,
                    color = progressColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "🎯",
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "${goal.progress}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = progressColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                LinearProgressIndicator(
                    progress = { goal.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = progressColor,
                    trackColor = progressColor.copy(alpha = 0.2f)
                )
            }
            
            // Target date if available
            goal.targetDate?.let { targetDate ->
                Spacer(modifier = Modifier.height(8.dp))
                val daysUntil = kotlin.math.max(0, 
                    java.util.concurrent.TimeUnit.MILLISECONDS.toDays(targetDate - System.currentTimeMillis())
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (daysUntil == 0L) "Due today!" else "$daysUntil days left",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (daysUntil <= 3) Color(0xFFE74C3C) else Color(0xFF64748B),
                        fontWeight = if (daysUntil <= 3) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun MoreGoalsIndicator(
    count: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(120.dp)
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF667EEA).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "+$count",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF667EEA)
            )
            Text(
                text = "more",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF667EEA)
            )
        }
    }
}

@Composable
fun CompletionStatisticsCard(
    goalAnalytics: WishViewModel.GoalAnalytics,
    wishesVsGoals: Pair<Int, Int>,
    streakData: Int,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        EnhancedAnalyticsCard(
            title = "📊 Your Statistics",
            subtitle = "Overview of your progress",
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Completion Rate
                StatisticItem(
                    title = "Completion",
                    value = "${(goalAnalytics.completionRate * 100).toInt()}%",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                
                // Average Progress
                StatisticItem(
                    title = "Avg Progress",
                    value = "${goalAnalytics.averageProgress.toInt()}%",
                    icon = Icons.Default.TrendingUp,
                    color = Color(0xFF667EEA),
                    modifier = Modifier.weight(1f)
                )
                
                // Streak
                StatisticItem(
                    title = "Streak",
                    value = "${streakData} days",
                    icon = Icons.Default.LocalFireDepartment,
                    color = Color(0xFFE74C3C),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Wishes vs Goals breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Items: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "${wishesVsGoals.first} Wishes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF667EEA),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "${wishesVsGoals.second} Goals",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatisticItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MotivationalInsightsCard(
    insights: List<WishViewModel.MotivationalInsight>,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    var currentInsightIndex by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(400)
        isVisible = true
    }
    
    // Auto-rotate insights every 8 seconds
    LaunchedEffect(insights) {
        if (insights.size > 1) {
            while (true) {
                kotlinx.coroutines.delay(8000)
                currentInsightIndex = (currentInsightIndex + 1) % insights.size
            }
        }
    }
    
    AnimatedVisibility(
        visible = isVisible && insights.isNotEmpty(),
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        val currentInsight = insights.getOrNull(currentInsightIndex)
        
        currentInsight?.let { insight ->
            EnhancedAnalyticsCard(
                title = "${insight.emoji} Insight",
                subtitle = getInsightTypeTitle(insight.type),
                modifier = modifier
            ) {
                Column {
                    Text(
                        text = insight.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF1A1D29),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    )
                    
                    insight.actionSuggestion?.let { suggestion ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF667EEA).copy(alpha = 0.08f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFF667EEA),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF667EEA),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    // Insight indicator dots
                    if (insights.size > 1) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            insights.forEachIndexed { index, _ ->
                                val isActive = index == currentInsightIndex
                                Box(
                                    modifier = Modifier
                                        .size(if (isActive) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isActive) Color(0xFF667EEA) 
                                            else Color(0xFF667EEA).copy(alpha = 0.3f)
                                        )
                                )
                                if (index < insights.size - 1) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingDeadlinesCard(
    upcomingDeadlines: List<WishViewModel.UpcomingDeadline>,
    onGoalClick: (Wish) -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(500)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        EnhancedAnalyticsCard(
            title = "⏰ Upcoming Deadlines",
            subtitle = "${upcomingDeadlines.size} goals with target dates",
            modifier = modifier
        ) {
            if (upcomingDeadlines.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📅",
                        fontSize = 48.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = "No upcoming deadlines",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Set target dates for your goals to track urgency!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    upcomingDeadlines.take(4).forEach { deadline ->
                        DeadlineItem(
                            deadline = deadline,
                            onClick = { onGoalClick(deadline.wish) }
                        )
                    }
                    
                    if (upcomingDeadlines.size > 4) {
                        Text(
                            text = "...and ${upcomingDeadlines.size - 4} more",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeadlineItem(
    deadline: WishViewModel.UpcomingDeadline,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val urgencyColor = when (deadline.urgencyLevel) {
        WishViewModel.UrgencyLevel.OVERDUE -> Color(0xFFE74C3C)
        WishViewModel.UrgencyLevel.URGENT -> Color(0xFFF39C12)
        WishViewModel.UrgencyLevel.SOON -> Color(0xFF667EEA)
        WishViewModel.UrgencyLevel.NORMAL -> Color(0xFF10B981)
    }
    
    val urgencyText = when (deadline.urgencyLevel) {
        WishViewModel.UrgencyLevel.OVERDUE -> "Overdue"
        WishViewModel.UrgencyLevel.URGENT -> "Urgent"
        WishViewModel.UrgencyLevel.SOON -> "Soon"
        WishViewModel.UrgencyLevel.NORMAL -> "${deadline.daysUntilDeadline} days"
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = urgencyColor.copy(alpha = 0.05f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, urgencyColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deadline.wish.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1D29),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${deadline.wish.progress}% complete",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
            }
            
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = urgencyColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = urgencyText,
                    style = MaterialTheme.typography.labelSmall,
                    color = urgencyColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun EnhancedAnalyticsCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        isVisible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "analytics_card_scale"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Column(
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF667EEA)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            // Content
            content()
        }
    }
}

private fun getInsightTypeTitle(type: WishViewModel.InsightType): String {
    return when (type) {
        WishViewModel.InsightType.ENCOURAGEMENT -> "Stay motivated!"
        WishViewModel.InsightType.CELEBRATION -> "Celebrate your wins!"
        WishViewModel.InsightType.REMINDER -> "Don't forget!"
        WishViewModel.InsightType.TIP -> "Pro tip"
        WishViewModel.InsightType.MILESTONE -> "You're doing great!"
    }
}
