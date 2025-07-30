package com.example.mywishlistapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // App Bar with back button
        AppBarView(
            title = "Profile & Achievements",
            onBackNavClicked = { navController?.navigateUp() }
        )
        
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
        Text(
            text = "Profile: ${userProfile.username}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // User Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem(label = "Wishes", value = userProfile.totalWishes.toString())
            StatItem(label = "Completed", value = userProfile.completedWishes.toString())
            StatItem(label = "Level", value = userProfile.level.toString())
            StatItem(label = "XP", value = userProfile.experiencePoints.toString())
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Badges",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        val badgeList = AchievementSystem.achievements.map { achievement ->
            achievement.copy(isUnlocked = userProfile.earnedBadges.contains(achievement.id))
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(badgeList) { badge ->
                BadgeItem(badge = badge)
            }
        }
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
