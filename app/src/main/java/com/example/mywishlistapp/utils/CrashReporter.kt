package com.example.mywishlistapp.utils

import android.util.Log

/**
 * Centralized crash reporting utility
 * Provides logging and crash reporting functionality
 */
object CrashReporter {
    
    private const val TAG = "WishListApp"
    
    /**
     * Log an error with context
     */
    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        
        // If Firebase Crashlytics is available, report the error
        try {
            // Firebase Crashlytics integration (if available)
            // FirebaseCrashlytics.getInstance().recordException(throwable ?: Exception(message))
            // FirebaseCrashlytics.getInstance().log("$tag: $message")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Crashlytics not available: ${e.message}")
        }
    }
    
    /**
     * Log a warning
     */
    fun logWarning(tag: String, message: String) {
        Log.w(tag, message)
        
        try {
            // Firebase Crashlytics integration (if available)
            // FirebaseCrashlytics.getInstance().log("WARNING - $tag: $message")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Crashlytics not available: ${e.message}")
        }
    }
    
    /**
     * Log debug information
     */
    fun logDebug(tag: String, message: String) {
        Log.d(tag, message)
    }
    
    /**
     * Log info
     */
    fun logInfo(tag: String, message: String) {
        Log.i(tag, message)
        
        try {
            // Firebase Crashlytics integration (if available)
            // FirebaseCrashlytics.getInstance().log("INFO - $tag: $message")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Crashlytics not available: ${e.message}")
        }
    }
    
    /**
     * Record a non-fatal exception
     */
    fun recordNonFatalException(throwable: Throwable) {
        Log.e(TAG, "Non-fatal exception recorded", throwable)
        
        try {
            // Firebase Crashlytics integration (if available)
            // FirebaseCrashlytics.getInstance().recordException(throwable)
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Crashlytics not available: ${e.message}")
        }
    }
    
    /**
     * Set user identifier for crash reports
     */
    fun setUserId(userId: String) {
        try {
            // Firebase Crashlytics integration (if available)
            // FirebaseCrashlytics.getInstance().setUserId(userId)
            Log.i(TAG, "User ID set for crash reporting: $userId")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Crashlytics not available: ${e.message}")
        }
    }
    
    /**
     * Set custom key-value pairs for crash reports
     */
    fun setCustomKey(key: String, value: String) {
        try {
            // Firebase Crashlytics integration (if available)
            // FirebaseCrashlytics.getInstance().setCustomKey(key, value)
            Log.i(TAG, "Custom key set: $key = $value")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Crashlytics not available: ${e.message}")
        }
    }
    
    /**
     * Database operation error reporting
     */
    fun reportDatabaseError(operation: String, table: String, error: Throwable) {
        val message = "Database operation failed: $operation on $table"
        logError("DatabaseError", message, error)
        setCustomKey("db_operation", operation)
        setCustomKey("db_table", table)
        recordNonFatalException(error)
    }
    
    /**
     * Network operation error reporting
     */
    fun reportNetworkError(operation: String, url: String, error: Throwable) {
        val message = "Network operation failed: $operation for $url"
        logError("NetworkError", message, error)
        setCustomKey("network_operation", operation)
        setCustomKey("network_url", url)
        recordNonFatalException(error)
    }
    
    /**
     * UI error reporting
     */
    fun reportUIError(screen: String, error: Throwable) {
        val message = "UI error on screen: $screen"
        logError("UIError", message, error)
        setCustomKey("ui_screen", screen)
        recordNonFatalException(error)
    }
}