package com.freewheelin.pulley.revision2023.room.studymemo

import androidx.room.*
import com.freewheelin.pulley.revision2023.model.StudyMemo
import com.freewheelin.pulley.revision2023.model.StudyMemoCase
import com.freewheelin.pulley.revision2023.model.StudyMemoOS

@Dao
interface StudyMemoDao {

    @Query("select * from study_memo")
    fun getAll(): List<StudyMemo>

    @Query("select * from study_memo where mainId=:mainId limit 1")
    fun get(mainId:String): StudyMemo?

    @Query("select * from study_memo where studentId=:studentId and mainId=:mainId and subId=:subId and os=:os and memoCase=:memoCase limit 1")
    fun getFromParams(studentId:String, mainId:Int, subId: Int, os: StudyMemoOS, memoCase: StudyMemoCase): StudyMemo?

    @Query("select count(*) from study_memo where studentId=:studentId and mainId=:assignId")
    fun countMemo(studentId:String, assignId:Int): Int

    @Query("SELECT COALESCE(MAX(id), 0) FROM study_memo;")
    fun getMaxId(): Int
//    @Query("select exists ( select 1 from study_memo where studentId=:studentId and mainId=:mainId and subId=:subId and memoCase=:memoCase)")
//    fun existMemo(studentId: String, mainId: Int, subId: Int, os: StudyMemoOS, memoCase: StudyMemoCase): Boolean
//    @Query("select max(createdAt) from study_memo where studentId=:studentId")
//    fun getLatestTimestamp(studentId:String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(memo: List<StudyMemo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertOne(memo: StudyMemo)

    @Delete
    fun delete(memo: StudyMemo)

//    @Query("DELETE FROM study_memo WHERE id=:memoId")
//    fun delete(memoId: String)
}