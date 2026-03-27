package com.example.mywishlistapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Enhanced Dark Color Scheme - Modern Dark Mode
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = TextOnDark,
    primaryContainer = DeepPurple,
    onPrimaryContainer = TextOnDark,

    secondary = AccentTeal,
    onSecondary = TextOnDark,
    secondaryContainer = AccentBlue,
    onSecondaryContainer = TextOnDark,

    tertiary = AccentOrange,
    onTertiary = Color.Black,
    tertiaryContainer = AccentGreen,
    onTertiaryContainer = TextOnDark,

    background = Color(0xFF0F0F23),
    onBackground = TextOnDark,
    surface = Color(0xFF1A1A2E),
    onSurface = TextOnDark,
    surfaceVariant = Color(0xFF2A2A3E),
    onSurfaceVariant = Color(0xFFE0E0E6),

    error = AccentRed,
    onError = TextOnDark,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    outline = Color(0xFF6B7280),
    outlineVariant = Color(0xFF374151),
    scrim = Color(0xFF000000),
    inverseSurface = SurfaceWhite,
    inverseOnSurface = TextPrimary,
    inversePrimary = PrimaryPurple
)

// Enhanced Light Color Scheme - Modern Vibrant Style
private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = TextOnDark,
    primaryContainer = Color(0xFFE8F0FE),
    onPrimaryContainer = TextPrimary,

    secondary = AccentTeal,
    onSecondary = TextOnDark,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = TextPrimary,

    tertiary = AccentGreen,
    onTertiary = TextOnDark,
    tertiaryContainer = Color(0xFFE8F5E8),
    onTertiaryContainer = TextPrimary,

    background = BackgroundGradientStart,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundSecondary,
    onSurfaceVariant = TextSecondary,

    error = AccentRed,
    onError = TextOnDark,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),

    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFF1F5F9),
    scrim = Color(0xFF000000),
    inverseSurface = TextPrimary,
    inverseOnSurface = SurfaceWhite,
    inversePrimary = LightPurple
)

@Composable
fun MyWishListAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // FIX: removed WindowCompat.setDecorFitsSystemWindows(window, false) from here.
    // Edge-to-edge is now handled by enableEdgeToEdge() in MainActivity before setContent,
    // so insets are available correctly from the very first frame.
    // We only keep status bar icon appearance here.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}