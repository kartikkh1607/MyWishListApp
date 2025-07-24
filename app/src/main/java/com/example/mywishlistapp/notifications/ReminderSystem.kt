package com.example.mywishlistapp.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.MainActivity
import com.example.mywishlistapp.R
import java.util.*
import java.util.concurrent.TimeUnit

class ReminderSystem(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        const val CHANNEL_ID = "wishlist_reminders"
        const val CHANNEL_NAME = "Wishlist Reminders"
        const val NOTIFICATION_ID_BASE = 1000
        
        // Reminder intervals based on priority
        val REMINDER_INTERVALS = mapOf(
            Priority.HIGH to TimeUnit.HOURS.toMillis(1), // Every hour for high priority
            Priority.MEDIUM to TimeUnit.HOURS.toMillis(6), // Every 6 hours for medium
            Priority.LOW to TimeUnit.DAYS.toMillis(1) // Once a day for low priority
        )
        
        val SMART_REMINDER_TIMES = listOf(
            9, 12, 15, 18, 21 // 9 AM, 12 PM, 3 PM, 6 PM, 9 PM
        )
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for your wishlist items"
                enableVibration(true)
                enableLights(true)
                lightColor = ContextCompat.getColor(context, R.color.purple_500)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun scheduleReminder(wish: Wish, reminderType: ReminderType = ReminderType.SMART) {
        when (reminderType) {
            ReminderType.IMMEDIATE -> scheduleImmediateReminder(wish)
            ReminderType.SMART -> scheduleSmartReminder(wish)
            ReminderType.RECURRING -> scheduleRecurringReminder(wish)
            ReminderType.CUSTOM -> scheduleCustomReminder(wish)
        }
    }
    
    private fun scheduleImmediateReminder(wish: Wish) {
        val triggerTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)
        scheduleAlarm(wish, triggerTime, ReminderType.IMMEDIATE)
    }
    
    fun scheduleSmartReminder(wish: Wish) {
        val now = Calendar.getInstance()
        val nextReminderTime = Calendar.getInstance()
        
        // Find next smart reminder time based on priority
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val nextHour = SMART_REMINDER_TIMES.find { it > currentHour } 
            ?: SMART_REMINDER_TIMES.first()
        
        if (nextHour <= currentHour) {
            nextReminderTime.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        nextReminderTime.set(Calendar.HOUR_OF_DAY, nextHour)
        nextReminderTime.set(Calendar.MINUTE, 0)
        nextReminderTime.set(Calendar.SECOND, 0)
        
        scheduleAlarm(wish, nextReminderTime.timeInMillis, ReminderType.SMART)
    }
    
    private fun scheduleRecurringReminder(wish: Wish) {
        val interval = REMINDER_INTERVALS[wish.priority] ?: TimeUnit.HOURS.toMillis(6)
        val triggerTime = System.currentTimeMillis() + interval
        
        scheduleAlarm(wish, triggerTime, ReminderType.RECURRING)
    }
    
    fun scheduleCustomReminder(wish: Wish, customTime: Long = 0) {
        val triggerTime = if (customTime > 0) customTime else System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2)
        scheduleAlarm(wish, triggerTime, ReminderType.CUSTOM)
    }
    
    private fun scheduleAlarm(wish: Wish, triggerTime: Long, reminderType: ReminderType) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("wish_id", wish.id)
            putExtra("wish_title", wish.title)
            putExtra("wish_description", wish.description)
            putExtra("priority", wish.priority.name)
            putExtra("reminder_type", reminderType.name)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (NOTIFICATION_ID_BASE + wish.id).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback for devices that don't allow exact alarms
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }
    
    fun cancelReminder(wishId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (NOTIFICATION_ID_BASE + wishId).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
    }
    
    fun showInstantNotification(wish: Wish, message: String = "Don't forget about this wish!") {
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission not granted, return without showing notification
                return
            }
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("wish_id", wish.id)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            wish.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val priorityIcon = when (wish.priority) {
            Priority.HIGH -> "🔥"
            Priority.MEDIUM -> "⭐"
            Priority.LOW -> "💭"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$priorityIcon ${wish.title}")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${wish.description}\n\nCategory: ${wish.category}\nPriority: ${wish.priority}"))
            .setPriority(when (wish.priority) {
                Priority.HIGH -> NotificationCompat.PRIORITY_HIGH
                Priority.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
                Priority.LOW -> NotificationCompat.PRIORITY_LOW
            })
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Mark as Done",
                createMarkAsDoneIntent(wish.id)
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Snooze",
                createSnoozeIntent(wish)
            )
            .build()
        
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify((NOTIFICATION_ID_BASE + wish.id).toInt(), notification)
            }
        } else {
            notificationManager.notify((NOTIFICATION_ID_BASE + wish.id).toInt(), notification)
        }
    }
    
    private fun createMarkAsDoneIntent(wishId: Long): PendingIntent {
        val intent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = "MARK_DONE"
            putExtra("wish_id", wishId)
        }
        return PendingIntent.getBroadcast(
            context,
            wishId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createSnoozeIntent(wish: Wish): PendingIntent {
        val intent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = "SNOOZE"
            putExtra("wish_id", wish.id)
            putExtra("wish_title", wish.title)
            putExtra("wish_description", wish.description)
            putExtra("priority", wish.priority.name)
        }
        return PendingIntent.getBroadcast(
            context,
            wish.id.toInt() + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

enum class ReminderType {
    IMMEDIATE,    // 5 minutes from now
    SMART,        // Next logical time based on priority
    RECURRING,    // Based on priority intervals
    CUSTOM        // User-defined time
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val wishId = intent.getLongExtra("wish_id", -1)
        val wishTitle = intent.getStringExtra("wish_title") ?: "Your Wish"
        val wishDescription = intent.getStringExtra("wish_description") ?: ""
        val priorityName = intent.getStringExtra("priority") ?: "MEDIUM"
        val reminderTypeName = intent.getStringExtra("reminder_type") ?: "SMART"
        
        val priority = Priority.valueOf(priorityName)
        val reminderType = ReminderType.valueOf(reminderTypeName)
        
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

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val wishId = intent.getLongExtra("wish_id", -1)
        val action = intent.action
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel((ReminderSystem.NOTIFICATION_ID_BASE + wishId).toInt())
        
        when (action) {
            "MARK_DONE" -> {
                // TODO: Mark wish as completed in database
                showActionConfirmation(context, "Wish marked as done! 🎉")
            }
            "SNOOZE" -> {
                val wishTitle = intent.getStringExtra("wish_title") ?: "Your Wish"
                val wishDescription = intent.getStringExtra("wish_description") ?: ""
                val priorityName = intent.getStringExtra("priority") ?: "MEDIUM"
                val priority = Priority.valueOf(priorityName)
                
                val wish = Wish(
                    id = wishId,
                    title = wishTitle,
                    description = wishDescription,
                    priority = priority
                )
                
                // Snooze for 1 hour
                val reminderSystem = ReminderSystem(context)
                val snoozeTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
                reminderSystem.scheduleCustomReminder(wish, snoozeTime)
                
                showActionConfirmation(context, "Snoozed for 1 hour ⏰")
            }
        }
    }
    
    private fun showActionConfirmation(context: Context, message: String) {
        val notification = NotificationCompat.Builder(context, ReminderSystem.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Action Completed")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setTimeoutAfter(3000) // Auto-dismiss after 3 seconds
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(999, notification)
            }
        } else {
            notificationManager.notify(999, notification)
        }
    }
}
