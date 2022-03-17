package com.freewheelin.pulley.revision2021.model.response

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.ScreenTheme
import com.freewheelin.pulley.model.ProblemType
import com.freewheelin.pulley.model.Result
import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import com.freewheelin.pulley.revision2021.model.response.base.BaseResponse
import com.freewheelin.pulley.revision2021.model.response.base.BaseSingleResponseNode
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class AffiliatedTestGroup: BaseDiffItem, Serializable {
    var id: Int = 0
    lateinit var group_title: String
    var seq: Int = 0
    var school_id: Int = 0
    var major_id: Int = 0
    lateinit var updated_at: String
    lateinit var updated_by: String
    var is_active: Boolean = false

    var isCompleted: ObservableBoolean = ObservableBoolean(false)
    var isSelected: ObservableBoolean = ObservableBoolean(false)


    override fun getId() = "${id}"
}

class AffiliatedTestWorkbook: BaseDiffItem, Serializable {
    var id: Int = 0
    override fun getId() = "${id}"

    var group_id: Int = 0
    var problem_workbook_id: Int = -1
    lateinit var title: String
    lateinit var sub_title: String
    lateinit var subject: String
    var seq: Int = 0
    var applying_end_date: String? = null
    var test_started_at: String? = null
    var test_finished_at: String? = null
    lateinit var result_announce: String
    lateinit var test_source: String
    var perfect_score: Int = 0
    var problem_number: Int = 0
    var test_period_minutes: Int = 0
    var test_taker_number: Int = 0
    lateinit var instruction: String
    var is_active: Boolean = false
    lateinit var updated_at: String
    lateinit var updated_by: String
    var is_math: Boolean = false
    var pulley_workbook_id: Int = 0
    var is_fixed_time: Boolean = false
    var version: Int = 0

    // ---
    var isSelected: ObservableBoolean = ObservableBoolean(false)
    var isCompleted: ObservableBoolean = ObservableBoolean(false)
    var showTimer: Boolean = true
    fun select(): AffiliatedTestWorkbook {
        isSelected.set(true)
        return this
    }
    var remainingTimeText = ObservableField<String>(" ")

    fun setStudentWorkbook(swList: List<AffiliatedStudentWorkbook>): AffiliatedTestWorkbook {
        val newSW = swList.filter { it.workbook_id == id }.get(0)
        started_at = newSW.started_at
        interrupted_at = newSW.interrupted_at
        finished_at = newSW.finished_at
        score = newSW.score
        school_id = newSW.school_id
        major_code = newSW.major_code
        name = newSW.name
        student_code = newSW.student_code
        return this
    }
    var started_at: String? = null
    var interrupted_at: String? = null
    var finished_at: String? = null
    var score: Int? = null
    var school_id: Int = 0
    lateinit var major_code: String
    lateinit var name: String
    var student_code: Int? = null

    fun isTestNotStartedYet(): Boolean {
        return !isTestStartTimeHasPassed()
    }
    fun isTestStartTimeHasPassed(): Boolean {
        return started_at != null // 값이 존재하면 시험이 시작된 이후임
    }
    fun getWorkbookStartBtnText(): String {
        return if (isTestStartTimeHasPassed()) {
            "이어풀기"
        } else {
            "시험 시작하기"
        }
    }
    fun isFinished() : Boolean {
        return finished_at != null
    }
    fun sceneTheme(): ScreenTheme {
        return if (isFinished()) ScreenTheme.Bright else ScreenTheme.BrightOutside
    }
    fun isNotFinished(): Boolean {
        return !isFinished()
    }

    fun workbookOrderResource(num: Int): Int {
        if (isFinished()) return R.drawable.ic_check_green_circle_24
        return when(num) {
            1 -> R.drawable.ic_1_grey_24
            2 -> R.drawable.ic_2_grey_24
            else -> R.drawable.ic_3_grey_24
        }
    }
}

class AffiliatedTestWorkbookOnStudent: BaseDiffItem, Serializable {
    var id: Int = 0
    override fun getId() = "${id}"

    lateinit var student_id: String
    var workbook_id: Int = 0
    var started_at: String? = null
    var interrupted_at: String? = null
    var finished_at: String? = null
    var score: Int? = 0
    var school_id: Int = 0
    var major_id: Int = 0
    lateinit var name: String
    lateinit var student_code: String
    var group_id: Int = 0
    lateinit var title: String
    lateinit var sub_title: String
    lateinit var subject: String
    var seq: Int = 0

