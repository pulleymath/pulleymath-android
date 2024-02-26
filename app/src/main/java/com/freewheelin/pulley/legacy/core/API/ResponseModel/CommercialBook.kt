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
    M3_2,

    E1_1,
    E1_2,
    E2_1,
    E2_2,
    E3_1,
    E3_2,
    E4_1,
    E4_2,
    E5_1,
    E5_2,
    E6_1,
    E6_2;

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

                E1_1 -> "초 1-1"
                E1_2 -> "초 1-2"
                E2_1 -> "초 2-1"
                E2_2 -> "초 2-2"
                E3_1 -> "초 3-1"
                E3_2 -> "초 3-2"
                E4_1 -> "초 4-1"
                E4_2 -> "초 4-2"
                E5_1 -> "초 5-1"
                E5_2 -> "초 5-2"
                E6_1 -> "초 6-1"
                E6_2 -> "초 6-2"
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

                E1_1.text -> E1_1
                E1_2.text -> E1_2
                E2_1.text -> E2_1
                E2_2.text -> E2_2
                E3_1.text -> E3_1
                E3_2.text -> E3_2
                E4_1.text -> E4_1
                E4_2.text -> E4_2
                E5_1.text -> E5_1
                E5_2.text -> E5_2
                E6_1.text -> E6_1
                E6_2.text -> E6_2

                else -> MATH_TOP
            }
        }

        val highSchoolArrays: Array<CommercialSubject>
            get() {
                return arrayOf(MATH_TOP, MATH_BOTTOM, MATH_ONE, MATH_TOP, PROBABILITY_AND_STATISTICS, CALCULUS, GEOMETRY)
            }
        val middleSchoolArrays: Array<CommercialSubject>
            get() {
                return arrayOf(M1_1, M1_2, M2_1, M2_2, M3_1, M3_2)
            }
        val elementarySchoolArray: Array<CommercialSubject>
            get() {
                return arrayOf(E3_1, E3_2, E4_1, E4_2, E5_1, E5_2, E6_1, E6_2)
            }
        val arrayOnSchool: Array<CommercialSubject>
            get() = if (schoolType.isHigh) {
                highSchoolArrays
            } else if (schoolType.isMiddle){
                middleSchoolArrays
            } else {
                elementarySchoolArray
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