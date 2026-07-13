package com.sultan.findit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sultan.findit.data.local.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM items ORDER BY foundDate DESC, id DESC")
    fun observeAllItems(): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM items
        WHERE name LIKE '%' || :keyword || '%'
        OR description LIKE '%' || :keyword || '%'
        OR categoryName LIKE '%' || :keyword || '%'
        OR foundLocation LIKE '%' || :keyword || '%'
        OR pickupLocation LIKE '%' || :keyword || '%'
        OR foundDate LIKE '%' || :keyword || '%'
        OR characteristics LIKE '%' || :keyword || '%'
        ORDER BY foundDate DESC, id DESC
        """
    )
    fun searchItems(keyword: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    fun observeItemById(id: Int): Flow<ItemEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<ItemEntity>)

    @Query("DELETE FROM items")
    suspend fun deleteAllItems()

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deleteItemById(id: Int)
}