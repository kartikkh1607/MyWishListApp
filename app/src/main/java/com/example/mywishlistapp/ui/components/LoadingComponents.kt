package com.example.mywishlistapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun PulsingLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF667EEA)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = modifier
            .size(60.dp)
            .scale(scale)
            .alpha(alpha)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        color,
                        color.copy(alpha = 0.6f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    if (isLoading) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon placeholder
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Gray.copy(alpha = alpha))
                )
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Title placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Gray.copy(alpha = alpha))
                    )
                    
                    // Description placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Gray.copy(alpha = alpha * 0.7f))
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Gray.copy(alpha = alpha * 0.7f))
                    )
                    
                    // Tags placeholder
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Gray.copy(alpha = alpha * 0.5f))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingOverlay(
    isVisible: Boolean,
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(animationSpec = tween(200)) + scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(200, easing = FastOutSlowInEasing)
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PulsingLoadingIndicator()
                    
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A1D29)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedCounter(
    count: Int,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineMedium
) {
    var oldCount by remember { mutableStateOf(count) }
    
    SideEffect {
        oldCount = count
    }
    
    Row(modifier = modifier) {
        val countString = count.toString()
        val oldCountString = oldCount.toString()
        
        for (i in countString.indices) {
            val oldChar = oldCountString.getOrNull(i)
            val newChar = countString[i]
            val char = if (oldChar == newChar) {
                oldCountString.getOrNull(i) ?: newChar
            } else newChar
            
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    slideInVertically { it } + fadeIn() togetherWith
                            slideOutVertically { -it } + fadeOut()
                },
                label = "counter"
            ) { character ->
                Text(
                    text = character.toString(),
                    style = style,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun BounceAnimation(
    targetValue: Float = 1.2f,
    content: @Composable () -> Unit
) {
    var isAnimating by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) targetValue else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = {
            isAnimating = false
        },
        label = "bounce"
    )
    
    LaunchedEffect(Unit) {
        delay(100)
        isAnimating = true
    }
    
    Box(
        modifier = Modifier
            .scale(scale)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        content()
    }
}

@Composable
fun SlideInFromBottom(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        content()
    }
}

@Composable
fun DashboardSkeleton() {
    // Shimmer animation
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    
    val shimmerBrush = Brush.horizontalGradient(
        colors = listOf(
            Color.Gray.copy(alpha = alpha),
            Color.Gray.copy(alpha = alpha * 0.5f),
            Color.Gray.copy(alpha = alpha)
        )
    )

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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header skeleton
        TopHeaderSkeleton(shimmerBrush)
        
        // Greeting card skeleton
        GreetingCardSkeleton(shimmerBrush)
        
        // Progress overview card skeleton
        ProgressOverviewSkeleton(shimmerBrush)
        
        // Quick stats skeleton
        QuickStatsSkeleton(shimmerBrush)
        
        // Category shortcuts skeleton
        CategoryShortcutsSkeleton(shimmerBrush)
        
        // Recent wishes skeleton
        RecentWishesSkeleton(shimmerBrush)
    }
}

@Composable
private fun TopHeaderSkeleton(shimmerBrush: Brush) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left icons
        Row {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(shimmerBrush)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(shimmerBrush)
            )
        }
        
        // Title
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerBrush)
        )
        
        // Right icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(shimmerBrush)
        )
    }
}

@Composable
private fun GreetingCardSkeleton(shimmerBrush: Brush) {
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
                    .background(shimmerBrush)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
            }
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(shimmerBrush)
            )
        }
    }
}

@Composable
private fun ProgressOverviewSkeleton(shimmerBrush: Brush) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            repeat(2) { index ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(shimmerBrush)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(shimmerBrush)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
                
                if (index == 0) Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun QuickStatsSkeleton(shimmerBrush: Brush) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Card(
                modifier = Modifier.weight(1f),
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
                            .background(shimmerBrush)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .width(30.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryShortcutsSkeleton(shimmerBrush: Brush) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(4) {
                    Card(
                        modifier = Modifier.width(80.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F0FE)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(shimmerBrush)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(shimmerBrush)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentWishesSkeleton(shimmerBrush: Brush) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
                
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            repeat(3) { index ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(shimmerBrush)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(shimmerBrush)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(shimmerBrush)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(shimmerBrush)
                    )
                }
                
                if (index < 2) Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
