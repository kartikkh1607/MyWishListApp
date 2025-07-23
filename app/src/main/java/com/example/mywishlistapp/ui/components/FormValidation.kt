package com.example.mywishlistapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ✅ Data model for validation results
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

// ✅ State class for validation status
sealed class ValidationState {
    object Idle : ValidationState()
    object Validating : ValidationState()
    object Valid : ValidationState()
    data class Invalid(val message: String) : ValidationState()
}

@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    validator: (String) -> ValidationResult = { ValidationResult(true) },
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var validationState by remember { mutableStateOf<ValidationState>(ValidationState.Idle) }
    var isFocused by remember { mutableStateOf(false) }
    var hasBeenTouched by remember { mutableStateOf(false) }

    // ✅ Real-time validation with debounce
    LaunchedEffect(value) {
        if (hasBeenTouched && value.isNotEmpty()) {
            validationState = ValidationState.Validating
            delay(300) // Debounce delay

            val result = validator(value)
            validationState = if (result.isValid) {
                ValidationState.Valid
            } else {
                ValidationState.Invalid(result.errorMessage ?: "Invalid input")
            }
        } else if (value.isEmpty() && hasBeenTouched) {
            validationState = ValidationState.Invalid("This field is required")
        }
    }

    // ✅ Border color animation with custom colors
    val borderColor by animateColorAsState(
        targetValue = when (validationState) {
            ValidationState.Valid -> Color(0xFF10B981) // Success green
            is ValidationState.Invalid -> Color(0xFFEF4444) // Error red
            ValidationState.Validating -> Color(0xFF667EEA) // Primary purple
            ValidationState.Idle -> if (isFocused) Color(0xFF667EEA) else Color(0xFFE1E8FF) // Primary or light border
        },
        animationSpec = tween(200),
        label = "border_color"
    )

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        )
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(backgroundBrush)
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(28.dp)
                )
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    if (!hasBeenTouched) hasBeenTouched = true
                    onValueChange(newValue)
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (validationState) {
                            ValidationState.Valid -> Color(0xFF10B981) // Success green
                            is ValidationState.Invalid -> Color(0xFFEF4444) // Error red
                            ValidationState.Validating -> Color(0xFF667EEA) // Primary purple
                            else -> Color(0xFF667EEA) // Primary purple for idle
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                },
                placeholder = {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = leadingIcon?.let { icon ->
                    {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = when (validationState) {
                                ValidationState.Valid -> Color(0xFF10B981) // Success green
                                is ValidationState.Invalid -> Color(0xFFEF4444) // Error red
                                ValidationState.Validating -> Color(0xFF667EEA) // Primary purple
                                else -> Color(0xFF667EEA) // Primary purple for idle
                            }
                        )
                    }
                },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        when (validationState) {
                            ValidationState.Validating -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            ValidationState.Valid -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Valid",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            is ValidationState.Invalid -> {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Invalid",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            else -> {}
                        }

                        trailingIcon?.let { icon ->
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                        if (focusState.isFocused && !hasBeenTouched) {
                            hasBeenTouched = true
                        }
                    },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                shape = RoundedCornerShape(28.dp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                keyboardActions = keyboardActions,
                visualTransformation = visualTransformation,
                singleLine = singleLine,
                maxLines = maxLines,
                enabled = enabled
            )
        }

        // ✅ Animated error message
        AnimatedVisibility(
            visible = validationState is ValidationState.Invalid,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut()
        ) {
            if (validationState is ValidationState.Invalid) {
                Text(
                    text = (validationState as ValidationState.Invalid).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
    }
}

// ✅ Password field implementation
@Composable
fun ValidatedPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    validator: (String) -> ValidationResult = { ValidationResult(true) },
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }

    ValidatedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        validator = validator,
        leadingIcon = Icons.Default.Lock,
        trailingIcon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
        keyboardType = KeyboardType.Password,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        modifier = modifier
    )
}

// ✅ Validation Rules Object
object ValidationRules {
    fun required(message: String = "This field is required"): (String) -> ValidationResult = { value ->
        ValidationResult(isValid = value.isNotBlank(), errorMessage = if (value.isBlank()) message else null)
    }

    fun minLength(min: Int, message: String? = null): (String) -> ValidationResult = { value ->
        val isValid = value.length >= min
        ValidationResult(isValid, if (!isValid) (message ?: "Must be at least $min characters") else null)
    }

    fun maxLength(max: Int, message: String? = null): (String) -> ValidationResult = { value ->
        val isValid = value.length <= max
        ValidationResult(isValid, if (!isValid) (message ?: "Must be at most $max characters") else null)
    }

    fun email(message: String = "Please enter a valid email"): (String) -> ValidationResult = { value ->
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        ValidationResult(isValid = value.matches(emailRegex), errorMessage = if (!value.matches(emailRegex)) message else null)
    }

    fun combine(vararg validators: (String) -> ValidationResult): (String) -> ValidationResult = { value ->
        val firstError = validators.map { it(value) }.firstOrNull { !it.isValid }
        ValidationResult(isValid = firstError == null, errorMessage = firstError?.errorMessage)
    }
}

// ✅ Form Progress Indicator
@Composable
fun FormValidationIndicator(
    isFormValid: Boolean,
    totalFields: Int,
    validFields: Int,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (totalFields > 0) validFields.toFloat() / totalFields else 0f,
        animationSpec = tween(300),
        label = "progress"
    )

    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Form Progress", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Text("$validFields/$totalFields", style = MaterialTheme.typography.labelMedium, color = if (isFormValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = if (isFormValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            if (isFormValid) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text("Ready to submit!", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
