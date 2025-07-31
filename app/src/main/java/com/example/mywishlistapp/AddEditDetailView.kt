package com.example.mywishlistapp

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import java.text.DecimalFormat
import java.util.Locale
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.ui.components.*
import com.example.mywishlistapp.ui.theme.MyWishListAppTheme
import kotlinx.coroutines.launch

@Composable
fun AddEditDetailView(
    id: Long,
    viewModel: WishViewModel,
    navController: NavController
) {
    val snackMessage = remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Focus requesters for each text field
    val titleFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }
    val tagsFocusRequester = remember { FocusRequester() }

    // Initialize or load wish data
    LaunchedEffect(key1 = id) {
        if (id != 0L) {
            // We're editing - the ViewModel will handle loading the wish
            // No need to manually collect here as the ViewModel manages state
        } else {
            // We're adding - clear the fields
            viewModel.wishTitleState = ""
            viewModel.wishDescriptionState = ""
            viewModel.wishCategoryState = ""
            viewModel.wishTagsState = ""
            viewModel.wishPriorityState = Priority.MEDIUM
            viewModel.wishPriceState = ""
            viewModel.wishImageUrlState = ""
        }
    }

    // Load wish data for editing
    if (id != 0L) {
        val wish = viewModel.getWishbyId(id).collectAsState(initial = Wish(0L, "", ""))
        LaunchedEffect(key1 = wish.value) {
            if (wish.value.id != 0L) {
                viewModel.wishTitleState = wish.value.title
                viewModel.wishDescriptionState = wish.value.description
                viewModel.wishCategoryState = wish.value.category
                viewModel.wishTagsState = wish.value.tags.joinToString(", ")
                viewModel.wishPriorityState = wish.value.priority
                viewModel.wishPriceState = wish.value.price?.toString() ?: ""
                viewModel.wishImageUrlState = wish.value.imageUrl ?: ""
            }
        }
    }

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        topBar = {
            AppBarView(
                title = if (id != 0L)
                    stringResource(R.string.update_wish)
                else
                    stringResource(R.string.add_wish),
                onBackNavClicked = {
                    navController.navigateUp()
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                item {
                    Text(
                        text = if (id != 0L) "Update Your Wish" else "Create New Wish",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Basic Information Section
                item {
                    BasicInfoSection(viewModel)
                }

                // Price and Image Section
                item {
                    PriceAndImageSection(viewModel)
                }

                // Categories and Tags Section
                item {
                    Text(
                        text = "🏷️ Categories & Tags",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667EEA),
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                    )
                }

                item {
                    CategoryDropdown(
                        selectedCategory = viewModel.wishCategoryState,
                        onCategorySelected = { viewModel.onWishCategoryChanged(it) }
                    )
                }

                item {
                    VoiceInputField(
                        value = viewModel.wishTagsState,
                        onValueChanged = { viewModel.onWishTagsChanged(it) },
                        label = "Enter tags (comma separated)",
                        placeholder = "e.g., smartphone, android, budget"
                    )
                }

                // Priority Section
                item {
                    Text(
                        text = "⭐ Priority",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667EEA),
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                    )
                }

                item {
                    PrioritySelector(
                        selectedPriority = viewModel.wishPriorityState,
                        onPrioritySelected = { viewModel.onWishPriorityChanged(it) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

item {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f)

    Button(
        onClick = {
            if (viewModel.wishTitleState.isNotEmpty() &&
                viewModel.wishDescriptionState.isNotEmpty()
            ) {
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
                            imageUrl = viewModel.wishImageUrlState.takeIf { it.isNotBlank() } ?: ""
                        )
                    )
                    navController.navigateUp()
                } else {
                    viewModel.addWish(
                        Wish(
                            title = viewModel.wishTitleState.trim(),
                            description = viewModel.wishDescriptionState.trim(),
                            category = viewModel.wishCategoryState,
                            tags = viewModel.getTagsList(),
                            priority = viewModel.wishPriorityState,
                            price = viewModel.wishPriceState.trim(),
                            imageUrl = viewModel.wishImageUrlState.takeIf { it.isNotBlank() } ?: ""
                        )
                    )
                    navController.navigateUp()
                }
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Please fill in title and description",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                })
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
            text = if (id != 0L) "✨ Update Wish" else "✨ Add Wish",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
            }
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            focusedElevation = 12.dp
        )
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChanged,
            label = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF667EEA),
                    fontWeight = FontWeight.SemiBold
                )
            },
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9CA3AF).copy(alpha = 0.8f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667EEA),
                unfocusedBorderColor = Color.Transparent,
                cursorColor = Color(0xFF667EEA),
                focusedLabelColor = Color(0xFF667EEA),
                unfocusedLabelColor = Color(0xFF8B9DC3),
                focusedTextColor = Color(0xFF1A1D29),
                unfocusedTextColor = Color(0xFF2D3748),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            singleLine = !isDescription,
            maxLines = if (isDescription) 4 else 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = if (isDescription) ImeAction.Default else ImeAction.Next
            )
        )
    }
}

