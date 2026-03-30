package com.example.mywishlistapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.ui.WishViewModel
import com.example.mywishlistapp.ui.filterWishes
import com.example.mywishlistapp.ui.priorityColor
import com.example.mywishlistapp.ui.priorityEmoji

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: WishViewModel
) {
    val allWishes by viewModel.getAllWishes.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
    var selectedPriority by remember { mutableStateOf<Priority?>(null) }

    val filteredWishes = remember(searchQuery, selectedCategories, selectedPriority, allWishes) {
        filterWishes(
            wishes = allWishes,
            query = searchQuery,
            selectedCategories = selectedCategories,
            selectedPriority = selectedPriority
        )
    }

    // Show a back arrow only when SearchScreen was pushed on top of another
    // destination (e.g., tapping the search icon on the Dashboard). When it is
    // reached via the bottom-nav tab there is no previous entry so the arrow hides.
    val canNavigateBack = navController.previousBackStackEntry != null
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Search Wishes", fontWeight = FontWeight.Bold) },
                windowInsets = WindowInsets(0.dp),
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
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
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            ModernSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onFilterClick = { showFilters = !showFilters }
            )

            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                SearchFilters(
                    selectedCategories = selectedCategories,
                    onCategoriesChange = { selectedCategories = it },
                    selectedPriority = selectedPriority,
                    onPriorityChange = { selectedPriority = it },
                    availableCategories = allWishes.map { it.category }.distinct()
                        .filter { it.isNotEmpty() }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val hasActiveFilter = searchQuery.isNotEmpty() ||
                        selectedCategories.isNotEmpty() ||
                        selectedPriority != null

                if (filteredWishes.isEmpty() && hasActiveFilter) {
                    item {
                        EmptySearchResults(
                            query = searchQuery,
                            onClearFilters = {
                                searchQuery = ""
                                selectedCategories = emptySet()
                                selectedPriority = null
                            }
                        )
                    }
                } else {
                    item {
                        SearchResultsHeader(count = filteredWishes.size, query = searchQuery)
                    }
                    itemsIndexed(filteredWishes, key = { _, wish -> wish.id }) { _, wish ->
                        SwipeToDeleteWishCard(
                            modifier = Modifier.animateItem(),
                            wish = wish,
                            onWishClick = {
                                navController.navigate(Screen.AddScreen(id = wish.id)) {
                                    launchSingleTop = true
                                }
                            },
                            onDelete = {
                                viewModel.deleteWish(wish)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Wish deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.addWish(wish)
                                    }
                                }
                            },
                            viewModel = viewModel
                        )
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
    modifier: Modifier = Modifier
) {
    var isSearchActive by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
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
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            text = "Search your wishes...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { isSearchActive = it.isFocused },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onFilterClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filters",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchFilters(
    selectedCategories: Set<String>,
    onCategoriesChange: (Set<String>) -> Unit,
    selectedPriority: Priority?,
    onPriorityChange: (Priority?) -> Unit,
    availableCategories: List<String>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                color = MaterialTheme.colorScheme.onSurface
            )

            if (availableCategories.isNotEmpty()) {
                Text(
                    text = "Categories",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    availableCategories.forEach { category ->
                        val isSelected = category in selectedCategories
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onCategoriesChange(
                                    if (isSelected) selectedCategories - category
                                    else selectedCategories + category
                                )
                            },
                            label = { Text(category, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.2f
                                ),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            Text(
                text = "Priority",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Priority.entries.forEach { priority ->
                    val isSelected = priority == selectedPriority
                    val pColor = priorityColor(priority)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            // tap same chip → deselect; tap different → select only that one
                            onPriorityChange(if (isSelected) null else priority)
                        },
                        label = {
                            Text(
                                "${priorityEmoji(priority)} ${priority.name}",
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = pColor.copy(alpha = 0.2f),
                            selectedLabelColor = pColor
                        )
                    )
                }
            }

            if (selectedCategories.isNotEmpty() || selectedPriority != null) {
                OutlinedButton(
                    onClick = { onCategoriesChange(emptySet()); onPriorityChange(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear All Filters")
                }
            }
        }
    }
}

@Composable
fun EmptySearchResults(query: String, onClearFilters: () -> Unit) {
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
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (query.isNotEmpty()) "No wishes match \"$query\""
            else "Try adjusting your filters",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onClearFilters) {
            Text("Clear filters")
        }
    }
}

@Composable
fun SearchResultsHeader(count: Int, query: String) {
    Text(
        text = if (query.isNotEmpty()) {
            "$count result${if (count != 1) "s" else ""} for \"$query\""
        } else {
            "$count wish${if (count != 1) "es" else ""} found"
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

