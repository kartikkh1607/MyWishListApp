package com.example.mywishlistapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController, viewModel: WishViewModel) {
    // State variables for settings
    var isDarkTheme by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var reminderSounds by remember { mutableStateOf(true) }
    var autoBackup by remember { mutableStateOf(false) }
    var biometricLock by remember { mutableStateOf(false) }
    
    // Dynamic theme colors based on dark mode selection
    val backgroundColor = if (isDarkTheme) BackgroundDark else BackgroundLight
    val surfaceColor = if (isDarkTheme) SurfaceDark else SurfaceWhite
    val textColor = if (isDarkTheme) Color.White else TextPrimary
    val secondaryTextColor = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else TextSecondary

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
                    SettingsSwitchItem(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Theme",
                        subtitle = "Enable dark mode for better viewing in low light",
                        checked = isDarkTheme,
                        onCheckedChange = { isDarkTheme = it }
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
                        icon = Icons.Default.VolumeUp,
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
                        onClick = {
                            // TODO: Implement export functionality
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SettingsClickableItem(
                        icon = Icons.Default.DeleteSweep,
                        title = "Clear All Data",
                        subtitle = "Permanently delete all wishes and data",
                        onClick = {
                            // TODO: Show confirmation dialog
                        },
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
                        onClick = { /* Show version info */ }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SettingsClickableItem(
                        icon = Icons.Default.Help,
                        title = "Help & Support",
                        subtitle = "Get help and contact support",
                        onClick = { /* Open help */ }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    SettingsClickableItem(
                        icon = Icons.Default.Star,
                        title = "Rate This App",
                        subtitle = "Share your feedback on the app store",
                        onClick = { /* Open app store rating */ }
                    )
                }
            }
        }
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
