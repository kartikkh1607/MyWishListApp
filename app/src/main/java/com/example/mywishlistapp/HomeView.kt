package com.example.mywishlistapp

import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.Data.Wish
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ADDED: Screen navigation sealed class
//sealed class Screen(val route: String) {
//    object AddScreen : Screen("add_screen")
//    object WishListScreen : Screen("wishlist_screen")
//    object SearchScreen : Screen("search_screen")
//    object NotificationsScreen : Screen("notifications_screen")
//    object ProfileScreen : Screen("profile_screen")
//    object SettingsScreen : Screen("settings_screen")
//    object AnalyticsScreen : Screen("analytics_screen")
//}

// Theme Constants
object WishlistTheme {
    val PrimaryColor = Color(0xFF667EEA)
    val SecondaryColor = Color(0xFF764BA2)
    val SuccessColor = Color(0xFF10B981)
    val WarningColor = Color(0xFFF59E0B)
    val ErrorColor = Color(0xFFEF4444)
    val SurfaceColor = Color.White
    val BackgroundColor = Color(0xFFF5F3FF)
    val OnSurfaceColor = Color(0xFF1A1D29)
    val OnBackgroundColor = Color(0xFF2D3748)
    val MutedColor = Color(0xFF64748B)

    val CardElevation = 8.dp
    val SmallCardElevation = 4.dp
    val LargeRadius = 24.dp
    val MediumRadius = 16.dp
    val SmallRadius = 12.dp
}

// Helper data class for statistics

// Add this near the top of your HomeView file
data class HWishStats(
    val totalWishes: Int = 0,
    val completedWishes: Int = 0,
    val highPriorityWishes: Int = 0,
    val goalCount: Int = 0,
    val totalSavingsTarget: Double = 0.0,
    val totalSaved: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(navController: NavHostController, viewModel: WishViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State collection with error handling
    val recentWishes by viewModel.recentWishes.collectAsState()
    val wishStats by viewModel.wishStats.collectAsState()
    val unreadNotificationCount by viewModel.getUnreadNotificationCount().collectAsState()
    val userName by viewModel.getUserName().collectAsState(initial = "User")
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // UI states
    var showVoiceDialog by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Calculate statistics
    // Calculate statistics - FIXED with proper null handling
    val stats = remember(wishStats, recentWishes) {
        when {
            wishStats != null -> {
                val currentStats = wishStats!!
                HWishStats(
                    totalWishes = currentStats.totalWishes,
                    completedWishes = currentStats.completedWishes,
                    highPriorityWishes = currentStats.highPriorityWishes,
                    totalSavingsTarget = recentWishes.sumOf { it.price.toDoubleOrNull() ?: 0.0 },
                    totalSaved = recentWishes.sumOf { it.savedAmount }
                )
            }

            else -> HWishStats(
                totalWishes = recentWishes.size,
                completedWishes = recentWishes.count { it.isCompleted },
                highPriorityWishes = recentWishes.count { it.priority == Priority.HIGH },
                totalSavingsTarget = recentWishes.sumOf { it.price.toDoubleOrNull() ?: 0.0 },
                totalSaved = recentWishes.sumOf { it.savedAmount }
            )
        }
    }


    // Get current time for greeting
    val currentHour =
        remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greeting = remember(currentHour) {
        when (currentHour) {
            in 0..5 -> "Good Night"
            in 6..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        WishlistTheme.BackgroundColor,
                        Color(0xFFE8E5FF),
                        Color(0xFFDDD9FF).copy(alpha = 0.5f)
                    )
                )
            )
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0),
            topBar = {
                StunningTopBar(
                    userName = userName,
                    greeting = greeting,
                    unreadNotificationCount = unreadNotificationCount,
                    onSearchClicked = { navController.navigate(Screen.SearchScreen.route) },
                    onNotificationClicked = { navController.navigate(Screen.NotificationsScreen.route) },
                    onProfileClicked = { navController.navigate(Screen.ProfileScreen.route) }
                )
            },
            floatingActionButton = {
                AnimatedFloatingActionButton(
                    onClick = { navController.navigate(Screen.AddScreen.route + "/0") }
                )
            },
            floatingActionButtonPosition = FabPosition.End
        ) { innerPadding ->
            // Error state
            if (errorMessage.isNotEmpty()) {
                ErrorBanner(
                    message = errorMessage,
                    onDismiss = { viewModel.clearError() }
                )
            }

            // Loading state
            if (isLoading) {
                LoadingIndicator()
            } else if (stats.totalWishes == 0) {
                // Empty state
                StunningEmptyState {
                    navController.navigate(Screen.AddScreen.route + "/0")
                }
            } else {
                // Main content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Welcome Card
                    item {
                        PersonalizedWelcomeCard(
                            userName = userName,
                            greeting = greeting,
                            completionRate = if (stats.totalWishes > 0)
                                (stats.completedWishes.toFloat() / stats.totalWishes) else 0f,
                            streak = 7
                        )
                    }

                    // Stats Cards - FIXED: Remove the unsafe casting
                    item {
                        ModernStatsCards(stats = stats as HWishStats)
                    }

                    // Rest of your existing items...
                    // Quick Actions, Category Pills, Recent Wishes, etc.
                }
            }
        }
    }

    // Voice Command Dialog
    if (showVoiceDialog) {
        VoiceCommandDialog(
            isVisible = showVoiceDialog,
            onDismiss = { showVoiceDialog = false },
            onWishAdd = { title, description, category, priority, tags ->
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
            onWishSearch = { query ->
                navController.navigate(Screen.SearchScreen.route + "?query=$query")
                showVoiceDialog = false
            }
        )
    }
}


