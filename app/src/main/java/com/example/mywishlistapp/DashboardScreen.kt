package com.example.mywishlistapp

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.ui.components.DashboardSkeleton
import com.example.mywishlistapp.ui.components.GoalProgressAnalyticsCard
import com.example.mywishlistapp.ui.components.CompletionStatisticsCard
import com.example.mywishlistapp.ui.components.MotivationalInsightsCard
import com.example.mywishlistapp.ui.components.UpcomingDeadlinesCard
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Data class for category items
data class CategoryItem(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavHostController, viewModel: WishViewModel) {
    val wishList = viewModel.getAllWishes.collectAsState(initial = null)
    val unreadNotificationCount by viewModel.getUnreadNotificationCount().collectAsState()
    val userProfile by viewModel.userProfile.collectAsState(initial = null)
    val currentTime = Calendar.getInstance().time
    val dateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
    val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    
    // Personal Growth Analytics Data
    val goalAnalytics by viewModel.getGoalAnalytics().collectAsState(initial = WishViewModel.GoalAnalytics())
    val activeGoals by viewModel.getActiveGoalsWithProgress().collectAsState(initial = emptyList())
    val upcomingDeadlines by viewModel.getUpcomingDeadlines().collectAsState(initial = emptyList())
    val motivationalInsights by viewModel.getMotivationalInsights().collectAsState(initial = emptyList())
    val wishesVsGoals by viewModel.getCompletionStats().collectAsState(initial = Pair(0, 0))
    val streakData by viewModel.getStreakData().collectAsState(initial = 0)

    // Enhanced greeting with emojis
    val greeting = when (hourOfDay) {
        in 5..11 -> "Good Morning ☀️"
        in 12..16 -> "Good Afternoon 🌤️"
        in 17..20 -> "Good Evening 🌅"
        else -> "Good Night 🌙"
    }

    // Check if data is loading
    val isLoading = wishList.value == null || userProfile == null
    
    // Calculate progress metrics (only when data is available)
    val totalWishes = wishList.value?.size ?: 0
    val completedWishes = wishList.value?.count { it.isCompleted } ?: 0
    val completionRate = if (totalWishes > 0) (completedWishes.toFloat() / totalWishes.toFloat()) else 0f
    val totalSavingsTarget = wishList.value?.sumOf { it.price.toDoubleOrNull() ?: 0.0 } ?: 0.0
    val totalSaved = wishList.value?.sumOf { it.savedAmount } ?: 0.0
    val savingsProgress = if (totalSavingsTarget > 0) (totalSaved / totalSavingsTarget).toFloat() else 0f

    // Animation states
    var contentVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        contentVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
        // Top Header with functional icons
        TopHeaderSection(
            onNotificationClick = {
                // Navigate to Notifications screen
                navController.navigate(Screen.NotificationsScreen.route)
            },
            onGamifyClick = {
                // Navigate to Profile screen to see achievements
                navController.navigate(Screen.ProfileScreen.route)
            },
            onSearchClick = {
                navController.navigate(Screen.SearchScreen.route)
            },
            onProfileClick = {
                navController.navigate(Screen.ProfileScreen.route)
            },
            hasNotifications = unreadNotificationCount > 0
        )

