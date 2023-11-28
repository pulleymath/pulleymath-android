package com.freewheelin.pulley.legacy.model.contents

import com.freewheelin.pulley.legacy.core.API.ResponseModel.CommercialSubject
import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.net.URLEncoder
import java.util.*


class MockExam: Content, BaseDiffItem {

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
    var needPulleyPlus: Boolean = false

    enum class ExamType {
        ksat, // 수능
        mock, // 모의고사
        mock_twins; // 쌍둥이모의고사

        companion object {
            fun valueOnString(text: String) = values().find{ it.text == text }
        }

        val text: String
            get() {
                return when(this) {
                    ksat -> "수능"
                    mock -> "모의고사"
                    mock_twins -> "모의고사 (쌍둥이)"
                }
            }
        val isTwins: Boolean
            get() {
                return when(this) {
                    mock_twins -> true
                    else -> false
                }
            }
    }
    var examType: ExamType? = null
    var isTwins: Boolean = false
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
        }
    var grade: Int = 1
        get() {
            return if (chapter.length > 1 && field == 1) {
                val grade = chapter[1].toString().toIntOrNull()
                grade ?: field
            } else {
                field
            }
        }

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

    val mockExamTitle: String
        get() {
            return subject
        }

    var updated: Boolean = false

    fun getRemainProblemCount(): Int {
        return totalNumber - markedNumber
    }

    fun getPdfUrl(): String {
        return "https://s3.ap-northeast-2.amazonaws.com/mathflat${pdfFile}"
    }

    fun getMockTitle(): String {
        println("aspasp mockExamTitle : ${mockExamTitle}")
        println("aspasp selectOptional : ${selectOptional}")
        val title = mockExamTitle.split("월").let { it[0].plus("월 고$grade ").plus(it[1].trim()) }
        println("aspasp title : ${title}")
        println("aspasp type.getStr() : ${type.getStr()}")
//        val selectStr =
//                if (selectOptional.isNotEmpty())
//                    "${selectOptional.joinToString(", ") { it.text }} 선택"
//                else
//                    "선택과목 없음"
//        return "$title [${type.getStr()}] - $selectStr"
        return title
    }

    constructor()
    constructor(content: Content) : super(content) {
        this.time = content.time
    }

    override fun getId(): String {
        return "${pieceID}"
    }
}


