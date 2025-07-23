package com.example.mywishlistapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = query,
        onValueChange = { newValue ->
            if (newValue != query) {
                onQueryChanged(newValue)
            }
        },
        label = { Text("Search wishes") },
        placeholder = { Text("Type your wish...") },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                if (query.isNotEmpty()) {
                    onSearch()
                }
            }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Gray
        )
    )
}

@Composable
fun FilterChips(
    availableFilters: List<String>,
    selectedFilters: List<String>,
    onFilterSelected: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        availableFilters.forEach { filter ->
            val isSelected = selectedFilters.contains(filter)

            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter, !isSelected) },
                label = { Text(text = filter) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                    selectedLabelColor = Color.White,
                    containerColor = Color.Gray.copy(alpha = 0.1f),
                    labelColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    }
}

@Composable
fun SortOptions(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("Date Added", "Priority", "Alphabetical")
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Sort By: $selectedOption")
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SearchAndFilterView(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    availableFilters: List<String>,
    selectedFilters: List<String>,
    onFilterSelected: (String, Boolean) -> Unit,
    selectedSort: String,
    onSortSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        SearchBar(
            query = query,
            onQueryChanged = onQueryChanged,
            onSearch = onSearch
        )

        Spacer(modifier = Modifier.height(8.dp))

        FilterChips(
            availableFilters = availableFilters,
            selectedFilters = selectedFilters,
            onFilterSelected = onFilterSelected
        )

        Spacer(modifier = Modifier.height(8.dp))

        SortOptions(
            selectedOption = selectedSort,
            onOptionSelected = onSortSelected
        )
    }
}