@Composable
fun CurrencyTextField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Only allow numbers and decimal point
            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                onValueChange(newValue)
            }
        },
        label = {
            Text(
                text = "Price (Optional)",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF667EEA),
                fontWeight = FontWeight.SemiBold
            )
        },
        placeholder = {
            Text(
                text = "e.g., 299.99",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9CA3AF).copy(alpha = 0.8f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.AttachMoney,
                contentDescription = null,
                tint = Color(0xFF667EEA)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(28.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF667EEA),
            unfocusedBorderColor = Color(0xFFE1E8FF),
            cursorColor = Color(0xFF667EEA),
            focusedLabelColor = Color(0xFF667EEA),
            unfocusedLabelColor = Color(0xFF8B9DC3),
            focusedTextColor = Color(0xFF1A1D29),
            unfocusedTextColor = Color(0xFF2D3748),
            focusedContainerColor = Color.White.copy(alpha = 0.8f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.6f)
        )
    )
}

@Composable
fun ImageUrlField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    text = "Image URL (Optional)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF667EEA),
                    fontWeight = FontWeight.SemiBold
                )
            },
            placeholder = {
                Text(
                    text = "https://example.com/image.jpg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9CA3AF).copy(alpha = 0.8f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = Color(0xFF667EEA)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(28.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667EEA),
                unfocusedBorderColor = Color(0xFFE1E8FF),
                cursorColor = Color(0xFF667EEA),
                focusedLabelColor = Color(0xFF667EEA),
                unfocusedLabelColor = Color(0xFF8B9DC3),
                focusedTextColor = Color(0xFF1A1D29),
                unfocusedTextColor = Color(0xFF2D3748),
                focusedContainerColor = Color.White.copy(alpha = 0.8f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.6f)
            )
        )
        
        // Image preview
        if (value.isNotBlank() && (value.startsWith("http://") || value.startsWith("https://"))) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                AsyncImage(
                    model = value,
                    contentDescription = "Wish image preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(8.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun WishTextFields(
    label: String,
    value: String,
    onValueChanged: (String) -> Unit,
    placeholder: String = "",
    focusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    imeAction: ImeAction = ImeAction.Next
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isDescription = label.contains("description", ignoreCase = true)
    
    ModernTextField(
        label = label,
        value = value,
        onValueChanged = onValueChanged,
        placeholder = placeholder,
        isDescription = isDescription
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        "Electronics", "Travel", "Gaming", "Books", "Sports", 
        "Fashion", "Home", "Food", "Health", "Education", "Other"
    )
    
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {},
            readOnly = true,
            label = {
                Text(
                    text = "Select category",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF667EEA),
                    fontWeight = FontWeight.SemiBold
                )
            },
            placeholder = {
                Text(
                    text = "Choose a category",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9CA3AF).copy(alpha = 0.7f)
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667EEA),
                unfocusedBorderColor = Color(0xFFE1E8FF),
                cursorColor = Color(0xFF667EEA),
                focusedLabelColor = Color(0xFF667EEA),
                unfocusedLabelColor = Color(0xFF8B9DC3),
                focusedTextColor = Color(0xFF1A1D29),
                unfocusedTextColor = Color(0xFF4A5568),
                focusedContainerColor = Color.White.copy(alpha = 0.8f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.6f)
            )
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(
                Color.White,
                shape = RoundedCornerShape(12.dp)
            )
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = category,
                            color = Color(0xFF2C3E50)
                        )
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PrioritySelector(
    selectedPriority: Priority,
    onPrioritySelected: (Priority) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Priority Level",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF667EEA),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
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
                    
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPrioritySelected(priority) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                priorityColor.copy(alpha = 0.12f)
                            } else {
                                Color(0xFFF8FAFC)
                            }
                        ),
                        border = if (isSelected) {
                            BorderStroke(2.dp, priorityColor)
                        } else {
                            BorderStroke(1.dp, Color(0xFFE2E8F0))
                        },
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isSelected) 6.dp else 2.dp
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
                                fontSize = 20.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            
                            Text(
                                text = priorityLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) priorityColor else Color(0xFF64748B),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}