    lateinit var applying_end_date: String
    lateinit var test_started_at: String
    lateinit var test_finished_at: String
    lateinit var result_announce: String
    lateinit var test_source: String
    var perfect_score: Int = 0
    var problem_number: Int = 0
    var test_period_minutes: Int = 0
    var test_taker_number: Int = 0
    lateinit var instruction: String
    var is_active: Boolean = false
    lateinit var updated_at: String
    lateinit var updated_by: String
    var is_math: Boolean = false
    var pulley_workbook_id: Int = 0
    var is_fixed_time: Boolean = false

}

class AffiliatedTestCard: BaseDiffItem, Serializable {
    var groupId: Int = 0
    var group_title: String = ""
    var seq: Int = 0

    override fun getId() = "$groupId"
    lateinit var workbookList: List<AffiliatedTestWorkbook>

    lateinit var firstWorkbook: AffiliatedTestWorkbook
    lateinit var secondWorkbook: AffiliatedTestWorkbook
    lateinit var thirdWorkbook: AffiliatedTestWorkbook
    lateinit var selectedWorkbook: AffiliatedTestWorkbook

    constructor(id: Int, list: List<AffiliatedTestWorkbook>) {
        groupId = id
        workbookList = list
        val filteredValue1 = workbookList.filter { it.seq == 1 }.get(0)
        firstWorkbook = filteredValue1
//        firstWorkbook.setTimer()
        val filteredValue2 = workbookList.filter { it.seq == 2 }.get(0)
        secondWorkbook = filteredValue2
        val filteredValue3 = workbookList.filter { it.seq == 3 }.get(0)
        thirdWorkbook = filteredValue3

        selectedWorkbook =
            when {
                thirdWorkbook.isFinished() -> filteredValue3.select()
                secondWorkbook.isFinished() -> filteredValue3.select()
                firstWorkbook.isFinished() -> filteredValue2.select()
                else -> filteredValue1.select()
            }
    }

    var isSelected: ObservableBoolean = ObservableBoolean(false)
    var isCompleted: ObservableBoolean = ObservableBoolean(false)

    fun workbookOrderResource(num: Int): Int {
        val wb = when(num) {
            1 -> firstWorkbook
            2 -> secondWorkbook
            else -> thirdWorkbook
        }
        return getOrderDrawable(wb, num)
    }
    fun getOrderDrawable (wb: AffiliatedTestWorkbook, num: Int): Int {
        if (wb.isFinished()) return R.drawable.ic_check_green_circle_24
        return when(num) {
            1 ->  R.drawable.ic_1_grey_24
            2 -> R.drawable.ic_2_grey_24
            else -> R.drawable.ic_3_grey_24
        }
    }

    fun workbookBtnBackgroundResource(num: Int): Int {
        val wb = when(num) {
            1 -> firstWorkbook
            2 -> secondWorkbook
            else -> thirdWorkbook
        }
        if (wb.isFinished()) return R.drawable.bg_purple_6d6dff_round
        return R.drawable.bg_white_ffffff_stroke_purple_6d6dff_round
    }

    val sdf by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA) }

    fun isTestEnable(currentTimeString: String?): Boolean {
        if (selectedWorkbook.test_started_at == null && selectedWorkbook.seq != 1) return true
        val testStartTime = selectedWorkbook.test_started_at ?: return false
        val currentServerTimeString = currentTimeString ?: return false

        val paredDate = sdf.parse(testStartTime)
        val parsedCurrentServerDate = sdf.parse(currentServerTimeString)

        val minDiff = (paredDate.time - parsedCurrentServerDate.time).toFloat() / (60 * 1000).toFloat()

        // endtime check
        val testFinishTime = selectedWorkbook.test_finished_at
        val finishDate = sdf.parse(testFinishTime)
        val finishDiff = (finishDate.time - parsedCurrentServerDate.time).toFloat() / (60 * 1000).toFloat()

//        println("tpehf, minDiff ${minDiff}, finishDiff : ${finishDiff}")
        return finishDiff > 0 && minDiff < 30
    }

    fun stepSelectRelease() {
        firstWorkbook.isSelected.set(false)
        secondWorkbook.isSelected.set(false)
        thirdWorkbook.isSelected.set(false)
        selectedWorkbook.isSelected.set(false)
    }

    var remainingTimeText = ObservableField("")

    fun areAllWorkbookFinished() : Boolean {
        return firstWorkbook.isFinished() && secondWorkbook.isFinished() && thirdWorkbook.isFinished()
    }
}

