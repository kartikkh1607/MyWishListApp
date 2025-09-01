package com.example.mywishlistapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinimalDashboardScreen(navController: NavHostController, viewModel: WishViewModel) {
    Log.d("MinimalDashboard", "Starting MinimalDashboardScreen")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "✅ Dashboard Base Working!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Test accessing viewModel properties one by one
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Testing ViewModel Access:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Test 1: Basic ViewModel access
                Log.d("MinimalDashboard", "Testing basic ViewModel access...")
                Text(
                    "✅ ViewModel accessed successfully",
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Test 2: getAllWishes access
                Log.d("MinimalDashboard", "Testing getAllWishes access...")
                val wishList = viewModel.getAllWishes.collectAsState(initial = emptyList())
                Text(
                    "✅ getAllWishes: ${wishList.value.size} wishes",
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Test 3: userProfile access
                Log.d("MinimalDashboard", "Testing userProfile access...")
                val userProfile by viewModel.userProfile.collectAsState()
                Text(
                    "✅ UserProfile: ${userProfile.username}",
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Test 4: getUnreadNotificationCount access
                Log.d("MinimalDashboard", "Testing getUnreadNotificationCount access...")
                val unreadCount by viewModel.getUnreadNotificationCount().collectAsState()
                Text(
                    "✅ Unread notifications: $unreadCount",
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
