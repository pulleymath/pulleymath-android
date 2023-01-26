package com.freewheelin.pulley.revision2023.room.cookinginfoitem

import androidx.room.*
import com.freewheelin.pulley.revision2021.model.CookingInfo
import com.freewheelin.pulley.revision2021.model.CookingInfoItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CookingInfoItemDao {

    @Query("SELECT * FROM lc_cooking_info_item_table where cookingId=:cookingId")
    fun getAllCookingInfoItem(cookingId: Int): Flow<List<CookingInfoItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CookingInfoItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CookingInfoItem>)

    @Delete
    suspend fun delete(info: CookingInfoItem)

    @Query("DELETE FROM lc_cooking_info_item_table")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM lc_cooking_info_item_table WHERE itemId LIKE :id")
    suspend fun compareInfo(id: String): Int
}