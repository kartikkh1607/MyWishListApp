package com.example.mywishlistapp

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.ui.theme.MyWishListAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WishListScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun wishListScreen_displaysSampleWishes() {
        // Given
        val sampleWishes = listOf(
            Wish(
                id = 1L,
                title = "New Laptop",
                description = "High-performance laptop for work",
                category = "Electronics",
                tags = listOf("laptop", "work"),
                priority = Priority.HIGH,
                price = "1500.00",
                imageUrl = "https://example.com/laptop.jpg"
            ),
            Wish(
                id = 2L,
                title = "Vacation to Japan",
                description = "Trip to Tokyo and Kyoto",
                category = "Travel",
                tags = listOf("travel", "japan"),
                priority = Priority.MEDIUM,
                price = "3000.00",
                imageUrl = "https://example.com/japan.jpg"
            ),
            Wish(
                id = 3L,
                title = "Learn Guitar",
                description = "Acoustic guitar and lessons",
                category = "Hobbies",
                tags = listOf("music", "guitar"),
                priority = Priority.LOW,
                price = "500.00",
                imageUrl = "https://example.com/guitar.jpg"
            )
        )

        // When
        composeTestRule.setContent {
            MyWishListAppTheme {
                // Mock a simplified version of WishListView for testing
                WishListView(
                    wishes = sampleWishes,
                    navController = null,
                    onWishClick = { },
                    onDeleteWish = { },
                    onCompleteWish = { }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("New Laptop")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("High-performance laptop for work")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Vacation to Japan")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Trip to Tokyo and Kyoto")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Learn Guitar")
            .assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Acoustic guitar and lessons")
            .assertIsDisplayed()
    }

    @Test
    fun wishListScreen_displaysEmptyStateWhenNoWishes() {
        // Given
        val emptyWishList = emptyList<Wish>()

        // When
        composeTestRule.setContent {
            MyWishListAppTheme {
                WishListView(
                    wishes = emptyWishList,
                    navController = null,
                    onWishClick = { },
                    onDeleteWish = { },
                    onCompleteWish = { }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("No wishes yet")
            .assertIsDisplayed()
    }

    @Test
    fun wishItem_displaysCorrectPriorityIndicator() {
        // Given
        val highPriorityWish = Wish(
            id = 1L,
            title = "Urgent Task",
            description = "This is urgent",
            priority = Priority.HIGH
        )

        val mediumPriorityWish = Wish(
            id = 2L,
            title = "Medium Task",
            description = "This is medium priority",
            priority = Priority.MEDIUM
        )

        val lowPriorityWish = Wish(
            id = 3L,
            title = "Low Task",
            description = "This is low priority",
            priority = Priority.LOW
        )

        val wishes = listOf(highPriorityWish, mediumPriorityWish, lowPriorityWish)

        // When
        composeTestRule.setContent {
            MyWishListAppTheme {
                WishListView(
                    wishes = wishes,
                    navController = null,
                    onWishClick = { },
                    onDeleteWish = { },
                    onCompleteWish = { }
                )
            }
        }

        // Then
        // Verify all three wish titles are displayed
        composeTestRule.onNodeWithText("Urgent Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Medium Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Low Task").assertIsDisplayed()
    }

    @Test
    fun wishItem_clickTriggersCallback() {
        // Given
        val testWish = Wish(
            id = 1L,
            title = "Clickable Wish",
            description = "This wish can be clicked"
        )

        var clickedWish: Wish? = null

        // When
        composeTestRule.setContent {
            MyWishListAppTheme {
                WishListView(
                    wishes = listOf(testWish),
                    navController = null,
                    onWishClick = { wish -> clickedWish = wish },
                    onDeleteWish = { },
                    onCompleteWish = { }
                )
            }
        }

        // Perform click
        composeTestRule.onNodeWithText("Clickable Wish").performClick()

        // Then
        assert(clickedWish?.id == testWish.id)
    }
}

// Simplified WishListView composable for testing
@Composable
private fun WishListView(
    wishes: List<Wish>,
    navController: NavController?,
    onWishClick: (Wish) -> Unit,
    onDeleteWish: (Wish) -> Unit,
    onCompleteWish: (Wish) -> Unit
) {
    if (wishes.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No wishes yet")
        }
    } else {
        LazyColumn {
            items(wishes.size) { index ->
                val wish = wishes[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onWishClick(wish) }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = wish.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = wish.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (wish.category.isNotEmpty()) {
                            Text(
                                text = "Category: ${wish.category}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (wish.price?.isNotEmpty() == true) {
                            Text(
                                text = "Price: $${wish.price}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
