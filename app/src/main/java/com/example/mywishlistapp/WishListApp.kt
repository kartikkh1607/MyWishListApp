package com.example.mywishlistapp

import android.app.Application

class WishListApp: Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            android.util.Log.d("WishListApp", "Initializing application")
            Graph.provide(this)
            android.util.Log.d("WishListApp", "Application initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("WishListApp", "Failed to initialize application", e)
            // Don't rethrow - let the app continue with fallback behavior
        }
    }
}
