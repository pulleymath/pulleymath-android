package com.freewheelin.pulley.revision2023.room.patternstudy

import androidx.room.*
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.revision2023.model.PriorConcept
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternStudyDao {

    @Query("SELECT * FROM plan_book_table where assignID > :assignId")
    fun getAllBooks(assignId: Int): Flow<List<Book>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(books: List<Book>)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("DELETE FROM plan_book_table")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM plan_book_table WHERE assignID LIKE :id")
    suspend fun compareInfo(id: Int): Int

}