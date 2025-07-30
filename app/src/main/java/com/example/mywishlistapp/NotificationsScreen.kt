package com.example.mywishlistapp

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.mywishlistapp.models.NotificationItem
import com.example.mywishlistapp.models.NotificationType
import com.example.mywishlistapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavHostController, viewModel: WishViewModel) {
// Collect notifications from the ViewModel's StateFlow
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

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
        // Enhanced App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Notifications",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier.background(
                Brush.horizontalGradient(
                    colors = listOf(
                        PrimaryPurple.copy(alpha = 0.95f),
                        SecondaryPurple.copy(alpha = 0.95f)
                    )
                )
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Today Section
            val todayNotifications = notifications.filter { 
                isSameDay(it.timestamp, Date())
            }
            
            if (todayNotifications.isNotEmpty()) {
                item {
                    SectionHeader("Today")
                }
                
                items(todayNotifications, key = { it.id }) { notification ->
                    SwipeableNotificationCard(
                        notification = notification,
                        onDismiss = { viewModel.removeNotification(notification.id) },
                        onClick = { /* Handle notification click */ }
                    )
                }
            }

            // Earlier Section
            val earlierNotifications = notifications.filter { 
                !isSameDay(it.timestamp, Date())
            }
            
            if (earlierNotifications.isNotEmpty()) {
                item {
                    SectionHeader("Earlier")
                }
                
                items(earlierNotifications, key = { it.id }) { notification ->
                    SwipeableNotificationCard(
                        notification = notification,
                        onDismiss = { viewModel.removeNotification(notification.id) },
                        onClick = { /* Handle notification click */ }
                    )
                }
            }

            // Empty state
            if (notifications.isEmpty()) {
                item {
                    EmptyNotificationsState()
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
    )
}

@Composable
fun EnhancedNotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "notification_scale"
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
            containerColor = if (notification.isRead) 
                Color.White.copy(alpha = 0.7f) 
            else 
                Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 4.dp else 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Notification Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = when (notification.type) {
                                NotificationType.REMINDER -> listOf(
                                    AccentOrange.copy(alpha = 0.2f),
                                    AccentOrange.copy(alpha = 0.1f)
                                )
                                NotificationType.ACHIEVEMENT -> listOf(
                                    AccentGreen.copy(alpha = 0.2f),
                                    AccentGreen.copy(alpha = 0.1f)
                                )
                                NotificationType.WISH_UPDATE -> listOf(
                                    AccentBlue.copy(alpha = 0.2f),
                                    AccentBlue.copy(alpha = 0.1f)
                                )
                                NotificationType.GENERAL -> listOf(
                                    PrimaryPurple.copy(alpha = 0.2f),
                                    PrimaryPurple.copy(alpha = 0.1f)
                                )
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.type) {
                        NotificationType.REMINDER -> Icons.Default.Schedule
                        NotificationType.ACHIEVEMENT -> Icons.Default.EmojiEvents
                        NotificationType.WISH_UPDATE -> Icons.Default.Update
                        NotificationType.GENERAL -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = when (notification.type) {
                        NotificationType.REMINDER -> AccentOrange
                        NotificationType.ACHIEVEMENT -> AccentGreen
                        NotificationType.WISH_UPDATE -> AccentBlue
                        NotificationType.GENERAL -> PrimaryPurple
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Unread indicator
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentBlue)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatNotificationTime(notification.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }
    }
}

@Composable
fun EmptyNotificationsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(60.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrimaryPurple.copy(alpha = 0.1f),
                            PrimaryPurple.copy(alpha = 0.05f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = PrimaryPurple.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "All Caught Up!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You don't have any notifications right now.\nWe'll let you know when something happens!",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

private fun formatNotificationTime(timestamp: Date): String {
    val now = Date()
    val diff = now.time - timestamp.time
    
    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(timestamp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableNotificationCard(
    notification: NotificationItem,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDismiss()
                true
            } else {
                false
            }
        },
        positionalThreshold = { it * 0.3f }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            SwipeToDeleteBackground(dismissState)
        }
    ) {
        EnhancedNotificationCard(
            notification = notification,
            onClick = onClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteBackground(dismissState: androidx.compose.material3.SwipeToDismissBoxState) {
    val color by animateColorAsState(
        when (dismissState.dismissDirection) {
            SwipeToDismissBoxValue.EndToStart -> AccentRed
            else -> Color.Transparent
        },
        label = "swipe_color"
    )
    
    val scale by animateFloatAsState(
        when (dismissState.dismissDirection) {
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
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete Notification",
            tint = Color.White,
            modifier = Modifier.scale(scale)
        )
    }
}

private fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