@Composable
fun StunningTopBar(
    userName: String,
    greeting: String,
    unreadNotificationCount: Int,
    onSearchClicked: () -> Unit,
    onNotificationClicked: () -> Unit,
    onProfileClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(WishlistTheme.LargeRadius),
        colors = CardDefaults.cardColors(containerColor = WishlistTheme.PrimaryColor),
        elevation = CardDefaults.cardElevation(defaultElevation = WishlistTheme.CardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$greeting!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onSearchClicked) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box {
                    IconButton(onClick = onNotificationClicked) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    if (unreadNotificationCount > 0) {
                        Badge(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp),
                            containerColor = WishlistTheme.ErrorColor
                        ) {
                            Text(
                                text = if (unreadNotificationCount > 99) "99+" else unreadNotificationCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                IconButton(onClick = onProfileClicked) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PersonalizedWelcomeCard(
    userName: String,
    greeting: String,
    completionRate: Float,
    streak: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(WishlistTheme.LargeRadius),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = WishlistTheme.CardElevation)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            WishlistTheme.PrimaryColor.copy(alpha = 0.1f),
                            WishlistTheme.SecondaryColor.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome back, $userName!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = WishlistTheme.OnSurfaceColor
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Completion Rate
                            Column {
                                Text(
                                    text = "${(completionRate * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WishlistTheme.SuccessColor
                                )
                                Text(
                                    text = "Complete",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WishlistTheme.MutedColor
                                )
                            }

                            // Streak
                            Column {
                                Text(
                                    text = "$streak",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WishlistTheme.WarningColor
                                )
                                Text(
                                    text = "Day Streak",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WishlistTheme.MutedColor
                                )
                            }
                        }
                    }

                    // Motivational Icon
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                WishlistTheme.SuccessColor.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                completionRate >= 0.8f -> "🎉"
                                completionRate >= 0.5f -> "💪"
                                else -> "🌟"
                            },
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernStatsCards(stats: HWishStats) {
    Column {
        SectionHeader(title = "Your Statistics")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeStatCard(
                modifier = Modifier.weight(1f),
                title = "Total",
                value = stats.totalWishes.toString(),
                icon = Icons.AutoMirrored.Filled.List,
                color = WishlistTheme.PrimaryColor
            )

            HomeStatCard(
                modifier = Modifier.weight(1f),
                title = "Completed",
                value = stats.completedWishes.toString(),
                icon = Icons.Default.CheckCircle,
                color = WishlistTheme.SuccessColor
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeStatCard(
                modifier = Modifier.weight(1f),
                title = "Pending",
                value = (stats.totalWishes - stats.completedWishes).toString(),
                icon = Icons.Default.Schedule,
                color = WishlistTheme.WarningColor
            )

            HomeStatCard(
                modifier = Modifier.weight(1f),
                title = "High Priority",
                value = stats.highPriorityWishes.toString(),
                icon = Icons.Default.PriorityHigh,
                color = WishlistTheme.ErrorColor
            )
        }

        if (stats.totalSavingsTarget > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            SavingsProgressCard(
                totalTarget = stats.totalSavingsTarget,
                totalSaved = stats.totalSaved
            )
        }
    }
}

@Composable
fun HomeStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(WishlistTheme.MediumRadius),
        colors = CardDefaults.cardColors(containerColor = WishlistTheme.SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = WishlistTheme.SmallCardElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = WishlistTheme.MutedColor,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun SavingsProgressCard(
    totalTarget: Double,
    totalSaved: Double
) {
    val progress = if (totalTarget > 0) (totalSaved / totalTarget).coerceIn(0.0, 1.0) else 0.0
    val progressPercentage = (progress * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(WishlistTheme.MediumRadius),
        colors = CardDefaults.cardColors(containerColor = WishlistTheme.SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = WishlistTheme.SmallCardElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = WishlistTheme.SuccessColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Savings Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WishlistTheme.OnSurfaceColor
                )
            }

            LinearProgressIndicator(
                progress = { progress.toFloat() }, // CORRECTED: Use lambda for progress
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = WishlistTheme.SuccessColor,
                trackColor = WishlistTheme.SuccessColor.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Saved: $${totalSaved.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WishlistTheme.SuccessColor,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$progressPercentage% of $${totalTarget.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WishlistTheme.MutedColor
                )
            }
        }
    }
}

@Composable
fun ModernQuickActions(
    onAddWish: () -> Unit,
    onViewAllWishes: () -> Unit,
    onSearch: () -> Unit,
    onAnalytics: () -> Unit
) {
    Column {
        SectionHeader(title = "Quick Actions")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "Add Wish",
                icon = Icons.Default.Add,
                color = WishlistTheme.PrimaryColor,
                onClick = onAddWish
            )

            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "View All",
                icon = Icons.Default.List,
                color = WishlistTheme.SuccessColor,
                onClick = onViewAllWishes
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "Search",
                icon = Icons.Default.Search,
                color = WishlistTheme.WarningColor,
                onClick = onSearch
            )

            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "Analytics",
                icon = Icons.Default.Analytics,
                color = WishlistTheme.SecondaryColor,
                onClick = onAnalytics
            )
        }
    }
}

