package com.freewheelin.pulley.revision2021.model

import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.freewheelin.pulley.bases.MyApplication.Companion.user
import com.freewheelin.pulley.databinding.ItemCookingQuizDetailBinding
import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import java.io.Serializable

data class LCCookingWrapper(
    val data: CookingInfo,
    val error: String?,
    val message: String?,
    val current_time: String?
)
@Entity(tableName = "lc_cooking_info_table")
data class CookingInfo(
    @PrimaryKey(autoGenerate = false) var conceptCookingId: Int,
    val chapterId: Int,
    val imageUrl: String,
    val video: Video,
    val exerciseGroups: List<CookingExercise>
) {

//    var chapterId: Int = 0
//    var conceptCookingId: Int = 0
//    var imageUrl: String = ""
//
//    lateinit var video: Video
//    lateinit var exerciseGroups: List<CookingExercise>

    inner class Video {
        var url: String = ""
        var uuid: String = ""
        var startTime: Int? = null
        var endTime: Int? = null
    }
}

class CookingExercise: Serializable {

    var name: String = ""
    var cookingExerciseId = ""
    var imageUrl: String = ""
    var exerciseQuizzes: List<CookingQuiz>? = null

    val cookingQuiz0: CookingQuiz?
        get() {
            exerciseQuizzes?.let {
                if (it.isNotEmpty()) return it[0]
            }
            return null
        }
    val cookingQuiz1: CookingQuiz?
        get() {
            exerciseQuizzes?.let {
                if (it.size > 1) return it[1]
            }
            return null
        }
    val cookingQuiz2: CookingQuiz?
        get() {
            exerciseQuizzes?.let {
                if (it.size > 2) return it[2]
            }
            return null
        }
    val cookingQuiz3: CookingQuiz?
        get() {
            exerciseQuizzes?.let {
                if (it.size > 3) return it[3]
            }
            return null
        }
    val cookingQuiz4: CookingQuiz?
        get() {
            exerciseQuizzes?.let {
                if (it.size > 4) return it[4]
            }
            return null
        }

    val cookingQuiz5: CookingQuiz?
        get() {
            exerciseQuizzes?.let {
                if (it.size > 5) return it[5]
            }
            return null
        }
    val cookingQuiz6: CookingQuiz?
        get() {
            exerciseQuizzes?.let {
                if (it.size > 6) return it[6]
            }
            return null
        }
    val cookingQuiz7: CookingQuiz?
        get() {
            exerciseQuizzes?.let {
                if (it.size > 7) return it[7]
            }
            return null
        }
    val cookingQuiz8: CookingQuiz?
        get() {
            exerciseQuizzes?.let {
                if (it.size > 8) return it[8]
            }
            return null
        }
    val cookingQuiz9: CookingQuiz?
        get() {
            exerciseQuizzes?.let {
                if (it.size > 9) return it[9]
            }
            return null
        }

}


class CookingQuiz: BaseDiffItem, Serializable {
    var exerciseQuizId: Int = 0
    var quizFormat: String = "SINGLE_SELECT"
    var userQuizSolvingHistoryId: Int = 0
    var quizImageUrl: String = "https://pulley-cm-book-pdfs.s3.ap-northeast-2.amazonaws.com/test/cooking_only_quiz.png"
    var hintImageUrl: String? = null

    var answerFormat: String = "SELECTIVE1"
    var answerOptions: List<ExerciseQuizAnswerOption> = listOf()
    var answer: String = "3" // 문제의 정답
    var userAnswer: String? = null


    var isHintUsed: ObservableBoolean = ObservableBoolean(false)
    var afterTryAnswered: ObservableBoolean = ObservableBoolean(false)
    var isAnswerEntered: ObservableBoolean = ObservableBoolean(false)
    var isCorrectAnswer: ObservableBoolean = ObservableBoolean(false)
    var selectedQuizAnswerImageUrl = ObservableField<String>("")

    val hintExist: Boolean
        get() = hintImageUrl != null

    val sortedAnswerOptions: List<ExerciseQuizAnswerOption>
        get() {
            return answerOptions.sortedBy { it.number }
        }
    val readyToAnswerOption1Url: String
        get () {
            val imageUrl = if (sortedAnswerOptions.size > 0) sortedAnswerOptions[0].imageUrl else ""
            return imageUrl
        }
    val readyToAnswerOption2Url: String
        get () {
            val imageUrl = if (sortedAnswerOptions.size > 1) sortedAnswerOptions[1].imageUrl else ""
            return imageUrl
        }
    val readyToAnswerOption3Url: String
        get () {
            val imageUrl = if (sortedAnswerOptions.size > 2) sortedAnswerOptions[2].imageUrl else ""
            return imageUrl
        }
    val readyToAnswerOption4Url: String
        get () {
            val imageUrl = if (sortedAnswerOptions.size > 3) sortedAnswerOptions[3].imageUrl else ""
            return imageUrl
        }
    val readyToAnswerOption5Url: String
        get () {
            val imageUrl = if (sortedAnswerOptions.size > 4) sortedAnswerOptions[4].imageUrl else ""
            return imageUrl
        }

