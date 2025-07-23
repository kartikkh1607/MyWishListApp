package com.example.mywishlistapp

import android.content.Context
import androidx.room.Room
import com.example.mywishlistapp.Data.WishDataBase
import com.example.mywishlistapp.Data.WishRepository

object Graph {

    lateinit var dataBase: WishDataBase

    val wishRepository by lazy {
        WishRepository(wishDao = dataBase.wishDao())
    }

    fun provide(context: Context){
        dataBase = Room.databaseBuilder(context , WishDataBase::class.java , "wishList.db")
            .fallbackToDestructiveMigration()
            .build()
    }

}