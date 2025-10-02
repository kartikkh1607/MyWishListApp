package com.example.mywishlistapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.Data.Wish
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

// Stunning Top Bar with glassmorphism effect
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StunningTopBar(
    userName: String,
    greeting: String,
    unreadNotificationCount: Int,
    onSearchClicked: () -> Unit,
    onNotificationClicked: () -> Unit,
    onProfileClicked: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF6366F1).copy(alpha = 0.05f),
                                Color.White
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Avatar with gradient border
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF6366F1),
                                        Color(0xFF8B5CF6)
                                    )
                                )
                            )
                            .clickable { onProfileClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (userName.isNotEmpty()) userName.first().uppercase() else "U",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6366F1)
                            )
                        }
                    }
                    
                    // Action buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onSearchClicked,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF3F4F6))
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF374151),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Box {
                            IconButton(
                                onClick = onNotificationClicked,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3F4F6))
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color(0xFF374151),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            // Notification badge
                            if (unreadNotificationCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .offset(x = (-4).dp, y = 4.dp)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                        .align(Alignment.TopEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Greeting text
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.padding(top = 12.dp)
                )
                
                Text(
                    text = "Welcome back, $userName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

// Personalized Welcome Card with animation
@Composable
fun PersonalizedWelcomeCard(
    userName: String,
    greeting: String,
    completionRate: Float,
    streak: Int
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color(0xFF6366F1).copy(alpha = 0.1f)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF6366F1).copy(alpha = 0.1f),
                                Color(0xFF8B5CF6).copy(alpha = 0.05f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hello $userName! 👋",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                            
                            Text(
                                text = "Your dreams are ${(completionRate * 100).toInt()}% closer to reality",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF6B7280),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        // Streak badge
                        if (streak > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFFFBBF24),
                                                Color(0xFFF59E0B)
                                            )
                                        )
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "$streak day streak",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Progress bar
                    LinearProgressIndicator(
                        progress = { completionRate },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF6366F1),
                        trackColor = Color(0xFFE5E7EB)
                    )
                }
            }
        }
    }
}

// Modern Stats Cards with animations
@Composable
fun ModernStatsCards(
    totalWishes: Int,
    completedWishes: Int,
    pendingWishes: Int,
    highPriorityWishes: Int,
    totalSavingsTarget: Double,
    totalSaved: Double
) {
    val stats = listOf(
        StatsData("Total", totalWishes, Icons.Default.FormatListBulleted, Color(0xFF6366F1)),
        StatsData("Completed", completedWishes, Icons.Default.CheckCircle, Color(0xFF10B981)),
        StatsData("Pending", pendingWishes, Icons.Default.Schedule, Color(0xFFF59E0B)),
        StatsData("Priority", highPriorityWishes, Icons.Default.PriorityHigh, Color(0xFFEF4444))
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(stats) { stat ->
            AnimatedStatsCard(stat)
        }
    }
}

