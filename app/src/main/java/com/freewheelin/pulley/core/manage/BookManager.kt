package com.freewheelin.pulley.core.manage

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.activities.learning.tabFragment.book.FilterCategory
import com.freewheelin.pulley.activities.learning.tabFragment.book.FilterOrder
import com.freewheelin.pulley.activities.learning.tabFragment.book.FilterType
import com.freewheelin.pulley.core.API.ResponseModel.*
import com.freewheelin.pulley.core.API_V1
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.dialogs.WrongManagementDialog
import com.freewheelin.pulley.model.*
import com.freewheelin.pulley.model.contents.Book
import com.freewheelin.pulley.model.contents.BookPage
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.utils.responseError
import com.freewheelin.pulley.utils.responseFailed
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder

class ResponseBookList {
    val bookPlanDataList: List<Book> = listOf()
    val bookCategoryList: List<BookCategory>? = null
}

class ResponseBookInfo {
    val book: Book = Book()
    val bookPage: List<BookPage> = listOf()
}

class ResponseBookInfo2 {
    val bookPage: List<BookPage> = emptyList()
    val problemList: List<Problem> = emptyList()
    val assignID: Int? = null
}

class BookCategory {
    val bookSeries: String = ""
    val bookCategory: String? = ""
    val bookCategoryListID: Int = 0
    val containUploadNewPlan = false
    val scheduled: Boolean = false
}

object BookManager {
    const val ARG_BOOK = "ARG_BOOK"
    const val ARG_START_PROBLEM = "ARG_START_PROBLEM"
    const val ARG_NEED_LEADING = "ARG_NEED_LEADING"

    const val EVENT_BOOK_CLEAR = "EVENT_BOOK_CLEAR"
    const val EVENT_BOOK_SCORING = "EVENT_BOOK_SCORING"

    fun getNewPlanList(context: Context, user: User, cb: (books: List<Book>, categories: List<Pair<String, List<BookCategory>>>) -> Unit) {
        API_V2.getBookPlans(user.studentID).enqueue(object : Callback<ResponseBookList> {
            override fun onFailure(call: Call<ResponseBookList>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<ResponseBookList>, response: Response<ResponseBookList>) {
                if(response.isSuccessful) {
                    val books = response.body()?.bookPlanDataList ?: emptyList()
                    val categories = response.body()?.bookCategoryList ?: emptyList()
                    cb(books, convertCategoryByGroup(categories))
                } else {
                    responseError(context, response)
                }
            }
        })
    }

    fun getMyBookList(context: Context, user: User, cb: (list: MyBookList?) -> Unit) {
        API_V2.getMyBookList(user.studentID).enqueue(object: Callback<MyBookList> {
            override fun onFailure(call: Call<MyBookList>, t: Throwable) {}

            override fun onResponse(call: Call<MyBookList>, response: Response<MyBookList>) {
                if(response.isSuccessful)
                    cb(response.body())
            }
        })

    }

    fun assign(context: Context, book: Book, user: User, cb:(book: Book) -> Unit) {
        val param: Parameter = Parameter (
                "studentID" to user.studentID,
                "pieceID" to book.pieceID
        )

        API_V1.assignBook(param).enqueue(object: Callback<Template<Book>> {
            override fun onFailure(call: Call<Template<Book>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<Book>>, response: Response<Template<Book>>) {
                if(response.isSuccessful) {
                    val responseBook = response.body()!!.data
                    ContentManager.isNeedToSyncMyContentList = true
                    cb(responseBook)
                } else {

                }
            }
        })
    }

