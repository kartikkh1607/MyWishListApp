package com.example.mywishlistapp.presentation.state

import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.Data.UserProfile
import com.example.mywishlistapp.Data.Achievement

/**
 * Sealed class representing different UI states for better state management
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val exception: Throwable, val message: String = exception.message ?: "Unknown error") : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}

/**
 * Specific UI states for different screens
 */
sealed class WishListUiState {
    object Loading : WishListUiState()
    data class Success(val wishes: List<Wish>) : WishListUiState()
    data class Error(val message: String) : WishListUiState()
    object Empty : WishListUiState()
}

sealed class UserProfileUiState {
    object Loading : UserProfileUiState()
    data class Success(val profile: UserProfile, val achievements: List<Achievement>) : UserProfileUiState()
    data class Error(val message: String) : UserProfileUiState()
}

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val results: List<Wish>, val query: String) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
    data class Empty(val query: String) : SearchUiState()
}

/**
 * Extension functions for easier state handling
 */
inline fun <T> UiState<T>.onSuccess(action: (T) -> Unit): UiState<T> {
    if (this is UiState.Success) action(data)
    return this
}

inline fun <T> UiState<T>.onError(action: (Throwable, String) -> Unit): UiState<T> {
    if (this is UiState.Error) action(exception, message)
    return this
}

inline fun <T> UiState<T>.onLoading(action: () -> Unit): UiState<T> {
    if (this is UiState.Loading) action()
    return this
}