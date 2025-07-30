package com.example.mywishlistapp

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun Navigation(
    navController: NavHostController,
    viewModel: WishViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.DashboardScreen.route
    ) {
        // Home Screen with Material Shared-Axis Horizontal Transition
        composable(
            route = Screen.HomeScreen.route,
            enterTransition = {
                // When returning from Add/Edit screen (pop enter)
                when (initialState.destination.route) {
                    Screen.AddScreen.route + "/{id}" -> {
                        // Slide in from left + fade in
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> -fullWidth / 4 },
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = 50 // Slight delay for better transition
                            )
                        ) + fadeIn(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = 50
                            )
                        )
                    }
                    else -> {
                        // Default fade in
                        fadeIn(animationSpec = tween(300))
                    }
                }
            },
            exitTransition = {
                // When navigating to Add/Edit screen
                when (targetState.destination.route) {
                    Screen.AddScreen.route + "/{id}" -> {
                        // Slide out to left + fade out
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth / 4 },
                            animationSpec = tween(durationMillis = 300)
                        ) + fadeOut(
                            animationSpec = tween(durationMillis = 250)
                        )
                    }
                    else -> {
                        fadeOut(animationSpec = tween(300))
                    }
                }
            }
        ) {
            HomeView(navController, viewModel = viewModel)
        }

        // Add/Edit Screen with Material Shared-Axis Horizontal Transition
        composable(
            route = Screen.AddScreen.route + "/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = 0L
                    nullable = false
                }
            ),
            enterTransition = {
                // When navigating from Home screen
                when (initialState.destination.route) {
                    Screen.HomeScreen.route -> {
                        // Slide in from right + fade in
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth / 4 },
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = 50 // Slight delay for smoother transition
                            )
                        ) + fadeIn(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = 50
                            )
                        )
                    }
                    else -> {
                        fadeIn(animationSpec = tween(300))
                    }
                }
            },
            exitTransition = {
                // When navigating back to Home screen (pop exit)
                when (targetState.destination.route) {
                    Screen.HomeScreen.route -> {
                        // Slide out to right + fade out
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth / 4 },
                            animationSpec = tween(durationMillis = 300)
                        ) + fadeOut(
                            animationSpec = tween(durationMillis = 250)
                        )
                    }
                    else -> {
                        fadeOut(animationSpec = tween(300))
                    }
                }
            }
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            AddEditDetailView(id = id, viewModel = viewModel, navController = navController)
        }

        // Search Screen with Material Shared-Axis Horizontal Transition
        composable(
            route = Screen.SearchScreen.route,
            enterTransition = {
                // Slide in from right + fade in
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth / 4 },
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = 50 // Slight delay for smoother transition
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = 50
                    )
                )
            },
            exitTransition = {
                // Slide out to left + fade out
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth / 4 },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 250)
                )
            }
        ) {
            SearchScreen(navController = navController, viewModel = viewModel)
        }

        // Dashboard Screen
        composable(route = Screen.DashboardScreen.route) {
            DashboardScreen(navController = navController, viewModel = viewModel)
        }

        // WishList Screen (modern design)
        composable(route = Screen.WishListScreen.route) {
            WishListScreen(navController = navController, viewModel = viewModel)
        }

        // Calendar Screen (placeholder)
        composable(route = Screen.CalendarScreen.route) {
            CalendarScreen(navController = navController, viewModel = viewModel)
        }

        // Settings Screen (placeholder)
        composable(route = Screen.SettingsScreen.route) {
            SettingsScreen(navController = navController, viewModel = viewModel)
        }
        
        // Notifications Screen
        composable(route = Screen.NotificationsScreen.route) {
            NotificationsScreen(navController = navController, viewModel = viewModel)
        }

        // Profile Screen
        composable(route = Screen.ProfileScreen.route) {
            val userProfile by viewModel.userProfile.collectAsState()
            val achievements by viewModel.achievements.collectAsState()
            ProfileScreen(
                userProfile = userProfile,
                achievements = achievements,
                navController = navController,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
