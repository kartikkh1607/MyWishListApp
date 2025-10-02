package com.example.mywishlistapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.ui.components.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import com.example.mywishlistapp.AppBarView
import com.example.mywishlistapp.Screen
import com.example.mywishlistapp.WishViewModel

@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: WishViewModel
) {
    val context = LocalContext.current
    val allWishes = viewModel.getAllWishes.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
    var selectedPriorities by remember { mutableStateOf(setOf<Priority>()) }
    
    // Filter wishes based on search query and filters
    val filteredWishes = remember(searchQuery, selectedCategories, selectedPriorities, allWishes.value) {
        allWishes.value.filter { wish ->
            val matchesQuery = if (searchQuery.isBlank()) {
                true
            } else {
                wish.title.contains(searchQuery, ignoreCase = true) ||
                wish.description.contains(searchQuery, ignoreCase = true) ||
                wish.category.contains(searchQuery, ignoreCase = true) ||
                wish.tags.any { it.contains(searchQuery, ignoreCase = true) }
            }
            
            val matchesCategory = selectedCategories.isEmpty() || wish.category in selectedCategories
            val matchesPriority = selectedPriorities.isEmpty() || wish.priority in selectedPriorities
            
            matchesQuery && matchesCategory && matchesPriority
        }
    }
    
    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            AppBarView(
                title = "Search Wishes",
                onBackNavClicked = { navController.navigateUp() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF0F4FF),
                            Color(0xFFE8F0FE),
                            Color(0xFFF8FAFF)
                        )
                    )
                )
        ) {
            // Search Bar
            ModernSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onFilterClick = { showFilters = !showFilters }
            )
            
            // Filters
            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                SearchFilters(
                    selectedCategories = selectedCategories,
                    onCategoriesChange = { selectedCategories = it },
                    selectedPriorities = selectedPriorities,
                    onPrioritiesChange = { selectedPriorities = it },
                    availableCategories = allWishes.value.map { it.category }.distinct().filter { it.isNotEmpty() }
                )
            }
            
            // Results
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredWishes.isEmpty() && (searchQuery.isNotEmpty() || selectedCategories.isNotEmpty() || selectedPriorities.isNotEmpty())) {
                    item {
                        EmptySearchResults(
                            query = searchQuery,
                            onClearFilters = {
                                searchQuery = ""
                                selectedCategories = emptySet()
                                selectedPriorities = emptySet()
                            }
                        )
                    }
                } else {
                    item {
                        SearchResultsHeader(
                            count = filteredWishes.size,
                            query = searchQuery
                        )
                    }
                    
                    items(filteredWishes, key = { it.id }) { wish ->
                        WishItemECommerce(wish = wish) {
                            navController.navigate(Screen.AddScreen.route + "/${wish.id}")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
    suggestions: List<String> = emptyList()
) {
    var isSearchActive by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }
    
    Column(modifier = modifier.fillMaxWidth()) {
        // Enhanced Search Bar with Animation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isSearchActive) 12.dp else 8.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated search icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Color(0xFF667EEA).copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF667EEA),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Enhanced text field with focus handling
                OutlinedTextField(
                    value = query,
                    onValueChange = { newQuery ->
                        onQueryChange(newQuery)
                        showSuggestions = newQuery.isNotEmpty() && suggestions.isNotEmpty()
                    },
                    placeholder = {
                        BreathingAnimation {
                            Text(
                                text = "Search your wishes...",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            isSearchActive = focusState.isFocused
                            showSuggestions = focusState.isFocused && 
                                    query.isNotEmpty() && suggestions.isNotEmpty()
                        },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color(0xFF667EEA)
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                
                // Animated clear button
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    EnhancedBounceAnimation {
                        IconButton(
                            onClick = { 
                                onQueryChange("")
                                showSuggestions = false
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                
                // Animated filter button
                EnhancedBounceAnimation {
                    IconButton(
                        onClick = onFilterClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    Color(0xFF667EEA).copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint = Color(0xFF667EEA),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Auto-complete suggestions
        AnimatedVisibility(
            visible = showSuggestions,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column {
                    suggestions.take(5).forEachIndexed { index, suggestion ->
                        SlideInListItem(index = index) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onQueryChange(suggestion)
                                        showSuggestions = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = suggestion,
                                    fontSize = 14.sp,
                                    color = Color(0xFF2C3E50),
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Icon(
                                    imageVector = Icons.Default.NorthWest,
                                    contentDescription = "Use suggestion",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        if (index < suggestions.size - 1 && index < 4) {
                            HorizontalDivider(
                                color = Color(0xFFF1F5F9),
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchFilters(
    selectedCategories: Set<String>,
    onCategoriesChange: (Set<String>) -> Unit,
    selectedPriorities: Set<Priority>,
    onPrioritiesChange: (Set<Priority>) -> Unit,
    availableCategories: List<String>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filters",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
            
            // Categories Filter
            if (availableCategories.isNotEmpty()) {
                Text(
                    text = "Categories",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
                
                FilterChipRow(
                    items = availableCategories,
                    selectedItems = selectedCategories,
                    onSelectionChange = onCategoriesChange,
                    itemColor = { Color(0xFF667EEA) }
                )
            }
            
            // Priority Filter
            Text(
                text = "Priority",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B)
            )
            
            FilterChipRow(
                items = Priority.values().toList(),
                selectedItems = selectedPriorities,
                onSelectionChange = onPrioritiesChange,
                itemDisplayName = { "${getPriorityEmoji(it)} ${it.name}" },
                itemColor = { getPriorityColor(it) }
            )
            
            // Clear Filters
            if (selectedCategories.isNotEmpty() || selectedPriorities.isNotEmpty()) {
                OutlinedButton(
                    onClick = {
                        onCategoriesChange(emptySet())
                        onPrioritiesChange(emptySet())
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear All Filters")
                }
            }
        }
    }
}

@Composable
fun <T> FilterChipRow(
    items: List<T>,
    selectedItems: Set<T>,
    onSelectionChange: (Set<T>) -> Unit,
    itemDisplayName: (T) -> String = { it.toString() },
    itemColor: (T) -> Color = { Color(0xFF667EEA) }
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEach { item ->
            val isSelected = item in selectedItems
            FilterChip(
                selected = isSelected,
                onClick = {
                    val newSelection = if (isSelected) {
                        selectedItems - item
                    } else {
                        selectedItems + item
                    }
                    onSelectionChange(newSelection)
                },
                label = {
                    Text(
                        text = itemDisplayName(item),
                        fontSize = 12.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = itemColor(item).copy(alpha = 0.2f),
                    selectedLabelColor = itemColor(item)
                )
            )
        }
    }
}

@Composable
fun EmptySearchResults(
    query: String,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No wishes found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (query.isNotEmpty()) {
                "No wishes match \"$query\""
            } else {
                "Try adjusting your filters"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onClearFilters) {
            Text("Clear filters")
        }
    }
}

@Composable
fun SearchResultsHeader(
    count: Int,
    query: String
) {
    Text(
        text = if (query.isNotEmpty()) {
            "$count result${if (count != 1) "s" else ""} for \"$query\""
        } else {
            "$count wish${if (count != 1) "es" else ""} found"
        },
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF64748B),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

private fun getPriorityEmoji(priority: Priority): String {
    return when (priority) {
        Priority.HIGH -> "🔥"
        Priority.MEDIUM -> "⚡"
        Priority.LOW -> "🌱"
    }
}

private fun getPriorityColor(priority: Priority): Color {
    return when (priority) {
        Priority.HIGH -> Color(0xFFEF4444)
        Priority.MEDIUM -> Color(0xFFF59E0B)
        Priority.LOW -> Color(0xFF10B981)
    }
}
