package com.example.mywishlistapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mywishlistapp.ui.theme.*

// Theme options enum
enum class ThemeOption(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("Follow System")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController, viewModel: WishViewModel) {
    // Theme state from ViewModel
    val currentThemeString by viewModel.currentTheme.collectAsState()
    val selectedTheme = when (currentThemeString) {
        "Light" -> ThemeOption.LIGHT
        "Dark" -> ThemeOption.DARK
        else -> ThemeOption.SYSTEM
    }
    
    // State variables for other settings
    var notificationsEnabled by remember { mutableStateOf(true) }
    var reminderSounds by remember { mutableStateOf(true) }
    var autoBackup by remember { mutableStateOf(false) }
    var biometricLock by remember { mutableStateOf(false) }
    
    // Dialog states
    var showExportDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showVersionDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    
    // Derived state for theme colors based on selected theme
    val isDarkTheme = when (selectedTheme) {
        ThemeOption.LIGHT -> false
        ThemeOption.DARK -> true
        ThemeOption.SYSTEM -> false // You could implement system theme detection here
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDarkTheme) {
                        listOf(
                            BackgroundDark,
                            SurfaceDark
                        )
                    } else {
                        listOf(
                            BackgroundLight,
                            BackgroundSecondary
                        )
                    }
                )
            )
    ) {
        // Enhanced App Bar matching other screens
        AppBarView(
            title = "Settings",
            onBackNavClicked = { navController.navigateUp() }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Appearance Section
            item {
                SettingsSection(title = "Appearance") {
                    SettingsClickableItem(
                        icon = Icons.Default.Palette,
                        title = "App Theme",
                        subtitle = "Currently using ${selectedTheme.displayName} theme",
                        onClick = { showThemeDialog = true }
                    )
                }
            }

            // Notifications Section
            item {
                SettingsSection(title = "Notifications") {
                    SettingsSwitchItem(
                        icon = Icons.Default.Notifications,
                        title = "Push Notifications",
                        subtitle = "Receive notifications for wish reminders",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SettingsSwitchItem(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = "Reminder Sounds",
                        subtitle = "Play sound with reminder notifications",
                        checked = reminderSounds,
                        onCheckedChange = { reminderSounds = it },
                        enabled = notificationsEnabled
                    )
                }
            }

            // Privacy & Security Section
            item {
                SettingsSection(title = "Privacy & Security") {
                    SettingsSwitchItem(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric Lock",
                        subtitle = "Use fingerprint or face unlock to secure your wishes",
                        checked = biometricLock,
                        onCheckedChange = { biometricLock = it }
                    )
                }
            }

            // Data Management Section
            item {
                SettingsSection(title = "Data Management") {
                    SettingsSwitchItem(
                        icon = Icons.Default.CloudUpload,
                        title = "Auto Backup",
                        subtitle = "Automatically backup your wishes to cloud storage",
                        checked = autoBackup,
                        onCheckedChange = { autoBackup = it }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SettingsClickableItem(
                        icon = Icons.Default.Download,
                        title = "Export Data",
                        subtitle = "Export your wishes to a file",
                        onClick = { showExportDialog = true }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SettingsClickableItem(
                        icon = Icons.Default.DeleteSweep,
                        title = "Clear All Data",
                        subtitle = "Permanently delete all wishes and data",
                        onClick = { showClearDataDialog = true },
                        textColor = AccentRed
                    )
                }
            }

            // About Section
            item {
                SettingsSection(title = "About") {
                    SettingsClickableItem(
                        icon = Icons.Default.Info,
                        title = "App Version",
                        subtitle = "1.0.0",
                        onClick = { showVersionDialog = true }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SettingsClickableItem(
                        icon = Icons.AutoMirrored.Filled.Help,
                        title = "Help & Support",
                        subtitle = "Get help and contact support",
                        onClick = { /* TODO: Open help */ }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SettingsClickableItem(
                        icon = Icons.Default.Star,
                        title = "Rate This App",
                        subtitle = "Share your feedback on the app store",
                        onClick = { /* TODO: Open app store rating */ }
                    )
                }
            }
        }
    }
    
    // Dialogs
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Data") },
            text = { Text("This feature will allow you to export your wishes to a JSON file.") },
            confirmButton = {
                Button(onClick = { 
                    // TODO: Implement export functionality
                    showExportDialog = false 
                }) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data?") },
            text = { Text("Are you sure you want to delete all your wishes and data? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Clear Data", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showVersionDialog) {
        AlertDialog(
            onDismissRequest = { showVersionDialog = false },
            title = { Text("App Version") },
            text = { 
                Column {
                    Text("MyWishListApp")
                    Text("Version 1.0.0")
                    Text("Built with ❤️ using Jetpack Compose")
                }
            },
            confirmButton = {
                Button(onClick = { showVersionDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = selectedTheme,
            onThemeSelected = { theme ->
                viewModel.setTheme(theme.name)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF667EEA),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (enabled) Color(0xFF667EEA) else Color(0xFF667EEA).copy(alpha = 0.4f)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (enabled) Color(0xFF1A1D29) else Color(0xFF1A1D29).copy(alpha = 0.4f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) Color(0xFF64748B) else Color(0xFF64748B).copy(alpha = 0.4f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (textColor == AccentRed) AccentRed else Color(0xFF667EEA)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (textColor == AccentRed) AccentRed else Color(0xFF1A1D29)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (textColor == AccentRed) AccentRed.copy(alpha = 0.7f) else Color(0xFF64748B),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color(0xFF94A3B8)
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeOption,
    onThemeSelected: (ThemeOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = Color(0xFF667EEA),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Choose Theme",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF667EEA)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Select your preferred theme for the app",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                ThemeOption.values().forEach { theme ->
                    ThemeOptionItem(
                        theme = theme,
                        isSelected = currentTheme == theme,
                        onSelected = { onThemeSelected(theme) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Done",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}

@Composable
fun ThemeOptionItem(
    theme: ThemeOption,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val (icon, description) = when (theme) {
        ThemeOption.LIGHT -> Icons.Default.LightMode to "Light theme with bright colors"
        ThemeOption.DARK -> Icons.Default.DarkMode to "Dark theme to reduce eye strain"
        ThemeOption.SYSTEM -> Icons.Default.PhoneAndroid to "Follow your device's system theme"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelected() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelected,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF667EEA),
                unselectedColor = Color(0xFF94A3B8)
            )
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) Color(0xFF667EEA) else Color(0xFF94A3B8),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = theme.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) Color(0xFF1A1D29) else Color(0xFF64748B)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) Color(0xFF64748B) else Color(0xFF94A3B8)
            )
        }
    }
}
