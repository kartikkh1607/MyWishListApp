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

// Fix #1 — Form state is now a proper StateFlow data class,
//           not raw mutable public vars.
data class WishFormState(
    val title: String       = "",
    val description: String = "",
    val category: String    = "",
    val tags: String        = "",
    val priority: Priority  = Priority.MEDIUM,
    val price: String       = ""
)

class WishViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as WishListApp).wishRepository

    // ─── Form State (StateFlow) ───────────────────────────────────────────────
    private val _formState = MutableStateFlow(WishFormState())
    val formState: StateFlow<WishFormState> = _formState.asStateFlow()

    fun updateTitle(value: String)       = _formState.update { it.copy(title = value) }
    fun updateDescription(value: String) = _formState.update { it.copy(description = value) }
    fun updateCategory(value: String)    = _formState.update { it.copy(category = value) }
    fun updateTags(value: String)        = _formState.update { it.copy(tags = value) }
    fun updatePriority(value: Priority)  = _formState.update { it.copy(priority = value) }
    fun updatePrice(value: String)       = _formState.update { it.copy(price = value) }

    fun resetForm() = _formState.update { WishFormState() }

    fun loadWishIntoForm(wish: Wish) = _formState.update {
        WishFormState(
            title       = wish.title,
            description = wish.description,
            category    = wish.category,
            tags        = wish.tags.joinToString(", "),
            priority    = wish.priority,
            price       = wish.price
        )
    }

    fun getTagsList(): List<String> =
        _formState.value.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    // ─── Data ─────────────────────────────────────────────────────────────────
    val getAllWishes: StateFlow<List<Wish>> = repo.getWishes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getWishById(id: Long): Flow<Wish> = repo.getWishById(id)

    // ─── CRUD ─────────────────────────────────────────────────────────────────
    fun addWish(wish: Wish)    = viewModelScope.launch(Dispatchers.IO) { repo.addWish(wish) }
    fun updateWish(wish: Wish) = viewModelScope.launch(Dispatchers.IO) { repo.updateWish(wish) }
    fun deleteWish(wish: Wish) = viewModelScope.launch(Dispatchers.IO) { repo.deleteWish(wish) }
    fun deleteAllWishes()      = viewModelScope.launch(Dispatchers.IO) { repo.deleteAllWishes() }

    fun completeWish(wish: Wish) =
        viewModelScope.launch(Dispatchers.IO) { repo.updateWish(wish.copy(isCompleted = true)) }
}