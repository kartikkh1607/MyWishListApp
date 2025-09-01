package com.example.mywishlistapp

sealed class Screen(val route : String) {
    // Onboarding screen
    object OnboardingScreen : Screen("onboarding_screen")
    
    // Original screens
    object HomeScreen : Screen("home_screen")
    object AddScreen : Screen("Add_screen")
    object SearchScreen : Screen("search_screen")
    
    // New bottom navigation screens
    object DashboardScreen : Screen("dashboard_screen")
    object WishListScreen : Screen("wishlist_screen")
    object CalendarScreen : Screen("calendar_screen")
object SettingsScreen : Screen("settings_screen")
    object NotificationsScreen : Screen("notifications_screen")
    object ProfileScreen : Screen("profile_screen")
}
