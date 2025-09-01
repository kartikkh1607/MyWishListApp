package com.example.mywishlistapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleMainScreen() {
    LaunchedEffect(Unit) {
        Log.d("SimpleMainScreen", "Creating SimpleMainScreen")
    }
    
    val context = LocalContext.current
    val viewModel: WishViewModel = viewModel(
        factory = WishViewModelFactory(context.applicationContext as android.app.Application)
    )
    val navController = rememberNavController()
    
    LaunchedEffect(viewModel, navController) {
        Log.d("SimpleMainScreen", "ViewModel and NavController created successfully")
    }

    // Test just the navigation setup without complex UI
    NavHost(
        navController = navController,
        startDestination = "simple_screen"
    ) {
        composable("simple_screen") {
            // Test the full DashboardScreen to find the crashing component
            Log.d("SimpleMainScreen", "Testing FULL DashboardScreen")
            DashboardScreen(navController = navController, viewModel = viewModel)
        }
    }
}
