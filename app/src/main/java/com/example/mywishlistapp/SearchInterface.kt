package com.example.mywishlistapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.ui.theme.*
import java.lang.Thread.activeCount

data class SearchFilters(
    val categories: Set<String> = emptySet(),
    val priorities: Set<Priority> = emptySet(),
    val tags: Set<String> = emptySet()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search wishes..."
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = PrimaryPurple,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = TextTertiary
                    )
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = PrimaryPurple
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) color else Color.White,
        label = "chip_bg"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else color,
        label = "chip_content"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        label = "chip_scale"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = if (!isSelected) {
            CardDefaults.outlinedCardBorder()
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FilterSection(
    filters: SearchFilters,
    onFiltersChange: (SearchFilters) -> Unit,
    availableCategories: List<String>,
    availableTags: List<String>,
    modifier: Modifier = Modifier
) {
    var showFilters by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        // Filter Toggle Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { showFilters = !showFilters },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filters",
                    tint = PrimaryPurple
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Filters",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            
            // Active filter count
            val activeCount = filters.categories.size + filters.priorities.size + filters.tags.size
            if (activeCount > 0) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryPurple
                    )
                ) {
                    Text(
                        text = activeCount.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Expandable Filter Content
        AnimatedVisibility(
            visible = showFilters,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Categories
                if (availableCategories.isNotEmpty()) {
                    Text(
                        text = "Categories",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(availableCategories) { category ->
                            FilterChip(
                                text = category,
                                isSelected = category in filters.categories,
                                onClick = {
                                    val newCategories = if (category in filters.categories) {
                                        filters.categories - category
                                    } else {
                                        filters.categories + category
                                    }
                                    onFiltersChange(filters.copy(categories = newCategories))
                                },
                                color = getCategoryColor(category)
                            )
                        }
                    }
                }
                
                // Priorities
                Text(
                    text = "Priority",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(Priority.values().toList()) { priority ->
                        FilterChip(
                            text = "${getPriorityEmoji(priority)} ${priority.name}",
                            isSelected = priority in filters.priorities,
                            onClick = {
                                val newPriorities = if (priority in filters.priorities) {
                                    filters.priorities - priority
                                } else {
                                    filters.priorities + priority
                                }
                                onFiltersChange(filters.copy(priorities = newPriorities))
                            },
                            color = getPriorityColor(priority)
                        )
                    }
                }
                
                // Tags
                if (availableTags.isNotEmpty()) {
                    Text(
                        text = "Tags",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(availableTags.take(10)) { tag ->
                            FilterChip(
                                text = "#$tag",
                                isSelected = tag in filters.tags,
                                onClick = {
                                    val newTags = if (tag in filters.tags) {
                                        filters.tags - tag
                                    } else {
                                        filters.tags + tag
                                    }
                                    onFiltersChange(filters.copy(tags = newTags))
                                },
                                color = SoftBlue
                            )
                        }
                    }
                }
                
                // Clear Filters Button
                if (activeCount() > 0) {
                    OutlinedButton(
                        onClick = {
                            onFiltersChange(SearchFilters())
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear All Filters")
                    }
                }
            }
        }
    }
}

fun searchWishes(
    wishes: List<Wish>,
    query: String,
    filters: SearchFilters
): List<Wish> {
    return wishes.filter { wish ->
        // Text search
        val matchesQuery = if (query.isBlank()) {
            true
        } else {
            wish.title.contains(query, ignoreCase = true) ||
            wish.description.contains(query, ignoreCase = true) ||
            wish.tags.any { it.contains(query, ignoreCase = true) }
        }
        
        // Category filter
        val matchesCategory = filters.categories.isEmpty() || wish.category in filters.categories
        
        // Priority filter
        val matchesPriority = filters.priorities.isEmpty() || wish.priority in filters.priorities
        
        // Tags filter
        val matchesTags = filters.tags.isEmpty() || filters.tags.any { it in wish.tags }
        
        matchesQuery && matchesCategory && matchesPriority && matchesTags
    }
}

private fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "electronics" -> ElectronicsBlue
        "travel" -> TravelTeal
        "food" -> FoodOrange
        "books" -> BooksAmber
        "sports" -> SportsGreen
        "fashion" -> FashionPink
        "home" -> HomeIndigo
        "music" -> MusicPurple
        else -> PrimaryPurple
    }
}

private fun getPriorityColor(priority: Priority): Color {
    return when (priority) {
        Priority.HIGH -> ErrorRed
        Priority.MEDIUM -> WarningAmber
        Priority.LOW -> SuccessGreen
    }
}

private fun getPriorityEmoji(priority: Priority): String {
    return when (priority) {
        Priority.HIGH -> "🔥"
        Priority.MEDIUM -> "⚡"
        Priority.LOW -> "🌱"
    }
}