        // Show skeleton loading or actual content
        if (isLoading) {
            DashboardSkeleton()
        } else {
            AnimatedVisibility(
                visible = contentVisible,
                enter = fadeIn(animationSpec = tween(800)) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(800)
                )
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Enhanced User Greeting Section with improved typography
                    item {
                        GreetingSection(
                            greeting = greeting,
                            currentTime = dateFormat.format(currentTime),
                            userName = userProfile?.name?.takeIf { it.isNotEmpty() } ?: userProfile?.username ?: "User",
                            navController = navController,
                            onEditUserName = { newName ->
                                viewModel.saveUserName(newName)
                            }
                        )
                    }

                    // Progress Overview Card
                    if (totalWishes > 0) {
                        item {
                            ProgressOverviewCard(
                                completionRate = completionRate,
                                totalWishes = totalWishes,
                                completedWishes = completedWishes,
                                savingsProgress = savingsProgress,
                                totalSaved = totalSaved,
                                totalSavingsTarget = totalSavingsTarget
                            )
                        }
                    }

                    // Personal Growth Analytics - Goal Progress
                    if (activeGoals.isNotEmpty()) {
                        item {
                            GoalProgressAnalyticsCard(
                                activeGoals = activeGoals,
                                onGoalClick = { goal ->
                                    navController.navigate(Screen.AddScreen.route + "/${goal.id}")
                                }
                            )
                        }
                    }
                    
                    // Personal Growth Analytics - Statistics
                    if (goalAnalytics.totalGoals > 0) {
                        item {
                            CompletionStatisticsCard(
                                goalAnalytics = goalAnalytics,
                                wishesVsGoals = wishesVsGoals,
                                streakData = streakData
                            )
                        }
                    }
                    
                    // Motivational Insights
                    if (motivationalInsights.isNotEmpty()) {
                        item {
                            MotivationalInsightsCard(
                                insights = motivationalInsights
                            )
                        }
                    }
                    
                    // Upcoming Deadlines
                    if (upcomingDeadlines.isNotEmpty()) {
                        item {
                            UpcomingDeadlinesCard(
                                upcomingDeadlines = upcomingDeadlines,
                                onGoalClick = { goal ->
                                    navController.navigate(Screen.AddScreen.route + "/${goal.id}")
                                }
                            )
                        }
                    }
                    
                    // Motivational Message (fallback for users without goals)
                    if (goalAnalytics.totalGoals == 0) {
                        item {
                            MotivationalCard(
                                completionRate = completionRate,
                                totalWishes = totalWishes
                            )
                        }
                    }

                    // Quick Stats
                    item {
                        QuickStatsSection(wishList = wishList.value ?: emptyList())
                    }

                    // Category Shortcuts
                    item {
                        CategoryShortcuts(navController = navController)
                    }

                    // Recent Wishes (only if there are wishes)
                    wishList.value?.let { wishes ->
                        if (wishes.isNotEmpty()) {
                            item {
                                RecentWishesSection(
                                    wishes = wishes.take(3), // Show 3 recent wishes
                                    onWishClick = { wish ->
                                        navController.navigate(Screen.AddScreen.route + "/${wish.id}")
                                    },
                                    onViewAllClick = {
                                        navController.navigate(Screen.WishListScreen.route)
                                    }
                                )
                            }
                        }
                    }

                    // Quick Action Buttons
                    item {
                        QuickActionButtons(navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun TopHeaderSection(
        onNotificationClick: () -> Unit = {},
        onGamifyClick: () -> Unit = {},
        onSearchClick: () -> Unit = {},
        onProfileClick: () -> Unit = {},
        hasNotifications: Boolean = false
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side icons - Swapped positions: Gamify icon first, then Bell icon
            Row {
                IconButton(
                    onClick = onGamifyClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents, // Trophy/Gamify icon
                        contentDescription = "Achievements",
                        tint = Color(0xFF1A1D29),
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color(0xFF1A1D29),
                            modifier = Modifier.size(24.dp)
                        )
                        // Notification badge (only show when there are notifications)
                        if (hasNotifications) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE91E63))
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            }

            Text(
                text = "WISHLIST",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF667EEA),
                letterSpacing = 1.2.sp
            )

            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF1A1D29),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }


@Composable
fun GreetingSection(
        greeting: String,
        currentTime: String,
        userName: String,
        navController: NavHostController,
        onEditUserName: (String) -> Unit = {}
    ) {
        var visible by remember { mutableStateOf(false) }
        var showEditDialog by remember { mutableStateOf(false) }
        var tempUserName by remember { mutableStateOf(userName) }

        LaunchedEffect(userName) {
            tempUserName = userName
        }

        LaunchedEffect(Unit) {
            visible = true
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                initialOffsetY = { -40 },
                animationSpec = tween(600)
            )
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF667EEA),
                                        Color(0xFF764BA2)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.first().uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showEditDialog = true }
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.welcome_back_user, userName),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1D29)
                        )
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.your_growth_journey),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF667EEA)
                        )
                        Text(
                            text = currentTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Add quick action button
                    FloatingActionButton(
                        onClick = {
                            // Navigate to AddScreen to create a new wish
                            navController.navigate(Screen.AddScreen.route + "/0")
                        },
                        modifier = Modifier.size(40.dp),
                        containerColor = Color(0xFFE91E63),
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Wish",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Username Edit Dialog
        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = {
                    Text(
                        text = "Edit Your Name",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    OutlinedTextField(
                        value = tempUserName,
                        onValueChange = { tempUserName = it },
                        label = { Text("Enter your name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (tempUserName.isNotBlank()) {
                                onEditUserName(tempUserName)
                                showEditDialog = false
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showEditDialog = false
                            tempUserName = userName // Reset to original name
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }


@Composable
fun DashboardIllustration() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF667EEA),
                                    Color(0xFF764BA2)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Make Your Dreams Come True",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF1A1D29)
                )

                Text(
                    text = "Track your wishes and turn them into reality",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF64748B)
                )
            }
        }
    }


@Composable
fun QuickStatsSection(wishList: List<Wish>) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Wishes",
                value = wishList.size.toString(),
                icon = Icons.AutoMirrored.Filled.List,
                color = Color(0xFF667EEA),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Completed",
                value = wishList.count { it.isCompleted }.toString(),
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Pending",
                value = wishList.count { !it.isCompleted }.toString(),
                icon = Icons.Default.Schedule,
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
        }
    }


