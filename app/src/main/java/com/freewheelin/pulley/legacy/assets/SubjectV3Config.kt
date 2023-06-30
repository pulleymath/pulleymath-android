package com.freewheelin.pulley.legacy.assets

import com.freewheelin.pulley.revision2023.model.LearningFilterType
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import java.lang.IllegalStateException

enum class SubjectV3(val id: Int) {
    교육과정외(49),
    기타(0),
    중등(-2),
    수학_상(41),
    수학_하(42),
    수학I(43),
    수학II(44),
    확률과통계(45),
    미적분(46),
    기하(47),
    중1_1(50),
    중1_2(51),
    중2_1(52),
    중2_2(53),
    중3_1(54),
    중3_2(55);

    val bigUnits: List<BigUnitV3>
        get() {
            return when (this) {
                수학_상 -> listOf(BigUnitV3.다항식, BigUnitV3.방정식과_부등식, BigUnitV3.도형의_방정식)
                수학_하 -> listOf(BigUnitV3.집합과_명제, BigUnitV3.함수, BigUnitV3.순열과_조합)
                수학I -> listOf(BigUnitV3.지수함수와_로그함수, BigUnitV3.삼각함수, BigUnitV3.수열)
                수학II -> listOf(BigUnitV3.함수의_극한과_연속, BigUnitV3.미분, BigUnitV3.적분)
                확률과통계 -> listOf(BigUnitV3.경우의_수, BigUnitV3.확률, BigUnitV3.통계)
                미적분 -> listOf(BigUnitV3.수열의_극한, BigUnitV3.미분법, BigUnitV3.적분법)
                기하 -> listOf(BigUnitV3.이차곡선, BigUnitV3.벡터, BigUnitV3.공간도형)

                중1_1 -> listOf(BigUnitV3.중등_소인수_분해, BigUnitV3.중등_정수와_유리수, BigUnitV3.중등_문자와_식, BigUnitV3.중등_좌표평면과_그래프)
                중1_2 -> listOf(BigUnitV3.중등_기본도형, BigUnitV3.중등_평면도형, BigUnitV3.중등_입체도형, BigUnitV3.중등_통계)
                중2_1 -> listOf(BigUnitV3.중등_수와_식, BigUnitV3.중등_부등식, BigUnitV3.중등_방정식, BigUnitV3.중등_함수)
                중2_2 -> listOf(BigUnitV3.중등_도형의_성질, BigUnitV3.중등_도형의_닮음, BigUnitV3.중등_확률)
                중3_1 -> listOf(BigUnitV3.중등_실수와_그계산, BigUnitV3.중등_다항식의_곱셈과_인수분해, BigUnitV3.중등_이차방정식, BigUnitV3.중등_이차함수)
                중3_2 -> listOf(BigUnitV3.중등_삼각비, BigUnitV3.중등_원의_성질, BigUnitV3.중등_통계2)
                else -> listOf()
            }
        }

    val filterText: String
        get() {
            return when(this) {
                수학_상 -> "수학(상)"
                수학_하 -> "수학(하)"
                수학I -> "수학1"
                수학II -> "수학2"
                확률과통계 -> "확률과 통계"
                미적분 -> "미적분"
                기하 -> "기하"
                중등 -> "중학교"
                중1_1 -> "중1-1"
                중1_2 -> "중1-2"
                중2_1 -> "중2-1"
                중2_2 -> "중2-2"
                중3_1 -> "중3-1"
                중3_2 -> "중3-2"
                else -> "교육과정 외"
            }
        }

    val isMathSang: Boolean
        get() {
            return this == 수학_상
        }
    val isMathHa: Boolean
        get() {
            return this == 수학_하
        }
    val isMath1: Boolean
        get() {
            return this == 수학I
        }
    val isMath2: Boolean
        get() {
            return this == 수학II
        }
    val isProbabilityAndStatistics: Boolean
        get() {
            return this == 확률과통계
        }
    val isCalculus: Boolean
        get() {
            return this == 미적분
        }
    val isGeometry: Boolean
        get() {
            return this == 기하
        }
    val isMiddle1_1: Boolean
        get() {
            return this == 중1_1
        }

