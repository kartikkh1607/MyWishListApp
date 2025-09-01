package com.example.mywishlistapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.R
import java.util.concurrent.TimeUnit

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
                val priority = try {
                    Priority.valueOf(priorityName)
                } catch (e: IllegalArgumentException) {
                    Priority.MEDIUM
                }
                
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
