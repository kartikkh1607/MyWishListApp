package com.example.mywishlistapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.Data.Wish

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val wishId = intent.getLongExtra("wish_id", -1)
        val wishTitle = intent.getStringExtra("wish_title") ?: "Your Wish"
        val wishDescription = intent.getStringExtra("wish_description") ?: ""
        val priorityName = intent.getStringExtra("priority") ?: "MEDIUM"
        val reminderTypeName = intent.getStringExtra("reminder_type") ?: "SMART"
        
        val priority = try {
            Priority.valueOf(priorityName)
        } catch (e: IllegalArgumentException) {
            Priority.MEDIUM
        }
        
        val reminderType = try {
            ReminderType.valueOf(reminderTypeName)
        } catch (e: IllegalArgumentException) {
            ReminderType.SMART
        }
        
        val wish = Wish(
            id = wishId,
            title = wishTitle,
            description = wishDescription,
            priority = priority
        )
        
        val reminderSystem = ReminderSystem(context)
        
        val messages = mapOf(
            Priority.HIGH to listOf(
                "🔥 High priority reminder!",
                "⏰ This is important - don't miss out!",
                "🎯 Time to take action on this wish!",
                "🚨 Your high-priority wish needs attention!"
            ),
            Priority.MEDIUM to listOf(
                "⭐ Friendly reminder about your wish",
                "💫 Still thinking about this?",
                "🎪 Don't let this wish slip away!",
                "🌟 Time to make progress on this wish!"
            ),
            Priority.LOW to listOf(
                "💭 Just a gentle reminder...",
                "🌙 When you have time, consider this",
                "☁️ Still on your mind?",
                "🍃 No rush, but this is still here"
            )
        )
        
        val message = messages[priority]?.random() ?: "Don't forget about this wish!"
        reminderSystem.showInstantNotification(wish, message)
        
        // Schedule next reminder if it's recurring
        if (reminderType == ReminderType.RECURRING) {
            reminderSystem.scheduleReminder(wish, ReminderType.RECURRING)
        }
    }
}
