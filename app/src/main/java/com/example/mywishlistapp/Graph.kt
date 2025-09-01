package com.example.mywishlistapp

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mywishlistapp.Data.WishDataBase
import com.example.mywishlistapp.Data.WishRepository
import com.example.mywishlistapp.Data.UserProfileRepository

object Graph {
    
    lateinit var database: WishDataBase
        private set
    
    val wishRepository by lazy {
        if (!::database.isInitialized) {
            throw IllegalStateException("Graph.provide() must be called before accessing repositories")
        }
        WishRepository(wishDao = database.wishDao())
    }
    
    val userProfileRepository by lazy {
        if (!::database.isInitialized) {
            throw IllegalStateException("Graph.provide() must be called before accessing repositories")
        }
        UserProfileRepository(userProfileDao = database.userProfileDao())
    }
    
    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create user_profile table with all required columns
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS user_profile (
                    id INTEGER PRIMARY KEY NOT NULL,
                    username TEXT NOT NULL,
                    name TEXT NOT NULL DEFAULT '',
                    total_wishes INTEGER NOT NULL,
                    completed_wishes INTEGER NOT NULL,
                    high_priority_completed INTEGER NOT NULL,
                    current_streak INTEGER NOT NULL,
                    longest_streak INTEGER NOT NULL,
                    total_money_saved REAL NOT NULL,
                    level INTEGER NOT NULL,
                    experience_points INTEGER NOT NULL,
                    earned_badges TEXT NOT NULL,
                    profile_created_date INTEGER NOT NULL,
                    daily_challenges_completed INTEGER NOT NULL DEFAULT 0,
                    weekly_challenges_completed INTEGER NOT NULL DEFAULT 0,
                    last_login_date INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()},
                    login_streak INTEGER NOT NULL DEFAULT 0,
                    total_categories_explored INTEGER NOT NULL DEFAULT 0,
                    wishes_shared INTEGER NOT NULL DEFAULT 0,
                    personal_best_score INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())
            
            // Insert default profile with all columns
            database.execSQL("""
                INSERT INTO user_profile 
                (id, username, name, total_wishes, completed_wishes, high_priority_completed, 
                 current_streak, longest_streak, total_money_saved, level, experience_points, 
                 earned_badges, profile_created_date, daily_challenges_completed, 
                 weekly_challenges_completed, last_login_date, login_streak, 
                 total_categories_explored, wishes_shared, personal_best_score) 
                VALUES (1, 'User', '', 0, 0, 0, 0, 0, 0.0, 1, 0, '[]', ${System.currentTimeMillis()}, 
                        0, 0, ${System.currentTimeMillis()}, 0, 0, 0, 0)
            """.trimIndent())
        }
    }
    
    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add savedAmount column to Wish table
            database.execSQL("""
                ALTER TABLE `Wish-Table` ADD COLUMN `wish-saved-amount` REAL NOT NULL DEFAULT 0.0
            """.trimIndent())
        }
    }
    
    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add name column to user_profile table
            database.execSQL("""
                ALTER TABLE user_profile ADD COLUMN name TEXT NOT NULL DEFAULT ''
            """.trimIndent())
        }
    }
    
    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add Personal Growth Companion fields to Wish table
            database.execSQL("""
                ALTER TABLE `Wish-Table` ADD COLUMN `wish-is-goal` INTEGER NOT NULL DEFAULT 0
            """.trimIndent())
            
            database.execSQL("""
                ALTER TABLE `Wish-Table` ADD COLUMN `wish-target-date` INTEGER
            """.trimIndent())
            
            database.execSQL("""
                ALTER TABLE `Wish-Table` ADD COLUMN `wish-progress` INTEGER NOT NULL DEFAULT 0
            """.trimIndent())
        }
    }
    
    fun provide(context: Context){
        try {
            android.util.Log.d("Graph", "Starting database initialization")
            database = Room.databaseBuilder(context, WishDataBase::class.java, "wishlist.db")
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                .fallbackToDestructiveMigration() // Add fallback for migration issues
                .setJournalMode(androidx.room.RoomDatabase.JournalMode.TRUNCATE) // Better compatibility with some devices
                .build()
            android.util.Log.d("Graph", "Database initialized successfully")
            
            // Test database connection immediately
            try {
                database.openHelper.readableDatabase
                android.util.Log.d("Graph", "Database connection test successful")
            } catch (e: Exception) {
                android.util.Log.e("Graph", "Database connection test failed", e)
                throw e
            }
        } catch (e: Exception) {
            android.util.Log.e("Graph", "Failed to initialize database", e)
            throw e
        }
    }
}
