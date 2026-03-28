package com.example.mywishlistapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.WishListApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WishFormState(
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val tags: String = "",
    val priority: Priority = Priority.MEDIUM,
    val price: String = ""
)

class WishViewModel(application: Application) : AndroidViewModel(application) {

    // FIX: Safe cast with a clear error message instead of a silent crash
    private val repo = checkNotNull((application as? WishListApp)?.wishRepository) {
        "WishListApp not found — make sure AndroidManifest.xml has android:name='.WishListApp'"
    }

    // ─── Form State ───────────────────────────────────────────────────────────
    private val _formState = MutableStateFlow(WishFormState())
    val formState: StateFlow<WishFormState> = _formState.asStateFlow()

    fun updateTitle(value: String) = _formState.update { it.copy(title = value) }
    fun updateDescription(value: String) = _formState.update { it.copy(description = value) }
    fun updateCategory(value: String) = _formState.update { it.copy(category = value) }
    fun updateTags(value: String) = _formState.update { it.copy(tags = value) }
    fun updatePriority(value: Priority) = _formState.update { it.copy(priority = value) }
    fun updatePrice(value: String) = _formState.update { it.copy(price = value) }

    fun resetForm() = _formState.update { WishFormState() }

    fun loadWishIntoForm(wish: Wish) = _formState.update {
        WishFormState(
            title = wish.title,
            description = wish.description,
            category = wish.category,
            tags = wish.tags.joinToString(", "),
            priority = wish.priority,
            price = wish.price
        )
    }

    fun getTagsList(): List<String> =
        _formState.value.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    // ─── Saving State ─────────────────────────────────────────────────────────
    // FIX: Real loading state that reflects actual DB operation status.
    // The button observes this instead of using a fake 600ms delay.
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // ─── Data ─────────────────────────────────────────────────────────────────
    val getAllWishes: StateFlow<List<Wish>> = repo.getWishes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getWishById(id: Long): Flow<Wish?> = repo.getWishById(id)

    // ─── CRUD ─────────────────────────────────────────────────────────────────
    fun addWish(wish: Wish) = viewModelScope.launch(Dispatchers.IO) {
        _isSaving.value = true
        repo.addWish(wish)
        _isSaving.value = false
    }

    fun updateWish(wish: Wish) = viewModelScope.launch(Dispatchers.IO) {
        _isSaving.value = true
        repo.updateWish(wish)
        _isSaving.value = false
    }

    fun deleteWish(wish: Wish) = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteWish(wish)
    }

    fun deleteAllWishes() = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteAllWishes()
    }

    fun completeWish(wish: Wish) = viewModelScope.launch(Dispatchers.IO) {
        repo.updateWish(wish.copy(isCompleted = true))
    }
}