package com.example.mywishlistapp

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.ui.components.ItemTypeSelector
import com.example.mywishlistapp.ui.components.TargetDatePicker
import com.example.mywishlistapp.ui.components.ProgressTracker
import com.example.mywishlistapp.ui.components.MilestoneManager
import com.example.mywishlistapp.Data.Milestone
import com.example.mywishlistapp.GoalProgressBar
import com.example.mywishlistapp.ui.theme.MyWishListAppTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDetailView(
    id: Long,
    viewModel: WishViewModel,
    navController: NavController
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    
    // Display/Edit mode state
    var isEditMode by remember { mutableStateOf(id == 0L) } // New wishes start in edit mode
    var showAddFundsDialog by remember { mutableStateOf(false) }
    var fundsAmount by remember { mutableStateOf("") }
    
    // Animation states
    val modeTransition = updateTransition(targetState = isEditMode, label = "mode_transition")
    val contentAlpha by modeTransition.animateFloat(
        label = "content_alpha",
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        }
    ) { editMode -> if (editMode) 1f else 0.8f }
    
    val contentOffset by modeTransition.animateDp(
        label = "content_offset",
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        }
    ) { editMode -> if (editMode) 0.dp else 4.dp }

    LaunchedEffect(key1 = id) {
        if (id != 0L) {
            isEditMode = false // Existing wishes start in display mode
        } else {
            // We're adding - clear the fields
            viewModel.wishTitleState = ""
            viewModel.wishDescriptionState = ""
            viewModel.wishCategoryState = ""
            viewModel.wishTagsState = ""
            viewModel.wishPriorityState = Priority.MEDIUM
            viewModel.wishPriceState = ""
            viewModel.wishImageUrlState = ""
            // Reset Personal Growth fields
            viewModel.wishIsGoalState = false
            viewModel.wishTargetDateState = null
            viewModel.wishProgressState = 0
        }
    }

    val currentWish = if (id != 0L) {
        viewModel.getWishbyId(id).collectAsState(initial = Wish(0L, "", "")).value
    } else {
        null
    }
    
    LaunchedEffect(key1 = currentWish) {
        currentWish?.let { wish ->
            if (wish.id != 0L) {
                viewModel.wishTitleState = wish.title
                viewModel.wishDescriptionState = wish.description
                viewModel.wishCategoryState = wish.category
                viewModel.wishTagsState = wish.tags.joinToString(", ")
                viewModel.wishPriorityState = wish.priority
                viewModel.wishPriceState = wish.price
                viewModel.wishImageUrlState = wish.imageUrl
                // Load Personal Growth fields
                viewModel.wishIsGoalState = wish.isGoal
                viewModel.wishTargetDateState = wish.targetDate
                viewModel.wishProgressState = wish.progress
            }
        }
    }

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        topBar = {
            EnhancedAppBarView(
                title = when {
                    id == 0L -> stringResource(R.string.add_wish)
                    isEditMode -> stringResource(R.string.update_wish)
                    else -> currentWish?.title ?: stringResource(R.string.update_wish)
                },
                onBackNavClicked = {
                    navController.navigateUp()
                },
                showEditIcon = id != 0L && !isEditMode,
                onEditClicked = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    isEditMode = true
                },
                showShareIcon = id != 0L && !isEditMode && currentWish != null,
                onShareClicked = {
                    currentWish?.let { wish ->
                        val shareText = context.getString(
                            R.string.share_wish_text,
                            wish.title,
                            wish.description
                        )
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = isEditMode,
                transitionSpec = {
                    fadeIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + slideInHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) { if (targetState) it / 4 else -it / 4 } togetherWith
                    fadeOut(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + slideOutHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) { if (targetState) -it / 4 else it / 4 }
                },
                label = "mode_transition"
            ) { editMode ->
                if (editMode) {
                    // Edit Mode
                    EditModeContent(
                        id = id,
                        viewModel = viewModel,
                        navController = navController,
                        snackbarHostState = snackbarHostState,
                        onModeChanged = { isEditMode = false }
                    )
                } else {
                    // Display Mode
                    currentWish?.let { wish ->
                        DisplayModeContent(
                            wish = wish,
                            viewModel = viewModel,
                            onAddFunds = { showAddFundsDialog = true }
                        )
                    }
                }
            }
        }
        
        // Add Funds Dialog
        if (showAddFundsDialog) {
            AddFundsDialog(
                currentAmount = fundsAmount,
                onAmountChanged = { fundsAmount = it },
                onConfirm = {
                    val amount = fundsAmount.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        viewModel.addFundsToWish(id, amount)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAddFundsDialog = false
                        fundsAmount = ""
                    }
                },
                onDismiss = {
                    showAddFundsDialog = false
                    fundsAmount = ""
                }
            )
        }
    }
}