data class StatsData(
    val label: String,
    val value: Int,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun AnimatedStatsCard(stat: StatsData) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn()
    ) {
        Card(
            modifier = Modifier
                .width(100.dp)
                .height(120.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = stat.color.copy(alpha = 0.1f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    stat.icon,
                    contentDescription = null,
                    tint = stat.color,
                    modifier = Modifier.size(28.dp)
                )
                
                Text(
                    text = stat.value.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = stat.color,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Text(
                    text = stat.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Modern Quick Actions with beautiful design
@Composable
fun ModernQuickActions(
    onAddWish: () -> Unit,
    onViewAllWishes: () -> Unit,
    onSearch: () -> Unit,
    onAnalytics: () -> Unit
) {
    val actions = listOf(
        QuickAction("Add Wish", Icons.Default.Add, Color(0xFF6366F1), onAddWish),
        QuickAction("View All", Icons.Default.ViewList, Color(0xFF8B5CF6), onViewAllWishes),
        QuickAction("Search", Icons.Default.Search, Color(0xFF06B6D4), onSearch),
        QuickAction("Analytics", Icons.Default.Analytics, Color(0xFF10B981), onAnalytics)
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        actions.forEach { action ->
            QuickActionButton(
                action = action,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

data class QuickAction(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun QuickActionButton(
    action: QuickAction,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        )
    )
    
    Card(
        modifier = modifier
            .height(80.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                action.onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = action.color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                action.icon,
                contentDescription = null,
                tint = action.color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = action.label,
                style = MaterialTheme.typography.labelSmall,
                color = action.color,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

// Stylish Category Pills
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StylishCategoryPills(
    onCategoryClick: (String) -> Unit
) {
    val categories = listOf(
        CategoryInfo("Electronics", Icons.Default.Computer, Color(0xFF6366F1)),
        CategoryInfo("Travel", Icons.Default.FlightTakeoff, Color(0xFF06B6D4)),
        CategoryInfo("Books", Icons.Default.Book, Color(0xFF8B5CF6)),
        CategoryInfo("Games", Icons.Default.SportsEsports, Color(0xFF10B981)),
        CategoryInfo("Food", Icons.Default.Restaurant, Color(0xFFF59E0B)),
        CategoryInfo("Sports", Icons.Default.Sports, Color(0xFFEF4444))
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(categories) { category ->
            CategoryPill(
                category = category,
                onClick = { onCategoryClick(category.name) }
            )
        }
    }
}

data class CategoryInfo(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun CategoryPill(
    category: CategoryInfo,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f
    )
    
    Surface(
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(50),
        color = category.color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, category.color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                category.icon,
                contentDescription = null,
                tint = category.color,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodySmall,
                color = category.color,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

// Beautiful Wish Card with glassmorphism
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeautifulWishCard(
    wish: Wish,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showDeleteDialog = true
            }
            false
        }
    )
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInHorizontally()
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFEF4444),
                                    Color(0xFFDC2626)
                                )
                            )
                        ),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(end = 24.dp)
                            .size(24.dp)
                    )
                }
            },
            content = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClick() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Priority indicator
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    when (wish.priority) {
                                        Priority.HIGH -> Color(0xFFEF4444).copy(alpha = 0.1f)
                                        Priority.MEDIUM -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                                        Priority.LOW -> Color(0xFF10B981).copy(alpha = 0.1f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                when (wish.priority) {
                                    Priority.HIGH -> Icons.Default.PriorityHigh
                                    Priority.MEDIUM -> Icons.Default.Remove
                                    Priority.LOW -> Icons.Default.KeyboardArrowDown
                                },
                                contentDescription = null,
                                tint = when (wish.priority) {
                                    Priority.HIGH -> Color(0xFFEF4444)
                                    Priority.MEDIUM -> Color(0xFFF59E0B)
                                    Priority.LOW -> Color(0xFF10B981)
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = wish.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F2937),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            if (wish.description.isNotBlank()) {
                                Text(
                                    text = wish.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6B7280),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            
                            // Tags
                            if (wish.tags.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    wish.tags.take(3).forEach { tag ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF6366F1).copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = tag,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF6366F1),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Completion status
                        if (wish.isCompleted) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "View",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        )
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete Wish",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to delete \"${wish.title}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Urgent Wish Card for high priority items
@Composable
fun UrgentWishCard(
    wish: Wish,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedColor by infiniteTransition.animateColor(
        initialValue = Color(0xFFEF4444).copy(alpha = 0.1f),
        targetValue = Color(0xFFEF4444).copy(alpha = 0.2f),
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(2.dp, animatedColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(animatedColor)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Urgent",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = wish.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                
                Text(
                    text = "High Priority • Needs attention",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFEF4444)
                )
            }
        }
    }
}

// High Priority Header
@Composable
fun HighPriorityHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.LocalFireDepartment,
            contentDescription = null,
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "Urgent Wishes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

// View All Card
@Composable
fun ViewAllCard(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6366F1).copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6366F1)
            )
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF6366F1),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(18.dp)
            )
        }
    }
}

// Stunning Empty State
@Composable
fun StunningEmptyState(
    onAddWish: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF6366F1).copy(alpha = 0.1f),
                                Color(0xFF8B5CF6).copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CardGiftcard,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFF6366F1)
                )
            }
            
            Text(
                text = "Start Your Journey",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(top = 24.dp)
            )
            
            Text(
                text = "Add your first wish and begin\nturning dreams into reality",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Button(
                onClick = onAddWish,
                modifier = Modifier
                    .padding(top = 32.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1)
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Create Your First Wish",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Animated Floating Action Button
@Composable
fun AnimatedFloatingActionButton(
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        )
    )
    
    val rotation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(scale)
    ) {
        FloatingActionButton(
            onClick = {
                isPressed = true
                onClick()
            },
            containerColor = Color(0xFF6366F1),
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Wish",
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotation)
            )
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}
