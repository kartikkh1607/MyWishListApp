package com.example.mywishlistapp

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.Data.WishRepository
import com.example.mywishlistapp.models.NotificationItem
import com.example.mywishlistapp.models.NotificationType
import com.example.mywishlistapp.notifications.ReminderSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class WishViewModel(
    private val wishRepository: WishRepository = Graph.wishRepository,
    private val context: Context? = null
) : ViewModel() {
    
    private val reminderSystem = context?.let { ReminderSystem(it) }

    // Notification state management
    private val _notifications = MutableStateFlow(
        listOf(
            NotificationItem(
                1,
                "Wish Reminder",
                "Your wish 'New Gaming Laptop' is still pending. Don't forget to work towards it!",
                Date(),
                NotificationType.REMINDER
            ),
            NotificationItem(
                2,
                "Achievement Unlocked!",
                "Congratulations! You've added 10 wishes to your list.",
                Calendar.getInstance().apply { add(Calendar.HOUR, -2) }.time,
                NotificationType.ACHIEVEMENT,
                true
            ),
            NotificationItem(
                3,
                "Wish Update",
                "Your wish 'Learn Guitar' has been updated successfully.",
                Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }.time,
                NotificationType.WISH_UPDATE,
                true
            ),
            NotificationItem(
                4,
                "Welcome!",
                "Welcome to WishList! Start adding your dreams and make them come true.",
                Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -3) }.time,
                NotificationType.GENERAL,
                true
            )
        )
    )
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    var wishTitleState by mutableStateOf("")
    var wishDescriptionState by mutableStateOf("")
    var wishCategoryState by mutableStateOf("")
    var wishTagsState by mutableStateOf("")
    var wishPriorityState by mutableStateOf(Priority.MEDIUM)
    var wishPriceState by mutableStateOf("")
    var wishImageUrlState by mutableStateOf("")

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
    
    fun onWishPriceChanged(newString: String) {
        wishPriceState = newString
    }
    
    fun onWishImageUrlChanged(newString: String) {
        wishImageUrlState = newString
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
    
    // Calendar and scheduling related functions
    fun updateWishScheduledDate(wishId: Long, scheduledDate: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentWish = wishRepository.getWishById(wishId).first()
            val updatedWish = currentWish.copy(scheduledDate = scheduledDate)
            wishRepository.updateWish(updatedWish)
            
            // Update reminder if needed
            if (reminderSystem != null && updatedWish.reminderSet) {
                if (scheduledDate != null) {
                    reminderSystem.scheduleSmartReminder(updatedWish)
                } else {
                    reminderSystem.cancelReminder(wishId)
                }
            }
        }
    }
    
    fun toggleWishReminder(wishId: Long, reminderSet: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentWish = wishRepository.getWishById(wishId).first()
            val updatedWish = currentWish.copy(reminderSet = reminderSet)
            wishRepository.updateWish(updatedWish)
            
            // Schedule or cancel reminder
            if (reminderSystem != null) {
                if (reminderSet && updatedWish.scheduledDate != null) {
                    reminderSystem.scheduleSmartReminder(updatedWish)
                } else {
                    reminderSystem.cancelReminder(wishId)
                }
            }
        }
    }
    
    // Get wishes scheduled for a specific date
    @RequiresApi(Build.VERSION_CODES.O)
    fun getWishesForDate(date: LocalDate): Flow<List<Wish>> {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        return getAllWishes.map { wishes ->
            wishes.filter { it.scheduledDate == dateString }
        }
    }
    
    // Get all wishes with scheduled dates
    fun getScheduledWishes(): Flow<List<Wish>> {
        return getAllWishes.map { wishes ->
            wishes.filter { it.scheduledDate != null }
        }
    }
    
    // Get wishes with reminders enabled
    fun getWishesWithReminders(): Flow<List<Wish>> {
        return getAllWishes.map { wishes ->
            wishes.filter { it.reminderSet }
        }
    }
    
    // Notification management functions
    fun removeNotification(notificationId: Int) {
        viewModelScope.launch {
            _notifications.value = _notifications.value.filter { it.id != notificationId }
        }
    }
    
    fun markNotificationAsRead(notificationId: Int) {
        viewModelScope.launch {
            _notifications.value = _notifications.value.map { notification ->
                if (notification.id == notificationId) {
                    notification.copy(isRead = true)
                } else {
                    notification
                }
            }
        }
    }
    
    fun addNotification(title: String, message: String, type: NotificationType) {
        viewModelScope.launch {
            val newNotification = NotificationItem(
                id = (_notifications.value.maxOfOrNull { it.id } ?: 0) + 1,
                title = title,
                message = message,
                timestamp = Date(),
                type = type,
                isRead = false
            )
            _notifications.value = listOf(newNotification) + _notifications.value
        }
    }
    
    fun clearAllNotifications() {
        viewModelScope.launch {
            _notifications.value = emptyList()
        }
    }
    
    fun getUnreadNotificationCount(): StateFlow<Int> {
        return _notifications.map { notifications ->
            notifications.count { !it.isRead }
        }.stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    }
}
