package com.example.mywishlistapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class FeedbackType {
    SUCCESS, ERROR, WARNING, INFO
}

data class FeedbackMessage(
    val message: String,
    val type: FeedbackType,
    val duration: Long = 3000L,
    val action: (() -> Unit)? = null,
    val actionLabel: String? = null
)

@Composable
fun EnhancedSnackbar(
    message: FeedbackMessage,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, contentColor, icon) = when (message.type) {
        FeedbackType.SUCCESS -> Triple(
            Color(0xFF10B981),
            Color.White,
            Icons.Default.CheckCircle
        )
        FeedbackType.ERROR -> Triple(
            Color(0xFFEF4444),
            Color.White,
            Icons.Default.Error
        )
        FeedbackType.WARNING -> Triple(
            Color(0xFFF59E0B),
            Color.White,
            Icons.Default.Warning
        )
        FeedbackType.INFO -> Triple(
            Color(0xFF3B82F6),
            Color.White,
            Icons.Default.Info
        )
    }

    LaunchedEffect(message) {
        delay(message.duration)
        onDismiss()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = message.message,
                color = contentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            if (message.action != null && message.actionLabel != null) {
                TextButton(
                    onClick = {
                        message.action.invoke()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = contentColor
                    )
                ) {
                    Text(
                        text = message.actionLabel,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun FeedbackOverlay(
    messages: List<FeedbackMessage>,
    onDismissMessage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        messages.forEachIndexed { index, message ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { -it }
                ) + fadeOut()
            ) {
                EnhancedSnackbar(
                    message = message,
                    onDismiss = { onDismissMessage(index) }
                )
            }
        }
    }
}


// Confirmation dialog with enhanced styling
@Composable
fun ConfirmationDialog(
    isVisible: Boolean,
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    type: FeedbackType = FeedbackType.WARNING
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val icon = when (type) {
                        FeedbackType.SUCCESS -> Icons.Default.CheckCircle
                        FeedbackType.ERROR -> Icons.Default.Error
                        FeedbackType.WARNING -> Icons.Default.Warning
                        FeedbackType.INFO -> Icons.Default.Info
                    }
                    
                    val iconColor = when (type) {
                        FeedbackType.SUCCESS -> Color(0xFF10B981)
                        FeedbackType.ERROR -> Color(0xFFEF4444)
                        FeedbackType.WARNING -> Color(0xFFF59E0B)
                        FeedbackType.INFO -> Color(0xFF3B82F6)
                    }
                    
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (type) {
                            FeedbackType.SUCCESS -> Color(0xFF10B981)
                            FeedbackType.ERROR -> Color(0xFFEF4444)
                            FeedbackType.WARNING -> Color(0xFFF59E0B)
                            FeedbackType.INFO -> Color(0xFF3B82F6)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = confirmText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = dismissText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// Tooltip component
@Composable
fun Tooltip(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF2C3E50),
    textColor: Color = Color.White
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
