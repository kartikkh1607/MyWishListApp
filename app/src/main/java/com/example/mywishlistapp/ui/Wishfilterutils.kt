package com.example.mywishlistapp.ui

import androidx.compose.ui.graphics.Color
import com.example.mywishlistapp.Data.Priority

import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.ui.theme.AccentGreen
import com.example.mywishlistapp.ui.theme.AccentOrange
import com.example.mywishlistapp.ui.theme.AccentRed

// ─── Shared Priority Helpers ──────────────────────────────────────────────────

fun priorityEmoji(priority: Priority): String = when (priority) {
    Priority.HIGH -> "🔥"
    Priority.MEDIUM -> "⚡"
    Priority.LOW -> "🌱"
}

fun priorityColor(priority: Priority): Color = when (priority) {
    Priority.HIGH -> AccentRed
    Priority.MEDIUM -> AccentOrange
    Priority.LOW -> AccentGreen
}

// ─── Filter Utility ───────────────────────────────────────────────────────────

fun filterWishes(
    wishes: List<Wish>,
    query: String = "",
    selectedCategories: Set<String> = emptySet(),
    selectedPriority: Priority? = null
): List<Wish> {
    return wishes.filter { wish ->
        val matchesQuery = if (query.isBlank()) true else {
            wish.title.contains(query, ignoreCase = true) ||
                    wish.description.contains(query, ignoreCase = true) ||
                    wish.category.contains(query, ignoreCase = true) ||
                    wish.tags.any { it.contains(query, ignoreCase = true) }
        }
        val matchesCategory = selectedCategories.isEmpty() || wish.category in selectedCategories
        val matchesPriority = selectedPriority == null || wish.priority == selectedPriority

        matchesQuery && matchesCategory && matchesPriority
    }
}