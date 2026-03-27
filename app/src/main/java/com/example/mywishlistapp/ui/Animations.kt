package com.example.mywishlistapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.*
import kotlinx.coroutines.delay

// ─── Shared Spring Specs ──────────────────────────────────────────────────────

/** Gentle bounce used for scale animations on press */
val BounceSpring = spring<Float>(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh)

/** Soft spring for entrance slide-in */
val EntranceSpring = spring<Float>(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)

/** Medium spring for icon scale in navigation bar */
val NavIconSpring = spring<Float>(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)

/** Smooth tween for progress bar fill */
val ProgressTween = tween<Float>(800, easing = FastOutSlowInEasing)

// ─── Reusable Animation Composables ──────────────────────────────────────────

/**
 * Staggered fade+slide-in entrance for list items.
 * @param index Position in the list — higher index = longer delay
 * @param delayPerItemMs Milliseconds to add per index step (default 60ms)
 * @param maxDelayMs Cap on the total delay so late items aren't too slow
 */
@Composable
fun StaggeredEntrance(
    index: Int,
    delayPerItemMs: Int = 60,
    maxDelayMs: Int = 500,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val delayMs = remember { (index * delayPerItemMs).coerceAtMost(maxDelayMs) }

    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) +
                slideInVertically(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) { it / 3 }
    ) {
        content()
    }
}
