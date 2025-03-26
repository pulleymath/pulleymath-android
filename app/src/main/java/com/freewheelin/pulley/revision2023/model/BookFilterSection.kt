package com.freewheelin.pulley.revision2023.model

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem


data class BookFilterElement(
    val type: Type,
    val name: String,
    val parentTitle: String? = null,
    var value: String? = null,
    val seq: Int? = null,
    val code: String? = null,
): BaseDiffItem {
    var isSelected: ObservableBoolean = ObservableBoolean(false)
    var calendarValue: ObservableField<String> = ObservableField(value)

    enum class Type {
        Header, Item, Toggle, Calendar
    }
    val filterType: LearningFilterType
        get() {
            return LearningFilterType.valueOfNonNull(value)
        }
    override fun getId(): String {
        return "${hashCode()}"
    }
    companion object {
        fun getToggle(): BookFilterElement {
            return BookFilterElement(Type.Toggle, "핀 설정한 문제집 제외")
        }
        fun getCalendar(value: String?): BookFilterElement {
            return BookFilterElement(Type.Calendar, "캘린더", value = value)
        }
    }
}


data class BookFilterSection(
    val filterTitle: String,
    val filterItems: List<BookFilterItem>
)

data class BookFilterItem (
    val name: String,
    val value: String,
    val seq: Int,
    val code: String
)

enum class BookFilterParent {
    PULLEY_WORKBOOK,
    COMMERCIAL_BOOK,
    CUSTOM_WORKBOOK,
    WRONG_NOTE,
    SCRAP_NOTE;
}

enum class LearningFilterType(val title: String, val displayedName: String) {
    과목_전체("과목", "전체"),
    과목_수학_상("과목", "수학(상)"),
    과목_수학_하("과목", "수학(하)"),
    과목_수학1("과목", "수학1"),
    과목_수학2("과목", "수학2"),
    과목_확통("과목", "확률과 통계"),
    과목_미적분("과목", "미적분"),
    과목_기하("과목", "기하"),

    과목_초1_1("과목", "초 1-1"),
    과목_초1_2("과목", "초 1-2"),
    과목_초2_1("과목", "초 2-1"),
    과목_초2_2("과목", "초 2-2"),
    과목_초3_1("과목", "초 3-1"),
    과목_초3_2("과목", "초 3-2"),
    과목_초4_1("과목", "초 4-1"),
    과목_초4_2("과목", "초 4-2"),
    과목_초5_1("과목", "초 5-1"),
    과목_초5_2("과목", "초 5-2"),
    과목_초6_1("과목", "초 6-1"),
    과목_초6_2("과목", "초 6-2"),

    과목_중1_1("과목", "중 1-1"),
    과목_중1_2("과목", "중 1-2"),
    과목_중2_1("과목", "중 2-1"),
    과목_중2_2("과목", "중 2-2"),
    과목_중3_1("과목", "중 3-1"),
    과목_중3_2("과목", "중 3-2"),

    유형_전체("문제집 유형", "전체"),
    유형_유형서("문제집 유형", "유형서"),
    유형_내신서("문제집 유형", "내신서"),
    유형_기출서("문제집 유형", "기출서"),

    추천_전체("추천 등급", "전체"), // 고등 추천 타입
    추천_1등급("추천 등급", "1등급"),
    추천_2_3등급("추천 등급", "2~3등급"),
    추천_3_4등급("추천 등급", "3~4등급"),
    추천_4등급이하("추천 등급", "4등급이하"),

    추천레벨_전체("추천 레벨", "전체"),  // 중등 추천 타입
    추천레벨_상("추천 레벨", "상"),
    추천레벨_중("추천 레벨", "중"),
    추천레벨_하("추천 레벨", "하"),

    학습유형_전체("학습 유형", "전체"),
    학습유형_유형학습("학습 유형", "유형학습"),
    학습유형_워크북("학습 유형", "워크북"),
    학습유형_오답학습("학습 유형", "오답학습"),
    학습유형_추천학습("학습 유형", "추천학습"),
    학습유형_모의고사("학습 유형", "모의고사"),
    학습유형_테스트("학습 유형", "테스트"),

    난이도_전체("난이도", "전체"),
    난이도_하("난이도", "하"),
    난이도_중하("난이도", "중하"),
    난이도_중("난이도", "중"),
    난이도_상("난이도", "상"),
    난이도_최상("난이도", "최상"),

    풀리북스_유형_전체("풀리북스 유형", "전체"),
    풀리북스_유형_고등예비("풀리북스 유형", "고등예비"),
    풀리북스_유형_개념서("풀리북스 유형", "개념서"),
    풀리북스_유형_유형서("풀리북스 유형", "유형서"),
    풀리북스_유형_심화서("풀리북스 유형", "심화서"),
    풀리북스_유형_내신서("풀리북스 유형", "내신서"),
    풀리북스_유형_기출서("풀리북스 유형", "기출서"),
    풀리북스_유형_실전모의고사("풀리북스 유형", "실전모의고사"),
    풀리북스_유형_공식집("풀리북스 유형", "공식집"),

    출판사_전체("출판사", "전체"),
    출판사_아름다운샘("출판사", "아름다운샘"),
    출판사_진학사("출판사", "진학사"),
    출판사_CSM17("출판사", "CSM17"),
    출판사_YH에듀케이션("출판사", "YH에듀케이션"),
    출판사_EBS("출판사", "EBS"),

    보기설정_전체("보기 설정", "전체"),
    보기설정_맞은문제("보기 설정", "맞은 문제"),
    보기설정_틀린문제("보기 설정", "틀린 문제"),
    보기설정_안_푼_문제("보기 설정", "안 푼 문제"),

    보기설정_클리어_미포함("보기 설정", "클리어 미포함"),
    보기설정_클리어_포함("보기 설정", "클리어 포함"),

    핀_포함("핀", "전체"),
    핀_미포함("핀", "전체"),
    기타("기타", "전체");

    companion object {
        fun convertTagAtFilterType(tag: String): LearningFilterType {
            return when (tag.trim()) {
                유형_유형서.displayedName -> 유형_유형서
                유형_내신서.displayedName -> 유형_내신서
                유형_기출서.displayedName -> 유형_기출서
                추천_전체.displayedName, "모든 등급" -> 추천_전체
                추천_1등급.displayedName -> 추천_1등급
                추천_2_3등급.displayedName -> 추천_2_3등급
                추천_3_4등급.displayedName -> 추천_3_4등급
                추천_4등급이하.displayedName, "4등급 이하" -> 추천_4등급이하
                추천레벨_전체.displayedName, "모든 레벨" -> 추천레벨_전체
                추천레벨_상.displayedName -> 추천레벨_상
                추천레벨_중.displayedName -> 추천레벨_중
                추천레벨_하.displayedName -> 추천레벨_하

                else -> 과목_전체
            }
        }
        fun valueOfNonNull(value: String?): LearningFilterType =
            value?.let {
                values().firstOrNull { it.name == value } ?: 기타
            } ?: 기타
    }

}