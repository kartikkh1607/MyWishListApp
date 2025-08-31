package com.example.mywishlistapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.Data.Wish

@Composable
fun WishItemECommerce(wish: Wish, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "wish_item_scale"
    )
    
    val priorityColor = when (wish.priority) {
        Priority.HIGH -> Color(0xFFEF4444)
        Priority.MEDIUM -> Color(0xFFF59E0B)
        Priority.LOW -> Color(0xFF10B981)
    }
    
    val categoryIcon = when (wish.category.lowercase()) {
        "electronics" -> Icons.Default.Computer
        "travel" -> Icons.Default.FlightTakeoff
        "books" -> Icons.Default.Book
        "gaming" -> Icons.Default.Games
        "food" -> Icons.Default.Restaurant
        "fashion" -> Icons.Default.ShoppingBag
        "home" -> Icons.Default.Home
        "sports" -> Icons.Default.FitnessCenter
        else -> Icons.Default.Category
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 8.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false
            )
            .clickable(
                onClick = {
                    isPressed = true
                    onClick()
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box {
            Column {
                // Image section with overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = wish.imageUrl.ifEmpty { "https://via.placeholder.com/300x200/667EEA/FFFFFF?text=Wish" },
                        contentDescription = wish.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    )
                    
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.3f)
                                    ),
                                    startY = 100f
                                )
                            )
                    )
                    
                    // Priority indicator
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(priorityColor)
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (wish.priority) {
                                Priority.HIGH -> Icons.Default.PriorityHigh
                                Priority.MEDIUM -> Icons.Default.Remove
                                Priority.LOW -> Icons.Default.KeyboardArrowDown
                            },
                            contentDescription = "${wish.priority} priority",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    // Completion status
                    if (wish.isCompleted) {
                        Box(
                            modifier = Modifier
                                .padding(12.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                                .align(Alignment.TopStart),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                // Content section
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Title and category row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = wish.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1D29),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            if (wish.description.isNotEmpty()) {
                                Text(
                                    text = wish.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF64748B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        
                        // Category icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF667EEA).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = wish.category,
                                tint = Color(0xFF667EEA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Price and savings row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            if (wish.price.isNotEmpty()) {
                                Text(
                                    text = "$$${wish.price}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                            
                            if (wish.savedAmount > 0) {
                                val progress = if (wish.price.isNotEmpty()) {
                                    (wish.savedAmount / (wish.price.toDoubleOrNull() ?: 1.0)).coerceAtMost(1.0)
                                } else 0.0
                                
                                Text(
                                    text = "Saved: $${wish.savedAmount.toInt()} (${(progress * 100).toInt()}%)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                        
                        // Rating if available
                        if (wish.rating > 0f) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1f", wish.rating),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1A1D29)
                                )
                            }
                        }
                    }
                    
                    // Tags if available
                    if (wish.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            wish.tags.take(3).forEach { tag ->
                                Surface(
                                    color = Color(0xFF667EEA).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF667EEA),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}
