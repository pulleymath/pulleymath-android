package com.freewheelin.pulley.revision2023.room.challenge

import androidx.room.*
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.revision2023.model.challenge.MainChallengeHeaderItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MainChallengeHeaderItemDao {

    @Query("SELECT * FROM main_challenge_header_item_table where studentId=:studentId")
    fun getAllHeaderItem(studentId: String = user?.studentID!!): Flow<List<MainChallengeHeaderItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: MainChallengeHeaderItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<MainChallengeHeaderItem>)

//    @Delete
    @Query("DELETE FROM main_challenge_header_item_table where id=:id AND studentId=:studentId")
    suspend fun delete(id: Int, studentId: String)
//    suspend fun delete(item: MainChallengeHeaderItem)

//    @Query("DELETE FROM main_challenge_header_item_table")
//    suspend fun deleteAll()

//    @Query("SELECT COUNT(*) FROM main_challenge_header_item_table WHERE id LIKE :id")
//    suspend fun compareInfo(id: Int): Int
}