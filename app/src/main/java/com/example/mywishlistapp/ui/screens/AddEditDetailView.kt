package com.example.mywishlistapp.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mywishlistapp.R
import com.example.mywishlistapp.ui.WishViewModel
import com.example.mywishlistapp.ui.theme.BackgroundLight
import com.example.mywishlistapp.ui.theme.BackgroundSecondary

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

    // Guard against double-tap / multiple navigateUp calls using a robust time-based decouncer
    var lastNavTime by remember { mutableStateOf(0L) }
    val safeNavigateUp: () -> Unit = {
        val now = System.currentTimeMillis()
        if (now - lastNavTime > 500L) {
            lastNavTime = now
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
                        onNavigateToHome = {
                            navController.navigate(Screen.DashboardScreen) {
                                popUpTo(Screen.DashboardScreen) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
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

