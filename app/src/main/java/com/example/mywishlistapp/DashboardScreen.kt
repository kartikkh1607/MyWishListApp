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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mywishlistapp.Data.Wish
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
    val wishList = viewModel.getAllWishes.collectAsState(initial = emptyList())
    val unreadNotificationCount by viewModel.getUnreadNotificationCount().collectAsState()
    val currentTime = Calendar.getInstance().time
    val dateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
    val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    
    val greeting = when (hourOfDay) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..20 -> "Good Evening"
        else -> "Good Night"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Top Header with functional icons
        TopHeaderSection(
            onNotificationClick = {
                // Navigate to Notifications screen
                navController.navigate(Screen.NotificationsScreen.route)
            },
            onSearchClick = {
                navController.navigate(Screen.SearchScreen.route)
            },
            hasNotifications = unreadNotificationCount > 0
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Enhanced User Greeting Section
            item {
                GreetingSection(
                    greeting = greeting,
                    currentTime = dateFormat.format(currentTime),
                    userName = "Kartik",
                    navController = navController
                )
            }

            // Quick Stats
            item {
                QuickStatsSection(wishList = wishList.value)
            }

            // Category Shortcuts
            item {
                CategoryShortcuts(navController = navController)
            }

            // Recent Wishes (only if there are wishes)
            if (wishList.value.isNotEmpty()) {
                item {
                    RecentWishesSection(
                        wishes = wishList.value.take(2), // Reduced to 2 for less clutter
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
    }
}

@Composable
fun TopHeaderSection(
    onNotificationClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    hasNotifications: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
    navController: NavHostController
){
    var visible by remember { mutableStateOf(false) }
    
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$greeting, $userName! 👋",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1D29)
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
