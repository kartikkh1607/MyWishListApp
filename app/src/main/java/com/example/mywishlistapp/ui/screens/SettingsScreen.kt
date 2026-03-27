package com.example.mywishlistapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mywishlistapp.ui.WishViewModel
import com.example.mywishlistapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController, viewModel: WishViewModel) {
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showVersionDialog by remember { mutableStateOf(false) }
    // Fix #9 — state for help and feedback dialogs
    var showHelpDialog by remember { mutableStateOf(false) }

    // Fix #9 — needed to launch intents for Rate and Help
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    // Fix #7 — use theme palette tokens
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        TopAppBar(
            title = { Text("Settings", fontWeight = FontWeight.Bold) },
            windowInsets = WindowInsets(0.dp),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Data Management
            item {
                SettingsSection(title = "Data") {
                    SettingsClickableItem(
                        icon = Icons.Default.DeleteSweep,
                        title = "Clear All Data",
                        subtitle = "Permanently delete all wishes",
                        onClick = { showClearDataDialog = true },
                        textColor = AccentRed
                    )
                }
            }

            // About
            item {
                SettingsSection(title = "About") {
                    SettingsClickableItem(
                        icon = Icons.Default.Info,
                        title = "App Version",
                        subtitle = "1.0.0",
                        onClick = { showVersionDialog = true }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Fix #9 — Help now opens a dialog with contact info
                    SettingsClickableItem(
                        icon = Icons.AutoMirrored.Filled.Help,
                        title = "Help & Support",
                        subtitle = "Get help and contact support",
                        onClick = { showHelpDialog = true }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Fix #9 — Rate now launches Play Store (or browser fallback)
                    SettingsClickableItem(
                        icon = Icons.Default.Star,
                        title = "Rate This App",
                        subtitle = "Share your feedback on the app store",
                        onClick = {
                            val packageName = context.packageName
                            try {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=$packageName")
                                    )
                                )
                            } catch (e: android.content.ActivityNotFoundException) {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    // Clear Data Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data?") },
            text = { Text("Are you sure you want to delete all your wishes? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteAllWishes(); showClearDataDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) { Text("Clear", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Version Dialog
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
                Button(onClick = { showVersionDialog = false }) { Text("OK") }
            }
        )
    }

    // Fix #9 — Help dialog (was a no-op before)
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Help & Support") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Having trouble with MyWishListApp?")
                    Text("• Swipe left on a wish to delete it")
                    Text("• Swipe right on a wish to mark it complete")
                    Text("• Tap the + button to add a new wish")
                    Text("• Use the search icon to find specific wishes")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "For further help, contact us at support@mywishlist.app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showHelpDialog = false }) { Text("Got it") }
            }
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        // Fix #7 — use theme surface instead of hardcoded Color.White
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface
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
            // Fix #7 — use AccentRed token and theme primary, not hardcoded Color(0xFF667EEA)
            tint = if (textColor == AccentRed) AccentRed else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (textColor == AccentRed) AccentRed else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (textColor == AccentRed) AccentRed.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            // Fix #7 — use theme token
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}