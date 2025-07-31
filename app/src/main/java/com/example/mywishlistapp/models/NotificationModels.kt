package com.example.mywishlistapp.models

import java.util.*

// Notification data classes
data class NotificationItem(
    val id: Int,
    val title: String,
    val message: String,
    val timestamp: Date,
    val type: NotificationType,
    val isRead: Boolean = false
)

enum class NotificationType {
    REMINDER, ACHIEVEMENT, GENERAL, WISH_UPDATE, SYSTEM
}