class AffiliatedTestProblem: BaseDiffItem, Serializable {
    var id: Int = 0
    override fun getId() = "$id"

    var workbook_id: Int = 0
    var no: Int = 0
    var subject: String? = null
    lateinit var unit: String
    lateinit var part: String
    lateinit var intention: String
    lateinit var reference: String
    var level: Int = 0
    var point: Int = 0
    lateinit var type: String
    lateinit var answer: String
    var source_id: Int = 0
    lateinit var source_num: String
    lateinit var img_url: String
    lateinit var thumb_url: String
    lateinit var updated_at: String
    lateinit var updated_by: String
    var pulley_problem_id: Int = 0
    lateinit var answer_img_url: String

    var user_answer: String? = null
    var is_correct: Boolean? = null
    var result: Int? = null
        get() {
            if (is_correct == null) return null
            return if (is_correct == true) 1 else -2
//            if (user_answer == null) return null
//            if (answer == user_answer) return 1
//            return -2
        }

    fun getProblemType(): ProblemType {
        return if (type == "주관식") {
            return ProblemType.short
        } else if (type == "객관식") {
            return ProblemType.single
        } else {
            return ProblemType.multi
        }
    }
    fun getResultByScoring(): Result {
        if (result == 1)
            return Result.correct
        else if(result == -2)
            return Result.incorrect
        else
            return Result.yet
    }

    fun getStepOnScore(score: Int): Int {
        // default는 2
        val subject = subject ?: return 2
        return when(subject) {
            "확률과 통계" -> {
                when (score) {
                    in 0..50 -> 1
                    in 51..85 -> 2
                    in 86..100 -> 3
                    else -> 2
                }
            }
            "미적분" -> {
                when (score) {
                    in 0..45 -> 1
                    in 46..85 -> 2
                    in 86..100 -> 3
                    else -> 2
                }
            }
            "물리학" -> {
                when (score) {
                    in 0..40 -> 1
                    in 41..85 -> 2
                    in 86..100 -> 3
                    else -> 2
                }
            }
            "화학" -> {
                when (score) {
                    in 0..45 -> 1
                    in 46..85 -> 2
                    in 86..100 -> 3
                    else -> 2
                }
            }
            "생명과학" -> {
                when (score) {
                    in 0..40 -> 1
                    in 41..85 -> 2
                    in 86..100 -> 3
                    else -> 2
                }
            }
            else -> 2
        }
    }
    fun levelString() : String {
        return when (level) {
            1 -> "하"
            2 -> "중"
            3 -> "상"
            else -> "최상"
        }
    }
    fun referenceVisible(): Boolean {
        return when(reference) {
            "", null -> false
            else -> true
        }
    }
}
class AffiliatedTestAnswer: Serializable {

    lateinit var user_workbook: AffiliatedStudentWorkbook
    lateinit var problem_list: List<AffiliatedTestProblem>
    lateinit var answer_list: List<AffiliatedTestAnswer2>
}

class AffiliatedTestAnswer2: BaseDiffItem, Serializable {
    var id: Int = 0
    override fun getId() = "$id"

    lateinit var student_id: String
    var workbook_id: Int = 0
    var problem_no: Int = 0
    var started_at: String? = null
    var user_answer: String? = null
    var is_correct: Boolean? = null
    var marked_at: String? = null

    var result: Int = 0
    get() {
        return when (is_correct) {
            null -> 0
            true -> 1
            false -> -2
        }
    }

}

class AffiliatedGroup: Serializable {
    lateinit var group_list: List<AffiliatedTestGroup>
    lateinit var workbook_list: List<AffiliatedTestWorkbook>
    lateinit var student_workbook_list: List<AffiliatedStudentWorkbook>
}

class AffiliatedStudentWorkbook: Serializable {
    var id: Int = 0
    lateinit var student_id: String
    var group_id: Int = 0
    var workbook_id: Int = 0
    var started_at: String? = null
    var interrupted_at: String? = null
    var finished_at: String? = null
    var score: Int? = null
    var school_id: Int = 0
    lateinit var major_code: String
    lateinit var name: String
    var student_code: Int? = null
}

