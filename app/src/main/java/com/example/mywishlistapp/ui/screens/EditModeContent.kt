package com.example.mywishlistapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.R
import com.example.mywishlistapp.ui.WishFormState
import com.example.mywishlistapp.ui.WishViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditModeContent(
    id: Long,
    formState: WishFormState,
    viewModel: WishViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToHome: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onModeChanged: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val isSaving by viewModel.isSaving.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (id != 0L) "✨ Update Your Wish" else "🌟 Create New Wish",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (id != 0L) "Make changes to perfect your wish"
                        else "Turn your dreams into achievable goals",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        item {
            EnhancedSectionCard(title = "📝 Basic Details", subtitle = "What do you wish for?") {
                StandardTextField(
                    label = "Title *",
                    value = formState.title,
                    onValueChanged = { viewModel.updateTitle(it) },
                    placeholder = "e.g., New MacBook Pro",
                    isError = formState.title.isEmpty()
                )
                StandardTextField(
                    label = "Description",
                    value = formState.description,
                    onValueChanged = { viewModel.updateDescription(it) },
                    placeholder = "Describe your wish...",
                    isDescription = true
                )
                StandardTextField(
                    label = "Price (Optional)",
                    value = formState.price,
                    onValueChanged = { viewModel.updatePrice(it) },
                    placeholder = "e.g., 999"
                )
            }
        }

        item { CategoryAndTagsSection(formState = formState, viewModel = viewModel) }

        item {
            PrioritySelector(
                selectedPriority = formState.priority,
                onPrioritySelected = { viewModel.updatePriority(it) }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            EnhancedActionButtonWithValidation(
                text = if (id != 0L) stringResource(R.string.update_wish_button)
                else stringResource(R.string.add_wish_button),
                isFormValid = formState.title.isNotBlank(),
                isSaving = isSaving,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (id != 0L) {
                        viewModel.updateWish(
                            Wish(
                                id = id,
                                title = formState.title.trim(),
                                description = formState.description.trim(),
                                category = formState.category,
                                tags = viewModel.getTagsList(),
                                priority = formState.priority,
                                price = formState.price.trim()
                            )
                        )
                        onModeChanged()
                    } else {
                        viewModel.addWish(
                            Wish(
                                title = formState.title.trim(),
                                description = formState.description.trim(),
                                category = formState.category,
                                tags = viewModel.getTagsList(),
                                priority = formState.priority,
                                price = formState.price.trim()
                            )
                        )
                        onNavigateToHome()
                    }
                },
                onInvalidForm = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Please enter a title for your wish",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )
        }
    }
}