    fun getBook(context: Context, book: Book, user: User, cb: ((book: Book) -> Unit)) {
        API_V2.getBook(user.studentID, book.assignID ?: book.pieceID).enqueue(object: Callback<ResponseBookInfo2> {
            override fun onFailure(call: Call<ResponseBookInfo2>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<ResponseBookInfo2>, response: Response<ResponseBookInfo2>) {
                val responseBookPage = response.body()?.bookPage
                val responseProblems = response.body()?.problemList
                val responseAssignID = response.body()?.assignID

                if(response.isSuccessful && responseBookPage != null && responseProblems != null && responseAssignID != null) {
                    book.assignID = responseAssignID
                    book.bookPage = responseBookPage
                    book.problems = responseProblems
                    book.arrangeProblem()
                    book.arrangeChapter()
                    LogUtils.logEvent(context, user, PulleyEvent.INIT_TEST, "문제풀기", "유형학습 세팅","Log: 문항개수 0개\n" +
                            "param: ${"studentID: ${user.studentID}, id: ${book.assignID ?: book.pieceID}"}\n" +
                            "response: ${response.raw()}\n")
                    cb(book)

                }
            }
        })
    }

    fun getBooks(context: Context, user: User, filters: Set<FilterType>, cb: ((books: List<Book>, filters: Set<FilterType>) -> Unit)) {
        val filterString = filters.joinTo(StringBuilder(), separator = ",").toString()
        // Did
        API_V2.getBooksNew(user.studentID, filterString, FilterOrder.DEFAULT.text, FilterCategory.BOOK.text).enqueue(object: Callback<List<Book>> {
            override fun onFailure(call: Call<List<Book>>, t: Throwable) {}

            override fun onResponse(call: Call<List<Book>>, response: Response<List<Book>>) {
                val books = response.body()
                if(response.isSuccessful && books != null) {
                    cb(books, filters)
                } else { }
            }

        })
    }

    fun getBookInfo(context: Context, book: Book, user: User, cb: ((book: Book) -> Unit)) {
        val param: Parameter = Parameter(
                "studentID" to user.studentID,
                "assignID" to book.assignID!!
        )

        API_V1.getBookInfo(param).enqueue(object: Callback<Template<ResponseBookInfo>>{
            override fun onFailure(call: Call<Template<ResponseBookInfo>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<ResponseBookInfo>>, response: Response<Template<ResponseBookInfo>>) {
                val responseBookPage = response.body()?.data?.bookPage
                val responseBook = response.body()?.data?.book!!
                if(response.isSuccessful && responseBookPage != null) {
                    book.bookPage = responseBookPage
                    book.problems = responseBook.problems
                    cb(responseBook)
                }
            }
        })
    }

    fun getRecommendBookList(context: Context, user: User, cb:((recommendList: List<RecommendBookList>?) -> Unit)) {
        API_V2.getRecommendBookList(user.studentID).enqueue(object: Callback<List<RecommendBookList>> {
            override fun onFailure(call: Call<List<RecommendBookList>>, t: Throwable) {}

            override fun onResponse(call: Call<List<RecommendBookList>>, response: Response<List<RecommendBookList>>) {
                if(response.isSuccessful) {
                    cb(response.body())
                } else { }
            }
        })
    }
    fun clearBooks(context: Context, user: User, cb: (() -> Unit)) {
        val body = RequestBody.create(MediaType.parse("application/json"), user.studentID)
        API_V1.clearAllBooks(body).enqueue(object: Callback<Template<Map<String, String>>> {
            override fun onFailure(call: Call<Template<Map<String, String>>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<Map<String, String>>>, response: Response<Template<Map<String, String>>>) {
                if(response.isSuccessful) {
                    val intent = Intent(EVENT_BOOK_CLEAR)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    ContentManager.isNeedToSyncMyContentList = true
                    cb()
                } else {
                    responseError(context, response)
                }
            }
        })
    }
    fun convertCategoryByGroup(categories: List<BookCategory>): List<Pair<String, List<BookCategory>>> {
        val cateogryByGroup = categories.groupBy { it.bookSeries }.toMutableMap()
        cateogryByGroup.forEach {
            cateogryByGroup[it.key] = it.value.filter { it.bookCategory != null && it.bookCategory.isNotEmpty() }
        }
        return cateogryByGroup.toList()
    }

