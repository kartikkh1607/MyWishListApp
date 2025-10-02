package com.example.mywishlistapp.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mywishlistapp.*
import com.example.mywishlistapp.WishViewModel
import com.example.mywishlistapp.ui.screens.AddEditDetailView
import com.example.mywishlistapp.ui.screens.CalendarScreen
import com.example.mywishlistapp.ui.screens.DashboardScreen
import com.example.mywishlistapp.ui.screens.HomeView
import com.example.mywishlistapp.ui.screens.SearchScreen
import com.example.mywishlistapp.ui.screens.SettingsScreen

/**
 * Sealed class for navigation routes - Clean and type-safe navigation
 */
sealed class NavigationRoute(val route: String) {
    // Onboarding flow
    object Onboarding : NavigationRoute("onboarding")
    
    // Main app screens
    object Dashboard : NavigationRoute("dashboard")
    object WishList : NavigationRoute("wishlist")
    object Search : NavigationRoute("search")
    object Calendar : NavigationRoute("calendar")
    object Profile : NavigationRoute("profile")
    object Settings : NavigationRoute("settings")
    object Notifications : NavigationRoute("notifications")
    
    // Detail screens
    object AddEditWish : NavigationRoute("add_edit_wish") {
        fun createRoute(wishId: Long = 0L) = "$route/$wishId"
    }
    
    // Home (legacy support)
    object Home : NavigationRoute("home")
    
    companion object {
        const val WISH_ID_ARG = "wishId"
    }
}

/**
 * Enhanced Navigation Graph with improved animations and structure
 */
@Composable
fun NavigationGraph(
    navController: NavHostController,
    viewModel: WishViewModel
) {
    // Observe user profile to determine start destination
    val userProfile by viewModel.userProfile.collectAsState()
    val startDestination = if (userProfile.name.isEmpty()) {
        NavigationRoute.Onboarding.route
    } else {
        NavigationRoute.Dashboard.route
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            // iOS-like smooth slide transition
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 3 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeOut(
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 3 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeOut(
                animationSpec = tween(300)
            )
        }
    ) {
        
        // Onboarding Screen
        composable(
            route = NavigationRoute.Onboarding.route,
            enterTransition = {
                scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    )
                ) + fadeIn(animationSpec = tween(400))
            }
        ) {
            OnboardingScreen(
                viewModel = viewModel,
                onNavigateToDashboard = {
                    navController.navigate(NavigationRoute.Dashboard.route) {
                        popUpTo(NavigationRoute.Onboarding.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        
        // Dashboard Screen
        composable(route = NavigationRoute.Dashboard.route) {
            DashboardScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        
        // WishList Screen
        composable(route = NavigationRoute.WishList.route) {
            WishListScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        
        // Search Screen
        composable(route = NavigationRoute.Search.route) {
            SearchScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        
        // Calendar Screen
        composable(route = NavigationRoute.Calendar.route) {
            CalendarScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        
        // Profile Screen
        composable(route = NavigationRoute.Profile.route) {
            val userProfile by viewModel.userProfile.collectAsState()
            val achievements by viewModel.achievements.collectAsState()
            ProfileScreen(
                userProfile = userProfile,
                achievements = achievements,
                navController = navController,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Settings Screen
        composable(route = NavigationRoute.Settings.route) {
            SettingsScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        
        // Notifications Screen
        composable(route = NavigationRoute.Notifications.route) {
            NotificationsScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        
        // Add/Edit Wish Screen
        composable(
            route = "${NavigationRoute.AddEditWish.route}/{${NavigationRoute.WISH_ID_ARG}}",
            arguments = listOf(
                navArgument(NavigationRoute.WISH_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = 0L
                    nullable = false
                }
            ),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) { entry ->
            val wishId = entry.arguments?.getLong(NavigationRoute.WISH_ID_ARG) ?: 0L
            AddEditDetailView(
                id = wishId,
                viewModel = viewModel,
                navController = navController
            )
        }
        
        // Home Screen (legacy support)
        composable(route = NavigationRoute.Home.route) {
            HomeView(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}

/**
 * Extension functions for easier navigation
 */
fun NavHostController.navigateToAddWish() {
    navigate(NavigationRoute.AddEditWish.createRoute())
}

fun NavHostController.navigateToEditWish(wishId: Long) {
    navigate(NavigationRoute.AddEditWish.createRoute(wishId))
}

fun NavHostController.navigateToDashboard() {
    navigate(NavigationRoute.Dashboard.route) {
        popUpTo(NavigationRoute.Dashboard.route) {
            inclusive = true
        }
        launchSingleTop = true
    }
}