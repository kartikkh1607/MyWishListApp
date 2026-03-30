package com.example.mywishlistapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayModeContent(wish: Wish) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            DisplayCard(
                title = stringResource(R.string.basic_information),
                icon = Icons.Default.Info
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DisplayField(label = stringResource(R.string.title), value = wish.title)
                    DisplayField(
                        label = stringResource(R.string.description),
                        value = wish.description
                    )
                    if (wish.price.isNotBlank()) {
                        DisplayField(label = "Price", value = "₹${wish.price}")
                    }
                }
            }
        }

        item {
            DisplayCard(
                title = stringResource(R.string.categories_and_tags),
                icon = Icons.AutoMirrored.Filled.Label
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DisplayField(
                        label = stringResource(R.string.category),
                        value = wish.category.ifEmpty { "No category" })
                    if (wish.tags.isNotEmpty()) DisplayTagsField(tags = wish.tags)
                }
            }
        }

        item {
            DisplayCard(title = stringResource(R.string.priority), icon = Icons.Default.Star) {
                PriorityDisplayChip(priority = wish.priority)
            }
        }

        item {
            DisplayCard(title = "Status", icon = Icons.Default.CheckCircle) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (wish.isCompleted) "✅ Completed" else "⏳ Pending",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (wish.isCompleted) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
