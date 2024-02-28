package com.freewheelin.pulley.legacy.assets

import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.revision2023.model.LearningFilterType
import com.freewheelin.pulley.legacy.utils.LogUtils
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.revision2023.SchoolType
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
    중3_2(55),

    초1_1(56),
    초1_2(57),
    초2_1(58),
    초2_2(59),
    초3_1(60),
    초3_2(61),

    초4_1(62),
    초4_2(63),
    초5_1(64),
    초5_2(65),
    초6_1(66),
    초6_2(67);

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

                초1_1 -> listOf(BigUnitV3.초등_9까지의_수, BigUnitV3.초등_여러_가지_모양, BigUnitV3.초등_덧셈과_뺄셈1_1, BigUnitV3.초등_비교하기, BigUnitV3.초등_50까지의_수)
                초1_2 -> listOf(BigUnitV3.초등_100까지의_수, BigUnitV3.초등_덧셈과_뺄셈1_2_1, BigUnitV3.초등_여러_가지_모양2, BigUnitV3.초등_덧셈과_뺄셈1_2_2, BigUnitV3.초등_시계_보기와_규칙_찾기, BigUnitV3.초등_덧셈과_뺄셈1_2_3)
                초2_1 -> listOf(BigUnitV3.초등_세_자리_수, BigUnitV3.초등_여러_가지_도형, BigUnitV3.초등_덧셈과_뺄셈2_1, BigUnitV3.초등_길이_재기2_1, BigUnitV3.초등_분류하기, BigUnitV3.초등_곱셈2_1)
                초2_2 -> listOf(BigUnitV3.초등_네_자리_수, BigUnitV3.초등_곱셈구구, BigUnitV3.초등_길이_재기2_2, BigUnitV3.초등_시각과_시간, BigUnitV3.초등_표와_그래프, BigUnitV3.초등_규칙_찾기2_2)
                초3_1 -> listOf(BigUnitV3.초등_덧셈과_뺄셈3_1, BigUnitV3.초등_평면도형, BigUnitV3.초등_나눗셈3_1, BigUnitV3.초등_곱셈3_1, BigUnitV3.초등_길이와_시간, BigUnitV3.초등_분수와_소수)
                초3_2 -> listOf(BigUnitV3.초등_곱셈3_2, BigUnitV3.초등_나눗셈3_2, BigUnitV3.초등_원, BigUnitV3.초등_분수, BigUnitV3.초등_들이와_무게, BigUnitV3.초등_자료의_정리)
                초4_1 -> listOf(BigUnitV3.초등_큰_수, BigUnitV3.초등_각도, BigUnitV3.초등_곱셈과_나눗셈, BigUnitV3.초등_평면도형의_이동, BigUnitV3.초등_막대그래프, BigUnitV3.초등_규칙_찾기4_1)
                초4_2 -> listOf(BigUnitV3.초등_분수의_덧셈과_뺄셈4_2, BigUnitV3.초등_삼각형, BigUnitV3.초등_소수의_덧셈과_뺄셈, BigUnitV3.초등_사각형, BigUnitV3.초등_꺾은선그래프, BigUnitV3.초등_다각형)
                초5_1 -> listOf(BigUnitV3.초등_자연수의_혼합계산, BigUnitV3.초등_약수와_배수, BigUnitV3.초등_규칙과_대응, BigUnitV3.초등_약분과_통분, BigUnitV3.초등_분수의_덧셈과_뺄셈5_1, BigUnitV3.초등_다각형의_둘레와_넓이)
                초5_2 -> listOf(BigUnitV3.초등_수의_범위와_어림하기, BigUnitV3.초등_분수의_곱셈, BigUnitV3.초등_합동과_대칭, BigUnitV3.초등_소수의_곱셈, BigUnitV3.초등_직육면체, BigUnitV3.초등_평균과_가능성)
                초6_1 -> listOf(BigUnitV3.초등_분수의_나눗셈6_1, BigUnitV3.초등_각기둥과_각뿔, BigUnitV3.초등_소수의_나눗셈6_1, BigUnitV3.초등_비와_비율, BigUnitV3.초등_여러_가지_그래프, BigUnitV3.초등_직육면체의_부피와_겉넓이)
                초6_2 -> listOf(BigUnitV3.초등_분수의_나눗셈6_2, BigUnitV3.초등_소수의_나눗셈6_2, BigUnitV3.초등_공간과_입체, BigUnitV3.초등_비례식과_비례배분, BigUnitV3.초등_원의_넓이, BigUnitV3.초등_원기둥_원뿔_구)
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

                초1_1 -> "초1-1"
                초1_2 -> "초1-2"
                초2_1 -> "초2-1"
                초2_2 -> "초2-2"
                초3_1 -> "초3-1"
                초3_2 -> "초3-2"
                초4_1 -> "초4-1"
                초4_2 -> "초4-2"
                초5_1 -> "초5-1"
                초5_2 -> "초5-2"
                초6_1 -> "초6-1"
                초6_2 -> "초6-2"
                else -> "교육과정 외"
            }
        }

    val plannerText: String
        get() {
            return when(this) {
                수학_상 -> "수학(상)"
                수학_하 -> "수학(하)"
                수학I -> "수학 1"
                수학II -> "수학 2"
                확률과통계 -> "확률과 통계"
                미적분 -> "미적분"
                기하 -> "기하"
                중등 -> "중학교"
                중1_1 -> "1-1"
                중1_2 -> "1-2"
                중2_1 -> "2-1"
                중2_2 -> "2-2"
                중3_1 -> "3-1"
                중3_2 -> "3-2"

                초1_1 -> "1-1"
                초1_2 -> "1-2"
                초2_1 -> "2-1"
                초2_2 -> "2-2"
                초3_1 -> "3-1"
                초3_2 -> "3-2"
                초4_1 -> "4-1"
                초4_2 -> "4-2"
                초5_1 -> "5-1"
                초5_2 -> "5-2"
                초6_1 -> "6-1"
                초6_2 -> "6-2"
                else -> "교육과정 외"
            }
        }

    val isMathSang: Boolean get() = this == 수학_상
    val isMathHa: Boolean get() = this == 수학_하
    val isMath1: Boolean get() = this == 수학I
    val isMath2: Boolean get() = this == 수학II
    val isProbabilityAndStatistics: Boolean get() = this == 확률과통계
    val isCalculus: Boolean get() = this == 미적분
    val isGeometry: Boolean get() = this == 기하
    val isMiddle1_1: Boolean get() = this == 중1_1

    val isMiddle1_2: Boolean get() = this == 중1_2

    val isMiddle2_1: Boolean get() = this == 중2_1

    val isMiddle2_2: Boolean get() = this == 중2_2
    val isMiddle3_1: Boolean get() = this == 중3_1

    val isMiddle3_2: Boolean get() = this == 중3_2
    val isElementary1_1: Boolean get() = this == 초1_1
    val isElementary1_2: Boolean get() = this == 초1_2
    val isElementary2_1: Boolean get() = this == 초2_1
    val isElementary2_2: Boolean get() = this == 초2_2
    val isElementary3_1: Boolean get() = this == 초3_1
    val isElementary3_2: Boolean get() = this == 초3_2
    val isElementary4_1: Boolean get() = this == 초4_1
    val isElementary4_2: Boolean get() = this == 초4_2
    val isElementary5_1: Boolean get() = this == 초5_1
    val isElementary5_2: Boolean get() = this == 초5_2
    val isElementary6_1: Boolean get() = this == 초6_1
    val isElementary6_2: Boolean get() = this == 초6_2
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

                초1_1.name, "초1-1", "초 1-1" -> 초1_1
                초1_2.name, "초1-2", "초 1-2" -> 초1_2
                초2_1.name, "초2-1", "초 2-1" -> 초2_1
                초2_2.name, "초2-2", "초 2-2" -> 초2_2
                초3_1.name, "초3-1", "초 3-1" -> 초3_1
                초3_2.name, "초3-2", "초 3-2" -> 초3_2
                초4_1.name, "초4-1", "초 4-1" -> 초4_1
                초4_2.name, "초4-2", "초 4-2" -> 초4_2
                초5_1.name, "초5-1", "초 5-1" -> 초5_1
                초5_2.name, "초5-2", "초 5-2" -> 초5_2
                초6_1.name, "초6-1", "초 6-1" -> 초6_1
                초6_2.name, "초6-2", "초 6-2" -> 초6_2
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

                초1_1.id -> 초1_1
                초1_2.id -> 초1_2
                초2_1.id -> 초2_1
                초2_2.id -> 초2_2
                초3_1.id -> 초3_1
                초3_2.id -> 초3_2
                초4_1.id -> 초4_1
                초4_2.id -> 초4_2
                초5_1.id -> 초5_1
                초5_2.id -> 초5_2
                초6_1.id -> 초6_1
                초6_2.id -> 초6_2
                else -> {
                    수학_상
                }
            }
        }
        fun codeToSchoolType(code: Int): SchoolType {
            val subject = codeToSubject(code)
            return when (subject) {
                수학_상, 수학_하, 수학I, 수학II, 확률과통계, 미적분, 기하 -> SchoolType.HIGH
                중1_1, 중1_2, 중2_1, 중2_2, 중3_1, 중3_2 -> SchoolType.MIDDLE
                초1_1, 초1_2, 초2_1, 초2_2, 초3_1, 초3_2, 초4_1, 초4_2, 초5_1, 초5_2, 초6_1, 초6_2 -> SchoolType.ELEMENTARY
                else -> SchoolType.HIGH
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

                초1_1.id -> 초1_1
                초1_2.id -> 초1_2
                초2_1.id -> 초2_1
                초2_2.id -> 초2_2
                초3_1.id -> 초3_1
                초3_2.id -> 초3_2
                초4_1.id -> 초4_1
                초4_2.id -> 초4_2
                초5_1.id -> 초5_1
                초5_2.id -> 초5_2
                초6_1.id -> 초6_1
                초6_2.id -> 초6_2
                else -> {
                    수학_상
                }
            }
        }
        val totalListOnSchoolType: List<String>
            get() {
                return when (MyApplication.schoolType) {
                    SchoolType.ELEMENTARY -> listOf(
                        "전체", 초3_1.filterText, 초3_2.filterText, 초4_1.filterText, 초4_2.filterText, 초5_1.filterText, 초5_2.filterText, 초6_1.filterText, 초6_2.filterText
                    )
                    SchoolType.MIDDLE -> listOf(
                        "전체", 중1_1.filterText, 중1_2.filterText, 중2_1.filterText, 중2_2.filterText, 중3_1.filterText, 중3_2.filterText,
                    )
                    SchoolType.HIGH -> listOf(
                        "전체", 수학_상.filterText, 수학_하.filterText, 수학I.filterText, 수학II.filterText, 확률과통계.filterText, 미적분.filterText, 기하.filterText,
                    )
                    else -> listOf()
                }
            }
        val listOnSchoolType: List<String>
            get() {
                return when (MyApplication.schoolType) {
                    SchoolType.ELEMENTARY -> listOf(
                        /*초1_1.filterText, 초1_2.filterText, 초2_1.filterText, 초2_2.filterText,*/ 초3_1.filterText, 초3_2.filterText, 초4_1.filterText, 초4_2.filterText, 초5_1.filterText, 초5_2.filterText, 초6_1.filterText, 초6_2.filterText
                    )
                    SchoolType.MIDDLE -> listOf(
                        중1_1.filterText, 중1_2.filterText, 중2_1.filterText, 중2_2.filterText, 중3_1.filterText, 중3_2.filterText,
                    )
                    SchoolType.HIGH -> listOf(
                        수학_상.filterText, 수학_하.filterText, 수학I.filterText, 수학II.filterText, 확률과통계.filterText, 미적분.filterText, 기하.filterText,
                    )
                    else -> listOf()
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

    //-------- 이상하지만 중등 대단원의 id는 교육과정 순서와 다르다
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
    중등_통계2(SubjectV3.중3_2, "통계", 308),

    초등_9까지의_수(SubjectV3.초1_1, "9까지의 수", 116),
    초등_여러_가지_모양(SubjectV3.초1_1, "여러 가지 모양", 117),
    초등_덧셈과_뺄셈1_1(SubjectV3.초1_1, "덧셈과 뺄셈", 115),
    초등_비교하기(SubjectV3.초1_1, "비교하기", 114),
    초등_50까지의_수(SubjectV3.초1_1, "50까지의 수", 113),

    초등_100까지의_수(SubjectV3.초1_2, "100까지의 수", 216),
    초등_덧셈과_뺄셈1_2_1(SubjectV3.초1_2, "덧셈과 뺄셈(1)", 214),
    초등_여러_가지_모양2(SubjectV3.초1_2, "여러 가지 모양", 219),
    초등_덧셈과_뺄셈1_2_2(SubjectV3.초1_2, "덧셈과 뺄셈(2)", 218),
    초등_시계_보기와_규칙_찾기(SubjectV3.초1_2, "시계 보기와 규칙 찾기", 215),
    초등_덧셈과_뺄셈1_2_3(SubjectV3.초1_2, "덧셈과 뺄셈(3)", 217),

    초등_세_자리_수(SubjectV3.초2_1, "세 자리 수", 125),
    초등_여러_가지_도형(SubjectV3.초2_1, "여러 가지 도형", 123),
    초등_덧셈과_뺄셈2_1(SubjectV3.초2_1, "덧셈과 뺄셈", 128),
    초등_길이_재기2_1(SubjectV3.초2_1, "길이 재기", 124),
    초등_분류하기(SubjectV3.초2_1, "분류하기", 127),
    초등_곱셈2_1(SubjectV3.초2_1, "곱셈", 126),

    초등_네_자리_수(SubjectV3.초2_2, "네 자리 수", 231),
    초등_곱셈구구(SubjectV3.초2_2, "곱셈구구", 230),
    초등_길이_재기2_2(SubjectV3.초2_2, "길이 재기", 226),
    초등_시각과_시간(SubjectV3.초2_2, "시각과 시간", 229),
    초등_표와_그래프(SubjectV3.초2_2, "표와 그래프", 227),
    초등_규칙_찾기2_2(SubjectV3.초2_2, "규칙 찾기", 228),

    초등_덧셈과_뺄셈3_1(SubjectV3.초3_1, "덧셈과 뺄셈", 140),
    초등_평면도형(SubjectV3.초3_1, "평면도형", 141),
    초등_나눗셈3_1(SubjectV3.초3_1, "나눗셈", 135),
    초등_곱셈3_1(SubjectV3.초3_1, "곱셈", 139),
    초등_길이와_시간(SubjectV3.초3_1, "길이와 시간", 137),
    초등_분수와_소수(SubjectV3.초3_1, "분수와 소수", 136),

    초등_곱셈3_2(SubjectV3.초3_2, "곱셈", 242),
    초등_나눗셈3_2(SubjectV3.초3_2, "나눗셈", 240),
    초등_원(SubjectV3.초3_2, "원", 239),
    초등_분수(SubjectV3.초3_2, "분수", 241),
    초등_들이와_무게(SubjectV3.초3_2, "들이와 무게", 238),
    초등_자료의_정리(SubjectV3.초3_2, "자료의 정리", 244),

    초등_큰_수(SubjectV3.초4_1, "큰 수", 154),
    초등_각도(SubjectV3.초4_1, "각도", 150),
    초등_곱셈과_나눗셈(SubjectV3.초4_1, "곱셈과 나눗셈", 148),
    초등_평면도형의_이동(SubjectV3.초4_1, "평면도형의 이동", 152),
    초등_막대그래프(SubjectV3.초4_1, "막대그래프", 151),
    초등_규칙_찾기4_1(SubjectV3.초4_1, "규칙 찾기", 153),

    초등_분수의_덧셈과_뺄셈4_2(SubjectV3.초4_2, "분수의 덧셈과 뺄셈", 253),
    초등_삼각형(SubjectV3.초4_2, "삼각형", 256),
    초등_소수의_덧셈과_뺄셈(SubjectV3.초4_2, "소수의 덧셈과 뺄셈", 254),
    초등_사각형(SubjectV3.초4_2, "사각형", 251),
    초등_꺾은선그래프(SubjectV3.초4_2, "꺾은선그래프", 252),
    초등_다각형(SubjectV3.초4_2, "다각형", 257),

    초등_자연수의_혼합계산(SubjectV3.초5_1, "자연수의 혼합계산", 163),
    초등_약수와_배수(SubjectV3.초5_1, "약수와 배수", 165),
    초등_규칙과_대응(SubjectV3.초5_1, "규칙과 대응", 161),
    초등_약분과_통분(SubjectV3.초5_1, "약분과 통분", 167),
    초등_분수의_덧셈과_뺄셈5_1(SubjectV3.초5_1, "분수의 덧셈과 뺄셈", 162),
    초등_다각형의_둘레와_넓이(SubjectV3.초5_1, "다각형의 둘레와 넓이", 166),

    초등_수의_범위와_어림하기(SubjectV3.초5_2, "수의 범위와 어림하기", 270),
    초등_분수의_곱셈(SubjectV3.초5_2, "분수의 곱셈", 267),
    초등_합동과_대칭(SubjectV3.초5_2, "합동과 대칭", 264),
    초등_소수의_곱셈(SubjectV3.초5_2, "소수의 곱셈", 265),
    초등_직육면체(SubjectV3.초5_2, "직육면체", 268),
    초등_평균과_가능성(SubjectV3.초5_2, "평균과 가능성", 266),

    초등_분수의_나눗셈6_1(SubjectV3.초6_1, "분수의 나눗셈", 174),
    초등_각기둥과_각뿔(SubjectV3.초6_1, "각기둥과 각뿔", 178),
    초등_소수의_나눗셈6_1(SubjectV3.초6_1, "소수의 나눗셈", 177),
    초등_비와_비율(SubjectV3.초6_1, "비와 비율", 179),
    초등_여러_가지_그래프(SubjectV3.초6_1, "여러 가지 그래프", 175),
    초등_직육면체의_부피와_겉넓이(SubjectV3.초6_1, "직육면체의 부피와 겉넓이", 180),

    초등_분수의_나눗셈6_2(SubjectV3.초6_2, "분수의 나눗셈", 282),
    초등_소수의_나눗셈6_2(SubjectV3.초6_2, "소수의 나눗셈", 278),
    초등_공간과_입체(SubjectV3.초6_2, "공간과 입체", 279),
    초등_비례식과_비례배분(SubjectV3.초6_2, "비례식과 비례배분", 280),
    초등_원의_넓이(SubjectV3.초6_2, "원의 넓이", 281),
    초등_원기둥_원뿔_구(SubjectV3.초6_2, "원기둥, 원뿔, 구", 283);




//    val id: Int = subject.id

    companion object {
        fun titleOfNonNull(title: String): BigUnitV3 =
            values()
                .firstOrNull { it.title == title } ?: 교육과정외
        fun idOfNonNull(id: Int): BigUnitV3 =
            values()
                .firstOrNull { it.id == id } ?: 교육과정외

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