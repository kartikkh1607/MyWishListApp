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
fun TestDashboardMainScreen() {
    LaunchedEffect(Unit) {
        Log.d("TestDashboardMainScreen", "Creating TestDashboardMainScreen")
    }
    
    val context = LocalContext.current
    val viewModel: WishViewModel = viewModel(
        factory = WishViewModelFactory(context.applicationContext as android.app.Application)
    )
    val navController = rememberNavController()
    
    LaunchedEffect(viewModel, navController) {
        Log.d("TestDashboardMainScreen", "ViewModel and NavController created successfully")
    }

    // Test specifically the DashboardScreen that crashes
    NavHost(
        navController = navController,
        startDestination = "dashboard_screen"
    ) {
        composable("dashboard_screen") {
            Log.d("TestDashboardMainScreen", "Attempting to create MinimalDashboardScreen")
            MinimalDashboardScreen(navController = navController, viewModel = viewModel)
            Log.d("TestDashboardMainScreen", "MinimalDashboardScreen created successfully")
        }
    }
}
