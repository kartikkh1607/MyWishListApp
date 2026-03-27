package com.example.mywishlistapp.ui

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable data class AddScreen(val id: Long = 0L) : Screen
    @Serializable data object SearchScreen : Screen
    @Serializable data object DashboardScreen : Screen
    @Serializable data object WishListScreen : Screen
    @Serializable data object SettingsScreen : Screen
}
