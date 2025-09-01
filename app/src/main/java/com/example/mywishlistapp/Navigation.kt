package com.example.mywishlistapp

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
    // Observe user profile to determine if onboarding is needed
    val userProfile by viewModel.userProfile.collectAsState()
    val shouldShowOnboarding = userProfile.name.isEmpty()
    
    NavHost(
        navController = navController,
        startDestination = if (shouldShowOnboarding) Screen.OnboardingScreen.route else Screen.DashboardScreen.route
    ) {
        
        // Onboarding Screen
        composable(
            route = Screen.OnboardingScreen.route,
            enterTransition = {
                fadeIn(animationSpec = tween(600)) + scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(600)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) + scaleOut(
                    targetScale = 1.05f,
                    animationSpec = tween(300)
                )
            }
        ) {
            OnboardingScreen(
                viewModel = viewModel,
                onNavigateToDashboard = {
                    navController.navigate(Screen.DashboardScreen.route) {
                        // Clear the entire back stack including onboarding
                        popUpTo(Screen.OnboardingScreen.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
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

        // Dashboard Screen with enhanced transitions
        composable(
            route = Screen.DashboardScreen.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(400)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            DashboardScreen(navController = navController, viewModel = viewModel)
        }

        // WishList Screen with modern transitions
        composable(
            route = Screen.WishListScreen.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { it / 4 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(250))
            }
        ) {
            WishListScreen(navController = navController, viewModel = viewModel)
        }

        // Calendar Screen with slide transition
        composable(
            route = Screen.CalendarScreen.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut()
            }
        ) {
            CalendarScreen(navController = navController, viewModel = viewModel)
        }

        // Settings Screen with fade transition
        composable(
            route = Screen.SettingsScreen.route,
            enterTransition = {
                fadeIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + scaleIn(
                    initialScale = 0.95f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            },
            exitTransition = {
                fadeOut() + scaleOut(targetScale = 0.95f)
            }
        ) {
            SettingsScreen(navController = navController, viewModel = viewModel)
        }
        
        // Notifications Screen with slide from top
        composable(
            route = Screen.NotificationsScreen.route,
            enterTransition = {
                slideInVertically(
                    initialOffsetY = { -it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn()
            },
            exitTransition = {
                slideOutVertically(
                    targetOffsetY = { -it / 2 },
                    animationSpec = tween(300)
                ) + fadeOut()
            }
        ) {
            NotificationsScreen(navController = navController, viewModel = viewModel)
        }

        // Profile Screen with scale transition
        composable(
            route = Screen.ProfileScreen.route,
            enterTransition = {
                scaleIn(
                    initialScale = 0.9f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                scaleOut(
                    targetScale = 0.9f,
                    animationSpec = tween(300)
                ) + fadeOut()
            }
        ) {
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
