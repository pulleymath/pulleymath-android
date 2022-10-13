package com.freewheelin.pulley.revision2021.model

import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import java.io.Serializable

class LCPriorConceptInfo: BaseDiffItem, Serializable {
    var subject: String = ""
    var name: String = "중학 1-1. 일차식과 그 계산"
//    var isDone: Boolean = false
    var priorConceptImageUrl: String = "https://pulleycooking.s3.ap-northeast-2.amazonaws.com/pulley_cooking/15/312/73/pattern/1_1q.png"
    var learningCoursePriorConceptId: Int = -999

    var priorConceptChapterId: Int = -1 // 부모 id와 같다.
    var priorConceptCookingId: Int = -1
    var priorConceptCookingName: String = "" // 소개념 - 개념익히기 이름
    var priorConceptSubjectName: String = "" // subject name

    var tags: List<String> = listOf()

//    val goCookingMessage: String = "복습 끝내고 \n STEP 2 개념 공부하러 가기!"

    override fun getId(): String {
        return "$learningCoursePriorConceptId"
    }

    fun isBodyItem(): Boolean {
        // 0보다 작으면 헤더나 푸터를 위해 생성한 클래스이다.
        return learningCoursePriorConceptId >= 0
    }

}
