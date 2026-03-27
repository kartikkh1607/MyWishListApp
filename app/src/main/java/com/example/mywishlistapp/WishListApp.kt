package com.example.mywishlistapp

import android.app.Application
import androidx.room.Room
import com.example.mywishlistapp.Data.WishDataBase
import com.example.mywishlistapp.Data.WishRepository

class WishListApp : Application() {

    lateinit var wishRepository: WishRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = Room.databaseBuilder(this, WishDataBase::class.java, "wishlist.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
        wishRepository = WishRepository(db.wishDao())
    }
}
