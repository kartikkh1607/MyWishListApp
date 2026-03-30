package com.example.mywishlistapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mywishlistapp.ui.NavIconSpring
import com.example.mywishlistapp.ui.WishViewModel

private sealed class NavItem {
    data class Tab(val label: String, val icon: ImageVector, val route: Any) : NavItem()
    object AddFab : NavItem()
}

@Composable
fun MainScreen() {
    val viewModel: WishViewModel = viewModel()
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val isTopLevelDestination = navBackStackEntry?.destination?.let { dest ->
        dest.hasRoute<Screen.DashboardScreen>() ||
                dest.hasRoute<Screen.WishListScreen>() ||
                dest.hasRoute<Screen.SearchScreen>() ||
                dest.hasRoute<Screen.SettingsScreen>()
    } ?: true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars, // keeps top (notch) inset; bottom is owned by NavigationBar
        bottomBar = {
            if (isTopLevelDestination) {
                BottomNavigationBar(navController)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Navigation(navController = navController, viewModel = viewModel)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavItem.Tab("Home", Icons.Default.Home, Screen.DashboardScreen),
        NavItem.Tab("Wishes", Icons.AutoMirrored.Filled.List, Screen.WishListScreen),
        NavItem.AddFab,
        NavItem.Tab("Search", Icons.Default.Search, Screen.SearchScreen),
        NavItem.Tab("Settings", Icons.Default.Settings, Screen.SettingsScreen)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val haptic = LocalHapticFeedback.current

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        // NavigationBar consumes the bottom navigation bar inset itself so its items
        // are lifted above the gesture indicator. The outer Scaffold has
        // contentWindowInsets = WindowInsets(0.dp) to prevent double-counting.
        windowInsets = WindowInsets.navigationBars
    ) {
        items.forEach { item ->
            when (item) {
                is NavItem.AddFab -> {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .size(52.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    navController.navigate(Screen.AddScreen(id = 0L)) {
                                        launchSingleTop = true
                                    }
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Wish",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }

                is NavItem.Tab -> {
                    val isSelected = navBackStackEntry?.destination?.let { dest ->
                        when (item.route) {
                            is Screen.DashboardScreen -> dest.hasRoute<Screen.DashboardScreen>()
                            is Screen.WishListScreen  -> dest.hasRoute<Screen.WishListScreen>()
                            is Screen.SearchScreen    -> dest.hasRoute<Screen.SearchScreen>()
                            is Screen.SettingsScreen  -> dest.hasRoute<Screen.SettingsScreen>()
                            else                      -> false
                        }
                    } ?: false

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1f,
                        animationSpec = NavIconSpring,
                        label = "icon_${item.label}"
                    )

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                modifier = Modifier
                                    .size(24.dp)
                                    .scale(iconScale)
                            )
                        },
                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(item.route) {
                                popUpTo<Screen.DashboardScreen> { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }
}
