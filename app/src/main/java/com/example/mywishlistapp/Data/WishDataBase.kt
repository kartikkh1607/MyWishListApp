package com.example.mywishlistapp.Data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Wish::class],
    version = 13,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WishDataBase : RoomDatabase() {
    abstract fun wishDao(): WishDao
}
