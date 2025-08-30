package com.example.mywishlistapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay


@Composable
fun EnhancedEmptyWishListState(
    onAddWish: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        ) + slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated illustration
            AnimatedWishIllustration()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Main heading
            Text(
                text = "Your Wish List Awaits! ✨",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1D29),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Subheading
            Text(
                text = "Transform your dreams into reality.\nStart by adding your first wish!",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Action buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Primary action button
                EnhancedActionButton(
                    text = "Add Your First Wish",
                    icon = Icons.Default.Add,
                    onClick = onAddWish,
                    isPrimary = true
                )
                
                // Quick action suggestions
                QuickActionSuggestions(onAddWish = onAddWish)
            }
        }
    }
}

@Composable
fun AnimatedWishIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "wish_anim")
    
    val floatAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    
    val rotationAnimation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    
    Box(
        modifier = Modifier
            .size(200.dp)
            .offset(y = floatAnimation.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background glow effect
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF667EEA).copy(alpha = 0.1f),
                            Color(0xFF667EEA).copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Main illustration container
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
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Color.White
            )
        }
        
        // Floating elements
        FloatingElements()
    }
}

@Composable
fun FloatingElements() {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    
    // Star 1
    Box(
        modifier = Modifier
            .offset(x = (-60).dp, y = (-80).dp)
            .size(20.dp)
    ) {
        val rotation1 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing)
            ),
            label = "star1_rotation"
        )
        
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer { rotationZ = rotation1 }
        )
    }
    
    // Star 2
    Box(
        modifier = Modifier
            .offset(x = 70.dp, y = (-60).dp)
            .size(16.dp)
    ) {
        val rotation2 by infiniteTransition.animateFloat(
            initialValue = 360f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(6000, easing = LinearEasing)
            ),
            label = "star2_rotation"
        )
        
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFF59E0B),
            modifier = Modifier
                .size(16.dp)
                .graphicsLayer { rotationZ = rotation2 }
        )
    }
    
    // Heart
    Box(
        modifier = Modifier
            .offset(x = (-80).dp, y = 40.dp)
            .size(18.dp)
    ) {
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "heart_scale"
        )
        
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = Color(0xFFEC4899),
            modifier = Modifier
                .size(18.dp)
                .scale(scale)
        )
    }
}

@Composable
fun EnhancedActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isPrimary: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "button_scale"
    )
    
    Button(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier
            .scale(scale)
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) {
                Color(0xFF667EEA)
            } else {
                Color.Transparent
            }
        ),
        border = if (!isPrimary) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                Color(0xFF667EEA).copy(alpha = 0.3f)
            )
        } else null,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isPrimary) 8.dp else 0.dp,
            pressedElevation = if (isPrimary) 12.dp else 2.dp
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (isPrimary) Color.White else Color(0xFF667EEA)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isPrimary) Color.White else Color(0xFF667EEA)
        )
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@Composable
fun QuickActionSuggestions(
    onAddWish: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Quick suggestions:",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Medium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            QuickSuggestionChip(
                text = "📱 Tech",
                onClick = onAddWish
            )
            QuickSuggestionChip(
                text = "✈️ Travel",
                onClick = onAddWish
            )
            QuickSuggestionChip(
                text = "📚 Books",
                onClick = onAddWish
            )
        }
    }
}

@Composable
fun QuickSuggestionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "chip_scale"
    )
    
    Surface(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF667EEA).copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFF667EEA).copy(alpha = 0.2f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF667EEA),
            fontWeight = FontWeight.Medium
        )
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

@Composable
fun EmptySearchState(
    query: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Search illustration
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF667EEA).copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Color(0xFF667EEA).copy(alpha = 0.6f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No wishes found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1D29),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (query.isNotEmpty()) {
                "We couldn't find any wishes matching \"$query\".\nTry different keywords or add a new wish!"
            } else {
                "Start typing to search through your wishes"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}
