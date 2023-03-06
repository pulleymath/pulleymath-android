package com.freewheelin.pulley.core.API.ResponseModel

import com.freewheelin.pulley.model.contents.Book

enum class CommercialSubject {
    MATH_TOP,
    MATH_BOTTOM,
    MATH_ONE,
    MATH_TWO,
    PROBABILITY_AND_STATISTICS,
    CALCULUS,
    GEOMETRY;

    val text: String
        get() {
            return when(this) {
                MATH_TOP -> "수학(상)"
                MATH_BOTTOM -> "수학(하)"
                MATH_ONE -> "수학1"
                MATH_TWO -> "수학2"
                PROBABILITY_AND_STATISTICS -> "확률과 통계"
                CALCULUS -> "미적분"
                GEOMETRY -> "기하"
            }
        }

    companion object {
        fun init(subject: String) : CommercialSubject {
            return when(subject) {
                MATH_TOP.text -> MATH_TOP
                MATH_BOTTOM.text -> MATH_BOTTOM
                MATH_ONE.text -> MATH_ONE
                MATH_TWO.text -> MATH_TWO
                PROBABILITY_AND_STATISTICS.text -> PROBABILITY_AND_STATISTICS
                CALCULUS.text -> CALCULUS
                else -> GEOMETRY

            }
        }
    }
}

class CommercialBook {
    var subjectType: CommercialSubject? = null
    var bookName: String? = null
    var bookTag: String? = null
    var publisher: String? = null
    var pieceID: Int
    var tag: Tag = Tag.None

    enum class Tag {
        None, New, Best
    }


    constructor(book: Book) {
        this.pieceID = book.pieceID
        this.bookName = book.bookName
        this.subjectType = CommercialSubject.init(book.subject)
    }
}