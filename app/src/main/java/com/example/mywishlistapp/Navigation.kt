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
        startDestination = if (shouldShowOnboarding) Screen.OnboardingScreen.route else Screen.DashboardScreen.route,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(600)) + fadeIn(animationSpec = tween(600))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(600)) + fadeOut(animationSpec = tween(600))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(600)) + fadeIn(animationSpec = tween(600))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(600)) + fadeOut(animationSpec = tween(600))
        }
    ) {
        
        // Onboarding Screen - using global navigation transitions
        composable(route = Screen.OnboardingScreen.route) {
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
        
        // Home Screen - using global navigation transitions
        composable(route = Screen.HomeScreen.route) {
            HomeView(navController, viewModel = viewModel)
        }

        // Add/Edit Screen - using global navigation transitions
        composable(
            route = Screen.AddScreen.route + "/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = 0L
                    nullable = false
                }
            )
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            AddEditDetailView(id = id, viewModel = viewModel, navController = navController)
        }

        // Search Screen - using global navigation transitions
        composable(route = Screen.SearchScreen.route) {
            SearchScreen(navController = navController, viewModel = viewModel)
        }

        // Dashboard Screen - using global navigation transitions
        composable(route = Screen.DashboardScreen.route) {
            DashboardScreen(navController = navController, viewModel = viewModel)
        }

        // WishList Screen - using global navigation transitions
        composable(route = Screen.WishListScreen.route) {
            WishListScreen(navController = navController, viewModel = viewModel)
        }

        // Calendar Screen - using global navigation transitions
        composable(route = Screen.CalendarScreen.route) {
            CalendarScreen(navController = navController, viewModel = viewModel)
        }

        // Settings Screen - using global navigation transitions
        composable(route = Screen.SettingsScreen.route) {
            SettingsScreen(navController = navController, viewModel = viewModel)
        }
        
        // Notifications Screen - using global navigation transitions
        composable(route = Screen.NotificationsScreen.route) {
            NotificationsScreen(navController = navController, viewModel = viewModel)
        }

        // Profile Screen - using global navigation transitions
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
