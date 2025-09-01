package com.example.mywishlistapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mywishlistapp.Data.Milestone
import com.example.mywishlistapp.Data.Wish
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilestoneManager(
    goal: Wish,
    milestones: List<Milestone>,
    onMilestoneCompleted: (Long) -> Unit,
    onMilestoneUncompleted: (Long) -> Unit,
    onAddMilestone: (String, String, Long?) -> Unit,
    onEditMilestone: (Milestone) -> Unit,
    onDeleteMilestone: (Milestone) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddMilestoneDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with milestone count and progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = Color(0xFF667EEA),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Milestones",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF667EEA)
                        )
                        if (milestones.isNotEmpty()) {
                            val completed = milestones.count { it.isCompleted }
                            Text(
                                text = "$completed of ${milestones.size} completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
                
                IconButton(onClick = { showAddMilestoneDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Milestone",
                        tint = Color(0xFF667EEA)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (milestones.isEmpty()) {
                // Empty state
                EmptyMilestonesState(
                    onAddFirstMilestone = { showAddMilestoneDialog = true }
                )
            } else {
                // Milestone list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(milestones) { milestone ->
                        MilestoneItem(
                            milestone = milestone,
                            onToggleComplete = { 
                                if (milestone.isCompleted) {
                                    onMilestoneUncompleted(milestone.id)
                                } else {
                                    onMilestoneCompleted(milestone.id)
                                }
                            },
                            onEdit = { onEditMilestone(milestone) },
                            onDelete = { onDeleteMilestone(milestone) }
                        )
                    }
                }
            }
        }
    }
    
    // Add milestone dialog
    if (showAddMilestoneDialog) {
        AddMilestoneDialog(
            goalId = goal.id,
            onDismiss = { showAddMilestoneDialog = false },
            onConfirm = { title, description, targetDate ->
                onAddMilestone(title, description, targetDate)
                showAddMilestoneDialog = false
            }
        )
    }
}

@Composable
fun MilestoneItem(
    milestone: Milestone,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (milestone.isCompleted) 
                Color(0xFF10B981).copy(alpha = 0.1f) 
            else 
                Color(0xFFF8FAFC)
        ),
        border = if (milestone.isCompleted) 
            BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)) 
        else 
            BorderStroke(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Completion checkbox
            Checkbox(
                checked = milestone.isCompleted,
                onCheckedChange = { onToggleComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF10B981),
                    uncheckedColor = Color(0xFF94A3B8)
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Milestone content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (milestone.isCompleted) Color(0xFF64748B) else Color(0xFF1A1D29),
                    textDecoration = if (milestone.isCompleted) TextDecoration.LineThrough else null
                )
                
                if (milestone.description.isNotEmpty()) {
                    Text(
                        text = milestone.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                // Target date if set
                milestone.targetDate?.let { targetDate ->
                    val isOverdue = targetDate < System.currentTimeMillis() && !milestone.isCompleted
                    val dateText = dateFormat.format(Date(targetDate))
                    
                    Text(
                        text = "Due: $dateText",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverdue) Color(0xFFE74C3C) else Color(0xFF667EEA),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // Action buttons
            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF667EEA),
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE74C3C),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyMilestonesState(
    onAddFirstMilestone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎯",
            fontSize = 48.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "No milestones yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1D29)
        )
        
        Text(
            text = "Break down your goal into smaller, achievable steps",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onAddFirstMilestone,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF667EEA)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add First Milestone")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMilestoneDialog(
    goalId: Long,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var hasTargetDate by remember { mutableStateOf(false) }
    var targetDate by remember { mutableStateOf<Long?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Milestone",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Milestone Title") },
                    placeholder = { Text("e.g., Complete first chapter") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Additional details...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    minLines = 2
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasTargetDate,
                        onCheckedChange = { hasTargetDate = it }
                    )
                    Text(
                        text = "Set target date",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                if (hasTargetDate) {
                    // Target date picker would go here
                    // For now, just show a placeholder
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                    ) {
                        Text(
                            text = "📅 Date picker would go here",
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            title.trim(),
                            description.trim(),
                            if (hasTargetDate) targetDate else null
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add Milestone")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
