package com.example.mywishlistapp

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WishViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WishViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WishViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