class AffiliatedOpenProblem: Serializable {
    var id: Int = 0
    lateinit var student_id: String
    var workbook_id: Int = 0
    var problem_no: Int = 0
    var started_at: String? = null
    var user_answer: String? = null
    var is_correct: Boolean? = null
    lateinit var marked_at: String
}
class AffiliatedScoringResult: Serializable {
    lateinit var answer_list: List<AffiliatedTestAnswer2>
    lateinit var problem_list: List<AffiliatedTestProblem>
    lateinit var student_workbook: AffiliatedStudentWorkbook
}

class AffiliatedSolution: BaseDiffItem, Serializable {
    var id: Int = -1
    override fun getId() = "$id"

    var problem_id: Int = -1
    var group_no: Int = -1
    var seq: Int = -1
    var group_title: String = ""
    var media_file_id: Int = -1
    var subject: String = ""
    var type: String = ""
    var title: String = ""
    var length: Int = -1 // 비디오의 총 시간(초)
    var filename: String = ""
    var thumburl: String = ""
    var fileurl: String = ""


    constructor(type: ItemType, title: String, id: Int, parentId: Int = -1) {
        this.id = id
        itemType = type
        this.title = title
        this.parentId = parentId
    }
    constructor(type: ItemType, solution: AffiliatedSolution) {
        this.id = hashCode()
        itemType = type
        this.title = solution.title
        this.group_no = solution.group_no
        this.group_title = solution.group_title
    }
    var listOrder: String = ""

    var isSelected: Boolean = false // ObservableBoolean = ObservableBoolean(false)

    var itemType: ItemType? = null
        get() {
            return if (field == null) {
                when (type) {
                    "video" -> ItemType.videoItem
                    "pdf" -> ItemType.pdfItem
                    else -> ItemType.textHeader
                }
            } else {
                field
            }
        }

    fun isVideoGroup (): Boolean {
        val it = itemType ?: return false
        return when (it) {
            ItemType.videoGroupHeader, ItemType.videoItem, ItemType.videoFooter -> true
            else -> false
        }
     }
    fun isNotVideoGroup() : Boolean {
        return !isVideoGroup()
    }
    fun isVideoItemAndFooter(): Boolean {
        val it = itemType ?: return false
        return when (it) {
            ItemType.videoItem, ItemType.videoFooter -> true
            else -> false
        }
    }
    fun isNotVideoItemAndFooter(): Boolean {
        return !isVideoItemAndFooter()
    }
    var parentId: Int = -1
    enum class ItemType {
        textHeader,
        pdfItem,
        videoTextHeader,
        videoGroupHeader, // video group
        videoItem, // video group
        videoFooter // video group
    }

    val minSec: String
        get() {
            if (length == -1) return "00:00"
            val min = (length / 60).let {
                if (it < 10) {
                    return@let "0${it}"
                }
                return@let "${it}"
            }
            val sec = (length % 60).let {
                if (it < 10) return@let "0${it}"
                return@let "${it}"
            }
            return "${min}:${sec}"
        }
    var videoGroupDurationString: String = "00:00"
}


class AffiliatedMediaLogResponse<T>: Serializable {
    var data: T? = null
    var error: Any? = null
    var message: Any? = null
    var pageable: Any? = null
    var current_time: String? = ""
}

class AffiliatedTestResponse : BaseResponse<AffiliatedTestGroup>()
class AffiliatedTestWorkbookResponse : BaseResponse<AffiliatedTestWorkbook>()
class AffiliatedTestWorkbookOnStudentResponse : BaseResponse<AffiliatedTestWorkbookOnStudent>()
class AffiliatedTestProblemResponse : BaseResponse<AffiliatedTestProblem>()
class AffiliatedTestAnswerResponse : BaseSingleResponseNode<AffiliatedTestAnswer>()
class AffiliatedTestAnswer2Response : BaseResponse<AffiliatedTestAnswer2>()
class AffiliatedGroupResponse : BaseSingleResponseNode<AffiliatedGroup>()
class AffiliatedOpenProblemResponse : BaseSingleResponseNode<AffiliatedOpenProblem>()
class AffiliatedStudentWorkbookResponse : BaseSingleResponseNode<AffiliatedStudentWorkbook>()
class AffiliatedScoringResultResponse : BaseSingleResponseNode<AffiliatedScoringResult>()
class AffiliatedSolutionResponse : BaseResponse<AffiliatedSolution>()

data class AffiliatedMediaLog(val problem_id: Int, val media_id: Int, val media_file_id: Int, val student_id: String): Serializable
