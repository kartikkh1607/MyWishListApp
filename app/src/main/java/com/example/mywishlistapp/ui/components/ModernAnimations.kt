package com.example.mywishlistapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Modern iOS-like animations for enhanced user experience
 * Combines Material Design with iOS fluid interactions
 */

// Spring animation configurations
object SpringAnimations {
    val gentle = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val responsive = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val snappy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )
}

// Duration configurations
object AnimationDurations {
    const val quick = 150
    const val normal = 300
    const val slow = 500
    const val extraSlow = 800
}

/**
 * iOS-like pressable animation with subtle scale and haptic feedback
 */
@Composable
fun Modifier.pressableScale(
    pressedScale: Float = 0.96f,
    duration: Int = AnimationDurations.quick,
    onPress: (() -> Unit)? = null
): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "pressScale"
    )
    
    return this
        .scale(scale)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    onPress?.invoke()
                    tryAwaitRelease()
                    isPressed = false
                }
            )
        }
}

/**
 * Smooth slide-in animation from specified direction
 */
@Composable
fun slideInFromDirection(
    direction: SlideDirection,
    duration: Int = AnimationDurations.normal,
    delay: Int = 0
): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { fullWidth ->
            when (direction) {
                SlideDirection.LEFT -> -fullWidth
                SlideDirection.RIGHT -> fullWidth
                SlideDirection.UP -> 0
                SlideDirection.DOWN -> 0
            }
        },
        animationSpec = tween(
            durationMillis = duration,
            delayMillis = delay,
            easing = FastOutSlowInEasing
        )
    ) + slideInVertically(
        initialOffsetY = { fullHeight ->
            when (direction) {
                SlideDirection.UP -> fullHeight
                SlideDirection.DOWN -> -fullHeight
                SlideDirection.LEFT -> 0
                SlideDirection.RIGHT -> 0
            }
        },
        animationSpec = tween(
            durationMillis = duration,
            delayMillis = delay,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = duration,
            delayMillis = delay
        )
    )
}

/**
 * Smooth slide-out animation to specified direction
 */
@Composable
fun slideOutToDirection(
    direction: SlideDirection,
    duration: Int = AnimationDurations.normal
): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { fullWidth ->
            when (direction) {
                SlideDirection.LEFT -> -fullWidth
                SlideDirection.RIGHT -> fullWidth
                SlideDirection.UP -> 0
                SlideDirection.DOWN -> 0
            }
        },
        animationSpec = tween(
            durationMillis = duration,
            easing = FastOutLinearInEasing
        )
    ) + slideOutVertically(
        targetOffsetY = { fullHeight ->
            when (direction) {
                SlideDirection.UP -> -fullHeight
                SlideDirection.DOWN -> fullHeight
                SlideDirection.LEFT -> 0
                SlideDirection.RIGHT -> 0
            }
        },
        animationSpec = tween(
            durationMillis = duration,
            easing = FastOutLinearInEasing
        )
    ) + fadeOut(
        animationSpec = tween(durationMillis = duration)
    )
}

enum class SlideDirection {
    LEFT, RIGHT, UP, DOWN
}

/**
 * Staggered list animation for revealing items sequentially
 */
@Composable
fun <T> LazyListScope.itemsWithAnimation(
    items: List<T>,
    key: ((item: T) -> Any)? = null,
    staggerDelay: Int = 50,
    itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    items(
        count = items.size,
        key = if (key != null) { index -> key(items[index]) } else null
    ) { index ->
        AnimatedVisibility(
            visible = true,
            enter = slideInFromDirection(
                direction = SlideDirection.UP,
                duration = AnimationDurations.normal,
                delay = index * staggerDelay
            )
        ) {
            itemContent(items[index])
        }
    }
}

/**
 * Floating animation with subtle vertical movement
 */
@Composable
fun Modifier.floatingAnimation(
    amplitude: Dp = 4.dp,
    duration: Int = 3000
): Modifier {
    val density = LocalDensity.current
    val amplitudePx = with(density) { amplitude.toPx() }
    
    val animatedOffset by rememberInfiniteTransition(label = "floating").animateFloat(
        initialValue = 0f,
        targetValue = amplitudePx,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = duration,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatingOffset"
    )
    
    return this.graphicsLayer {
        translationY = -animatedOffset
    }
}

/**
 * Shimmer loading effect
 */
@Composable
fun Modifier.shimmerEffect(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    return this.graphicsLayer { this.alpha = alpha }
}

/**
 * Parallax scrolling effect
 */
@Composable
fun Modifier.parallaxScroll(
    scrollState: Float,
    rate: Float = 0.5f
): Modifier {
    return this.graphicsLayer {
        translationY = scrollState * rate
    }
}

/**
 * iOS-style card with subtle shadow and rounded corners
 */
@Composable
fun ModernCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = 8.dp,
    cornerRadius: Dp = 16.dp,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.pressableScale(onPress = onClick)
                } else Modifier
            ),
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = colors,
        content = content
    )
}

/**
 * Smooth color transition animation
 */
@Composable
fun animatedColor(
    targetColor: Color,
    duration: Int = AnimationDurations.normal,
    label: String = "colorAnimation"
): State<Color> {
    return animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(
            durationMillis = duration,
            easing = FastOutSlowInEasing
        ),
        label = label
    )
}

/**
 * Bouncy scale animation for attention-grabbing elements
 */
@Composable
fun Modifier.bounceScale(
    targetScale: Float = 1.1f,
    duration: Int = AnimationDurations.normal
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = targetScale,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = duration,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceScale"
    )
    
    return this.scale(scale)
}

/**
 * Slide reveal animation for content
 */
@Composable
fun SlideReveal(
    visible: Boolean,
    direction: SlideDirection = SlideDirection.UP,
    duration: Int = AnimationDurations.normal,
    delay: Int = 0,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInFromDirection(direction, duration, delay),
        exit = slideOutToDirection(direction, duration)
    ) {
        content()
    }
}

/**
 * Fade and scale transition for modal-like content
 */
@Composable
fun ModalTransition(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(
            animationSpec = tween(AnimationDurations.normal)
        ),
        exit = scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(AnimationDurations.quick)
        ) + fadeOut(
            animationSpec = tween(AnimationDurations.quick)
        )
    ) {
        content()
    }
}