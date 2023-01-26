package com.freewheelin.pulley.revision2023.room.cooking

import androidx.room.*
import com.freewheelin.pulley.revision2021.model.CookingInfo
import com.freewheelin.pulley.revision2023.model.PriorConcept
import kotlinx.coroutines.flow.Flow

@Dao
interface CookingInfoDao {

    @Query("SELECT * FROM lc_cooking_info_table where conceptCookingId=:conceptCookingId")
    fun getAllCookingInfo(conceptCookingId: Int): Flow<CookingInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cookingInfo: CookingInfo)

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun upsert(info: CookingInfo)

    @Delete
    suspend fun delete(info: CookingInfo)

    @Query("DELETE FROM lc_cooking_info_table")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM lc_cooking_info_table WHERE conceptCookingId LIKE :id")
    suspend fun compareInfo(id: String): Int
}