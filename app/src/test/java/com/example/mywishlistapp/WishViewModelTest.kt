package com.example.mywishlistapp

import android.app.Application
import android.content.Context
import com.example.mywishlistapp.Data.Priority
import com.example.mywishlistapp.Data.Wish
import com.example.mywishlistapp.Data.WishRepository
import com.example.mywishlistapp.Data.UserProfileRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.*

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class WishViewModelTest {

    @Mock
    private lateinit var mockWishRepository: WishRepository
    
    @Mock
    private lateinit var mockUserProfileRepository: UserProfileRepository
    
    private lateinit var wishViewModel: WishViewModel
    private lateinit var application: Application

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        // Use Robolectric application context
        application = RuntimeEnvironment.getApplication()
        
        // Initialize the viewmodel with mocked dependencies
        wishViewModel = WishViewModel(
            application = application,
            wishRepository = mockWishRepository,
            userProfileRepository = mockUserProfileRepository
        )
    }

    @Test
    fun `addWish should call repository addWish with correct wish`() = runTest {
        // Given
        val testWish = Wish(
            id = 0L,
            title = "Test Wish",
            description = "Test Description",
            category = "Electronics",
            tags = listOf("test", "wish"),
            priority = Priority.HIGH,
            price = "100.00",
            imageUrl = "https://example.com/image.jpg"
        )

        // When
        wishViewModel.addWish(testWish)

        // Then
        verify(mockWishRepository, times(1)).addWish(testWish)
    }

    @Test
    fun `onWishTitleChanged should update wishState title`() {
        // Given
        val newTitle = "New Title"

        // When
        wishViewModel.onWishTitleChanged(newTitle)

        // Then
        assert(wishViewModel.wishState.title == newTitle)
    }

    @Test
    fun `onWishDescriptionChanged should update wishState description`() {
        // Given
        val newDescription = "New Description"

        // When
        wishViewModel.onWishDescriptionChanged(newDescription)

        // Then
        assert(wishViewModel.wishState.description == newDescription)
    }

    @Test
    fun `onWishCategoryChanged should update wishState category`() {
        // Given
        val newCategory = "Books"

        // When
        wishViewModel.onWishCategoryChanged(newCategory)

        // Then
        assert(wishViewModel.wishState.category == newCategory)
    }

    @Test
    fun `onWishTagsChanged should update wishState tags`() {
        // Given
        val tagsString = "tag1, tag2, tag3"
        val expectedTags = listOf("tag1", "tag2", "tag3")

        // When
        wishViewModel.onWishTagsChanged(tagsString)

        // Then
        assert(wishViewModel.wishState.tags == expectedTags)
    }

    @Test
    fun `onWishPriorityChanged should update wishState priority`() {
        // Given
        val newPriority = Priority.LOW

        // When
        wishViewModel.onWishPriorityChanged(newPriority)

        // Then
        assert(wishViewModel.wishState.priority == newPriority)
    }

    @Test
    fun `onWishPriceChanged should update wishState price`() {
        // Given
        val newPrice = "99.99"

        // When
        wishViewModel.onWishPriceChanged(newPrice)

        // Then
        assert(wishViewModel.wishState.price == newPrice)
    }

    @Test
    fun `onWishImageUrlChanged should update wishState imageUrl`() {
        // Given
        val newImageUrl = "https://example.com/new-image.jpg"

        // When
        wishViewModel.onWishImageUrlChanged(newImageUrl)

        // Then
        assert(wishViewModel.wishState.imageUrl == newImageUrl)
    }

    @Test
    fun `updateWish should call repository updateWish with correct wish`() = runTest {
        // Given
        val testWish = Wish(
            id = 1L,
            title = "Updated Wish",
            description = "Updated Description",
            category = "Travel",
            tags = listOf("updated", "test"),
            priority = Priority.MEDIUM,
            price = "200.00",
            imageUrl = "https://example.com/updated-image.jpg"
        )

        // When
        wishViewModel.updateWish(testWish)

        // Then
        verify(mockWishRepository, times(1)).updateWish(testWish)
    }

    @Test
    fun `deleteWish should call repository deleteWish with correct wish`() = runTest {
        // Given
        val testWish = Wish(
            id = 1L,
            title = "Wish to Delete",
            description = "Description"
        )

        // When
        wishViewModel.deleteWish(testWish)

        // Then
        verify(mockWishRepository, times(1)).deleteWish(testWish)
    }
}