@Composable
fun StandardTextField(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    placeholder: String = "",
    isDescription: Boolean = false,
    isError: Boolean = false,
    errorMessage: String = "",
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    var isFocused by remember { mutableStateOf(false) }
    val animatedElevation by animateDpAsState(
        targetValue = if (isFocused) 12.dp else 6.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "elevation_animation"
    )
    
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = animatedElevation
            ),
            border = if (isError) BorderStroke(2.dp, MaterialTheme.colorScheme.error) else null
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChanged,
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                placeholder = {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.0f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.0f),
                    errorBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.0f),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f),
                    errorContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = !isDescription,
                maxLines = if (isDescription) 5 else 1,
                minLines = if (isDescription) 3 else 1,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = if (isDescription) ImeAction.Default else ImeAction.Next
                ),
                isError = isError
            )
        }
        
        // Error message
        AnimatedVisibility(
            visible = isError && errorMessage.isNotEmpty(),
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Form validation error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun CurrencyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val animatedElevation by animateDpAsState(
        targetValue = if (isFocused) 12.dp else 6.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "currency_elevation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = animatedElevation
        )
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                    onValueChange(newValue)
                }
            },
            label = {
                Text(
                    "Price (Optional)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            },
            placeholder = {
                Text(
                    "e.g., 299.99",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
                leadingIcon = {
                    Icon(
                        Icons.Default.AttachMoney,
                        contentDescription = "Price input field",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            shape = RoundedCornerShape(18.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.0f),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.0f),
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
            )
        )
    }
}

@Composable
fun ImageUrlField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val animatedElevation by animateDpAsState(
        targetValue = if (isFocused) 12.dp else 6.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "image_elevation"
    )
    
    Column(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = animatedElevation
            )
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = {
                    Text(
                        "Image URL (Optional)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                placeholder = {
                    Text(
                        "https://example.com/image.jpg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Image URL input field",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused },
                shape = RoundedCornerShape(18.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.0f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.0f),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
                )
            )
        }
        
        // Image preview with animation
        AnimatedVisibility(
            visible = value.isNotBlank(),
            enter = slideInVertically() + fadeIn() + expandVertically(),
            exit = slideOutVertically() + fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column {
                    AsyncImage(
                        model = value,
                        contentDescription = "Wish image preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = "Image Preview",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun BasicInfoSection(viewModel: WishViewModel, titleTouched: Boolean = false) {
    val titleError = viewModel.wishTitleState.isEmpty() && titleTouched
    
    EnhancedSectionCard(
        title = "📝 Basic Information",
        subtitle = "Tell us about your wish"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            StandardTextField(
                label = "Wish Title *",
                value = viewModel.wishTitleState,
                onValueChanged = { viewModel.onWishTitleChanged(it) },
                placeholder = "e.g., New Mountain Bike",
                isError = titleError,
                errorMessage = if (titleError) "Title is required" else ""
            )
            StandardTextField(
                label = "Description (Optional)",
                value = viewModel.wishDescriptionState,
                onValueChanged = { viewModel.onWishDescriptionChanged(it) },
                placeholder = "Add any additional details about your wish...",
                isDescription = true,
                isError = false,
                errorMessage = ""
            )
        }
    }
}

@Composable
fun PriceAndImageSection(viewModel: WishViewModel) {
    EnhancedSectionCard(
        title = "💰 Price & Image",
        subtitle = "Add pricing and visual details"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            CurrencyTextField(
                value = viewModel.wishPriceState,
                onValueChange = { viewModel.onWishPriceChanged(it) }
            )
            ImageUrlField(
                value = viewModel.wishImageUrlState,
                onValueChange = { viewModel.onWishImageUrlChanged(it) }
            )
        }
    }
}

