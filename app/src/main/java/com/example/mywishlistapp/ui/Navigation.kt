package com.example.mywishlistapp.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.mywishlistapp.ui.screens.AddEditDetailView
import com.example.mywishlistapp.ui.screens.DashboardScreen
import com.example.mywishlistapp.ui.screens.SearchScreen
import com.example.mywishlistapp.ui.screens.SettingsScreen
import com.example.mywishlistapp.ui.screens.WishListScreen

@Composable
fun Navigation(
    navController: NavHostController,
    viewModel: WishViewModel
) {
    NavHost(
        navController    = navController,
        startDestination = Screen.DashboardScreen,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        composable<Screen.DashboardScreen> {
            DashboardScreen(navController = navController, viewModel = viewModel)
        }

        composable<Screen.WishListScreen> {
            WishListScreen(navController = navController, viewModel = viewModel)
        }

        composable<Screen.AddScreen>(
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) +
                        fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) +
                        fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) +
                        fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) +
                        fadeOut(animationSpec = tween(300))
            }
        ) { entry ->
            val route = entry.toRoute<Screen.AddScreen>()
            AddEditDetailView(id = route.id, viewModel = viewModel, navController = navController)
        }

        composable<Screen.SearchScreen> {
            SearchScreen(navController = navController, viewModel = viewModel)
        }

        composable<Screen.SettingsScreen> {
            SettingsScreen(navController = navController, viewModel = viewModel)
        }
    }
}