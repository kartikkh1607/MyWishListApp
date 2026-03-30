package com.example.mywishlistapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.ui.BounceSpring
import com.example.mywishlistapp.ui.WishViewModel
import com.example.mywishlistapp.ui.filterWishes
import com.example.mywishlistapp.ui.priorityColor
import com.example.mywishlistapp.ui.priorityEmoji
import com.example.mywishlistapp.ui.theme.AccentGreen
import com.example.mywishlistapp.ui.theme.AccentRed
import com.example.mywishlistapp.ui.theme.BackgroundLight
import com.example.mywishlistapp.ui.theme.SurfaceWhite
import com.example.mywishlistapp.ui.theme.TextPrimary
import com.example.mywishlistapp.ui.theme.TextSecondary
import com.example.mywishlistapp.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishListScreen(navController: NavHostController, viewModel: WishViewModel) {
    val wishList by viewModel.getAllWishes.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredWishes = remember(searchQuery, wishList) {
        filterWishes(wishes = wishList, query = searchQuery)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundLight,
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "WISHLIST",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    // Fix #7 — use theme token instead of hardcoded Color(0xFF667EEA)
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp
                )
            }

            // Search bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        Icons.Default.Search, null,
                        tint = TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        ),
                        decorationBox = { inner ->
                            Box {
                                if (searchQuery.isEmpty()) {
                                    Text("Search wishes...", color = TextTertiary, fontSize = 14.sp)
                                }
                                inner()
                            }
                        }
                    )
                    AnimatedVisibility(searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Clear, "Clear", tint = TextTertiary)
                        }
                    }
                }
            }

            // Results count
            if (searchQuery.isNotEmpty()) {
                Text(
                    text = "${filteredWishes.size} result${if (filteredWishes.size != 1) "s" else ""} for \"$searchQuery\"",
                    style = MaterialTheme.typography.bodySmall,
                    // Fix #7 — use theme token
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            // Wish list or empty state
            if (filteredWishes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(if (searchQuery.isNotEmpty()) "🔍" else "✨", fontSize = 48.sp)
                        Text(
                            if (searchQuery.isNotEmpty()) "No results for \"$searchQuery\"" else "No wishes yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (searchQuery.isEmpty()) {
                            Button(
                                onClick = { navController.navigate(Screen.AddScreen(id = 0L)) },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Add Wish")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(filteredWishes, key = { _, wish -> wish.id }) { _, wish ->
                        SwipeToDeleteWishCard(
                            modifier = Modifier.animateItem(),
                            wish = wish,
                            onWishClick = {
                                navController.navigate(Screen.AddScreen(id = wish.id)) {
                                    launchSingleTop = true
                                }
                            },
                            onDelete = {
                                viewModel.deleteWish(wish)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Wish deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.addWish(wish)
                                    }
                                }
                            },
                            viewModel = viewModel
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteWishCard(
    modifier: Modifier = Modifier,
    wish: Wish,
    onWishClick: () -> Unit,
    onDelete: () -> Unit,
    viewModel: WishViewModel
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    val currentWish by rememberUpdatedState(wish)
    val currentOnDelete by rememberUpdatedState(onDelete)

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    currentOnDelete()
                    true
                }

                SwipeToDismissBoxValue.StartToEnd -> {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    scope.launch {
                        delay(200)
                        viewModel.updateWish(currentWish.copy(isCompleted = !currentWish.isCompleted))
                    }
                    false // Fix: Return false so the card springs back into view instead of staying swiped off
                }

                else -> false
            }
        }
    )

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        backgroundContent = {
            // FIX: Only show colored backgrounds while the swipe is in progress
            // (currentValue == Settled). Once the dismiss threshold is crossed and
            // confirmValueChange returns true, currentValue flips to EndToStart /
            // StartToEnd. At that point we render transparent so the card doesn't
            // turn solid red/green while Room's Flow is still propagating
            val isSettled = dismissState.currentValue == SwipeToDismissBoxValue.Settled
            when {
                isSettled && dismissState.targetValue == SwipeToDismissBoxValue.EndToStart ->
                    SwipeBackground(
                        color = AccentRed.copy(alpha = 0.9f),
                        icon = Icons.Default.Delete,
                        alignment = Alignment.CenterEnd,
                        label = "Delete"
                    )

                isSettled && dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd ->
                    SwipeBackground(
                        color = if (wish.isCompleted) MaterialTheme.colorScheme.surfaceVariant else AccentGreen.copy(
                            alpha = 0.9f
                        ),
                        icon = if (wish.isCompleted) Icons.Default.Undo else Icons.Default.CheckCircle,
                        alignment = Alignment.CenterStart,
                        label = if (wish.isCompleted) "Mark Undone" else "Complete"
                    )

                else -> Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Transparent, RoundedCornerShape(16.dp))
                )
            }
        }
    ) {
        WishCard(wish = wish, onClick = onWishClick, viewModel = viewModel)
    }
}

@Composable
private fun SwipeBackground(
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    alignment: Alignment,
    label: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color, RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, label, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                label, style = MaterialTheme.typography.labelSmall,
                color = Color.White, fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WishCard(wish: Wish, onClick: () -> Unit, viewModel: WishViewModel) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = BounceSpring,
        label = "card_scale"
    )

    val priorityColor = priorityColor(wish.priority)
    val priorityEmoji = priorityEmoji(wish.priority)
    val priorityLabel = wish.priority.name.lowercase().replaceFirstChar { it.uppercase() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    isPressed = true; tryAwaitRelease(); isPressed = false
                })
            }
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.12f)),
        shadowElevation = if (isPressed) 6.dp else 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (wish.isCompleted) {
                // ── Compact View for Completed Wishes ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(priorityColor)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = wish.title,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(12.dp))
                    Icon(
                        Icons.Default.CheckCircle,
                        null,
                        tint = AccentGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Completed",
                        color = AccentGreen,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // ── Expanded View for Pending Wishes ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // ── Title row + checkbox ─────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(priorityColor)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = wish.title,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // ── Description ──────────────────────────────────────────────────
                    if (wish.description.isNotEmpty()) {
                        Text(
                            text = wish.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 6.dp, start = 20.dp)
                        )
                    }

                    // ── Chips: priority · category · price ───────────────────────────
                    Row(
                        modifier = Modifier.padding(top = 10.dp, start = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = priorityColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "$priorityEmoji $priorityLabel",
                                style = MaterialTheme.typography.labelSmall,
                                color = priorityColor,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        if (wish.category.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = wish.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        if (wish.price.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = AccentGreen.copy(alpha = 0.10f)
                            ) {
                                Text(
                                    text = "₹${wish.price}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // ── Tags ─────────────────────────────────────────────────────────
                    if (wish.tags.isNotEmpty()) {
                        Row(
                            modifier = Modifier.padding(top = 6.dp, start = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            wish.tags.take(3).forEach { tag ->
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