@Composable
fun CategoryAndTagsSection(viewModel: WishViewModel) {
    EnhancedSectionCard(
        title = "🏷️ Categories & Tags",
        subtitle = "Organize and classify your wish"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            CategoryDropdown(
                selectedCategory = viewModel.wishCategoryState,
                onCategorySelected = { viewModel.onWishCategoryChanged(it) }
            )
            StandardTextField(
                label = "Tags (Optional)",
                value = viewModel.wishTagsState,
                onValueChanged = { viewModel.onWishTagsChanged(it) },
                placeholder = "e.g., outdoor, sports, recreation",
                isError = false,
                errorMessage = ""
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("Electronics", "Travel", "Gaming", "Books", "Sports", "Fashion", "Home", "Food", "Health", "Education", "Other")
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCategory.ifEmpty { "Select category" },
                onValueChange = {},
                readOnly = true,
                label = { 
                    Text(
                        "Category",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.0f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.0f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f),
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                ),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = category,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            ) 
                        },
                        onClick = {
                            onCategorySelected(category)
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.onSurface,
                            leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrioritySelector(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    EnhancedSectionCard(
        title = "⭐ Priority",
        subtitle = "Set the importance level"
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Priority.values().forEach { priority ->
                    val isSelected = selectedPriority == priority
                    val (containerColor, contentColor, priorityLabel, priorityIcon) = when (priority) {
                        Priority.LOW -> Tuple4(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.onPrimaryContainer,
                            "Low",
                            Icons.Default.KeyboardArrowDown
                        )
                        Priority.MEDIUM -> Tuple4(
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.colorScheme.onSecondaryContainer,
                            "Medium", 
                            Icons.Default.Remove
                        )
                        Priority.HIGH -> Tuple4(
                            MaterialTheme.colorScheme.errorContainer,
                            MaterialTheme.colorScheme.onErrorContainer,
                            "High",
                            Icons.Default.KeyboardArrowUp
                        )
                    }
                    
                    Card(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPrioritySelected(priority)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = containerColor.copy(alpha = if (isSelected) 1.0f else 0.4f)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isSelected) 8.dp else 1.dp
                        ),
                        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = priorityIcon,
                                contentDescription = "$priorityLabel priority icon",
                                modifier = Modifier.size(24.dp),
                                tint = contentColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = priorityLabel,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Priority explanation text
            Text(
                text = when (selectedPriority) {
                    Priority.HIGH -> "🔥 High priority items need immediate attention"
                    Priority.MEDIUM -> "⚡ Medium priority items are important but can wait"
                    Priority.LOW -> "🌱 Low priority items are nice to have"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

// Data class for priority selector is defined in HomeView.kt

// Display Mode Content
@Composable
fun DisplayModeContent(
    wish: Wish,
    viewModel: WishViewModel,
    onAddFunds: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Hero image if available
        if (wish.imageUrl.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    AsyncImage(
                        model = wish.imageUrl,
                        contentDescription = stringResource(R.string.share),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        
        // Basic Info Display
        item {
            DisplayCard(
                title = stringResource(R.string.basic_information),
                icon = Icons.Default.Info
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DisplayField(label = stringResource(R.string.title), value = wish.title)
                    DisplayField(label = stringResource(R.string.description), value = wish.description)
                }
            }
        }
        
        // Price and Savings Progress
        if (wish.price.isNotEmpty()) {
            val targetPrice = wish.price.toDoubleOrNull() ?: 0.0
            if (targetPrice > 0) {
                item {
                    SavingsProgressCard(
                        wish = wish,
                        targetPrice = targetPrice,
                        savedAmount = wish.savedAmount,
                        onAddFunds = onAddFunds
                    )
                }
            }
        }
        
        // Category and Tags
        item {
            DisplayCard(
                title = stringResource(R.string.categories_and_tags),
                icon = Icons.Default.Label
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DisplayField(label = stringResource(R.string.category), value = wish.category.ifEmpty { "No category" })
                    if (wish.tags.isNotEmpty()) {
                        DisplayTagsField(tags = wish.tags)
                    }
                }
            }
        }
        
        // Priority Display
        item {
            DisplayCard(
                title = stringResource(R.string.priority),
                icon = Icons.Default.Star
            ) {
                PriorityDisplayChip(priority = wish.priority)
            }
        }
        
        // Personal Growth Display - Item Type
        item {
            DisplayCard(
                title = "Item Type",
                icon = if (wish.isGoal) Icons.Default.Flag else Icons.Default.Star
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (wish.isGoal) "🎯" else "✨",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = if (wish.isGoal) "Goal" else "Wish",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (wish.isGoal) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // Personal Growth Display - Goal Progress (only for goals)
        if (wish.isGoal) {
            item {
                DisplayCard(
                    title = "Goal Progress",
                    icon = Icons.Default.TrendingUp
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${wish.progress}% Complete",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = when {
                                    wish.progress >= 80 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                    wish.progress >= 50 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    text = when {
                                        wish.progress >= 80 -> "🎯"
                                        wish.progress >= 50 -> "💪"
                                        else -> "🌱"
                                    },
                                    modifier = Modifier.padding(8.dp),
                                    fontSize = 16.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LinearProgressIndicator(
                            progress = { wish.progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = when {
                                wish.progress >= 80 -> MaterialTheme.colorScheme.tertiary
                                wish.progress >= 50 -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.primary
                            },
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    }
                }
            }
            
            // Target Date Display (only for goals with target dates)
            wish.targetDate?.let { targetDate ->
                item {
                    DisplayCard(
                        title = "Target Date",
                        icon = Icons.Default.CalendarToday
                    ) {
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        val formattedDate = dateFormat.format(Date(targetDate))
                        val now = System.currentTimeMillis()
                        val daysUntil = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(targetDate - now)
                        
                        Column {
                            androidx.compose.material3.Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                val (statusText, statusColor) = when {
                                    daysUntil < 0 -> Pair("${kotlin.math.abs(daysUntil)} days overdue", MaterialTheme.colorScheme.error)
                                    daysUntil == 0L -> Pair("Due today!", MaterialTheme.colorScheme.secondary)
                                    daysUntil <= 7 -> Pair("$daysUntil days remaining", MaterialTheme.colorScheme.secondary)
                                    else -> Pair("$daysUntil days remaining", MaterialTheme.colorScheme.tertiary)
                                }
                                
                                Icon(
                                    imageVector = when {
                                        daysUntil < 0 -> Icons.Default.Warning
                                        daysUntil <= 7 -> Icons.Default.Schedule
                                        else -> Icons.Default.CheckCircle
                                    },
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = statusColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            
            // Milestones Display (only for goals)
            item {
                val milestones by viewModel.getMilestonesForGoal(wish.id).collectAsState(initial = emptyList())
                
                MilestoneManager(
                    goal = wish,
                    milestones = milestones,
                    onMilestoneCompleted = { milestoneId ->
                        viewModel.completeMilestone(milestoneId)
                    },
                    onMilestoneUncompleted = { milestoneId ->
                        viewModel.uncompleteMilestone(milestoneId)
                    },
                    onAddMilestone = { title, description, dueDate ->
                        viewModel.addMilestone(
                            wishId = wish.id,
                            title = title,
                            description = description,
                            dueDate = dueDate
                        )
                    },
                    onEditMilestone = { milestone ->
                        viewModel.updateMilestone(milestone)
                    },
                    onDeleteMilestone = { milestone ->
                        viewModel.deleteMilestone(milestone)
                    }
                )
            }
        }
    }
}

// Edit Mode Content
@Composable
fun EditModeContent(
    id: Long,
    viewModel: WishViewModel,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    onModeChanged: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            // Welcome Header with better styling
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                        text = if (id != 0L) "Make changes to perfect your wish" else "Turn your dreams into achievable goals",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        item {
            var titleTouchedState by remember { mutableStateOf(false) }
            BasicInfoSection(viewModel, titleTouchedState)
        }

        item {
            CategoryAndTagsSection(viewModel)
        }

        item {
            PrioritySelector(
                selectedPriority = viewModel.wishPriorityState,
                onPrioritySelected = { viewModel.onWishPriorityChanged(it) }
            )
        }

        // Personal Growth Companion Components
        item {
            ItemTypeSelector(
                isGoal = viewModel.wishIsGoalState,
                onItemTypeChanged = { viewModel.onWishIsGoalChanged(it) }
            )
        }

        item {
            TargetDatePicker(
                targetDate = viewModel.wishTargetDateState,
                onTargetDateChanged = { viewModel.onWishTargetDateChanged(it) },
                isVisible = viewModel.wishIsGoalState
            )
        }

        item {
            ProgressTracker(
                progress = viewModel.wishProgressState,
                onProgressChanged = { viewModel.onWishProgressChanged(it) },
                isVisible = viewModel.wishIsGoalState
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            EnhancedActionButtonWithValidation(
                text = if (id != 0L) stringResource(R.string.update_wish_button) else stringResource(R.string.add_wish_button),
                isFormValid = viewModel.wishTitleState.isNotBlank(),
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    
                    if (id != 0L) {
                        viewModel.updateWish(
                            Wish(
                                id = id,
                                title = viewModel.wishTitleState.trim(),
                                description = viewModel.wishDescriptionState.trim(),
                                category = viewModel.wishCategoryState,
                                tags = viewModel.getTagsList(),
                                priority = viewModel.wishPriorityState,
                            price = "",
                            imageUrl = "",
                                isGoal = viewModel.wishIsGoalState,
                                targetDate = viewModel.wishTargetDateState,
                                progress = viewModel.wishProgressState
                            )
                        )
                        onModeChanged()
                    } else {
                        viewModel.addWish(
                            Wish(
                                title = viewModel.wishTitleState.trim(),
                                description = viewModel.wishDescriptionState.trim(),
                                category = viewModel.wishCategoryState,
                                tags = viewModel.getTagsList(),
                                priority = viewModel.wishPriorityState,
                            price = "",
                            imageUrl = "",
                                isGoal = viewModel.wishIsGoalState,
                                targetDate = viewModel.wishTargetDateState,
                                progress = viewModel.wishProgressState
                            )
                        )
                        navController.navigateUp()
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

// Enhanced App Bar with Edit and Share icons
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
                modifier = Modifier.semantics { contentDescription = "Navigate back to previous screen" }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        actions = {
            if (showShareIcon) {
                IconButton(
                    onClick = onShareClicked,
                    modifier = Modifier.semantics { contentDescription = "Share this wish with others" }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.share),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            if (showEditIcon) {
                IconButton(
                    onClick = onEditClicked,
                    modifier = Modifier.semantics { contentDescription = "Edit this wish" }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

// Display Card Component
@Composable
fun DisplayCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    imageVector = icon,
                    contentDescription = "$title section icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            content()
        }
    }
}

// Display Field Component
@Composable
fun DisplayField(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value.ifEmpty { "Not specified" },
            style = MaterialTheme.typography.bodyLarge,
            color = if (value.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Normal
        )
    }
}

// Display Tags Component
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DisplayTagsField(
    tags: List<String>
) {
    Column {
        Text(
            text = "Tags",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tags.forEach { tag ->
                AssistChip(
                    onClick = { },
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

// Priority Display Chip
@Composable
fun PriorityDisplayChip(priority: Priority) {
    val (priorityColor, priorityLabel, priorityIcon) = when (priority) {
        Priority.HIGH -> Triple(MaterialTheme.colorScheme.error, stringResource(R.string.high), "🔥")
        Priority.MEDIUM -> Triple(MaterialTheme.colorScheme.secondary, stringResource(R.string.medium), "⚡")
        Priority.LOW -> Triple(MaterialTheme.colorScheme.tertiary, stringResource(R.string.low), "🌱")
    }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = priorityColor.copy(alpha = 0.12f)
        ),
        border = BorderStroke(2.dp, priorityColor),
        modifier = Modifier.wrapContentWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = priorityIcon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = priorityLabel,
                style = MaterialTheme.typography.labelMedium,
                color = priorityColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Savings Progress Card
@Composable
fun SavingsProgressCard(
    wish: Wish,
    targetPrice: Double,
    savedAmount: Double,
    onAddFunds: () -> Unit
) {
    val progress = (savedAmount / targetPrice).coerceIn(0.0, 1.0)
    val progressPercentage = (progress * 100)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    imageVector = Icons.Default.Savings,
                    contentDescription = "Savings progress section",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.savings_progress),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            // Progress bar using existing GoalProgressBar component
            GoalProgressBar(
                title = "Savings Progress",
                currentAmount = savedAmount.toFloat(),
                targetAmount = targetPrice.toFloat(),
                currency = "$",
                modifier = Modifier.padding(vertical = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = String.format("Saved: $%.2f", savedAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = String.format("Target: $%.2f", targetPrice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.1f%% saved", progressPercentage),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Button(
                    onClick = onAddFunds,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add funds to savings",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.add_funds),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Add Funds Dialog
@Composable
fun AddFundsDialog(
    currentAmount: String,
    onAmountChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.add_funds_to_wish),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = currentAmount,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        onAmountChanged(newValue)
                    }
                },
                label = { Text(stringResource(R.string.amount)) },
                placeholder = { Text(stringResource(R.string.enter_amount)) },
                leadingIcon = { 
                    Icon(Icons.Default.AttachMoney, contentDescription = null) 
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = currentAmount.toDoubleOrNull()?.let { it > 0 } == true
            ) {
                Text(stringResource(R.string.add_funds))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

// Enhanced Section Card Component
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    subtitle?.let { sub ->
                        Text(
                            text = sub,
                            style = MaterialTheme.typography.bodyMedium,
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

// Enhanced Action Button with validation and loading states
@Composable
fun EnhancedActionButtonWithValidation(
    text: String,
    isFormValid: Boolean,
    onClick: () -> Unit,
    onInvalidForm: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_scale"
    )
    
    val buttonColor by animateColorAsState(
        targetValue = if (isFormValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_color"
    )
    
    Button(
        onClick = {
            if (isFormValid) {
                isLoading = true
                onClick()
                // Reset loading after a brief delay for user feedback
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    kotlinx.coroutines.delay(800)
                    isLoading = false
                }
            } else {
                onInvalidForm()
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        if (isFormValid) {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    }
                )
            },
        enabled = !isLoading,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isFormValid) 8.dp else 4.dp,
            pressedElevation = 12.dp
        )
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )) togetherWith fadeOut(animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ))
            },
            label = "button_content"
        ) { loading ->
            if (loading) {
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
                        text = "Saving...",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    AnimatedVisibility(
                        visible = isFormValid,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    
                    AnimatedVisibility(
                        visible = !isFormValid,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    
                    Text(
                        text = if (isFormValid) text else "Form Incomplete",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Enhanced Action Button with spring animation (legacy)
@Composable
fun EnhancedActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_scale"
    )
    
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WishTextFieldsPreview() {
    MyWishListAppTheme {
        /* WishTextFields(
            label = "Text",
            value = "Text",
            onValueChanged = {}
        ) */
    }
}