@Composable
fun BasicInfoSection(viewModel: WishViewModel) {
    Column {
        Text(
            text = "📝 Basic Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF667EEA),
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        )

        OutlinedTextField(
            value = viewModel.wishTitleState,
            onValueChange = { viewModel.onWishTitleChanged(it) },
            label = {
                Text(
                    text = "Enter wish title",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF667EEA),
                    fontWeight = FontWeight.SemiBold
                )
            },
            placeholder = {
                Text(
                    text = "e.g., New Bike",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9CA3AF).copy(alpha = 0.8f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFF667EEA)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667EEA),
                unfocusedBorderColor = Color(0xFFE1E8FF),
                cursorColor = Color(0xFF667EEA),
                focusedLabelColor = Color(0xFF667EEA),
                unfocusedLabelColor = Color(0xFF8B9DC3),
                focusedTextColor = Color(0xFF1A1D29),
                unfocusedTextColor = Color(0xFF2D3748),
                focusedContainerColor = Color.White.copy(alpha = 0.8f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.6f)
            )
        )

        OutlinedTextField(
            value = viewModel.wishDescriptionState,
            onValueChange = { viewModel.onWishDescriptionChanged(it) },
            label = {
                Text(
                    text = "Enter wish description",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF667EEA),
                    fontWeight = FontWeight.SemiBold
                )
            },
            placeholder = {
                Text(
                    text = "e.g., Looking for a mountain bike with good suspension",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF9CA3AF).copy(alpha = 0.8f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Color(0xFF667EEA)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(28.dp),
            singleLine = false,
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF667EEA),
                unfocusedBorderColor = Color(0xFFE1E8FF),
                cursorColor = Color(0xFF667EEA),
                focusedLabelColor = Color(0xFF667EEA),
                unfocusedLabelColor = Color(0xFF8B9DC3),
                focusedTextColor = Color(0xFF1A1D29),
                unfocusedTextColor = Color(0xFF2D3748),
                focusedContainerColor = Color.White.copy(alpha = 0.8f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.6f)
            )
        )
    }
}

@Composable
fun PriceAndImageSection(viewModel: WishViewModel) {
    Column {
        Text(
            text = "💰 Price & Image",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF667EEA),
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        )

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

@Composable
fun CategoryAndTagsSection(viewModel: WishViewModel) {
    Column {
        Text(
            text = "🏷️ Categories & Tags",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF667EEA),
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        )

        CategoryDropdown(
            selectedCategory = viewModel.wishCategoryState,
            onCategorySelected = { viewModel.onWishCategoryChanged(it) }
        )

        VoiceInputField(
            value = viewModel.wishTagsState,
            onValueChanged = { viewModel.onWishTagsChanged(it) },
            label = "Enter tags (comma separated)",
            placeholder = "e.g., smartphone, android, budget"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WishTextFieldsPreview() {
    MyWishListAppTheme {
        WishTextFields(
            label = "Text",
            value = "Text",
            onValueChanged = {}
        )
    }
}
