package com.example.mywishlistapp.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL
import java.util.regex.Pattern

data class ScrapedData(
    val title: String = "",
    val price: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val category: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebScraperDialog(
    onDismiss: () -> Unit,
    onDataScraped: (ScrapedData) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var scrapedData by remember { mutableStateOf<ScrapedData?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
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
                        text = "Add from URL",
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
                
                // URL Input
                OutlinedTextField(
                    value = url,
                    onValueChange = { 
                        url = it
                        errorMessage = ""
                        scrapedData = null
                    },
                    label = { Text("Product URL") },
                    placeholder = { Text("Paste product link here...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "URL",
                            tint = Color(0xFF667EEA)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Scrape Button
                Button(
                    onClick = {
                        if (url.isNotEmpty()) {
                            scope.launch {
                                isLoading = true
                                errorMessage = ""
                                try {
                                    val data = scrapeWebsite(url)
                                    scrapedData = data
                                    if (data.title.isEmpty()) {
                                        errorMessage = "Could not extract product details from this URL"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Failed to load website: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = url.isNotEmpty() && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF667EEA)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isLoading) "Extracting..." else "Extract Details")
                }
                
                // Error Message
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
                
                // Scraped Data Preview
                scrapedData?.let { data ->
                    if (data.title.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
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
                                    text = "Extracted Details:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF667EEA),
                                    fontWeight = FontWeight.SemiBold
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Product Image
                                if (data.imageUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = data.imageUrl,
                                        contentDescription = "Product Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                            .background(
                                                Color(0xFFF5F5F5),
                                                RoundedCornerShape(8.dp)
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                                
                                // Product Title
                                Text(
                                    text = data.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1D29)
                                )
                                
                                // Price
                                if (data.price.isNotEmpty()) {
                                    Text(
                                        text = "Price: ${data.price}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                
                                // Description
                                if (data.description.isNotEmpty()) {
                                    Text(
                                        text = data.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF64748B),
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                            
                            Button(
                                onClick = {
                                    onDataScraped(data)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981)
                                )
                            ) {
                                Text("Use This")
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun scrapeWebsite(url: String): ScrapedData = withContext(Dispatchers.IO) {
    try {
        val connection = URL(url).openConnection()
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android)")
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        
        val html = connection.getInputStream().bufferedReader().use { it.readText() }
        
        // Extract title
        val title = extractTitle(html, url)
        
        // Extract price
        val price = extractPrice(html)
        
        // Extract image
        val imageUrl = extractImage(html, url)
        
        // Extract description
        val description = extractDescription(html)
        
        // Determine category based on URL or content
        val category = determineCategory(url, html)
        
        ScrapedData(
            title = title,
            price = price,
            imageUrl = imageUrl,
            description = description,
            category = category
        )
    } catch (e: IOException) {
        Log.e("WebScraper", "Failed to scrape: ${e.message}")
        throw e
    }
}

private fun extractTitle(html: String, url: String): String {
    // Try Open Graph title first
    val ogTitlePattern = Pattern.compile("<meta\\s+property=\"og:title\"\\s+content=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE)
    var matcher = ogTitlePattern.matcher(html)
    if (matcher.find()) {
        return cleanText(matcher.group(1) ?: "")
    }
    
    // Try regular title tag
    val titlePattern = Pattern.compile("<title[^>]*>([^<]+)</title>", Pattern.CASE_INSENSITIVE)
    matcher = titlePattern.matcher(html)
    if (matcher.find()) {
        return cleanText(matcher.group(1) ?: "")
    }
    
    // Try meta title
    val metaTitlePattern = Pattern.compile("<meta\\s+name=\"title\"\\s+content=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE)
    matcher = metaTitlePattern.matcher(html)
    if (matcher.find()) {
        return cleanText(matcher.group(1) ?: "")
    }
    
    return ""
}

private fun extractPrice(html: String): String {
    // Common price patterns
    val pricePatterns = listOf(
        Pattern.compile("\\$([0-9,]+(?:\\.[0-9]{2})?)"),
        Pattern.compile("USD\\s*([0-9,]+(?:\\.[0-9]{2})?)"),
        Pattern.compile("price[^>]*>\\s*\\$?([0-9,]+(?:\\.[0-9]{2})?)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\"price\"\\s*:\\s*\"?([0-9,]+(?:\\.[0-9]{2})?)\"?", Pattern.CASE_INSENSITIVE)
    )
    
    for (pattern in pricePatterns) {
        val matcher = pattern.matcher(html)
        if (matcher.find()) {
            val price = matcher.group(1)?.replace(",", "") ?: ""
            if (price.isNotEmpty()) {
                return price
            }
        }
    }
    
    return ""
}

private fun extractImage(html: String, baseUrl: String): String {
    // Try Open Graph image
    val ogImagePattern = Pattern.compile("<meta\\s+property=\"og:image\"\\s+content=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE)
    var matcher = ogImagePattern.matcher(html)
    if (matcher.find()) {
        val imageUrl = matcher.group(1) ?: ""
        return resolveUrl(imageUrl, baseUrl)
    }
    
    // Try Twitter card image
    val twitterImagePattern = Pattern.compile("<meta\\s+name=\"twitter:image\"\\s+content=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE)
    matcher = twitterImagePattern.matcher(html)
    if (matcher.find()) {
        val imageUrl = matcher.group(1) ?: ""
        return resolveUrl(imageUrl, baseUrl)
    }
    
    return ""
}

private fun extractDescription(html: String): String {
    // Try Open Graph description
    val ogDescPattern = Pattern.compile("<meta\\s+property=\"og:description\"\\s+content=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE)
    var matcher = ogDescPattern.matcher(html)
    if (matcher.find()) {
        return cleanText(matcher.group(1) ?: "").take(200)
    }
    
    // Try meta description
    val metaDescPattern = Pattern.compile("<meta\\s+name=\"description\"\\s+content=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE)
    matcher = metaDescPattern.matcher(html)
    if (matcher.find()) {
        return cleanText(matcher.group(1) ?: "").take(200)
    }
    
    return ""
}

private fun determineCategory(url: String, html: String): String {
    val urlLower = url.lowercase()
    
    return when {
        urlLower.contains("electronics") || urlLower.contains("tech") -> "Electronics"
        urlLower.contains("book") -> "Books"
        urlLower.contains("cloth") || urlLower.contains("fashion") -> "Fashion"
        urlLower.contains("home") || urlLower.contains("furniture") -> "Home"
        urlLower.contains("game") -> "Gaming"
        urlLower.contains("sport") || urlLower.contains("fitness") -> "Sports"
        urlLower.contains("food") -> "Food"
        urlLower.contains("travel") -> "Travel"
        else -> "Other"
    }
}

private fun cleanText(text: String): String {
    return text.trim()
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("\\n", " ")
        .replace("\\t", " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

private fun resolveUrl(imageUrl: String, baseUrl: String): String {
    return when {
        imageUrl.startsWith("http") -> imageUrl
        imageUrl.startsWith("//") -> "https:$imageUrl"
        imageUrl.startsWith("/") -> {
            val base = URL(baseUrl)
            "${base.protocol}://${base.host}$imageUrl"
        }
        else -> {
            val base = URL(baseUrl)
            "${base.protocol}://${base.host}/${imageUrl.removePrefix("./")}"
        }
    }
}