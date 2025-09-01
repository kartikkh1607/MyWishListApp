package com.example.mywishlistapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mywishlistapp.Data.Achievement
import com.example.mywishlistapp.Data.AchievementSystem
import com.example.mywishlistapp.Data.UserProfile

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    achievements: List<Achievement>,
    modifier: Modifier = Modifier,
    navController: androidx.navigation.NavHostController? = null
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF0F4FF),
                        Color(0xFFE8F0FE),
                        Color(0xFFF8FAFF)
                    )
                )
            )
    ) {
        item {
            // App Bar with back button
            AppBarView(
                title = "Profile & Achievements",
                onBackNavClicked = { navController?.navigateUp() }
            )
        }
        
        item {
            // Profile Header Card
            EnhancedProfileHeader(userProfile = userProfile)
        }
        
        item {
            // Experience Progress Section
            ExperienceProgressSection(userProfile = userProfile)
        }
        
        item {
            // User Stats Grid
            UserStatsGrid(userProfile = userProfile)
        }
        
        item {
            // Achievements Section
            AchievementsSection(
                userProfile = userProfile,
                achievements = achievements
            )
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun EnhancedProfileHeader(userProfile: UserProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Avatar with level indicator
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // Avatar background
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF667EEA),
                                    Color(0xFF764BA2)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                // Level badge
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF39C12)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile.level.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Username
            Text(
                text = userProfile.username,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF667EEA)
            )
            
            // User title or description
            Text(
                text = "Wish List Achiever Level ${userProfile.level}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
fun ExperienceProgressSection(userProfile: UserProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Experience Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF667EEA)
                )
                
                Text(
                    text = "${userProfile.experiencePoints} XP",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Animated Progress Bar
            AnimatedExperienceProgressBar(
                currentXP = userProfile.experiencePoints,
                level = userProfile.level
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val currentLevelXP = (userProfile.level - 1) * 1000
                val nextLevelXP = userProfile.level * 1000
                val progressInLevel = userProfile.experiencePoints - currentLevelXP
                val progressNeeded = nextLevelXP - currentLevelXP
                
                Text(
                    text = "Level ${userProfile.level}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
                
                Text(
                    text = "${progressNeeded - progressInLevel} XP to Level ${userProfile.level + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
fun AnimatedExperienceProgressBar(
    currentXP: Int,
    level: Int
) {
    val currentLevelXP = (level - 1) * 1000
    val nextLevelXP = level * 1000
    val progressInLevel = currentXP - currentLevelXP
    val totalLevelXP = nextLevelXP - currentLevelXP
    val targetProgress = (progressInLevel.toFloat() / totalLevelXP.toFloat()).coerceIn(0f, 1f)
    
    // Animated progress value
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "xp_progress"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFE5E7EB))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF667EEA),
                            Color(0xFF764BA2)
                        )
                    )
                )
        )
    }
}

@Composable
fun UserStatsGrid(userProfile: UserProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Your Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF667EEA),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnhancedStatItem(
                    label = "Total Wishes",
                    value = userProfile.totalWishes.toString(),
                    icon = Icons.Default.Star,
                    color = Color(0xFF667EEA)
                )
                EnhancedStatItem(
                    label = "Completed",
                    value = userProfile.completedWishes.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF10B981)
                )
                EnhancedStatItem(
                    label = "Success Rate",
                    value = if (userProfile.totalWishes > 0) 
                        "${(userProfile.completedWishes * 100 / userProfile.totalWishes)}%" 
                    else "0%",
                    icon = Icons.Default.TrendingUp,
                    color = Color(0xFFF39C12)
                )
            }
        }
    }
}

@Composable
fun EnhancedStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.1f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1D29)
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AchievementsSection(
    userProfile: UserProfile,
    achievements: List<Achievement>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Achievements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF667EEA)
                )
                
                val unlockedCount = userProfile.earnedBadges.size
                val totalCount = AchievementSystem.achievements.size
                
                Text(
                    text = "$unlockedCount/$totalCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val badgeList = AchievementSystem.achievements.map { achievement ->
                achievement.copy(isUnlocked = userProfile.earnedBadges.contains(achievement.id))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(badgeList) { badge ->
                    EnhancedBadgeItem(badge = badge)
                }
            }
        }
    }
}

@Composable
fun EnhancedBadgeItem(badge: Achievement) {
    val isUnlocked = badge.isUnlocked
    
    // Animation for unlock state
    val scale by animateFloatAsState(
        targetValue = if (isUnlocked) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "badge_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isUnlocked) 1f else 0.4f,
        animationSpec = tween(300),
        label = "badge_alpha"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .alpha(alpha)
    ) {
        // Badge container
        Card(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUnlocked) {
                    Color(0xFFF39C12).copy(alpha = 0.1f)
                } else {
                    Color(0xFFE5E7EB)
                }
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isUnlocked) 6.dp else 2.dp
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge.icon,
                    fontSize = 28.sp,
                    color = if (isUnlocked) Color(0xFFF39C12) else Color(0xFF9CA3AF)
                )
                
                // Unlock indicator
                if (isUnlocked) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Unlocked",
                        tint = Color(0xFF10B981),
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = badge.title,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = if (isUnlocked) Color(0xFF1A1D29) else Color(0xFF9CA3AF),
            fontWeight = if (isUnlocked) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.width(64.dp)
        )
    }
}

// Legacy components for backward compatibility

@Composable
fun BadgeItem(badge: Achievement) {
    val alpha = if (badge.isUnlocked) 1f else 0.3f
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(alpha)
    ) {
        Text(text = badge.icon, fontSize = 36.sp)
        Text(
            text = badge.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(64.dp),
            textAlign = TextAlign.Center
        )
    }
}
