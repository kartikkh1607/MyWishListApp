package com.example.mywishlistapp.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "Wish-Table")
data class Wish(
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0L,
    @ColumnInfo(name = "wish-title")
    val title : String = "",
    @ColumnInfo(name = "wish-description")
    val description : String = ""
)

object DummyWish {
    val wishlist = listOf(
        Wish(
            title = "Android Phone",
            description = "Looking for a budget-friendly mid-range phone."
        ),
        Wish(
            title = "iPhone",
            description = "Want to try iOS experience, maybe iPhone 14 or newer."
        ),
        Wish(
            title = "PS5",
            description = "For 4K gaming — God of War is a must play!"
        ),
        Wish(
            title = "MacBook",
            description = "For Android & iOS dev — ideal for working with Xcode."
        ),
        Wish(
            title = "Trip to Europe",
            description = "Solo trip is needed"
        ),
        Wish(
            title = "New Headphones",
            description = "Noise cancelling, preferably Sony or Bose."
        )
    )
}