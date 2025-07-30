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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(navController: NavHostController, viewModel: WishViewModel) {

    val context = LocalContext.current
    val wishList = viewModel.getAllWishes.collectAsState(initial = emptyList())
    val unreadNotificationCount by viewModel.getUnreadNotificationCount().collectAsState()
    
    // Loading and UI states
    var isLoading by remember { mutableStateOf(true) }
    var showVoiceDialog by remember { mutableStateOf(false) }
    
    // Simulate loading for demonstration
    LaunchedEffect(wishList.value) {
        isLoading = true
        delay(800) // Simulated loading time
        isLoading = false
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            AppBarView(
                title = "WishList",
                onSearchClicked = {
                    navController.navigate(Screen.SearchScreen.route)
                },
                showActions = true
            )
        },
        floatingActionButton = {
            EnhancedFAB(
                onClick = {
                    navController.navigate(Screen.AddScreen.route + "/0")
                },
                icon = Icons.Default.Add
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF0F4FF), // Soft blue-white
                            Color(0xFFE8F0FE), // Light blue gradient
                            Color(0xFFF8FAFF)  // Very light blue-white
                        )
                    )
                )
        ) {
            if (wishList.value.isEmpty()) {
                EmptyWishListState {
                    navController.navigate(Screen.AddScreen.route + "/0")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 88.dp // Space for FAB
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(wishList.value, key = { it.id }) { wish ->
val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteWish(wish)
                                    true
                                } else false
                            },
                            positionalThreshold = { it * 0.3f }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                SwipeBackground(dismissState)
                            },
                        ) { EnhancedWishItem(wish = wish) {
                            navController.navigate(Screen.AddScreen.route + "/${wish.id}")
                        }
                        }
                    }
                }
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
            androidx.compose.material3.Icon(
                imageVector = Icons.AutoMirrored.Outlined.List,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = colorResource(R.color.app_bar_color).copy(alpha = 0.6f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        androidx.compose.material3.Text(
            text = "Your Wish List is Empty",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        androidx.compose.material3.Text(
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
        androidx.compose.material3.Icon(
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
                    androidx.compose.material3.Icon(
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
                        androidx.compose.material3.Text(
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
                            androidx.compose.material3.Text(
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
                        androidx.compose.material3.Text(
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
                    
                    androidx.compose.material3.Text(
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
                                androidx.compose.material3.Text(
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
                                androidx.compose.material3.Text(
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
                    androidx.compose.material3.Icon(
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
        androidx.compose.material3.Icon(
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
