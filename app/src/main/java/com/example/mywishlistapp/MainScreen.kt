package com.example.mywishlistapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    LaunchedEffect(Unit) {
        Log.d("MainScreen", "Creating MainScreen")
    }
    
    val context = LocalContext.current
    val viewModel: WishViewModel = viewModel(
        factory = WishViewModelFactory(context.applicationContext as android.app.Application)
    )
    val navController = rememberNavController()
    
    LaunchedEffect(viewModel, navController) {
        Log.d("MainScreen", "ViewModel and NavController created successfully")
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
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
        BottomNavItem("Home", Icons.Default.Home, Screen.DashboardScreen.route),
        BottomNavItem("Wishes", Icons.AutoMirrored.Filled.List, Screen.WishListScreen.route),
        BottomNavItem("Add", Icons.Default.Add, Screen.AddScreen.route + "/0"),
        BottomNavItem("Calendar", Icons.Default.CalendarToday, Screen.CalendarScreen.route),
        BottomNavItem("Settings", Icons.Default.Settings, Screen.SettingsScreen.route)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val hapticFeedback = LocalHapticFeedback.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 12.dp,
        color = Color.White
    ) {
        NavigationBar(
            containerColor = Color.White,
            contentColor = Color(0xFF667EEA),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 8.dp,
                    vertical = 8.dp
                )
                .height(72.dp)
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = currentRoute == item.route
                val isAddButton = item.label == "Add"
                
                // Animation states for each item
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "scale_${item.label}"
                )
                
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.2f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "icon_scale_${item.label}"
                )

                if (isAddButton) {
                    // Special floating action button style for "Add" button
                    FloatingActionButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier
                            .scale(scale)
                            .size(56.dp),
                        shape = CircleShape,
                        containerColor = Color(0xFF667EEA),
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 12.dp
                        )
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier
                                .size(28.dp)
                                .scale(iconScale)
                        )
                    }
                } else {
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
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.scale(if (isSelected) 1.05f else 1f)
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            // Different haptic feedback for different actions
                            hapticFeedback.performHapticFeedback(
                                if (isSelected) HapticFeedbackType.TextHandleMove
                                else HapticFeedbackType.LongPress
                            )
                            
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.scale(scale * 0.95f), // Slightly smaller scale for regular items
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFF667EEA),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8),
                            indicatorColor = Color(0xFF667EEA)
                        )
                    )
                }
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)
