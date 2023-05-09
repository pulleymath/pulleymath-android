package com.freewheelin.pulley.revision2023.repository

import android.content.Context
import com.freewheelin.pulley.activities.learning.tabFragment.book.FilterCategory
import com.freewheelin.pulley.activities.learning.tabFragment.book.FilterOrder
import com.freewheelin.pulley.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.core.API.ResponseModel.CommercialBook
import com.freewheelin.pulley.core.API.ResponseModel.CommercialSubject
import com.freewheelin.pulley.core.API.ResponseModel.MyBookList
import com.freewheelin.pulley.core.API.ResponseModel.RecommendBookList
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.revision2023.model.BookFilterElement
import com.freewheelin.pulley.revision2023.model.BookFilterElement.Type
import com.freewheelin.pulley.revision2023.model.BookFilterParent
import com.freewheelin.pulley.revision2023.model.BookFilterSection
//import com.freewheelin.pulley.revision2023.room.patternstudy.PatternStudyDao
//import com.freewheelin.pulley.revision2023.room.patternstudy.PatternStudyDatabase
import com.freewheelin.pulley.revision2023.service.PatternStudyApi
import com.freewheelin.pulley.revision2023.service.PatternStudyService
import com.google.gson.Gson
import io.reactivex.Completable
import kotlinx.coroutines.CoroutineScope

class PatternStudyRepository(val context: Context, private val applicationScope: CoroutineScope) {
    private val patternStudyApi: PatternStudyService by lazy { PatternStudyApi.patternStudyService() }
//    private val dao: PatternStudyDao = PatternStudyDatabase.getDatabase(context, applicationScope).patternStudyDao()

//    fun flowMyPlans(): Flow<List<Book>> {
//        return dao.getAllBooks(0)
//    }
//    suspend fun deleteMyPlans(book: Book) {
//        dao.deleteBook(book)
//    }
//    suspend fun upsertAllMyPlans(books: List<Book>) {
//        dao.upsertAll(books)
//    }
    suspend fun fetchMyPlans(): MyBookList? {
        return patternStudyApi.getPatternStudyPlanList().data
    }
    suspend fun fetchHistory(): List<Book> {
        return patternStudyApi.getPatternStudyHistory().data
    }

    suspend fun fetchRecommendBooks(): List<RecommendBookList>? {
        return patternStudyApi.getPatternStudyRecommendBookList().data
    }

    suspend fun fetchAllBookList(filter: String, order: String = FilterOrder.DEFAULT.text, category: String = FilterCategory.BOOK.text): List<Book>? {
        return patternStudyApi.getAllPatternStudyBookList(
            filter = filter,
            order = order,
            category = category,
        ).data
    }

    fun setPin(pieceId: Int, isPinned: Boolean): Completable {
        return patternStudyApi.setPin(pieceID = pieceId, isPinned = isPinned)
    }

    fun deleteFromMyBook(pieceId: Int): Completable {
        return patternStudyApi.deleteFromMyBook(pieceId)
    }

    suspend fun fetchWorkbook(subject: CommercialSubject?): List<CommercialBook> {
        return patternStudyApi.getCommercials(subject)
    }
    suspend fun fetchBookFilter(filter: BookFilterParent, topElement: BookFilterElement): List<BookFilterElement> {
        val filterList = mutableListOf(topElement)

        patternStudyApi.fetchBookFilter(filter.name).data.forEach {
            val filterTitle = it.filterTitle
            val header = BookFilterElement(Type.Header, filterTitle, null, null, null)
            val items = it.filterItems.map { item ->
                BookFilterElement(Type.Item, item.name, it.filterTitle, item.value, item.seq)
            }
                .sortedBy { it.seq }
            filterList.add(header)
            filterList += items
        }
        return filterList.toList()
    }
    suspend fun fetchOnlyBookFilter(filter: BookFilterParent): List<BookFilterSection> {
        return patternStudyApi.fetchBookFilter(filter.name).data
    }
}