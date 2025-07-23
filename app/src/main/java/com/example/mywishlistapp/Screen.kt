package com.example.mywishlistapp

sealed class Screen(val route : String) {
    object HomeScreen : Screen("home_screen")
    object AddScreen : Screen("Add_screen")
    object SearchScreen : Screen("search_screen")
}
