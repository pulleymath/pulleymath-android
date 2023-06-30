package com.freewheelin.pulley.legacy.assets

import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import java.lang.IllegalStateException

enum class Subject(val id: Int) {
    수학_상(3110),
    수학_하(3120),
    수학I(3210),
    수학II(3220),
    확률과통계(3310),
    미적분(3320),
    기하(3330),
    중학교(2);

    val bigUnits: List<BigUnit>
        get() {
            return when (this) {
                수학_상 -> listOf(BigUnit.다항식, BigUnit.방정식과_부등식, BigUnit.도형의_방정식)
                수학_하 -> listOf(BigUnit.집합과_명제, BigUnit.함수, BigUnit.순열과_조합)
                수학I -> listOf(BigUnit.지수함수와_로그함수, BigUnit.삼각함수, BigUnit.수열)
                수학II -> listOf(BigUnit.함수의_극한과_연속, BigUnit.미분, BigUnit.적분)
                확률과통계 -> listOf(BigUnit.경우의_수, BigUnit.확률, BigUnit.통계)
                미적분 -> listOf(BigUnit.수열의_극한, BigUnit.미분법, BigUnit.적분법)
                기하 -> listOf(BigUnit.이차곡선, BigUnit.벡터, BigUnit.공간도형)
                중학교 -> listOf()
            }
        }

    val filterText: String
        get() {
            return when(this) {
                수학_상 -> return "수학(상)"
                수학_하 -> return "수학(하)"
                수학I -> return "수학1"
                수학II -> return "수학2"
                확률과통계 -> return "확률과 통계"
                미적분 -> return "미적분"
                기하 -> return "기하"
                중학교 -> return "중학교"
            }
        }

    val code: Int
        get() {
            return when {
                this.id > 1000 -> this.id / 10
                else -> this.id
            }
        }

    companion object {


        fun init(unitCode: Int): Subject {
            val unitCodeStr = unitCode.toString()
            return when (unitCodeStr.take(3).toInt()) {
                311 -> 수학_상
                312 -> 수학_하
                321 -> 수학I
                322 -> 수학II
                331 -> 확률과통계
                332 -> 미적분
                333 -> 기하
                else -> {
                    중학교
                }
            }
        }
    }
}

enum class BigUnit(val subject: Subject, val title: String, val suffixId: Int) {
    다항식(Subject.수학_상, "다항식", 0),
    방정식과_부등식(Subject.수학_상, "방정식과 부등식", 1),
    도형의_방정식(Subject.수학_상, "도형의 방정식", 2),

    집합과_명제(Subject.수학_하, "집합과 명제", 0),
    함수(Subject.수학_하, "함수", 1),
    순열과_조합(Subject.수학_하, "순열과 조합", 2),

    지수함수와_로그함수(Subject.수학I, "지수함수와 로그함수", 0),
    삼각함수(Subject.수학I, "삼각함수", 1),
    수열(Subject.수학I, "수열", 2),

    함수의_극한과_연속(Subject.수학II, "함수의 극한과 연속", 0),
    미분(Subject.수학II, "미분", 1),
    적분(Subject.수학II, "적분", 2),

    경우의_수(Subject.확률과통계, "경우의 수", 0),
    확률(Subject.확률과통계, "확률", 1),
    통계(Subject.확률과통계, "통계", 2),

    수열의_극한(Subject.미적분, "수열의 극한", 0),
    미분법(Subject.미적분, "미분법", 1),
    적분법(Subject.미적분, "적분법", 2),

    이차곡선(Subject.기하, "이차곡선", 0),
    벡터(Subject.기하, "벡터", 1),
    공간도형(Subject.기하, "공간도형", 2);

    val id: Int = subject.id + suffixId

    companion object {
        fun init(id: Int): BigUnit {
            return when (id) {
                다항식.id -> 다항식
                방정식과_부등식.id -> 방정식과_부등식
                도형의_방정식.id -> 도형의_방정식
                집합과_명제.id -> 집합과_명제
                함수.id -> 함수
                순열과_조합.id -> 순열과_조합
                지수함수와_로그함수.id -> 지수함수와_로그함수
                삼각함수.id -> 삼각함수
                수열.id -> 수열
                함수의_극한과_연속.id -> 함수의_극한과_연속
                미분.id -> 미분
                적분.id -> 적분
                경우의_수.id -> 경우의_수
                확률.id -> 확률
                통계.id -> 통계
                수열의_극한.id -> 수열의_극한
                미분법.id -> 미분법
                적분법.id -> 적분법
                이차곡선.id -> 이차곡선
                벡터.id -> 벡터
                공간도형.id -> 공간도형
                else -> {
                    LogUtils.errorEvent(PulleyEvent.ERROR, null, msg="\"예상하지 못한 ID: ${id}\"")
                    공간도형
                }
            }
        }

        fun initOrNull(id: Int): BigUnit? {
            return when (id) {
                다항식.id -> 다항식
                방정식과_부등식.id -> 방정식과_부등식
                도형의_방정식.id -> 도형의_방정식
                집합과_명제.id -> 집합과_명제
                함수.id -> 함수
                순열과_조합.id -> 순열과_조합
                지수함수와_로그함수.id -> 지수함수와_로그함수
                삼각함수.id -> 삼각함수
                수열.id -> 수열
                함수의_극한과_연속.id -> 함수의_극한과_연속
                미분.id -> 미분
                적분.id -> 적분
                경우의_수.id -> 경우의_수
                확률.id -> 확률
                통계.id -> 통계
                수열의_극한.id -> 수열의_극한
                미분법.id -> 미분법
                적분법.id -> 적분법
                이차곡선.id -> 이차곡선
                벡터.id -> 벡터
                공간도형.id -> 공간도형
                else -> {
                    LogUtils.errorEvent(PulleyEvent.ERROR, null, msg="\"예상하지 못한 ID: ${id}\"")
                    null
                }
            }
        }

    }
}