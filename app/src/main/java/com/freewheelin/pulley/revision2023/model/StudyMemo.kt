package com.freewheelin.pulley.revision2023.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "study_memo")
data class StudyMemo (
    @PrimaryKey var id: Int,
    var studentId: String = "",
    var mainId: Int = 0, // 서버와 ios에서는 pieceId이기때문에 이 명칭을 사용함, 안드기준 assignId이다.
    var subId: Int = 0, // problemId
    var width: Int = 0,
    var memoCase: StudyMemoCase,
    var os: StudyMemoOS = StudyMemoOS.ANDROID, // ios와 android
    var file: String = "",
//    var createdAt: String = ""
) {

    override fun hashCode(): Int {
        return studentId.hashCode() + mainId.hashCode() + subId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return hashCode() == other?.hashCode()
    }

}

enum class StudyMemoCase {
    PATTERN_LEARNING_PROBLEM, // 유형학습 문제
    PATTERN_LEARNING_SOLUTION, // 유형학습 해설
//    CONCEPT_COOKING_LEARNING, // 개념학습 개념익히기
//    CONCEPT_COOKING_EXERCISE, // 개념학습 연습문제
    CONCEPT_LEARNING_TYPE_PROBLEM, // 개념학습 문제
    CONCEPT_LEARNING_WRONG_PROBLEM, // 개념학습 오답문제
    PULLEY_BOOKS_PROBLEM, // 풀리북 문제
}

enum class StudyMemoOS {
    IOS, ANDROID
}

data class StudyMemoRequest (
    val memoCase: StudyMemoCase,
    val mainId: Int,
    val subId: Int,
    val width: Int,
    val file: String,
    val os: StudyMemoOS = StudyMemoOS.ANDROID,
)