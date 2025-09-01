package com.example.mywishlistapp

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.FabPosition
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.ui.components.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

// Helper data class for tuple handling
data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(navController: NavHostController, viewModel: WishViewModel) {
    val context = LocalContext.current
    
    // Use optimized flows for better performance
    val recentWishes by viewModel.recentWishes.collectAsState()
    val wishStats by viewModel.wishStats.collectAsState()
    val unreadNotificationCount by viewModel.getUnreadNotificationCount().collectAsState()

    // Calculate statistics using cached data when available
    val (totalWishes, completedWishes, highPriorityWishes, goalCount) = remember(wishStats) {
        if (wishStats != null) {
            Tuple4(
                wishStats!!.totalWishes,
                wishStats!!.completedWishes,
                wishStats!!.highPriorityWishes,
                wishStats!!.goalCount
            )
        } else {
            // Fallback to empty stats
            Tuple4(0, 0, 0, 0)
        }
    }
    
    val pendingWishes = remember(totalWishes, completedWishes) {
        totalWishes - completedWishes
    }
    
    // Optimized calculations with memoization
    val (totalSavingsTarget, totalSaved) = remember(recentWishes) {
        val target = recentWishes.sumOf { it.price.toDoubleOrNull() ?: 0.0 }
        val saved = recentWishes.sumOf { it.savedAmount }
        Pair(target, saved)
    }
    
    // Loading and UI states
    var showVoiceDialog by remember { mutableStateOf(false) }
    
    // Get current time for greeting
    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greeting = remember(currentHour) {
        when (currentHour) {
            in 0..5 -> "Good Night! 🌙"
            in 6..11 -> "Good Morning! ☀️"
            in 12..16 -> "Good Afternoon! 🌤️"
            in 17..20 -> "Good Evening! 🌅"
            else -> "Good Night! 🌙"
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ModernAppBarView(
                title = "Dashboard",
                greeting = greeting,
                onSearchClicked = {
                    navController.navigate(Screen.SearchScreen.route)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8FAFF),
                            Color(0xFFF0F4FF),
                            Color(0xFFE8F0FE)
                        )
                    )
                )
        ) {
            if (totalWishes == 0) {
                EnhancedEmptyWishListState {
                    navController.navigate(Screen.AddScreen.route + "/0")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        DashboardStatsSection(
                            totalWishes = totalWishes,
                            completedWishes = completedWishes,
                            pendingWishes = pendingWishes,
                            highPriorityWishes = highPriorityWishes,
                            totalSavingsTarget = totalSavingsTarget,
                            totalSaved = totalSaved
                        )
                    }
                    
                    item {
                        QuickActionsSection(
                            onAddWish = { navController.navigate(Screen.AddScreen.route + "/0") },
                            onViewAllWishes = { navController.navigate(Screen.WishListScreen.route) },
                            onSearch = { navController.navigate(Screen.SearchScreen.route) }
                        )
                    }
                    
                    item {
                        CategoryQuickAccessSection(
                            onCategoryClick = { category ->
                                navController.navigate(Screen.AddScreen.route + "/0?category=$category")
                            }
                        )
                    }
                    
                    if (recentWishes.isNotEmpty()) {
                        item {
                            HomeRecentWishesSection(
                                recentWishes = recentWishes,
                                onWishClick = { wish ->
                                    navController.navigate(Screen.AddScreen.route + "/${wish.id}")
                                },
                                onViewAll = {
                                    navController.navigate(Screen.WishListScreen.route)
                                }
                            )
                        }
                    }
                    
                    if (highPriorityWishes > 0) {
                        item {
                            HighPriorityWishesSection(
                                highPriorityWishes = recentWishes.filter { it.priority == Priority.HIGH },
                                onWishClick = { wish ->
                                    navController.navigate(Screen.AddScreen.route + "/${wish.id}")
                                }
                            )
                        }
                    }
                }
            }
            
            // Enhanced Floating Action Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                EnhancedFAB(
                    onClick = {
                        navController.navigate(Screen.AddScreen.route + "/0")
                    },
                    icon = Icons.Default.Add
                )
            }
        }
    }

    // Voice Command Dialog
    if (showVoiceDialog) {
        VoiceCommandDialog(
            isVisible = showVoiceDialog,
            onDismiss = { showVoiceDialog = false },
            onWishAdd = { title: String, description: String, category: String, priority: Priority, tags: List<String> ->
                viewModel.addWish(
                    Wish(
                        title = title,
                        description = description,
                        category = category,
                        priority = priority,
                        tags = tags
                    )
                )
                showVoiceDialog = false
            },
            onWishSearch = { query: String ->
                navController.navigate(Screen.SearchScreen.route + "?query=$query")
                showVoiceDialog = false
            },
            onVoiceCommand = { /* handle voice command if needed */ }
        )
    }
}

