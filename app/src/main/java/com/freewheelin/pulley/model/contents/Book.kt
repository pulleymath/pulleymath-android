package com.freewheelin.pulley.model.contents

import com.freewheelin.pulley.activities.learning.tabFragment.book.FilterType
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.Result
import java.io.Serializable

class BookCategoryList: Serializable {
    var bookSeries: String = ""
    var bookCategory: String = ""
}

enum class BookType {
    COMMERCIAL,
    CUSTOM_BOOK,
    BOOK,
    MO,
    RECOMMEND,
    TEST,
    NOTE;

    val getTagTitle: String
    get() {
        return when(this) {
            BOOK, CUSTOM_BOOK -> "유형학습"
            MO -> "모의고사"
            RECOMMEND -> "추천학습"
            TEST -> "테스트"
            else -> "오답학습"
        }
    }
}

enum class ClientBookType {
    MY,
    RECOMMEND,
    ALL;

    val getText: String
        get() {
            return when(this) {
                MY -> "내"
                RECOMMEND -> "추천"
                else -> "전체"
            }
        }
}


class Book: Content {

    var description: String = ""
    var bookTag: String? = null
    val tag: List<String>
        get() {
            return if(bookTag == null)
                return listOf()
            else {
                bookTag!!.split(",").map { tag -> convertTag(tag) }
            }
        }
    val tagOnFilterType: List<FilterType>
        get() {
            return if (bookTag == null) listOf()
            else bookTag!!.split(",").map { tag -> convertTagAtFiltertType(tag) }
        }

    var addNewAssignPlan: Boolean = false
    var uploadNewPlan = false
    var bookCategoryListID: Int = 0

    var bookPage: List<BookPage>? = null

//    var chapter: String = "" // Mock에서도 사용하기 때문에 Content로 올라감

    var activeRecommendTag: String? = null
    var bookCategoryList: BookCategoryList? = null

    val originProblems: List<Problem>
        get() = problems.filter { !it.isSimilarProblem() }
    var pin: Boolean = false
    var backgroundImageUrl: String = ""
    var recommendType = ""
    var clientBookType:ClientBookType = ClientBookType.ALL

    constructor()
    constructor(content: Content): super(content)

    fun arrangeChapter() {
        bookPage?.forEach {
            it.problems = problems.filter { problem  -> problem.page == it.page}.toMutableList()
        }
    }

    fun setMarkingStateToComplete() {
        markingState = "COMPLETED"
    }

    fun getFilteredPage(): List<BookPage> {
        val bookPage = bookPage ?: return emptyList()

        bookPage.forEach {
            it.filteredProblems = it.problems.filter { it.getResultByScoring() == Result.incorrect }.toMutableList()
        }

        return bookPage.filter { (it.filteredProblems != null && it.filteredProblems!!.isNotEmpty()) }
    }
    private fun convertTag(tag: String): String {
        return when (tag.trim()) {
            "기출" -> "기출서"
            "문제풀이" -> "유형서"
            "내신대비" -> "내신서"
            else -> tag
        }
    }
    private fun convertTagAtFiltertType(tag: String): FilterType {
        return when (tag.trim()) {
            "기출" -> FilterType.유형_기출서
            "문제풀이" -> FilterType.유형_유형서
            "내신대비" -> FilterType.유형_내신서
            "내신대비" -> FilterType.유형_내신서
            "1등급" -> FilterType.추천_1등급
            "2~3등급" -> FilterType.추천_2_3등급
            "3~4등급" -> FilterType.추천_3_4등급
            "4등급이하" -> FilterType.추천_4등급이하
            else -> FilterType.추천_1등급
        }
    }
}

class BookPage: Serializable {
    var page: Int = 0
    var chapterMiddleName: String = ""
    var chapterLittleName: String = ""
    var pageName: String = ""

    var problems: MutableList<Problem> = mutableListOf()
    var filteredProblems: MutableList<Problem>? = null

    fun getPageProblems(): MutableList<Problem> {
        return filteredProblems ?: problems
    }
}
