package com.freewheelin.pulley.legacy.core.API.ResponseModel

import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.legacy.model.contents.Book

enum class CommercialSubject {
    MATH_TOP,
    MATH_BOTTOM,
    MATH_ONE,
    MATH_TWO,
    PROBABILITY_AND_STATISTICS,
    CALCULUS,
    GEOMETRY,
    M1_1,
    M1_2,
    M2_1,
    M2_2,
    M3_1,
    M3_2;

    val highSchoolArrays: Array<CommercialSubject>
        get() {
            return arrayOf(MATH_TOP, MATH_BOTTOM, MATH_ONE, MATH_TOP, PROBABILITY_AND_STATISTICS, CALCULUS, GEOMETRY)
        }
    val middleSchoolArrays: Array<CommercialSubject>
        get() {
            return arrayOf(M1_1, M1_2, M2_1, M2_2, M3_1, M3_2)
        }
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
                M1_1 -> "중 1-1"
                M1_2 -> "중 1-2"
                M2_1 -> "중 2-1"
                M2_2 -> "중 2-2"
                M3_1 -> "중 3-1"
                M3_2 -> "중 3-2"
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
                GEOMETRY.text -> GEOMETRY
                M1_1.text -> M1_1
                M1_2.text -> M1_2
                M2_1.text -> M2_1
                M2_2.text -> M2_2
                M3_1.text -> M3_1
                M3_2.text -> M3_2

                else -> MATH_TOP
            }
        }

        val arrayOnSchool: Array<CommercialSubject>
            get() = if (schoolType.isHigh) {
                arrayOf(MATH_TOP, MATH_BOTTOM, MATH_ONE, MATH_TOP, PROBABILITY_AND_STATISTICS, CALCULUS, GEOMETRY)
            } else {
                arrayOf(M1_1, M1_2, M2_1, M2_2, M3_1, M3_2)
            }
    }
}

class CommercialBook {
    var bookName: String? = null
    var bookTag: String? = null
    var publisher: String? = null
    var subject: String? = null
    var pieceID: Int
    var tag: Tag = Tag.None

    val subjectType: CommercialSubject?
        get() {
            return subject?.let {
                CommercialSubject.init(it)
            }
        }

    enum class Tag {
        None, New, Best
    }


    constructor(book: Book) {
        this.pieceID = book.pieceID
        this.bookName = book.bookName
//        this.subjectType = CommercialSubject.init(book.subject)
    }
}