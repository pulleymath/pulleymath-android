package com.freewheelin.pulley.legacy.model

import com.freewheelin.pulley.legacy.utils.NumberUtils
import java.util.*

class MyLog {

    enum class Category {
        mock,
        snackTest,
        unitStudy,
        wrongStudy;

        val title: String
           get() {
               return when(this) {
                   mock -> "모의고사"
                   wrongStudy -> "오답학습"
                   snackTest -> "테스트"
                   unitStudy -> "유형학습"
               }
           }
    }
    var category: Category = Category.mock
    var title: String = ""
    var score: Int = 0
    var problems: List<Problem> = ArrayList()
}