@Composable
fun QuickActionButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
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
            scope.launch {
                delay(100)
                isPressed = false
            }
            onClick()
        },
        modifier = modifier
            .scale(scale)
            .height(56.dp),
        shape = RoundedCornerShape(WishlistTheme.MediumRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.1f),
            contentColor = color
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StylishCategoryPills(
    onCategoryClick: (String) -> Unit
) {
    val categories = listOf(
        Triple("Electronics", Icons.Default.Computer, WishlistTheme.PrimaryColor),
        Triple("Travel", Icons.Default.FlightTakeoff, WishlistTheme.SuccessColor),
        Triple("Books", Icons.Default.Book, WishlistTheme.WarningColor),
        Triple("Sports", Icons.Default.Sports, WishlistTheme.ErrorColor),
        Triple("Home", Icons.Default.Home, WishlistTheme.SecondaryColor),
        Triple("Games", Icons.Default.Games, Color(0xFF9333EA)),
        Triple("Food", Icons.Default.Restaurant, Color(0xFFEC4899))
    )

    Column {
        SectionHeader(title = "Popular Categories")

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(categories) { (category, icon, color) ->
                CategoryPill(
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
fun CategoryPill(
    category: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pill_scale"
    )

    Card(
        modifier = Modifier
            .scale(scale)
            .clickable {
                isPressed = true
                scope.launch {
                    delay(100)
                    isPressed = false
                }
                onClick()
            },
        shape = RoundedCornerShape(WishlistTheme.MediumRadius),
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeautifulWishCard(
    wish: Wish,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    // CORRECTED: Proper SwipeToDismissBox usage
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            SwipeBackground(dismissState = dismissState)
        }
    ) {
        WishCard(
            wish = wish,
            onClick = onClick
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WishCard(
    wish: Wish,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
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
            .clickable {
                isPressed = true
                scope.launch {
                    delay(100)
                    isPressed = false
                }
                onClick()
            },
        shape = RoundedCornerShape(WishlistTheme.MediumRadius),
        colors = CardDefaults.cardColors(containerColor = WishlistTheme.SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = WishlistTheme.SmallCardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Icon
            val categoryIcon = getCategoryIcon(wish.category)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        WishlistTheme.PrimaryColor.copy(alpha = 0.1f),
                        RoundedCornerShape(WishlistTheme.SmallRadius)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = WishlistTheme.PrimaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title and Category
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = wish.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WishlistTheme.OnSurfaceColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (wish.category.isNotEmpty()) {
                        Text(
                            text = wish.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = WishlistTheme.PrimaryColor,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .background(
                                    WishlistTheme.PrimaryColor.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Priority Badge
                val priorityConfig = getPriorityConfig(wish.priority)
                Text(
                    text = "${priorityConfig.third} ${wish.priority.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = priorityConfig.first,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(
                            priorityConfig.second,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )

                // Description
                Text(
                    text = wish.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = WishlistTheme.MutedColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Tags
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
                                color = WishlistTheme.MutedColor,
                                modifier = Modifier
                                    .background(
                                        WishlistTheme.MutedColor.copy(alpha = 0.1f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        if (wish.tags.size > 3) {
                            Text(
                                text = "+${wish.tags.size - 3}",
                                style = MaterialTheme.typography.labelSmall,
                                color = WishlistTheme.MutedColor
                            )
                        }
                    }
                }
            }

            // Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = WishlistTheme.MutedColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeBackground(dismissState: SwipeToDismissBoxState) {
    // CORRECTED: Use targetValue instead of dismissDirection
    val color by animateColorAsState(
        when (dismissState.targetValue) {
            SwipeToDismissBoxValue.EndToStart -> WishlistTheme.ErrorColor
            else -> Color.Transparent
        },
        label = "swipe_color"
    )

    val scale by animateFloatAsState(
        when (dismissState.targetValue) {
            SwipeToDismissBoxValue.EndToStart -> 1.2f
            else -> 0.8f
        },
        label = "icon_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color,
                shape = RoundedCornerShape(WishlistTheme.MediumRadius)
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

@Composable
fun UrgentWishCard(
    wish: Wish,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "urgent_card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                isPressed = true
                scope.launch {
                    delay(100)
                    isPressed = false
                }
                onClick()
            },
        shape = RoundedCornerShape(WishlistTheme.MediumRadius),
        colors = CardDefaults.cardColors(containerColor = WishlistTheme.SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = WishlistTheme.CardElevation),
        border = BorderStroke(2.dp, WishlistTheme.ErrorColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            WishlistTheme.ErrorColor.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Urgent indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        WishlistTheme.ErrorColor.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🔥", fontSize = 18.sp)
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = wish.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WishlistTheme.ErrorColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = wish.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = WishlistTheme.MutedColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "HIGH PRIORITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = WishlistTheme.ErrorColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = WishlistTheme.ErrorColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ViewAllCard(
    text: String,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "view_all_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                isPressed = true
                scope.launch {
                    delay(100)
                    isPressed = false
                }
                onClick()
            },
        shape = RoundedCornerShape(WishlistTheme.MediumRadius),
        colors = CardDefaults.cardColors(
            containerColor = WishlistTheme.PrimaryColor.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = WishlistTheme.PrimaryColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = WishlistTheme.PrimaryColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AnimatedFloatingActionButton(
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
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
            scope.launch {
                delay(100)
                isPressed = false
            }
            onClick()
        },
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = WishlistTheme.PrimaryColor.copy(alpha = 0.3f),
                spotColor = WishlistTheme.PrimaryColor.copy(alpha = 0.3f)
            ),
        containerColor = WishlistTheme.PrimaryColor,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 12.dp,
            pressedElevation = 20.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Wish",
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun StunningEmptyState(
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
                            WishlistTheme.PrimaryColor.copy(alpha = 0.15f),
                            WishlistTheme.PrimaryColor.copy(alpha = 0.05f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.List,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = WishlistTheme.PrimaryColor.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Start Your Wish Journey!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = WishlistTheme.OnSurfaceColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your wish list is empty, but your dreams aren't!\nStart by adding your first wish and watch\nyour goals come to life.",
            style = MaterialTheme.typography.bodyMedium,
            color = WishlistTheme.MutedColor,
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
                containerColor = WishlistTheme.PrimaryColor
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

@Composable
fun HighPriorityHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.PriorityHigh,
            contentDescription = null,
            tint = WishlistTheme.ErrorColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "High Priority Wishes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = WishlistTheme.ErrorColor
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = WishlistTheme.OnBackgroundColor,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(WishlistTheme.SmallRadius),
        colors = CardDefaults.cardColors(
            containerColor = WishlistTheme.ErrorColor.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, WishlistTheme.ErrorColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = WishlistTheme.ErrorColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WishlistTheme.ErrorColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = WishlistTheme.ErrorColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = WishlistTheme.PrimaryColor,
                strokeWidth = 4.dp
            )
            Text(
                text = "Loading your wishes...",
                style = MaterialTheme.typography.bodyMedium,
                color = WishlistTheme.MutedColor
            )
        }
    }
}

@Composable
fun VoiceCommandDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onWishAdd: (String, String, String, Priority, List<String>) -> Unit,
    onWishSearch: (String) -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Voice Command",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Voice commands are not implemented yet. This is a placeholder for future functionality.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Planned features:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("• Add wish by voice", style = MaterialTheme.typography.bodySmall)
                        Text("• Search wishes by voice", style = MaterialTheme.typography.bodySmall)
                        Text("• Navigate app by voice", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Got it")
                }
            }
        )
    }
}

// Helper functions
fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
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
        "" -> Icons.Default.Category
        else -> Icons.Default.Star
    }
}

fun getPriorityConfig(priority: Priority): Triple<Color, Color, String> {
    return when (priority) {
        Priority.LOW -> Triple(
            WishlistTheme.SuccessColor,
            WishlistTheme.SuccessColor.copy(alpha = 0.1f),
            "🌱"
        )

        Priority.MEDIUM -> Triple(
            WishlistTheme.WarningColor,
            WishlistTheme.WarningColor.copy(alpha = 0.1f),
            "⚡"
        )

        Priority.HIGH -> Triple(
            WishlistTheme.ErrorColor,
            WishlistTheme.ErrorColor.copy(alpha = 0.1f),
            "🔥"
        )
    }
}