@Composable
fun EmptyWishListState(onAddWish: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large decorative icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(60.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colorResource(R.color.app_bar_color).copy(alpha = 0.1f),
                            colorResource(R.color.app_bar_color).copy(alpha = 0.05f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.List,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = colorResource(R.color.app_bar_color).copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your Wish List is Empty",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add your first wish to get started!\nTap the + button to create one.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeBackground(dismissState: SwipeToDismissBoxState) {
    val color by animateColorAsState(
        when (dismissState.dismissDirection) {
            SwipeToDismissBoxValue.EndToStart -> Color(0xFFE74C3C)
            else -> Color.Transparent
        },
        label = "swipe_color"
    )

    val scale by animateFloatAsState(
        when (dismissState.dismissDirection) {
            SwipeToDismissBoxValue.EndToStart -> 1.3f
            else -> 0.8f
        },
        label = "icon_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = Color.White,
            modifier = Modifier.scale(scale)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnhancedWishItem(wish: Wish, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f) // Glassmorphism effect
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 12.dp,
            pressedElevation = 6.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.8f),
                            Color.White.copy(alpha = 0.6f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dynamic Category Icon with more comprehensive mapping
                val categoryIcon = when (wish.category.lowercase()) {
                    "electronics", "electronic", "tech", "technology" -> Icons.Default.Computer
                    "book", "books", "reading", "education" -> Icons.Default.Book
                    "home", "house", "household", "furniture" -> Icons.Default.Home
                    "games", "gaming", "game", "video games" -> Icons.Default.Games
                    "work", "office", "business", "professional" -> Icons.Default.Work
                    "travel", "trip", "vacation", "holiday" -> Icons.Default.FlightTakeoff
                    "sports", "sport", "fitness", "exercise" -> Icons.Default.Sports
                    "car", "automotive", "vehicle", "transport" -> Icons.Default.DirectionsCar
                    "food", "restaurant", "dining", "cooking" -> Icons.Default.Restaurant
                    "music", "audio", "sound", "musical" -> Icons.Default.MusicNote
                    "entertainment", "fun", "hobby" -> Icons.Default.SportsEsports
                    "gift", "gifts", "present" -> Icons.Default.CardGiftcard
                    "" -> Icons.Default.Category // Default icon for empty category
                    else -> Icons.Default.Star // Fallback for unknown categories
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF667EEA), // Beautiful purple-blue
                                    Color(0xFF764BA2)  // Deep purple
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Enhanced content with better typography
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Title and Category Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = wish.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A1D29),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )

                        // Category Badge
                        if (wish.category.isNotEmpty()) {
                            Text(
                                text = wish.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF667EEA),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(
                                        Color(0xFF667EEA).copy(alpha = 0.1f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Priority Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (priorityColor, priorityBgColor, priorityIcon) = when (wish.priority) {
                            Priority.LOW -> Triple(
                                Color(0xFF10B981), // Emerald-500
                                Color(0xFF10B981).copy(alpha = 0.1f),
                                "🌱"
                            )
                            Priority.MEDIUM -> Triple(
                                Color(0xFFF59E0B), // Amber-500
                                Color(0xFFF59E0B).copy(alpha = 0.1f),
                                "⚡"
                            )
                            Priority.HIGH -> Triple(
                                Color(0xFFEF4444), // Red-500
                                Color(0xFFEF4444).copy(alpha = 0.1f),
                                "🔥"
                            )
                        }

                        // Priority badge with icon and text
                        Text(
                            text = "$priorityIcon ${wish.priority.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .background(
                                    priorityBgColor,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = wish.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B).copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp,
                        fontSize = 13.sp
                    )

                    // Tags Row
                    if (wish.tags.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            wish.tags.take(3).forEach { tag ->
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF8B9DC3),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .background(
                                            Color(0xFF8B9DC3).copy(alpha = 0.08f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 9.sp
                                )
                            }
                            if (wish.tags.size > 3) {
                                Text(
                                    text = "+${wish.tags.size - 3}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF8B9DC3),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                // Subtle arrow indicator
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF667EEA).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFF667EEA),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationFAB(
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "fab_scale"
    )

    FloatingActionButton(
        onClick = {
            isPressed = true
            onClick()
            // Reset pressed state after a short delay
            kotlinx.coroutines.GlobalScope.launch {
                kotlinx.coroutines.delay(100)
                isPressed = false
            }
        },
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = Color(0xFF667EEA).copy(alpha = 0.3f),
                spotColor = Color(0xFF667EEA).copy(alpha = 0.3f)
            ),
        containerColor = Color(0xFF667EEA),
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 12.dp,
            pressedElevation = 20.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Wish",
            modifier = Modifier.size(28.dp),
            tint = Color.White
        )
    }
}

@Composable
fun WishItem(wish: Wish, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 8.dp, end = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.white)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    )
    {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = wish.title, fontWeight = FontWeight.ExtraBold)
            Text(text = wish.description)
        }
    }
}