    fun score(context: Context, user: User, book: Book, answeredSet: Set<Problem>, cb:() -> Unit) {
        val param: Parameter = Parameter(
                "assignID" to book.assignID!!,
                "studentID" to user.studentID
        )
        param["studyData"] = answeredSet.map {
            val problemParam: Parameter = Parameter(
                    "category" to it.rawCategory,
                    "page" to it.page!!,
                    "problemID" to it.id,
                    "problemNum" to it.problemNum!!,
                    "result" to it.getResultByUserAnswer().rawValue,
                    "studentID" to user.studentID,
                    "unitCode" to it.unitCode,
                    "userAnswer" to it.userAnswer!!
            )

            if(it.studyID != null)
                problemParam["studyID"] = it.studyID

            if(it.rootProblem != null)
                problemParam["parentProblemID"] = it.rootProblem!!.id

            problemParam
        }

        API_V1.scoreBook(param).enqueue(object: Callback<Void>{
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context!!, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful) {
                    answeredSet.forEach { it.mark() }
                    book.markedNumber = book.originProblems.filter { it.getResultByScoring() != Result.yet }.size
                    if(book.markedNumber == book.originProblems.size)
                        book.setMarkingStateToComplete()

                    ContentManager.isNeedToSyncMyContentList = true
                    cb()

                    val intent = Intent(EVENT_BOOK_SCORING)
                    intent.putExtra(ARG_BOOK, book)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                } else
                    responseError(context, response)
            }
        })
    }

    fun notifyBookScored(context: Context, book: Book) {
        book.markedNumber = book.originProblems.filter { it.getResultByScoring() != Result.yet }.size
        if(book.markedNumber == book.originProblems.size)
            book.setMarkingStateToComplete()

        val intent = Intent(EVENT_BOOK_SCORING)
        intent.putExtra(ARG_BOOK, book)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    fun review(context: Context, book: Book, user: User, cb:(book: Book) -> Unit) {
        val param: Parameter = Parameter(
                "assignID" to book.assignID!!,
                "studentID" to user.studentID
        )

        API_V1.reviewBook(param).enqueue(object: Callback<Template<ResponseBookInfo>> {
            override fun onFailure(call: Call<Template<ResponseBookInfo>>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Template<ResponseBookInfo>>, response: Response<Template<ResponseBookInfo>>) {
                val responseBookPage = response.body()?.data?.bookPage
                val responseBook = response.body()?.data?.book!!
                responseBook.assignID = book.assignID
                if(response.isSuccessful && responseBookPage != null) {
                    responseBook.bookPage = responseBookPage
                    responseBook.arrangeProblem()
                    cb(responseBook)
                }
            }
        })
    }

    fun reviewBookV2(context: Context, book: Book, user: User, cb:(book: Book) -> Unit) {
        val param: Parameter = Parameter(
            "assignID" to book.assignID!!,
            "studentID" to user.studentID
        )
        API_V2.reviewBook(param).enqueue(object : Callback<Template<ResponseBookInfo>> {
            override fun onResponse(
                call: Call<Template<ResponseBookInfo>>,
                response: Response<Template<ResponseBookInfo>>
            ) {
                val responseBookPage = response.body()?.data?.bookPage
                val responseBook = response.body()?.data?.book!!
                responseBook.assignID = book.assignID
                if(response.isSuccessful && responseBookPage != null) {
                    responseBook.bookPage = responseBookPage
                    responseBook.arrangeProblem()

                    cb(responseBook)
                }
            }

            override fun onFailure(call: Call<Template<ResponseBookInfo>>, t: Throwable) {
                responseFailed(context, t)
            }

        })

    }
    fun reviewCustomBookV2(context: Context, book: Book, user: User, cb:(book: Book) -> Unit) {
        val param: Parameter = Parameter(
            "assignID" to book.assignID!!,
            "studentID" to user.studentID
        )

        API_V2.reviewCustomBook(param).enqueue(object : Callback<ResponseBody<Book>> {
            override fun onResponse(
                call: Call<ResponseBody<Book>>,
                response: Response<ResponseBody<Book>>
            ) {
                response.body()?.data?.let {
                    cb(it)
                }

            }

            override fun onFailure(call: Call<ResponseBody<Book>>, t: Throwable) {
                responseFailed(context, t)
            }
        })


    }

    fun togglePin(context: Context, book: Book, user: User, cb:() -> Unit) {

        val id = if(book.assignID == null) book.pieceID else book.assignID!!
        //did
        API_V2.setPin(user.studentID, id, !book.pin).enqueue(object: Callback<Void>{
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.code() == 200) {
                    book.pin = !book.pin
                    cb()
                } else {
                    responseError(context, response)
                }
            }
        })
    }

    fun getCommercialBook(context: Context, subject: CommercialSubject?, cb:(list: List<CommercialBook>?) -> Unit) {
        API_V2.getCommercials(subject).enqueue(object: Callback<List<CommercialBook>> {
            override fun onFailure(call: Call<List<CommercialBook>>, t: Throwable) {}

            override fun onResponse(call: Call<List<CommercialBook>>, response: Response<List<CommercialBook>>) {
                cb(response.body())
            }

        })
    }

    fun getCommercialBookPage(context: Context, commercialBook: CommercialBook, cb:(pages: List<CommercialBookPage>?) -> Unit) {
        API_V2.getCommercialBookPage(commercialBook.pieceID).enqueue(object: Callback<CommercialBookPageResponse> {
            override fun onFailure(call: Call<CommercialBookPageResponse>, t: Throwable) {
            }

            override fun onResponse(call: Call<CommercialBookPageResponse>, response: Response<CommercialBookPageResponse>) {
                cb(response.body()?.commercialPageList)
            }

        })
    }

    fun getCommercialSimilarProblemCnt(context: Context,
                                       user: User,
                                       pieceID: Int,
                                       pageProblems: List<CommercialBookPage>,
                                       problemPerCnt: Int,
                                       containClear: Boolean,
                                       level: WrongManagementDialog.Level,
                                       cb: (cnt: Int?) -> Unit) {


        val param = Parameter(
            "pageIDSet" to pageProblems.map { it.id },
                "problemCount" to problemPerCnt,
                "containsClearProblem" to containClear,
                "studentID" to user.studentID,
                "problemDifficulty" to level.text
        )
        API_V2.getCommercialSimilarCnt(
                pieceID,
                param).enqueue(object: Callback<Int> {
            override fun onFailure(call: Call<Int>, t: Throwable) {}

            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                cb(response.body())
            }

        })
    }

    fun makeCustomBook(context: Context,
                       user: User,
                       commercialBook: CommercialBook,
                       pages: Set<CommercialBookPage>,
                       problemPerCnt: Int,
                       containClear: Boolean,
                       level: WrongManagementDialog.Level,
                       cb: (book: Book) -> Unit) {
        val param = Parameter(
                "pageIDSet" to pages.map { it.id },
                "problemCount" to problemPerCnt,
                "containsClearProblem" to containClear,
                "studentID" to user.studentID,
                "problemDifficulty" to level.text
        )

        API_V2.makeCustomBook(commercialBook.pieceID, param).enqueue(object: Callback<Book> {
            override fun onFailure(call: Call<Book>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Book>, response: Response<Book>) {
                val book = response.body()

                if(book != null)
                    cb(book)
                else {
                    responseError(context, response, param)
                }

            }
        })
    }


    fun deleteBook(context: Context, user: User, book: Book, cb:() -> Unit) {

        val id = if(book.assignID == null) book.pieceID else book.assignID!!

        API_V2.deleteFromMyBook(user.studentID, id).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                responseFailed(context, t)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.code() == 200)
                    cb()
            }

        })
    }
}