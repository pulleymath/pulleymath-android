package com.freewheelin.pulley.revision2023.room.challenge

//@Dao
//interface MainChallengeDetailDao {
//
//    @Query("SELECT * FROM main_challenge_detail_item_table")
//    fun getAllChallengeDetails(): Flow<List<MainChallengeDetailItem>>
//
//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    suspend fun insert(item: MainChallengeDetailItem)
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun upsertAll(items: List<MainChallengeDetailItem>)
//
//    @Delete
//    suspend fun delete(item: MainChallengeDetailItem)
//
////    @Query("DELETE FROM main_challenge_header_item_table")
////    suspend fun deleteAll()
//
//    @Query("SELECT COUNT(*) FROM main_challenge_detail_item_table WHERE challengeId LIKE :challengeId")
//    suspend fun compareInfo(challengeId: Int): Int
//}