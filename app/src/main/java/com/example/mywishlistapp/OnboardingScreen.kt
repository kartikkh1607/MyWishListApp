package com.example.mywishlistapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: WishViewModel = viewModel(),
    onNavigateToDashboard: () -> Unit
) {
    var userName by remember { mutableStateOf("") }
    val isNameValid = userName.length >= 3
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    
    // Animation state
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
        // Auto-focus the text field after animation
        kotlinx.coroutines.delay(800)
        focusRequester.requestFocus()
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(600)) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(600)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Welcome emoji/icon
                    Text(
                        text = "🌱",
                        fontSize = 80.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    // Welcome title
                    Text(
                        text = stringResource(R.string.welcome_to_companion),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Subtitle
                    Text(
                        text = stringResource(R.string.onboarding_subtitle),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 48.dp)
                    )
                }
            }
            
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(
                    animationSpec = tween(600, delayMillis = 400)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Name input question
                    Text(
                        text = stringResource(R.string.what_should_we_call_you),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    // Name input field
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it.take(50) }, // Limit to 50 characters
                        label = { Text(stringResource(R.string.name_hint)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                if (isNameValid) {
                                    viewModel.saveUserName(userName.trim())
                                    onNavigateToDashboard()
                                }
                            }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    // Validation hint
                    if (userName.isNotEmpty() && !isNameValid) {
                        Text(
                            text = stringResource(R.string.name_too_short),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Continue button
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.saveUserName(userName.trim())
                            onNavigateToDashboard()
                        },
                        enabled = isNameValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.continue_button),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            fontSize = 16.sp
                        )
                    }
                    
                    // Encouraging message
                    Text(
                        text = stringResource(R.string.getting_started),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}
