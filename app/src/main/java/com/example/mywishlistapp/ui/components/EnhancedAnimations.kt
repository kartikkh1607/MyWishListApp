package com.example.mywishlistapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.ripple.rememberRipple

// Enhanced bouncing animation with customizable parameters
@Composable
fun EnhancedBounceAnimation(
    scale: Float = 0.95f,
    duration: Int = 150,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) scale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "bounce_scale"
    )

    Box(
        modifier = Modifier
            .scale(animatedScale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            }
    ) {
        content()
    }
}

// Pulsing animation for attention-grabbing elements
@Composable
fun PulsingAnimation(
    minScale: Float = 0.9f,
    maxScale: Float = 1.1f,
    duration: Int = 1000,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(modifier = Modifier.scale(scale)) {
        content()
    }
}

// Shimmer loading animation
@Composable
fun ShimmerAnimation(
    modifier: Modifier = Modifier,
    shimmerColor: Color = Color.White.copy(alpha = 0.7f),
    backgroundColor: Color = Color.Gray.copy(alpha = 0.3f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    Box(
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        backgroundColor,
                        shimmerColor,
                        backgroundColor
                    ),
                    startX = translateAnim - 500f,
                    endX = translateAnim
                )
            )
    )
}

// Floating Action Button with enhanced animations
@Composable
fun EnhancedFAB(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFF667EEA),
    contentColor: Color = Color.White,
    size: Dp = 64.dp
) {
    var isPressed by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(300) // Entrance delay
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "fab_scale"
    )

    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 2.dp.value else 8.dp.value,
        label = "fab_elevation"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        FloatingActionButton(
            onClick = {
                isPressed = true
                onClick()
            },
            modifier = modifier
                .size(size)
                .scale(scale),
            containerColor = containerColor,
            contentColor = contentColor,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = elevation.dp,
                pressedElevation = (elevation + 4).dp
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

// Ripple click effect for cards and buttons
@Composable
fun RippleClickable(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    rippleColor: Color = Color(0xFF667EEA).copy(alpha = 0.12f),
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = rippleColor),
                enabled = enabled,
                onClick = onClick
            )
    ) {
        content()
    }
}

// Slide in animation for list items
@Composable
fun SlideInListItem(
    index: Int,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay((index * 50).toLong()) // Staggered animation
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(
            animationSpec = tween(300, delayMillis = index * 50)
        ),
        exit = slideOutHorizontally() + fadeOut()
    ) {
        content()
    }
}

// Breathing animation for waiting states
@Composable
fun BreathingAnimation(
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_alpha"
    )

    Box(modifier = Modifier.graphicsLayer(alpha = alpha)) {
        content()
    }
}

// Success checkmark animation
@Composable
fun SuccessAnimation(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    Color(0xFF10B981),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Check,
                contentDescription = "Success",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
