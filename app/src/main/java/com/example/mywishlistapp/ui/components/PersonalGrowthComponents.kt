package com.example.mywishlistapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mywishlistapp.R
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ItemTypeSelector(
    isGoal: Boolean,
    onItemTypeChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    EnhancedSectionCard(
        title = "🎯 ${stringResource(R.string.item_type)}",
        subtitle = "Choose what this represents"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Wish FilterChip
            FilterChip(
                onClick = { onItemTypeChanged(false) },
                label = { 
                    Text(
                        stringResource(R.string.wish),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                selected = !isGoal,
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF667EEA).copy(alpha = 0.2f),
                    selectedLabelColor = Color(0xFF667EEA)
                ),
                leadingIcon = if (!isGoal) {
                    { Text("✨", fontSize = 16.sp) }
                } else null
            )
            
            // Goal FilterChip
            FilterChip(
                onClick = { onItemTypeChanged(true) },
                label = { 
                    Text(
                        stringResource(R.string.goal),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                selected = isGoal,
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                    selectedLabelColor = Color(0xFF10B981)
                ),
                leadingIcon = if (isGoal) {
                    { Text("🎯", fontSize = 16.sp) }
                } else null
            )
        }
        
        // Help text based on selection
        Text(
            text = if (isGoal) stringResource(R.string.goal_description_help) 
                  else stringResource(R.string.wish_description_help),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetDatePicker(
    targetDate: Long?,
    onTargetDateChanged: (Long?) -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        EnhancedSectionCard(
            title = "📅 ${stringResource(R.string.target_date)}",
            subtitle = "When do you want to achieve this?"
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF667EEA),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (targetDate != null) {
                                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                    .format(Date(targetDate))
                            } else {
                                stringResource(R.string.no_date_set)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (targetDate != null) Color(0xFF1A1D29) else Color(0xFF94A3B8),
                            fontWeight = if (targetDate != null) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                    
                    if (targetDate != null) {
                        IconButton(
                            onClick = { onTargetDateChanged(null) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.clear_date),
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDate ->
                onTargetDateChanged(selectedDate)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Calendar.getInstance().timeInMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                }
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun ProgressTracker(
    progress: Int,
    onProgressChanged: (Int) -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        EnhancedSectionCard(
            title = "📈 ${stringResource(R.string.track_progress)}",
            subtitle = "How much have you accomplished?"
        ) {
            Column {
                // Progress display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.progress_percentage, progress),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667EEA)
                    )
                    
                    // Progress circle or badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when {
                            progress >= 80 -> Color(0xFF10B981).copy(alpha = 0.2f)
                            progress >= 50 -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                            else -> Color(0xFF667EEA).copy(alpha = 0.2f)
                        },
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = when {
                                progress >= 80 -> "🎯"
                                progress >= 50 -> "💪"
                                else -> "🌱"
                            },
                            modifier = Modifier.padding(8.dp),
                            fontSize = 16.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress slider
                Slider(
                    value = progress.toFloat(),
                    onValueChange = { onProgressChanged(it.toInt()) },
                    valueRange = 0f..100f,
                    steps = 19, // Creates 20 steps (0, 5, 10, ..., 100)
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF667EEA),
                        activeTrackColor = Color(0xFF667EEA),
                        inactiveTrackColor = Color(0xFF667EEA).copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Progress markers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "0%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "50%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "100%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedSectionCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        isVisible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape = RoundedCornerShape(20.dp),
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
            // Section header
            Column(
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF667EEA)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            // Section content
            content()
        }
    }
}
