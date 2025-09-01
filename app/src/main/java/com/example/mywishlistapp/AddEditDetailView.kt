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
fun ModernTextField(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    placeholder: String = "",
    isDescription: Boolean = false,
    isError: Boolean = false,
    errorMessage: String = "",
    modifier: Modifier = Modifier
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
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = animatedElevation
            ),
            border = if (isError) BorderStroke(1.dp, Color(0xFFE74C3C)) else null
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChanged,
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isError) Color(0xFFE74C3C) else Color(0xFF667EEA),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                placeholder = {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9CA3AF).copy(alpha = 0.7f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused },
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF667EEA),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent
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
                    contentDescription = null,
                    tint = Color(0xFFE74C3C),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFE74C3C)
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
            containerColor = Color.White.copy(alpha = 0.95f)
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
                    color = Color(0xFF667EEA),
                    fontWeight = FontWeight.SemiBold
                )
            },
            placeholder = {
                Text(
                    "e.g., 299.99",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9CA3AF).copy(alpha = 0.7f)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = Color(0xFF667EEA)
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
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = Color(0xFF667EEA),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
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
                containerColor = Color.White.copy(alpha = 0.95f)
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
                        color = Color(0xFF667EEA),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                placeholder = {
                    Text(
                        "https://example.com/image.jpg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9CA3AF).copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = Color(0xFF667EEA)
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
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF667EEA),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
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
                    containerColor = Color.White
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
                        color = Color(0xFF667EEA),
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
fun BasicInfoSection(viewModel: WishViewModel) {
    val titleError = viewModel.wishTitleState.isEmpty()
    val descriptionError = viewModel.wishDescriptionState.isEmpty()
    
    EnhancedSectionCard(
        title = "📝 Basic Information",
        subtitle = "Tell us about your wish"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ModernTextField(
                label = "Wish Title *",
                value = viewModel.wishTitleState,
                onValueChanged = { viewModel.onWishTitleChanged(it) },
                placeholder = "e.g., New Mountain Bike",
                isError = titleError && viewModel.wishTitleState.isEmpty(),
                errorMessage = if (titleError && viewModel.wishTitleState.isEmpty()) "Title is required" else ""
            )
            ModernTextField(
                label = "Description *",
                value = viewModel.wishDescriptionState,
                onValueChanged = { viewModel.onWishDescriptionChanged(it) },
                placeholder = "e.g., A durable mountain bike for weekend trail adventures...",
                isDescription = true,
                isError = descriptionError && viewModel.wishDescriptionState.isEmpty(),
                errorMessage = if (descriptionError && viewModel.wishDescriptionState.isEmpty()) "Description is required" else ""
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
            ModernTextField(
                label = "Tags (Optional)",
                value = viewModel.wishTagsState,
                onValueChanged = { viewModel.onWishTagsChanged(it) },
                placeholder = "e.g., outdoor, sports, recreation"
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
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
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
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(text = category) },
                        onClick = {
                            onCategorySelected(category)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Priority.values().forEach { priority ->
                    val isSelected = selectedPriority == priority
                    val (priorityColor, priorityLabel, priorityIcon) = when (priority) {
                        Priority.HIGH -> Triple(Color(0xFFE74C3C), "High", "🔥")
                        Priority.MEDIUM -> Triple(Color(0xFFF39C12), "Medium", "⚡")
                        Priority.LOW -> Triple(Color(0xFF27AE60), "Low", "🌱")
                    }
                    
                    var isPressed by remember { mutableStateOf(false) }
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.95f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessHigh
                        ),
                        label = "priority_scale"
                    )
                    
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isPressed = true
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        tryAwaitRelease()
                                        isPressed = false
                                    }
                                )
                            }
                            .clickable { 
                                onPrioritySelected(priority)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) priorityColor.copy(alpha = 0.15f) else Color(0xFFF8FAFC)
                        ),
                        border = if (isSelected) BorderStroke(2.dp, priorityColor) else BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isSelected) 8.dp else 4.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = priorityIcon,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Text(
                                text = priorityLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) priorityColor else Color(0xFF64748B),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

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
                        color = if (wish.isGoal) Color(0xFF10B981) else Color(0xFF667EEA)
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
                                color = Color(0xFF667EEA)
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = when {
                                    wish.progress >= 80 -> Color(0xFF10B981).copy(alpha = 0.2f)
                                    wish.progress >= 50 -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                    else -> Color(0xFF667EEA).copy(alpha = 0.2f)
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
                                wish.progress >= 80 -> Color(0xFF10B981)
                                wish.progress >= 50 -> Color(0xFFF59E0B)
                                else -> Color(0xFF667EEA)
                            },
                            trackColor = Color(0xFF667EEA).copy(alpha = 0.2f)
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
                                color = Color(0xFF1A1D29)
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                val (statusText, statusColor) = when {
                                    daysUntil < 0 -> Pair("${kotlin.math.abs(daysUntil)} days overdue", Color(0xFFE74C3C))
                                    daysUntil == 0L -> Pair("Due today!", Color(0xFFF39C12))
                                    daysUntil <= 7 -> Pair("$daysUntil days remaining", Color(0xFFF59E0B))
                                    else -> Pair("$daysUntil days remaining", Color(0xFF10B981))
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
                MilestoneManager(
                    goal = wish,
                    milestones = emptyList(), // TODO: Load from ViewModel
                    onMilestoneCompleted = { milestoneId ->
                        // TODO: Implement milestone completion
                    },
                    onMilestoneUncompleted = { milestoneId ->
                        // TODO: Implement milestone un-completion
                    },
                    onAddMilestone = {
                        // TODO: Implement add milestone
                    },
                    onEditMilestone = { milestone ->
                        // TODO: Implement edit milestone
                    },
                    onDeleteMilestone = { milestone ->
                        // TODO: Implement delete milestone
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
                    containerColor = Color(0xFF667EEA).copy(alpha = 0.08f)
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
                        color = Color(0xFF667EEA),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (id != 0L) "Make changes to perfect your wish" else "Turn your dreams into achievable goals",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        item {
            BasicInfoSection(viewModel)
        }

        item {
            PriceAndImageSection(viewModel)
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
                isFormValid = viewModel.wishTitleState.isNotBlank() && viewModel.wishDescriptionState.isNotBlank(),
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
                                price = viewModel.wishPriceState.trim(),
                                imageUrl = viewModel.wishImageUrlState.takeIf { it.isNotBlank() } ?: "",
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
                                price = viewModel.wishPriceState.trim(),
                                imageUrl = viewModel.wishImageUrlState.takeIf { it.isNotBlank() } ?: "",
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
                            message = "Please fill in both title and description",
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
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackNavClicked) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        actions = {
            if (showShareIcon) {
                IconButton(onClick = onShareClicked) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.share),
                        tint = Color.White
                    )
                }
            }
            if (showEditIcon) {
                IconButton(onClick = onEditClicked) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = Color.White
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF667EEA)
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    contentDescription = null,
                    tint = Color(0xFF667EEA),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF667EEA)
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
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value.ifEmpty { "Not specified" },
            style = MaterialTheme.typography.bodyLarge,
            color = if (value.isEmpty()) Color(0xFF94A3B8) else Color(0xFF1A1D29),
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
            color = Color(0xFF64748B),
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
                        containerColor = Color(0xFF667EEA).copy(alpha = 0.1f),
                        labelColor = Color(0xFF667EEA)
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
        Priority.HIGH -> Triple(Color(0xFFE74C3C), stringResource(R.string.high), "🔥")
        Priority.MEDIUM -> Triple(Color(0xFFF39C12), stringResource(R.string.medium), "⚡")
        Priority.LOW -> Triple(Color(0xFF27AE60), stringResource(R.string.low), "🌱")
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.savings_progress),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
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
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = String.format("Target: $%.2f", targetPrice),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = String.format("%.1f%% saved", progressPercentage),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF667EEA),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Button(
                    onClick = onAddFunds,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
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
            containerColor = Color.White.copy(alpha = 0.95f)
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
                        color = Color(0xFF667EEA)
                    )
                    subtitle?.let { sub ->
                        Text(
                            text = sub,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B),
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
        targetValue = if (isFormValid) Color(0xFF667EEA) else Color(0xFF94A3B8),
        animationSpec = tween(300),
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
            disabledContainerColor = Color(0xFF94A3B8)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isFormValid) 8.dp else 4.dp,
            pressedElevation = 12.dp
        )
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
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
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Saving...",
                        color = Color.White,
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
                            tint = Color.White,
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
                            tint = Color.White,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    
                    Text(
                        text = if (isFormValid) text else "Form Incomplete",
                        color = Color.White,
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
            containerColor = Color(0xFF667EEA)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Text(
            text = text,
            color = Color.White,
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
