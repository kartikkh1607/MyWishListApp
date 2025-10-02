package com.example.mywishlistapp.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.Screen
import com.example.mywishlistapp.WishViewModel
import com.example.mywishlistapp.ui.components.HeroStat
import com.example.mywishlistapp.ui.components.KeyMetricCard
import com.example.mywishlistapp.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

// Data class for category items
data class CategoryItem(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavHostController, viewModel: WishViewModel) {
    val currentTime = Calendar.getInstance().time
    val dateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
    val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    
    // Get real data from ViewModel
    val wishList by viewModel.getAllWishes.collectAsState(initial = emptyList())
    val actualWishes = wishList

    // Enhanced greeting with emojis
    val greeting = when (hourOfDay) {
        in 5..11 -> "Good Morning ☀️"
        in 12..16 -> "Good Afternoon 🌤️"
        in 17..20 -> "Good Evening 🌅"
        else -> "Good Night 🌙"
    }

    // Calculate progress metrics from real data
    val currentTimeMs = System.currentTimeMillis()
    val upcomingItems = actualWishes.filter { wish ->
        !wish.isCompleted && wish.targetDate != null && wish.targetDate!! > currentTimeMs 
    }.sortedBy { wish -> wish.targetDate }
    val inProgressItems = actualWishes.filter { wish ->
        !wish.isCompleted && wish.isGoal && wish.progress > 0 
    }
    val totalWishes = actualWishes.size
    val completedWishes = actualWishes.count { wish -> wish.isCompleted }
    val completionRate = if (totalWishes > 0) (completedWishes.toFloat() / totalWishes.toFloat()) else 0f
    val totalSaved = 0.0 // This would come from your savings calculation
    val totalSavingsTarget = 0.0 // This would come from your savings goals
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
                        BackgroundLight,
                        BackgroundSecondary,
                        Color(0xFFF8FAFF)
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
            hasNotifications = false // Remove notification indicator
        )

        // Show actual content (now using placeholder data)
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
                // At a Glance Layout - Hero Stat Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Calculate user level based on completed wishes (level = completedWishes / 5 + 1)
                        val userLevel = (completedWishes / 5) + 1
                        val progressToNextLevel = (completedWishes % 5) / 5f
                        
                        HeroStat(
                            level = userLevel,
                            progress = progressToNextLevel,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        
                        // Key Metrics Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            KeyMetricCard(
                                icon = Icons.Filled.Add,
                                label = "Wishes Added",
                                value = totalWishes.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            
                            KeyMetricCard(
                                icon = Icons.Filled.CheckCircle,
                                label = "Wishes Fulfilled",
                                value = completedWishes.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            
                            KeyMetricCard(
                                icon = Icons.Filled.DateRange,
                                label = "Upcoming Milestones",
                                value = upcomingItems.size.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Motivational Message
                item {
                    MotivationalCard(
                        completionRate = completionRate,
                        totalWishes = totalWishes
                    )
                }

                // Quick Stats
                item {
                    QuickStatsSection(wishList = actualWishes)
                }

                // Category Shortcuts
                item {
                    CategoryShortcuts(navController = navController)
                }

                // Upcoming Items Section
                if (upcomingItems.isNotEmpty()) {
                    item {
                        UpcomingItemsSection(
                            upcomingItems = upcomingItems.take(5),
                            onItemClick = { item ->
                                navController.navigate(Screen.AddScreen.route + "/${item.id}")
                            }
                        )
                    }
                }
                
                // In Progress Section
                if (inProgressItems.isNotEmpty()) {
                    item {
                        InProgressItemsSection(
                            inProgressItems = inProgressItems.take(5),
                            onItemClick = { item ->
                                navController.navigate(Screen.AddScreen.route + "/${item.id}")
                            }
                        )
                    }
                }
                
                // Recent Wishes
                if (actualWishes.isNotEmpty()) {
                    item {
                        RecentWishesSection(
                            wishes = actualWishes.take(3), // Show 3 recent wishes
                            onWishClick = { wish ->
                                navController.navigate(Screen.AddScreen.route + "/${wish.id}")
                            },
                            onViewAllClick = {
                                navController.navigate(Screen.WishListScreen.route)
                            }
                        )
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
                        tint = Color.Black,
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
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        // Notification badge (only show when there are notifications)
                        if (hasNotifications) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error)
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
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.2.sp
            )

            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Black,
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
                            text = if (userName.isNotEmpty()) userName.first().uppercase() else "U",
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
                            text = "Welcome back, $userName!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1D29)
                        )
                        Text(
                            text = "Your growth journey continues...",
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
            delay(animationDelay.toLong())
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
                        overflow = TextOverflow.Ellipsis
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

@Composable
fun UpcomingItemsSection(
    upcomingItems: List<Wish>,
    onItemClick: (Wish) -> Unit
) {
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
                text = "⏰ Upcoming",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1D29),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(upcomingItems, key = { it.id }) { item ->
                    UpcomingItemCard(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun UpcomingItemCard(
    item: Wish,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF7ED)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1D29),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Due date
            item.targetDate?.let { targetDate ->
                val daysUntil = max(0, TimeUnit.MILLISECONDS.toDays(targetDate - System.currentTimeMillis()))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            daysUntil == 0L -> "Today"
                            daysUntil == 1L -> "Tomorrow"
                            daysUntil < 7 -> "${daysUntil}d"
                            else -> "${daysUntil / 7}w"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun InProgressItemsSection(
    inProgressItems: List<Wish>,
    onItemClick: (Wish) -> Unit
) {
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
                text = "🚧 In Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1D29),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(inProgressItems, key = { it.id }) { item ->
                    InProgressItemCard(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun InProgressItemCard(
    item: Wish,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFECFDF5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1D29),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.progress}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Medium
                )
            }
            
            LinearProgressIndicator(
                progress = item.progress / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF10B981),
                trackColor = Color(0xFF10B981).copy(alpha = 0.2f)
            )
        }
    }
}