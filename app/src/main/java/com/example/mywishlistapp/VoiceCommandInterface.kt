package com.example.mywishlistapp

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.mywishlistapp.Data.Priority
import java.util.*

// Voice Command States
enum class VoiceState {
    IDLE, LISTENING, PROCESSING, COMPLETED, ERROR
}

data class VoiceCommand(
    val action: String,
    val title: String? = null,
    val description: String? = null,
    val category: String? = null,
    val priority: String? = null,
    val tags: List<String> = emptyList()
)

class VoiceCommandProcessor {
    companion object {
        fun parseVoiceInput(input: String): VoiceCommand? {
            val lowerInput = input.lowercase()
            
            return when {
                lowerInput.contains("add") || lowerInput.contains("create") -> {
                    extractWishDetails(lowerInput)
                }
                lowerInput.contains("search") || lowerInput.contains("find") -> {
                    VoiceCommand(action = "search", title = extractSearchTerm(lowerInput))
                }
                lowerInput.contains("delete") || lowerInput.contains("remove") -> {
                    VoiceCommand(action = "delete", title = extractWishTitle(lowerInput))
                }
                else -> null
            }
        }
        
        private fun extractWishDetails(input: String): VoiceCommand {
            val title = extractWishTitle(input)
            val description = extractDescription(input)
            val category = extractCategory(input)
            val priority = extractPriority(input)
            
            return VoiceCommand(
                action = "add",
                title = title,
                description = description,
                category = category,
                priority = priority
            )
        }
        
        private fun extractWishTitle(input: String): String? {
            // Simple extraction - can be enhanced with NLP
            val patterns = listOf(
                "add (.+?) to",
                "create (.+?) with",
                "I want (.+?)\\.",
                "add (.+?)$"
            )
            
            patterns.forEach { pattern ->
                val regex = pattern.toRegex()
                val match = regex.find(input)
                if (match != null) {
                    return match.groupValues[1].trim()
                }
            }
            
            return null
        }
        
        private fun extractDescription(input: String): String? {
            if (input.contains("description") || input.contains("details")) {
                val regex = "(?:description|details)\\s+(.+?)(?:\\s+category|\\s+priority|\$)".toRegex()
                return regex.find(input)?.groupValues?.get(1)?.trim()
            }
            return null
        }
        
        private fun extractCategory(input: String): String? {
            val categories = listOf("electronics", "travel", "gaming", "books", "sports", 
                "fashion", "home", "food", "health", "education")
            
            categories.forEach { category ->
                if (input.contains(category)) {
                    return category.replaceFirstChar { it.titlecase() }
                }
            }
            return null
        }
        
        private fun extractPriority(input: String): String? {
            return when {
                input.contains("high priority") || input.contains("urgent") -> "HIGH"
                input.contains("low priority") || input.contains("someday") -> "LOW"
                else -> "MEDIUM"
            }
        }
        
        private fun extractSearchTerm(input: String): String? {
            val regex = "(?:search|find)\\s+(.+?)$".toRegex()
            return regex.find(input)?.groupValues?.get(1)?.trim()
        }
    }
}

@Composable
fun VoiceInputField(
    value: String,
    onValueChanged: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth().padding(4.dp)) {
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
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
            trailingIcon = {
                IconButton(onClick = { showDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Mic, 
                        contentDescription = "Voice Input",
                        tint = Color(0xFF667EEA)
                    )
                }
            },
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

    if (showDialog) {
        VoiceCommandDialog(
            isVisible = true,
            onDismiss = { showDialog = false },
            onVoiceCommand = { voiceCommand ->
                if (voiceCommand.title != null) {
                    onValueChanged(voiceCommand.title)
                }
                showDialog = false
            },
            onWishAdd = { _, _, _, _, _ -> },
            onWishSearch = { _ -> }
        )
    }
}

@Composable
fun VoiceCommandUI(
    isListening: Boolean,
    voiceState: VoiceState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isListening) 1.2f else 1f,
        animationSpec = tween(300),
        label = "voice_scale"
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Voice Button
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    if (isListening) {
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFEF4444),
                                Color(0xFFDC2626)
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF667EEA),
                                Color(0xFF764BA2)
                            )
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = if (isListening) onStopListening else onStartListening
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop Listening" else "Start Voice Command",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status Text
        Text(
            text = when (voiceState) {
                VoiceState.IDLE -> "Tap to speak"
                VoiceState.LISTENING -> "Listening..."
                VoiceState.PROCESSING -> "Processing..."
                VoiceState.COMPLETED -> "Command executed!"
                VoiceState.ERROR -> "Try again"
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = when (voiceState) {
                VoiceState.ERROR -> Color(0xFFEF4444)
                VoiceState.COMPLETED -> Color(0xFF10B981)
                else -> Color(0xFF64748B)
            }
        )
        
        // Voice suggestions
        if (voiceState == VoiceState.IDLE) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Say: \"Add iPhone to my wishlist\" or \"Search for books\"",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun VoiceCommandDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onVoiceCommand: (VoiceCommand) -> Unit,
    onWishAdd: (String, String, String, Priority, List<String>) -> Unit,
    onWishSearch: (String) -> Unit
) {
    if (!isVisible) return
    
    val context = LocalContext.current
    var voiceState by remember { mutableStateOf(VoiceState.IDLE) }
    var isListening by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🎤 Voice Command",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            VoiceCommandUI(
                isListening = isListening,
                voiceState = voiceState,
                onStartListening = {
                    // Check permission first
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        isListening = true
                        voiceState = VoiceState.LISTENING
                        // TODO: Start actual speech recognition
                    } else {
                        voiceState = VoiceState.ERROR
                    }
                },
                onStopListening = {
                    isListening = false
                    voiceState = VoiceState.IDLE
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
