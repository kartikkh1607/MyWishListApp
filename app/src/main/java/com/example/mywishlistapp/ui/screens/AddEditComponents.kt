package com.example.mywishlistapp.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.R
import com.example.mywishlistapp.ui.WishFormState
import com.example.mywishlistapp.ui.WishViewModel
import com.example.mywishlistapp.ui.priorityEmoji

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardTextField(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isDescription: Boolean = false,
    isError: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }

    val cardElevation by animateDpAsState(
        targetValue = if (isFocused) 10.dp else 4.dp,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "elevation"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            isFocused -> MaterialTheme.colorScheme.primary
            else -> Color.Transparent
        },
        label = "border_color"
    )

    Column(modifier = modifier.padding(vertical = 6.dp)) {
        // Label sits above the card — avoids the floating-label oddity
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isError) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(cardElevation),
            border = BorderStroke(1.5.dp, borderColor)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = if (isDescription) 110.dp else 52.dp)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .onFocusChanged { isFocused = it.isFocused },
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default
                ),
                singleLine = !isDescription,
                maxLines = if (isDescription) 6 else 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = if (isDescription) ImeAction.Default else ImeAction.Next
                ),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = androidx.compose.ui.text.TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    fontSize = 15.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
fun CategoryAndTagsSection(
    formState: WishFormState,
    viewModel: WishViewModel
) {
    EnhancedSectionCard(title = "🏷️ Categories & Tags", subtitle = "Organize your wish") {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            CategoryDropdown(
                selectedCategory = formState.category,
                onCategorySelected = { viewModel.updateCategory(it) }
            )
            StandardTextField(
                label = "Tags (Optional)",
                value = formState.tags,
                onValueChanged = { viewModel.updateTags(it) },
                placeholder = "e.g., outdoor, sports, recreation"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf(
        "Electronics", "Travel", "Gaming", "Books", "Sports",
        "Fashion", "Home", "Food", "Health", "Education", "Other"
    )
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = "Category",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(if (expanded) 10.dp else 4.dp),
            border = if (expanded) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            else BorderStroke(1.5.dp, Color.Transparent)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedCategory.ifEmpty { "Select a category" },
                        style = androidx.compose.ui.text.TextStyle(
                            color = if (selectedCategory.isEmpty())
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            else MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Default
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    category, style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            onClick = { onCategorySelected(category); expanded = false }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrioritySelector(selectedPriority: Priority, onPrioritySelected: (Priority) -> Unit) {
    EnhancedSectionCard(title = "⭐ Priority", subtitle = "Set the importance level") {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Priority.entries.forEach { priority ->
                val isSelected = selectedPriority == priority
                val label = priority.name.lowercase().replaceFirstChar { it.uppercase() }
                FilterChip(
                    selected = isSelected,
                    onClick = { onPrioritySelected(priority) },
                    label = { Text("${priorityEmoji(priority)} $label") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun DisplayCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    icon,
                    "$title icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    title, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary
                )
            }
            content()
        }
    }
}

@Composable
fun DisplayField(label: String, value: String) {
    Column {
        Text(
            label, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value.ifEmpty { "Not specified" },
            style = MaterialTheme.typography.bodyLarge,
            color = if (value.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DisplayTagsField(tags: List<String>) {
    Column {
        Text(
            "Tags", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tags.forEach { tag ->
                AssistChip(
                    onClick = {},
                    label = { Text(tag) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        labelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
fun PriorityDisplayChip(priority: Priority) {
    val (priorityColor, priorityLabel, priorityIcon) = when (priority) {
        Priority.HIGH -> Triple(MaterialTheme.colorScheme.error, stringResource(R.string.high), "🔥")
        Priority.MEDIUM -> Triple(
            MaterialTheme.colorScheme.secondary,
            stringResource(R.string.medium),
            "⚡"
        )

        Priority.LOW -> Triple(
            MaterialTheme.colorScheme.tertiary,
            stringResource(R.string.low),
            "🌱"
        )
    }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = priorityColor.copy(alpha = 0.12f)),
        border = BorderStroke(2.dp, priorityColor),
        modifier = Modifier.wrapContentWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(priorityIcon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                priorityLabel, style = MaterialTheme.typography.labelMedium,
                color = priorityColor, fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EnhancedSectionCard(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Column {
                    Text(
                        title, style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary
                    )
                    subtitle?.let {
                        Text(
                            it, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
            content()
        }
    }
}

@Composable
fun EnhancedActionButtonWithValidation(
    text: String,
    isFormValid: Boolean,
    isSaving: Boolean = false,
    onClick: () -> Unit,
    onInvalidForm: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "button_scale"
    )
    val buttonColor by animateColorAsState(
        targetValue = if (isFormValid) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "button_color"
    )

    Button(
        onClick = {
            if (isFormValid) {
                onClick()
            } else {
                onInvalidForm()
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    if (isFormValid && !isSaving) {
                        isPressed = true; tryAwaitRelease(); isPressed = false
                    }
                })
            },
        enabled = !isSaving,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            if (isFormValid) 8.dp else 4.dp, 12.dp
        )
    ) {
        if (isSaving) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Saving...", color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(
                text = if (isFormValid) text else "Form Incomplete",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
