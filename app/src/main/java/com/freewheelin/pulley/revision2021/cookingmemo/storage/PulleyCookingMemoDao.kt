package com.freewheelin.pulley.revision2021.cookingmemo.storage

import androidx.room.*
import com.freewheelin.pulley.revision2021.cookingmemo.storage.PulleyCookingMemo

@Dao
interface PulleyCookingMemoDao {
    @Query("select * from pulley_cooking_memo")
    fun getAll(): List<PulleyCookingMemo>

    @Query("select * from pulley_cooking_memo where id=:memoId limit 1")
    fun get(memoId:String): PulleyCookingMemo?

//    @Query("select count(*) from pulley_cooking_memo where student_id=:studentId and pdf_id=:pdfId")
//    fun countPdf(studentId:String, pdfId:Int): Int

    @Query("select max(updated_at) from pulley_cooking_memo where student_id=:studentId")
    fun getLatestTimestamp(studentId:String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(memo: List<PulleyCookingMemo>)

    @Delete
    fun delete(memo: PulleyCookingMemo)

    @Query("DELETE FROM pulley_cooking_memo WHERE id=:memoId")
    fun delete(memoId: String)
}