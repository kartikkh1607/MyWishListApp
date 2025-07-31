package com.example.mywishlistapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.navigation.NavHostController
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import kotlinx.coroutines.delay

import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishListScreen(navController: NavHostController, viewModel: WishViewModel) {
    val wishList = viewModel.getAllWishes.collectAsState(initial = emptyList())
    val unreadNotificationCount by viewModel.getUnreadNotificationCount().collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }
    var showSearchResults by remember { mutableStateOf(false) }
    
    // Filter wishes based on search query and selected tab
    val filteredWishes = remember(searchQuery, selectedTab, wishList.value) {
        val baseWishes = when (selectedTab) {
            0 -> wishList.value // All wishes
            1 -> wishList.value.filter { !it.isCompleted } // Only incomplete wishes (To-Dos)
            else -> wishList.value
        }
        
        if (searchQuery.isBlank()) {
            baseWishes
        } else {
            baseWishes.filter { wish ->
                wish.title.contains(searchQuery, ignoreCase = true) ||
                wish.description.contains(searchQuery, ignoreCase = true) ||
                wish.category.contains(searchQuery, ignoreCase = true) ||
                wish.tags.any { it.contains(searchQuery, ignoreCase = true) }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Top Header matching mockup
        WishListTopHeader(
            onNotificationClick = {
                navController.navigate(Screen.NotificationsScreen.route)
            },
            onSearchClick = {
                isSearchFocused = true
            },
            hasNotifications = unreadNotificationCount > 0
        )
        
        // Tab Row (Wishes / To-Dos)
        WishListTabRow(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
        
        // Search Bar with Clear Button
        EnhancedSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onClear = { searchQuery = "" },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        
        // Search Results Indicator
        if (searchQuery.isNotEmpty()) {
            SearchResultsIndicator(
                resultsCount = filteredWishes.size,
                searchQuery = searchQuery,
                onClear = { searchQuery = "" },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
        
        // Content based on selected tab
        WishesContent(
            wishes = filteredWishes,
            onWishClick = {
                navController.navigate(Screen.AddScreen.route + "/${it.id}")
            },
            viewModel = viewModel,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun WishListTopHeader(
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
fun WishListTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TabButton(
            text = "WISHES",
            isSelected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            text = "To-Dos",
            isSelected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF667EEA) else Color.Transparent,
            contentColor = if (isSelected) Color.White else TextSecondary
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        )
    ) {
        Text(
            text = text,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = Color(0xFF1A1D29),
                    fontSize = 14.sp
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = "Search the wishes title",
                                color = TextTertiary,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
            
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EnhancedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF1A1D29), fontSize = 14.sp),
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = "Search the wishes title",
                                color = TextTertiary,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
            AnimatedVisibility(query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultsIndicator(resultsCount: Int, searchQuery: String, onClear: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (resultsCount == 1) "1 result for \"$searchQuery\"" else "$resultsCount results for \"$searchQuery\"",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B),
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onClear) {
            Text("Clear")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishesContent(
    wishes: List<Wish>,
    onWishClick: (Wish) -> Unit,
    viewModel: WishViewModel,
    modifier: Modifier = Modifier
) {
    // Pull-to-refresh state
    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Handle refresh action
    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            isRefreshing = true
            // Simulate network call or data refresh
            delay(1500) // 1.5 seconds refresh delay
            isRefreshing = false
            pullToRefreshState.endRefresh()
            
            // Show refresh notification
            viewModel.addNotification(
                title = "Refreshed!",
                message = "Your wish list has been updated",
                type = com.example.mywishlistapp.models.NotificationType.SYSTEM
            )
        }
    }
    
    Box(
        modifier = modifier.nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        if (wishes.isEmpty()) {
            // Pull-to-refresh for empty state
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    EmptyWishesState(modifier = Modifier.fillParentMaxSize())
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(wishes, key = { it.id }) { wish ->
                    SwipeToDeleteWishCard(
                        wish = wish,
                        onWishClick = { onWishClick(wish) },
                        onDelete = { viewModel.deleteWishWithGamification(wish) },
                        viewModel = viewModel
                    )
                }
            }
        }
        
        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedWishCard(
    wish: Wish,
    onClick: () -> Unit,
    viewModel: WishViewModel
) {
    val haptic = LocalHapticFeedback.current

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300))
    ) {
        ModernWishCard(
            wish = wish, 
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun ModernWishCard(
    wish: Wish,
    onClick: () -> Unit,
    viewModel: WishViewModel
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority Indicator (colored circle like mockup)
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        when (wish.priority.name) {
                            "HIGH" -> AccentRed
                            "MEDIUM" -> AccentOrange
                            else -> AccentGreen
                        }
                    )
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = wish.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (wish.description.isNotEmpty()) {
                    Text(
                        text = wish.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                // Date
                Text(
                    text = "10 Apr 2024", // You can format wish.createdDate here
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            
            // Action Icon
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (wish.priority.name == "HIGH") AccentOrange else TextTertiary,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Checkbox (like mockup)
            Checkbox(
                checked = wish.isCompleted,
                onCheckedChange = { checked ->
                    if (checked && !wish.isCompleted) {
                        // Complete the wish with gamification
                        viewModel.completeWish(wish)
                    }
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = AccentGreen,
                    uncheckedColor = TextTertiary
                )
            )
        }
    }
}

@Composable
fun TodosContent(
    wishes: List<Wish>,
    modifier: Modifier = Modifier
) {
    if (wishes.isEmpty()) {
        EmptyTodosState(modifier = modifier)
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(wishes, key = { it.id }) { wish ->
                TodoCard(wish = wish)
            }
        }
    }
}

@Composable
fun TodoCard(
    wish: Wish
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceWhite,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = false,
                onCheckedChange = { checked -> /* Handle completion */ },
                colors = CheckboxDefaults.colors(
                    checkedColor = AccentGreen,
                    uncheckedColor = TextTertiary
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = wish.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                
                if (wish.description.isNotEmpty()) {
                    Text(
                        text = wish.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyWishesState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No wishes yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Create your first wish to get started!",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun EmptyTodosState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "All caught up! 🎉",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "You have no pending tasks",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteWishCard(
    wish: Wish,
    onWishClick: () -> Unit,
    onDelete: () -> Unit,
    viewModel: WishViewModel
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = color,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        AnimatedWishCard(
            wish = wish,
            onClick = onWishClick,
            viewModel = viewModel
        )
    }
}
