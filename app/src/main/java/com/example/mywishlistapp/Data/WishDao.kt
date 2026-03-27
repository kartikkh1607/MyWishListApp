package com.example.mywishlistapp.Data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WishDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addWish(wish: Wish)

    @Update
    suspend fun updateWish(wish: Wish)

    @Delete
    suspend fun deleteWish(wish: Wish)

    @Query("SELECT * FROM `Wish-Table` ORDER BY id DESC")
    fun getAllWishes(): Flow<List<Wish>>

    @Query("SELECT * FROM `Wish-Table` WHERE id = :id")
    fun getWishById(id: Long): Flow<Wish>

    @Query("DELETE FROM `Wish-Table`")
    suspend fun deleteAll()
}