@Composable
fun StatCard(
        title: String,
        value: String,
        icon: ImageVector,
        color: Color,
        modifier: Modifier = Modifier
    ) {
        var visible by remember { mutableStateOf(false) }
        val animationDelay = remember { (0..300).random() }

        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(animationDelay.toLong())
            visible = true
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(500, delayMillis = animationDelay)) +
                    slideInVertically(
                        initialOffsetY = { 60 },
                        animationSpec = tween(
                            500,
                            delayMillis = animationDelay,
                            easing = FastOutSlowInEasing
                        )
                    )
        ) {
            Card(
                modifier = modifier,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1D29)
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
    }


@Composable
fun CategoryShortcuts(navController: NavHostController) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Quick Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1D29),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val categories = listOf(
                        CategoryItem("Electronics", Icons.Default.Computer, Color(0xFF667EEA)),
                        CategoryItem("Travel", Icons.Default.FlightTakeoff, Color(0xFF10B981)),
                        CategoryItem("Books", Icons.Default.Book, Color(0xFFF59E0B)),
                        CategoryItem("Gaming", Icons.Default.Games, Color(0xFFEF4444))
                    )

                    items(categories) { category ->
                        CategoryShortcutCard(
                            category = category,
                            onClick = {
                                navController.navigate(Screen.AddScreen.route + "/0")
                            }
                        )
                    }
                }
            }
        }
    }


@Composable
fun CategoryShortcutCard(
        category: CategoryItem,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .width(80.dp)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = category.color.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = category.color,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = category.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = category.color,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }


@Composable
fun RecentWishesSection(
        wishes: List<Wish>,
        onWishClick: (Wish) -> Unit,
        onViewAllClick: () -> Unit = {}
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Wishes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1D29)
                    )

                    if (wishes.isNotEmpty()) {
                        TextButton(
                            onClick = onViewAllClick
                        ) {
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF667EEA)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (wishes.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No wishes yet",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Add your first wish to get started!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    wishes.forEach { wish ->
                        RecentWishItem(
                            wish = wish,
                            onClick = { onWishClick(wish) }
                        )
                        if (wish != wishes.last()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }


@Composable
fun RecentWishItem(
        wish: Wish,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF667EEA).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFF667EEA),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wish.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1D29)
                )
                Text(
                    text = wish.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
            }

            if (wish.isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }


@Composable
fun TodayToDoSection(wishes: List<Wish>) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Today's To-Do",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1D29),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val pendingWishes = wishes.filter { !it.isCompleted }.take(5)

                if (pendingWishes.isEmpty()) {
                    Text(
                        text = "All caught up! 🎉",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    pendingWishes.forEach { wish ->
                        ToDoItem(wish = wish)
                        if (wish != pendingWishes.last()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }


@Composable
fun ToDoItem(wish: Wish) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = wish.isCompleted,
                onCheckedChange = { /* Handle completion toggle */ },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF10B981),
                    uncheckedColor = Color(0xFF94A3B8)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wish.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1A1D29)
                )
                if (wish.description.isNotEmpty()) {
                    Text(
                        text = wish.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

@Composable
fun ProgressOverviewCard(
    completionRate: Float,
    totalWishes: Int,
    completedWishes: Int,
    savingsProgress: Float,
    totalSaved: Double,
    totalSavingsTarget: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Your Progress 📊",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1D29)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Wishes Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Wishes Completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "$completedWishes of $totalWishes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1D29)
                    )
                }
                Text(
                    text = "${(completionRate * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = completionRate,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF10B981),
                trackColor = Color(0xFF10B981).copy(alpha = 0.2f)
            )
            
            if (totalSavingsTarget > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Savings Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Savings Goal",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "$${totalSaved.toInt()} of $${totalSavingsTarget.toInt()}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1D29)
                        )
                    }
                    Text(
                        text = "${(savingsProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667EEA)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = savingsProgress,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF667EEA),
                    trackColor = Color(0xFF667EEA).copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun MotivationalCard(
    completionRate: Float,
    totalWishes: Int
) {
    val motivationalMessage = when {
        totalWishes == 0 -> "🌟 Start your wishlist journey!"
        completionRate >= 0.8f -> "🎉 You're crushing your goals!"
        completionRate >= 0.5f -> "💪 You're doing great, keep going!"
        completionRate >= 0.2f -> "🚀 You're making progress!"
        else -> "✨ Every journey starts with a single step!"
    }
    
    val gradientColors = when {
        completionRate >= 0.8f -> listOf(Color(0xFF10B981), Color(0xFF059669))
        completionRate >= 0.5f -> listOf(Color(0xFF667EEA), Color(0xFF764BA2))
        else -> listOf(Color(0xFFE91E63), Color(0xFFAD1457))
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(gradientColors),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = motivationalMessage,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun QuickActionButtons(navController: NavHostController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Actions ⚡",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1D29),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Add Wish Button
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { navController.navigate(Screen.AddScreen.route + "/0") },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE91E63).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Wish",
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add Wish",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFE91E63),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                // View All Button
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { navController.navigate(Screen.WishListScreen.route) },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF667EEA).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "View All",
                            tint = Color(0xFF667EEA),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF667EEA),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                // Search Button
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { navController.navigate(Screen.SearchScreen.route) },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Search",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
