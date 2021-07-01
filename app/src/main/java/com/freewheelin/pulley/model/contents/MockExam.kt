package com.freewheelin.pulley.model.contents

import com.freewheelin.pulley.core.API.ResponseModel.CommercialSubject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.net.URLEncoder
import java.util.*


class MockExam: Content {

    enum class Type {
        nd, // 공통       Not Determined
        ns, // 이과(가형)  Natural Sciences
        la; // 문과(나형)  Liveral Arts

        companion object {
            val list = listOf(nd, ns, la)
        }

        fun getStr(): String {
            return when(this) {
                ns -> "가형"
                la -> "나형"
                else -> "공통"
            }
        }

        fun sortPriority(): Int {
            return when(this) {
                ns -> 1
                la -> 2
                else -> 0
            }
        }
    }
    // v3에 추가
    // mockID -> Content 로 이동
    var year: Int = 0
    var month: Int = 0
    var majorType:Int = 0
    var personalData:PersonalData? = null
    var createDate: Date = Date()
    var isRestart = false
    var selectOptional = mutableListOf<CommercialSubject>()

    fun isPersonalCompleted() : Boolean {
        return personalData?.markingState == "COMPLETED"
    }

    val type: Type
        get() {
            return when(majorType) {
                1 -> Type.la
                2 -> Type.ns
                else -> Type.nd
            }
//            if ("문과" in subject)
//                return Type.la
//            else if ("이과" in subject)
//                return Type.ns
//            else
//                return Type.nd
        }
    var grade: Int = 1
    var percent: Int? = null

    var pdfFile: String = ""
    var count: Int = 0
    val rating: Int?
        get() {
            if(this.percent == null)
                return null

            val percentage = 100 - this.percent!!
            return when {
                percentage <= 4 -> 1
                percentage <= 11 -> 2
                percentage <= 23 -> 3
                percentage <= 40 -> 4
                percentage <= 60 -> 5
                percentage <= 77 -> 6
                percentage <= 89 -> 7
                percentage <= 96 -> 8
                else -> 9
            }
        }

    val title: String
        get() {
            return subject
//            val willDeleteText = when(type) {
//                Type.la -> " 고${grade} 문과"
//                Type.ns -> " 고${grade} 이과"
//                else -> " 고${grade} 공통"
//            }
//
//            return subject.replace(willDeleteText, "")
        }

    var updated: Boolean = false

    fun getRemainProblemCount(): Int {
        return totalNumber - markedNumber
    }

    fun getPdfUrl(): String {
        return "https://s3.ap-northeast-2.amazonaws.com/mathflat${pdfFile}"
    }

    fun getMockTitle(): String {
        val title = title.split("월").let { it[0].plus("월 고$grade ").plus(it[1].trim()) }
        val selectStr =
                if (selectOptional.isNotEmpty())
                    "${selectOptional.joinToString(", ") { it.text }} 선택"
                else
                    "선택과목 없음"
        return "$title [${type.getStr()}] - $selectStr"
    }

    constructor()
    constructor(content: Content) : super(content) {
        this.time = content.time
    }
}


