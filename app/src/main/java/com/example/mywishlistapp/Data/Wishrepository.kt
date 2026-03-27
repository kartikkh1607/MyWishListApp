package com.example.mywishlistapp.Data

import kotlinx.coroutines.flow.Flow

class WishRepository(private val dao: WishDao) {

    fun getWishes(): Flow<List<Wish>> = dao.getAllWishes()

    fun getWishById(id: Long): Flow<Wish> = dao.getWishById(id)

    suspend fun addWish(wish: Wish) = dao.addWish(wish)

    suspend fun updateWish(wish: Wish) = dao.updateWish(wish)

    suspend fun deleteWish(wish: Wish) = dao.deleteWish(wish)

    suspend fun deleteAllWishes() = dao.deleteAll()
}