    val isMiddle1_2: Boolean
        get() {
            return this == 중1_2
        }

    val isMiddle2_1: Boolean
        get() {
            return this == 중2_1
        }

    val isMiddle2_2: Boolean
        get() {
            return this == 중2_2
        }

    val isMiddle3_1: Boolean
        get() {
            return this == 중3_1
        }

    val isMiddle3_2: Boolean
        get() {
            return this == 중3_2
        }

    companion object {
        fun valueOfNonNull(value: String?): SubjectV3 =
            value?.let {
                values()
                    .firstOrNull { it.name == value } ?: 기타
            } ?: 기타

        fun idOfNonNull(id: Int): SubjectV3 =
            values()
                .firstOrNull { it.id == id } ?: 기타

        fun convertStrToSubject(value: String): SubjectV3 {
            return when (value) {
                교육과정외.name -> 교육과정외
                중등.name -> 중등
                중1_1.name, "중 1-1", "중1-1" -> 중1_1
                중1_2.name, "중 1-2", "중1-2" -> 중1_2
                중2_1.name, "중 2-1", "중2-1" -> 중2_1
                중2_2.name, "중 2-2", "중2-2" -> 중2_2
                중3_1.name, "중 3-1", "중3-1" -> 중3_1
                중3_2.name, "중 3-2", "중3-2" -> 중3_2
                수학_상.name, "수학(상)" -> 수학_상
                수학_하.name, "수학(하)" -> 수학_하
                수학I.name, "수학1" -> 수학I
                수학II.name, "수학2" -> 수학II
                확률과통계.name, "확률과 통계" -> 확률과통계
                미적분.name -> 미적분
                기하.name -> 기하
                else -> { 수학_상 }
            }
        }
        fun codeToSubject(code: Int): SubjectV3 {
            return when (code) {
                수학_상.id -> 수학_상
                수학_하.id -> 수학_하
                수학I.id -> 수학I
                수학II.id -> 수학II
                확률과통계.id -> 확률과통계
                미적분.id -> 미적분
                기하.id -> 기하
                중1_1.id -> 중1_1
                중1_2.id -> 중1_2
                중2_1.id -> 중2_1
                중2_2.id -> 중2_2
                중3_1.id -> 중3_1
                중3_2.id -> 중3_2
                else -> {
                    수학_상
                }
            }
        }
        fun init(unitCode: Int): SubjectV3 {
            val unitCodeStr = unitCode.toString()
            return when (unitCodeStr.take(3).toInt()) {
                수학_상.id -> 수학_상
                수학_하.id -> 수학_하
                수학I.id -> 수학I
                수학II.id -> 수학II
                확률과통계.id -> 확률과통계
                미적분.id -> 미적분
                기하.id -> 기하
                중1_1.id -> 중1_1
                중1_2.id -> 중1_2
                중2_1.id -> 중2_1
                중2_2.id -> 중2_2
                중3_1.id -> 중3_1
                중3_2.id -> 중3_2
                else -> {
                    수학_상
                }
            }
        }
    }
}
enum class BigUnitV3(val subject: SubjectV3, val title: String, val id: Int) {
    교육과정외(SubjectV3.교육과정외, "교육과정 외", -1),
    중등(SubjectV3.중등, "중등", 0),

    다항식(SubjectV3.수학_상, "다항식", 368),
    방정식과_부등식(SubjectV3.수학_상, "방정식과 부등식", 371),
    도형의_방정식(SubjectV3.수학_상, "도형의 방정식", 369),

    집합과_명제(SubjectV3.수학_하, "집합과 명제", 374),
    함수(SubjectV3.수학_하, "함수", 375),
    순열과_조합(SubjectV3.수학_하, "순열과 조합", 373),

    지수함수와_로그함수(SubjectV3.수학I, "지수함수와 로그함수", 379),
    삼각함수(SubjectV3.수학I, "삼각함수", 377),
    수열(SubjectV3.수학I, "수열", 378),

    함수의_극한과_연속(SubjectV3.수학II, "함수의 극한과 연속", 383),
    미분(SubjectV3.수학II, "미분", 380),
    적분(SubjectV3.수학II, "적분", 382),

