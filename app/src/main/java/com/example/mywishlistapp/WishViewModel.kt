package com.example.mywishlistapp

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.Data.WishRepository
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.notifications.ReminderSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class WishViewModel(
    private val wishRepository: WishRepository = Graph.wishRepository,
    private val context: Context? = null
) : ViewModel() {
    
    private val reminderSystem = context?.let { ReminderSystem(it) }

    var wishTitleState by mutableStateOf("")
    var wishDescriptionState by mutableStateOf("")
    var wishCategoryState by mutableStateOf("")
    var wishTagsState by mutableStateOf("")
    var wishPriorityState by mutableStateOf(Priority.MEDIUM)

    fun onWishTitleChanged(newString : String){
        wishTitleState = newString
    }

    fun onWishDescriptionChanged(newString : String){
        wishDescriptionState = newString
    }
    
    fun onWishCategoryChanged(newString: String) {
        wishCategoryState = newString
    }
    
    fun onWishTagsChanged(newString: String) {
        wishTagsState = newString
    }
    
    fun onWishPriorityChanged(newPriority: Priority) {
        wishPriorityState = newPriority
    }
    
    // Convert tags string to list
    fun getTagsList(): List<String> {
        return if (wishTagsState.isBlank()) {
            emptyList()
        } else {
            wishTagsState.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }

    val getAllWishes: Flow<List<Wish>> = wishRepository.getWishes()

    fun addWish(wish: Wish){
        viewModelScope.launch(Dispatchers.IO) {
            wishRepository.addWish(wish = wish)
            
            // Schedule reminder for high-priority wishes
            if (wish.priority == Priority.HIGH && reminderSystem != null) {
                reminderSystem.scheduleSmartReminder(wish)
            }
        }
    }

    fun getWishbyId(id: Long) : Flow<Wish>{
           return wishRepository.getWishById(id = id)
    }

    fun updateWish(wish: Wish) {
        viewModelScope.launch(Dispatchers.IO) {
            wishRepository.updateWish(wish = wish)
            
            // Update or schedule reminder based on priority
            if (reminderSystem != null) {
                if (wish.priority == Priority.HIGH) {
                    reminderSystem.scheduleSmartReminder(wish)
                } else {
                    // Cancel existing reminder if priority is no longer high
                    reminderSystem.cancelReminder(wish.id)
                }
            }
        }
    }

    fun deleteWish(wish: Wish) {
        viewModelScope.launch(Dispatchers.IO) {
            wishRepository.deleteWish(wish = wish)
            
            // Cancel any existing reminders for deleted wish
            if (reminderSystem != null) {
                reminderSystem.cancelReminder(wish.id)
            }
        }
    }




}