package com.example.mywishlistapp

import android.app.Application

class WishListApp: Application() {
    
    companion object {
        var isInitialized = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        try {
            android.util.Log.d("WishListApp", "Initializing application")
            android.util.Log.d("WishListApp", "App context: ${this::class.java.simpleName}")
            android.util.Log.d("WishListApp", "Database path: ${getDatabasePath("wishlist.db").absolutePath}")
            
            // Initialize graph with better error handling
            Graph.provide(this)
            isInitialized = true
            android.util.Log.d("WishListApp", "Application initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("WishListApp", "Failed to initialize application", e)
            android.util.Log.e("WishListApp", "Exception type: ${e::class.java.simpleName}")
            android.util.Log.e("WishListApp", "Exception message: ${e.message}")
            android.util.Log.e("WishListApp", "Stack trace:", e)
            
            // Try to recover by clearing app data if needed
            try {
                val dbFile = getDatabasePath("wishlist.db")
                if (dbFile.exists()) {
                    android.util.Log.w("WishListApp", "Attempting to delete corrupted database")
                    dbFile.delete()
                    // Try to reinitialize
                    Graph.provide(this)
                    isInitialized = true
                    android.util.Log.d("WishListApp", "Application recovered and initialized successfully")
                }
            } catch (recoveryException: Exception) {
                android.util.Log.e("WishListApp", "Failed to recover from initialization error", recoveryException)
                isInitialized = false
            }
        }
    }
}