// Enhanced Dashboard Components

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernAppBarView(
    title: String,
    greeting: String,
    onSearchClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF667EEA)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 24.sp
                )
            }
            
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onSearchClicked() }
            )
        }
    }
}

@Composable
fun DashboardStatsSection(
    totalWishes: Int,
    completedWishes: Int,
    pendingWishes: Int,
    highPriorityWishes: Int,
    totalSavingsTarget: Double,
    totalSaved: Double
) {
    Column {
        Text(
            text = "📊 Your Statistics",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Total",
                value = totalWishes.toString(),
                icon = Icons.Default.ListAlt,
                color = Color(0xFF667EEA),
                backgroundColor = Color(0xFF667EEA).copy(alpha = 0.1f)
            )
            
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Completed",
                value = completedWishes.toString(),
                icon = Icons.Default.EmojiEvents,
                color = Color(0xFF10B981),
                backgroundColor = Color(0xFF10B981).copy(alpha = 0.1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Pending",
                value = pendingWishes.toString(),
                icon = Icons.Default.Analytics,
                color = Color(0xFFF59E0B),
                backgroundColor = Color(0xFFF59E0B).copy(alpha = 0.1f)
            )
            
            StatCard(
                modifier = Modifier.weight(1f),
                title = "High Priority",
                value = highPriorityWishes.toString(),
                icon = Icons.Default.LocalFireDepartment,
                color = Color(0xFFEF4444),
                backgroundColor = Color(0xFFEF4444).copy(alpha = 0.1f)
            )
        }
        
        // Savings progress card if there are any savings targets
        if (totalSavingsTarget > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            SavingsOverviewCard(
                totalTarget = totalSavingsTarget,
                totalSaved = totalSaved
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    backgroundColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        backgroundColor,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SavingsOverviewCard(
    totalTarget: Double,
    totalSaved: Double
) {
    val progress = if (totalTarget > 0) (totalSaved / totalTarget).coerceIn(0.0, 1.0) else 0.0
    val progressPercentage = (progress * 100).toInt()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Savings Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format("Saved: $%.0f", totalSaved),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$progressPercentage%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF667EEA),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = String.format("Target: $%.0f", totalTarget),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
fun QuickActionsSection(
    onAddWish: () -> Unit,
    onViewAllWishes: () -> Unit,
    onSearch: () -> Unit
) {
    Column {
        Text(
            text = "⚡ Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "Add Wish",
                icon = Icons.Default.Add,
                color = Color(0xFF667EEA),
                onClick = onAddWish
            )
            
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "View All",
                icon = Icons.Default.ListAlt,
                color = Color(0xFF10B981),
                onClick = onViewAllWishes
            )
            
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "Search",
                icon = Icons.Default.Search,
                color = Color(0xFFF59E0B),
                onClick = onSearch
            )
        }
    }
}

