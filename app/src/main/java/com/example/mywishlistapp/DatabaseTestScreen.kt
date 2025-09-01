package com.example.mywishlistapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseTestScreen() {
    var testResults by remember { mutableStateOf(listOf<String>()) }
    var currentTest by remember { mutableStateOf("Starting...") }
    
    LaunchedEffect(Unit) {
        try {
            currentTest = "Step 1: Checking Graph database initialization..."
            Log.d("DatabaseTest", currentTest)
            
            try {
                val database = Graph.database
                testResults = testResults + "✅ Graph.database is accessible"
                Log.d("DatabaseTest", "✅ Graph.database is accessible")
            } catch (e: Exception) {
                testResults = testResults + "❌ Graph.database is NOT initialized: ${e.message}"
                Log.e("DatabaseTest", "❌ Graph.database is NOT initialized", e)
                return@LaunchedEffect
            }
            
            currentTest = "Step 2: Accessing wishDao..."
            Log.d("DatabaseTest", currentTest)
            
            val wishDao = Graph.database.wishDao()
            testResults = testResults + "✅ WishDao obtained successfully"
            Log.d("DatabaseTest", "✅ WishDao obtained successfully")
            
            currentTest = "Step 3: Creating WishRepository..."
            Log.d("DatabaseTest", currentTest)
            
            val repository = Graph.wishRepository
            testResults = testResults + "✅ WishRepository created successfully"
            Log.d("DatabaseTest", "✅ WishRepository created successfully")
            
            currentTest = "Step 4: Accessing getWishes() flow..."
            Log.d("DatabaseTest", currentTest)
            
            val wishesFlow = repository.getWishes()
            testResults = testResults + "✅ getWishes() flow obtained"
            Log.d("DatabaseTest", "✅ getWishes() flow obtained")
            
            currentTest = "Step 5: Collecting from flow (this is where crash likely occurs)..."
            Log.d("DatabaseTest", currentTest)
            
            val wishes = wishesFlow.first()
            testResults = testResults + "✅ Successfully collected wishes: ${wishes.size} items"
            Log.d("DatabaseTest", "✅ Successfully collected wishes: ${wishes.size} items")
            
            currentTest = "All tests completed successfully!"
            
        } catch (e: Exception) {
            val errorMsg = "❌ CRASH at: $currentTest - ${e.message}"
            testResults = testResults + errorMsg
            Log.e("DatabaseTest", errorMsg, e)
            currentTest = "Test failed - see logs for details"
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "🔍 Database Component Test",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Current: $currentTest",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Test Results:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                testResults.forEach { result ->
                    Text(
                        text = result,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                
                if (testResults.isEmpty()) {
                    Text(
                        "Running tests...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