    val correctAnswerImage: String
     get() {
         val imageUrl =  when (answer) {
             "1" -> {
                 if (sortedAnswerOptions.size > 0) sortedAnswerOptions[0].imageUrl else ""
             }
             "2" -> {
                 if (sortedAnswerOptions.size > 1) sortedAnswerOptions[1].imageUrl else ""
             }
             "3" -> {
                 if (sortedAnswerOptions.size > 2) sortedAnswerOptions[2].imageUrl else ""
             }
             "4" -> {
                 if (sortedAnswerOptions.size > 3) sortedAnswerOptions[3].imageUrl else ""
             }
             "5" ->
                 if (sortedAnswerOptions.size > 4) sortedAnswerOptions[4].imageUrl else ""
             else -> ""
         }

         return imageUrl
     }

    override fun getId(): String {
        return "${exerciseQuizId}"
    }

    val format: QuizFormat
        get() {
            return when(quizFormat) {
                "SINGLE_SELECT" -> QuizFormat.Single
                "SHORT_ANSWER" -> QuizFormat.Short
                else -> QuizFormat.Multi
            }
        }
}

class ExerciseQuizAnswerOption: Serializable {
    var number: Int = 0
    var imageUrl: String = ""
}

@Entity(tableName = "lc_cooking_info_item_table")
data class CookingInfoItem(
    @PrimaryKey(autoGenerate = false) val itemId: Int,
    val cookingId: Int,
    val quiz: CookingQuiz? = null,
    val cookingInfo: CookingInfo? = null,
    val order: String = "",
    val exerciseList: List<CookingExercise>? = null,
    var type: ItemType = ItemType.Video,
    var video: CookingInfo.Video? = null,
    var studentId: String? = user?.studentID
): BaseDiffItem, Serializable {
    override fun getId(): String {
        return "${itemId}"
    }
    enum class ItemType {
        Video,
        Exercise,
        Footer
    }

    companion object {
//        fun getVideoItem(id: Int, video: CookingInfo.Video, exerciseList: List<CookingExercise>): CookingInfoItem {
        fun getVideoItem(info: CookingInfo): CookingInfoItem {
            return CookingInfoItem(
                itemId = info.conceptCookingId + 10000,
                cookingId = info.conceptCookingId,
                type = ItemType.Video,
                video = info.video,
                exerciseList = info.exerciseGroups,
                order = "A"
            )
        }

//        fun getExerciseItem(id: Int, quiz: CookingQuiz): CookingInfoItem {
//            return CookingInfoItem(
//                id = id,
//                type = ItemType.Exercise,
//                quiz = quiz,
//                order = "D${quiz.exerciseQuizId}"
//            ).apply {
//                val isSolved = quiz.userAnswer != null
//                val isCorrectAnswer = quiz.userAnswer == quiz.answer
//                quiz.afterTryAnswered.set(isSolved)
//                quiz.isAnswerEntered.set(isSolved)
//                quiz.isCorrectAnswer.set(isCorrectAnswer)
//
//                // TODO 이거 필요함?
//                if (quiz.format == QuizFormat.Single) {
//                    quiz.userAnswer?.toInt()?.let { position ->
//                        val selectedImageUrl = quiz.sortedAnswerOptions[position - 1].imageUrl
//                        quiz.selectedQuizAnswerImageUrl.set(selectedImageUrl)
//                    }
//                }
//            }
//        }
//        fun getExercise(id: Int, cookingInfo: CookingInfo, exerciseList: List<CookingExercise>): CookingInfoItem {
        fun getExercise(info: CookingInfo): CookingInfoItem {
            return CookingInfoItem(
                itemId = info.conceptCookingId + 100000,
                type = ItemType.Exercise,
                exerciseList = info.exerciseGroups,
                cookingInfo = info,
                cookingId = info.conceptCookingId,
                order = "B"
            )
        }
//        fun getFooter(id: Int): CookingInfoItem {
        fun getFooter(info: CookingInfo): CookingInfoItem {
            return CookingInfoItem(
                itemId = info.conceptCookingId + 1000000,
                cookingId = info.conceptCookingId,
                type = ItemType.Footer,
                order = "C"
            )
        }
     }
}

class CookingQuizSelection: BaseDiffItem, Serializable {
    var id: Int = hashCode()
    var seq: Int = 0
    var imageUrl : String = ""

    var parentQuiz: CookingQuiz
    var parentView: View
    var binding: ItemCookingQuizDetailBinding
    override fun getId(): String {
        return "$id"
    }

    constructor(url: String, index: Int, parent: CookingQuiz, view: View, binding: ItemCookingQuizDetailBinding) {
        imageUrl = url
        seq = index
        parentQuiz = parent
        parentView = view
        this.binding = binding
    }
}