@Composable
fun QuickActionButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_scale"
    )
    
    Button(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier
            .scale(scale)
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.1f),
            contentColor = color
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
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

@Composable
fun CategoryQuickAccessSection(
    onCategoryClick: (String) -> Unit
) {
    val categories = listOf(
        Triple("Electronics", Icons.Default.Computer, Color(0xFF667EEA)),
        Triple("Travel", Icons.Default.FlightTakeoff, Color(0xFF10B981)),
        Triple("Books", Icons.Default.Book, Color(0xFFF59E0B)),
        Triple("Sports", Icons.Default.Sports, Color(0xFFEF4444)),
        Triple("Home", Icons.Default.Home, Color(0xFF8B5CF6))
    )
    
    Column {
        Text(
            text = "🏷️ Popular Categories",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(categories.size) { index ->
                val (category, icon, color) = categories[index]
                CategoryChip(
                    category = category,
                    icon = icon,
                    color = color,
                    onClick = { onCategoryClick(category) }
                )
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "chip_scale"
    )
    
    Card(
        modifier = Modifier
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = category,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Medium
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

@Composable
fun HomeRecentWishesSection(
    recentWishes: List<Wish>,
    onWishClick: (Wish) -> Unit,
    onViewAll: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🕒 Recent Wishes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            
            Text(
                text = "View All",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF667EEA),
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { onViewAll() }
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        recentWishes.forEach { wish ->
            CompactWishItem(
                wish = wish,
                onClick = { onWishClick(wish) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun HighPriorityWishesSection(
    highPriorityWishes: List<Wish>,
    onWishClick: (Wish) -> Unit
) {
    Column {
        Text(
            text = "🔥 High Priority",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEF4444),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        highPriorityWishes.take(2).forEach { wish ->
            CompactWishItem(
                wish = wish,
                onClick = { onWishClick(wish) },
                highlightColor = Color(0xFFEF4444)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CompactWishItem(
    wish: Wish,
    onClick: () -> Unit,
    highlightColor: Color? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "compact_item_scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = if (highlightColor != null) BorderStroke(2.dp, highlightColor.copy(alpha = 0.3f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val categoryIcon = when (wish.category.lowercase()) {
                "electronics", "electronic", "tech" -> Icons.Default.Computer
                "book", "books", "reading" -> Icons.Default.Book
                "home", "house", "household" -> Icons.Default.Home
                "games", "gaming", "game" -> Icons.Default.Games
                "work", "office", "business" -> Icons.Default.Work
                "travel", "trip", "vacation" -> Icons.Default.FlightTakeoff
                "sports", "sport", "fitness" -> Icons.Default.Sports
                else -> Icons.Default.Star
            }
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        (highlightColor ?: Color(0xFF667EEA)).copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = highlightColor ?: Color(0xFF667EEA),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = wish.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1D29),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = wish.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
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

@Composable
fun EnhancedEmptyWishListState(
    onAddWish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated decorative icon
        var animationPlayed by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(
            targetValue = if (animationPlayed) 1f else 0.3f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "empty_state_scale"
        )
        
        LaunchedEffect(Unit) {
            delay(300)
            animationPlayed = true
        }
        
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(scale)
                .clip(RoundedCornerShape(70.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF667EEA).copy(alpha = 0.15f),
                            Color(0xFF667EEA).copy(alpha = 0.05f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🌟",
                fontSize = 64.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Start Your Wish Journey!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your wish list is empty, but your dreams aren't!\nStart by adding your first wish and watch\nyour goals come to life.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onAddWish,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF667EEA)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Your First Wish",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
