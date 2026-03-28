package com.example.mywishlistapp.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.R
import com.example.mywishlistapp.ui.WishFormState
import com.example.mywishlistapp.ui.WishViewModel
import com.example.mywishlistapp.ui.priorityEmoji
import com.example.mywishlistapp.ui.theme.BackgroundLight
import com.example.mywishlistapp.ui.theme.BackgroundSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDetailView(
    id: Long,
    viewModel: WishViewModel,
    navController: NavController
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    var isEditMode by rememberSaveable { mutableStateOf(id == 0L) }

    // Guard against double-tap / multiple navigateUp calls
    var navigated by rememberSaveable { mutableStateOf(false) }
    val safeNavigateUp: () -> Unit = {
        if (!navigated) {
            navigated = true
            navController.navigateUp()
        }
    }

    // FIX 1: Always collect unconditionally — no conditional collectAsState.
    // For new wishes (id == 0L), getWishById returns an empty flow so currentWish stays null.
    val currentWish by viewModel.getWishById(id).collectAsState(initial = null)

    val formState by viewModel.formState.collectAsState()

    // FIX 2: Use `id` as the key, not `currentWish`.
    // This runs exactly once when the screen opens for a given id.
    // Using currentWish as key caused the form to reset every time the DB emitted an update
    // (e.g. while the user was mid-edit).
    LaunchedEffect(key1 = id) {
        if (id == 0L) {
            viewModel.resetForm()
            isEditMode = true
        }
    }

    // FIX 3: Guard with formLoaded so we only load the wish into the form once.
    // Without this, every DB emission (e.g. from a Flow update) would call
    // loadWishIntoForm again and overwrite whatever the user has typed.
    var formLoaded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(key1 = id) {
        if (id != 0L && !formLoaded) {
            viewModel.getWishById(id).collect { wish ->
                if (wish != null && !formLoaded) {
                    viewModel.loadWishIntoForm(wish)
                    formLoaded = true
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            EnhancedAppBarView(
                title = when {
                    id == 0L -> stringResource(R.string.add_wish)
                    isEditMode -> stringResource(R.string.update_wish)
                    else -> currentWish?.title ?: stringResource(R.string.update_wish)
                },
                onBackNavClicked = { safeNavigateUp() },
                showEditIcon = id != 0L && !isEditMode,
                onEditClicked = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    isEditMode = true
                },
                showShareIcon = id != 0L && !isEditMode && currentWish != null,
                onShareClicked = {
                    currentWish?.let { wish ->
                        val shareText = "${wish.title}\n\n${wish.description}"
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            setType("text/plain")
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, null))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BackgroundLight, BackgroundSecondary, Color(0xFFF8FAFF))
                    )
                )
        ) {
            // Show a spinner while the wish is still loading from the DB
            if (id != 0L && currentWish == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Box
            }

            AnimatedContent(
                targetState = isEditMode,
                transitionSpec = {
                    fadeIn(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) +
                            slideInHorizontally(
                                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)
                            ) {
                                if (targetState) it / 4 else -it / 4
                            } togetherWith
                            fadeOut(
                                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)
                            ) +
                            slideOutHorizontally(
                                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)
                            ) {
                                if (targetState) -it / 4 else it / 4
                            }
                },
                label = "mode_transition"
            ) { editMode ->
                if (editMode) {
                    EditModeContent(
                        id = id,
                        formState = formState,
                        viewModel = viewModel,
                        onNavigateUp = safeNavigateUp,
                        snackbarHostState = snackbarHostState,
                        onModeChanged = { isEditMode = false }
                    )
                } else {
                    currentWish?.let { DisplayModeContent(wish = it) }
                }
            }
        }
    }
}

@Composable
fun EditModeContent(
    id: Long,
    formState: WishFormState,
    viewModel: WishViewModel,
    onNavigateUp: () -> Unit,
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
                        onNavigateUp()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedAppBarView(
    title: String,
    onBackNavClicked: () -> Unit,
    showEditIcon: Boolean = false,
    onEditClicked: () -> Unit = {},
    showShareIcon: Boolean = false,
    onShareClicked: () -> Unit = {}
) {
    TopAppBar(
        windowInsets = WindowInsets(0.dp),
        title = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBackNavClicked,
                modifier = Modifier.semantics { contentDescription = "Navigate back" }
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "Back",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        actions = {
            if (showShareIcon) {
                IconButton(onClick = onShareClicked) {
                    Icon(
                        Icons.Default.Share,
                        stringResource(R.string.share),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            if (showEditIcon) {
                IconButton(onClick = onEditClicked) {
                    Icon(
                        Icons.Default.Edit,
                        stringResource(R.string.edit),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
    )
}

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