    경우의_수(SubjectV3.확률과통계, "경우의 수", 384),
    확률(SubjectV3.확률과통계, "확률", 387),
    통계(SubjectV3.확률과통계, "통계", 386),

    수열의_극한(SubjectV3.미적분, "수열의 극한", 390),
    미분법(SubjectV3.미적분, "미분법", 389),
    적분법(SubjectV3.미적분, "적분법", 391),

    이차곡선(SubjectV3.기하, "이차곡선", 395),
    벡터(SubjectV3.기하, "벡터", 394),
    공간도형(SubjectV3.기하, "공간도형", 392),

    //-------- 거지같지만 중등 대단원의 id는 교육과정 순서와 다르다
    중등_소인수_분해(SubjectV3.중1_1, "소인수분해", 190),
    중등_정수와_유리수(SubjectV3.중1_1, "정수와 유리수", 189),
    중등_문자와_식(SubjectV3.중1_1, "문자와 식", 188),
    중등_좌표평면과_그래프(SubjectV3.중1_1, "좌표평면과 그래프", 187),

    중등_기본도형(SubjectV3.중1_2, "기본 도형", 293),
    중등_평면도형(SubjectV3.중1_2, "평면도형", 292),
    중등_입체도형(SubjectV3.중1_2, "입체도형", 290),
    중등_통계(SubjectV3.중1_2, "통계", 294),

    중등_수와_식(SubjectV3.중2_1, "수와 식", 197),
    중등_부등식(SubjectV3.중2_1, "부등식", 198),
    중등_방정식(SubjectV3.중2_1, "방정식", 199),
    중등_함수(SubjectV3.중2_1, "함수", 200),

    중등_도형의_성질(SubjectV3.중2_2, "도형의 성질", 298),
    중등_도형의_닮음(SubjectV3.중2_2, "도형의 닮음", 299),
    중등_확률(SubjectV3.중2_2, "확률", 300),

    중등_실수와_그계산(SubjectV3.중3_1, "실수와 그 계산", 209),
    중등_다항식의_곱셈과_인수분해(SubjectV3.중3_1, "다항식의 곱셈과 인수분해", 207),
    중등_이차방정식(SubjectV3.중3_1, "이차방정식", 208),
    중등_이차함수(SubjectV3.중3_1, "이차함수", 206),

    중등_삼각비(SubjectV3.중3_2, "삼각비", 306),
    중등_원의_성질(SubjectV3.중3_2, "원의 성질", 305),
    중등_통계2(SubjectV3.중3_2, "통계", 308);



//    val id: Int = subject.id

    companion object {
        fun titleOfNonNull(title: String): BigUnitV3 =
            values()
                .firstOrNull { it.title == title } ?: 교육과정외
        fun idOfNonNull(id: Int): BigUnitV3 =
            values()
                .firstOrNull { it.id == id } ?: 교육과정외

        fun getSubject(id: Int): SubjectV3 {
            return when (id) {
                다항식.id -> 다항식.subject
                방정식과_부등식.id -> 방정식과_부등식.subject
                도형의_방정식.id -> 도형의_방정식.subject
                집합과_명제.id -> 집합과_명제.subject
                함수.id -> 함수.subject
                순열과_조합.id -> 순열과_조합.subject
                지수함수와_로그함수.id -> 지수함수와_로그함수.subject
                삼각함수.id -> 삼각함수.subject
                수열.id -> 수열.subject
                함수의_극한과_연속.id -> 함수의_극한과_연속.subject
                미분.id -> 미분.subject
                적분.id -> 적분.subject
                경우의_수.id -> 경우의_수.subject
                확률.id -> 확률.subject
                통계.id -> 통계.subject
                수열의_극한.id -> 수열의_극한.subject
                미분법.id -> 미분법.subject
                적분법.id -> 적분법.subject
                이차곡선.id -> 이차곡선.subject
                벡터.id -> 벡터.subject
                공간도형.id -> 공간도형.subject
                교육과정외.id -> 교육과정외.subject
                중등.id -> 중등.subject
                else -> 교육과정외.subject
            }
        }
        fun init(id: Int): BigUnitV3 {
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

        fun initOrNull(id: Int): BigUnitV3? {
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