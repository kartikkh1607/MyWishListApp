package com.example.mywishlistapp.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mywishlistapp.Data.Wish

data class ShareOption(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val action: (Context, String) -> Unit
)

@Composable
fun SocialSharingBottomSheet(
    wish: Wish,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    val shareOptions = listOf(
        ShareOption(
            name = "WhatsApp",
            icon = Icons.Default.Chat,
            color = Color(0xFF25D366)
        ) { ctx, text ->
            shareToWhatsApp(ctx, text)
        },
        ShareOption(
            name = "Messages",
            icon = Icons.Default.Sms,
            color = Color(0xFF007AFF)
        ) { ctx, text ->
            shareToMessages(ctx, text)
        },
        ShareOption(
            name = "Email",
            icon = Icons.Default.Email,
            color = Color(0xFFEA4335)
        ) { ctx, text ->
            shareToEmail(ctx, text, wish.title)
        },
        ShareOption(
            name = "Copy",
            icon = Icons.Default.ContentCopy,
            color = Color(0xFF6B7280)
        ) { ctx, text ->
            copyToClipboard(ctx, text)
        },
        ShareOption(
            name = "More",
            icon = Icons.Default.Share,
            color = Color(0xFF667EEA)
        ) { ctx, text ->
            shareGeneral(ctx, text, wish.title)
        }
    )
    
    val shareText = buildShareText(wish)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Share Wish",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1D29)
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF64748B)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Wish preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8FAFF)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = wish.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1D29)
                    )
                    
                    if (wish.description.isNotEmpty()) {
                        Text(
                            text = wish.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    if (wish.price.isNotEmpty()) {
                        Text(
                            text = "Price: $${wish.price}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Share options
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(shareOptions) { option ->
                    ShareOptionItem(
                        option = option,
                        onClick = {
                            option.action(context, shareText)
                            if (option.name != "Copy") {
                                onDismiss()
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ShareOptionItem(
    option: ShareOption,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(option.color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.name,
                tint = option.color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = option.name,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF64748B)
        )
    }
}

private fun buildShareText(wish: Wish): String {
    val sb = StringBuilder()
    sb.append("Check out this item on my wishlist! 🌟\n\n")
    sb.append("${wish.title}\n")
    
    if (wish.description.isNotEmpty()) {
        sb.append("${wish.description}\n")
    }
    
    if (wish.price.isNotEmpty()) {
        sb.append("Price: $${wish.price}\n")
    }
    
    if (wish.category.isNotEmpty()) {
        sb.append("Category: ${wish.category}\n")
    }
    
    sb.append("\nShared from My Wishlist App 📱")
    
    return sb.toString()
}

private fun shareToWhatsApp(context: Context, text: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            `package` = "com.whatsapp"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        shareGeneral(context, text, "Share Wish")
    }
}

private fun shareToMessages(context: Context, text: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra("sms_body", text)
        }
        context.startActivity(Intent.createChooser(intent, "Share via Messages"))
    } catch (e: Exception) {
        shareGeneral(context, text, "Share Wish")
    }
}

private fun shareToEmail(context: Context, text: String, subject: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf<String>())
            putExtra(Intent.EXTRA_SUBJECT, "Wishlist Item: $subject")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share via Email"))
    } catch (e: Exception) {
        shareGeneral(context, text, subject)
    }
}

private fun copyToClipboard(context: Context, text: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Wish", text)
        clipboard.setPrimaryClip(clip)
        
        // Show toast or snackbar to confirm copy
        android.widget.Toast.makeText(context, "Copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        // Handle error silently or show error message
    }
}

private fun shareGeneral(context: Context, text: String, subject: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        context.startActivity(Intent.createChooser(intent, "Share Wish"))
    } catch (e: Exception) {
        // Handle error silently
    }
}