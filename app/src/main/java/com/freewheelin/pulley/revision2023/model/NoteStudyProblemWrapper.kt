package com.freewheelin.pulley.revision2023.model

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import java.util.*


data class NoteStudyProblemWrapper (
    val title: String,
    val type: NoteStudyType,
    val problem: Problem?
): BaseDiffItem {
    var problems: List<Problem> = listOf()
    var isSelected: ObservableBoolean = ObservableBoolean(false)

    var updateObservable: ObservableBoolean = ObservableBoolean(false)


    val isProblemCleared: ObservableBoolean
        get() {
            return ObservableBoolean(problem?.isClear ?: false)
        }
    val isProblemScraped: ObservableBoolean
        get() {
            return ObservableBoolean(problem?.isScrap ?: false)
        }

    override fun getId(): String {
        return "${hashCode()}"
    }
    companion object {
        fun getHeader(problems: List<Problem>): NoteStudyProblemWrapper {
            return NoteStudyProblemWrapper("999", NoteStudyType.Header, null).apply {
                this.problems = problems
            }
        }
        fun getGroupHeader(headerTxt: String): NoteStudyProblemWrapper {
            return NoteStudyProblemWrapper(headerTxt, NoteStudyType.GroupHeader, null).apply {
//
            }
        }
        fun getCard(problem: Problem): NoteStudyProblemWrapper {
            return NoteStudyProblemWrapper("9", NoteStudyType.Card, problem).apply {  }
        }
    }
}
enum class NoteStudyType {
    Header, GroupHeader, Card
}