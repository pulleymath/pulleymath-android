package com.pulleymath.android.pdf.memo.storage

import androidx.room.*

@Dao
interface PdfMemoDao {
    @Query("select * from pdf_memo")
    fun getAll(): List<PdfMemo>

    @Query("select * from pdf_memo where id=:memoId limit 1")
    fun get(memoId:String): PdfMemo?

    @Query("select max(updated_at) from pdf_memo where student_id=:studentId")
    fun getLatestTimestamp(studentId:String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(memo: List<PdfMemo>)

    @Delete
    fun delete(memo: PdfMemo)
}