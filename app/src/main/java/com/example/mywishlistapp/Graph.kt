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
    
    val wishRepository by lazy {
        WishRepository(wishDao = database.wishDao())
    }
    
    val userProfileRepository by lazy {
        UserProfileRepository(userProfileDao = database.userProfileDao())
    }
    
    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create user_profile table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS user_profile (
                    id INTEGER PRIMARY KEY NOT NULL,
                    username TEXT NOT NULL,
                    total_wishes INTEGER NOT NULL,
                    completed_wishes INTEGER NOT NULL,
                    high_priority_completed INTEGER NOT NULL,
                    current_streak INTEGER NOT NULL,
                    longest_streak INTEGER NOT NULL,
                    total_money_saved REAL NOT NULL,
                    level INTEGER NOT NULL,
                    experience_points INTEGER NOT NULL,
                    earned_badges TEXT NOT NULL,
                    profile_created_date INTEGER NOT NULL
                )
            """.trimIndent())
            
            // Insert default profile
            database.execSQL("""
                INSERT INTO user_profile 
                (id, username, total_wishes, completed_wishes, high_priority_completed, 
                 current_streak, longest_streak, total_money_saved, level, experience_points, 
                 earned_badges, profile_created_date) 
                VALUES (1, 'User', 0, 0, 0, 0, 0, 0.0, 1, 0, '[]', ${System.currentTimeMillis()})
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
    
    fun provide(context: Context){
        database = Room.databaseBuilder(context, WishDataBase::class.java, "wishlist.db")
            .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
            .build()
    }
}
