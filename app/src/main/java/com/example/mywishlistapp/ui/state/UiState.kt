package com.example.mywishlistapp.ui.state

import com.example.mywishlistapp.Data.Wish

/**
 * Sealed classes for managing UI states consistently across the app
 * Follows best practices for state management in Compose applications
 */

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>()
}

/**
 * Specific UI states for different screens
 */

// Wish List Screen States
sealed class WishListUiState {
    object Loading : WishListUiState()
    data class Success(
        val wishes: List<Wish>,
        val isRefreshing: Boolean = false
    ) : WishListUiState()
    data class Error(val message: String) : WishListUiState()
    object Empty : WishListUiState()
}

// Search Screen States
sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(
        val results: List<Wish>,
        val query: String,
        val totalCount: Int
    ) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
    data class Empty(val query: String) : SearchUiState()
}

// Add/Edit Screen States
sealed class AddEditUiState {
    object Idle : AddEditUiState()
    object Loading : AddEditUiState()
    object Saving : AddEditUiState()
    data class Success(val message: String) : AddEditUiState()
    data class Error(val message: String) : AddEditUiState()
    data class ValidationError(val errors: Map<String, String>) : AddEditUiState()
}

// Dashboard Screen States
sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val totalWishes: Int,
        val completedWishes: Int,
        val recentWishes: List<Wish>,
        val achievements: List<com.example.mywishlistapp.Data.Achievement>,
        val motivationalMessage: String
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

// Profile Screen States
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(
        val profile: com.example.mywishlistapp.Data.UserProfile,
        val achievements: List<com.example.mywishlistapp.Data.Achievement>,
        val stats: UserStats
    ) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

// Settings Screen States
sealed class SettingsUiState {
    object Idle : SettingsUiState()
    object Loading : SettingsUiState()
    data class Success(val message: String) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}

// User Statistics
data class UserStats(
    val totalWishes: Int = 0,
    val completedWishes: Int = 0,
    val totalGoals: Int = 0,
    val completedGoals: Int = 0,
    val currentStreak: Int = 0,
    val totalPoints: Int = 0,
    val level: Int = 1,
    val levelProgress: Float = 0f
)

// Form validation state
data class ValidationState(
    val isValid: Boolean = true,
    val errors: Map<String, String> = emptyMap()
) {
    fun hasError(field: String): Boolean = errors.containsKey(field)
    fun getError(field: String): String? = errors[field]
    
    companion object {
        fun success() = ValidationState(isValid = true, errors = emptyMap())
        fun error(errors: Map<String, String>) = ValidationState(isValid = false, errors = errors)
    }
}

// Network connection state
sealed class NetworkState {
    object Available : NetworkState()
    object Unavailable : NetworkState()
    object Unknown : NetworkState()
}

// Theme state
data class ThemeState(
    val isDarkMode: Boolean = false,
    val dynamicColors: Boolean = true,
    val selectedTheme: String = "System"
)