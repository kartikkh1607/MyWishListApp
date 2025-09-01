package com.example.mywishlistapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mywishlistapp.ui.theme.MyWishListAppTheme
import com.example.mywishlistapp.utils.CrashReporter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            CrashReporter.logDebug("MainActivity", "Starting MainActivity creation")
            CrashReporter.setCustomKey("activity_lifecycle", "onCreate_start")
            
            // Log device information
            CrashReporter.logDebug("MainActivity", "Android Version: ${android.os.Build.VERSION.RELEASE}")
            CrashReporter.logDebug("MainActivity", "API Level: ${android.os.Build.VERSION.SDK_INT}")
            CrashReporter.logDebug("MainActivity", "Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
            CrashReporter.logDebug("MainActivity", "Application: ${applicationInfo.packageName}")
            
            // Check if Application is properly initialized
            val app = application as? WishListApp
            if (app == null) {
                CrashReporter.logError("MainActivity", "Application is not WishListApp instance")
                throw IllegalStateException("Application is not properly configured")
            }
            
            if (!WishListApp.isInitialized) {
                CrashReporter.logWarning("MainActivity", "WishListApp not initialized, attempting to initialize...")
                // Give it a moment to initialize
                Thread.sleep(100)
                if (!WishListApp.isInitialized) {
                    CrashReporter.logError("MainActivity", "WishListApp failed to initialize")
                }
            }
            
            CrashReporter.setCustomKey("activity_lifecycle", "enableEdgeToEdge")
            // Enable edge-to-edge for modern UI
            enableEdgeToEdge()
            CrashReporter.logDebug("MainActivity", "Edge-to-edge enabled")

            CrashReporter.setCustomKey("activity_lifecycle", "setContent")
            setContent {
                CrashReporter.logDebug("MainActivity", "Setting content with MyWishListAppTheme")
                val wishViewModel: WishViewModel = viewModel()
                val currentTheme by wishViewModel.currentTheme.collectAsState()
                val isSystemInDarkTheme = isSystemInDarkTheme()
                
                val darkTheme = when (currentTheme) {
                    "Dark" -> true
                    "Light" -> false
                    else -> isSystemInDarkTheme // "System" or any other value uses system default
                }
                
                MyWishListAppTheme(darkTheme = darkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        CrashReporter.logDebug("MainActivity", "Creating MainScreen")
                        MainScreen()
                    }
                }
            }
            
            CrashReporter.setCustomKey("activity_lifecycle", "onCreate_complete")
            CrashReporter.logDebug("MainActivity", "MainActivity created successfully")
        } catch (e: Exception) {
            CrashReporter.logError("MainActivity", "Error creating MainActivity: ${e.message}", e)
            CrashReporter.reportUIError("MainActivity", e)
            throw e // Re-throw to let the system handle it
        }
    }
}
