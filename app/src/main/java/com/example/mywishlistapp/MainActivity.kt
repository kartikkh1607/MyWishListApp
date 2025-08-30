package com.example.mywishlistapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mywishlistapp.ui.theme.MyWishListAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d("MainActivity", "Starting MainActivity creation")
            
            // Enable edge-to-edge for modern UI
            enableEdgeToEdge()

            setContent {
                MyWishListAppTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainScreen()
                    }
                }
            }
            
            Log.d("MainActivity", "MainActivity created successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error creating MainActivity: ${e.message}", e)
            throw e // Re-throw to let the system handle it
        }
    }
}
