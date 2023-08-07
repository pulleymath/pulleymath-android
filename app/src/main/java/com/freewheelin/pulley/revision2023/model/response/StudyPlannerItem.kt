package com.freewheelin.pulley.revision2023.model.response

import androidx.databinding.ObservableBoolean
import com.ht.RecyclerAdapters.ExpandableAdapter.ExpandableItem

class StudyPlannerItem (
    val subjectId: Int,
    val chapterId: Int?,
    val chapterName: String?,
    val itemType: StudyPlannerItemType,
    var title: String?,
    val category: WeeklyPlanTag?,
    val workbookId: Int?,
    val progress: StudyPlannerProgress?,
    val studyPlanBookId: Int?,
    val items: List<StudyPlannerItem>?,
): ExpandableItem {
    override val children: List<*>
        get() = items ?: ArrayList<StudyPlannerItem>()

    val progressText: String
        get() {
            return if (progress?.solvedProblemCount == null || progress.solvedProblemCount == 0) {
                "${progress?.totalProblemCount}문제"
            } else {
                "${progress.solvedProblemCount}/${progress.totalProblemCount}"
            }
        }
    val isCompleted: Boolean
        get() {
            return progress?.isCompleted == true
        }

    var isSelected: Boolean = false

//    var isSelected2: ObservableBoolean = ObservableBoolean(false)

}

enum class StudyPlannerItemType {
    DIRECTORY, WORKBOOK
}

data class StudyPlannerProgress(
    val totalProblemCount: Int,
    val solvedProblemCount: Int?,
    val isCompleted: Boolean
) {

}
