package com.freewheelin.pulley.revision2021.model

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem
import java.io.Serializable

class CookingInfo {

    var chapterId: Int = 0
    var conceptCookingId: Int = 0
    var imageUrl: String = ""
//    var videoUrl: String = "" // 풀리로 옮겨오면서 변경된 값
    lateinit var video: Video
    lateinit var exerciseGroups: List<CookingExercise>



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

    val get1: String
        get() = "1"
    val get2: String
        get() = "2"
    val get3: String
        get() = "3"
    val get4: String
        get() = "4"
    val get5: String
        get() = "5"
}


class CookingQuiz: BaseDiffItem, Serializable {
    var exerciseQuizId: Int = 0
    var quizFormat: String = "SINGLE_SELECT"
    var userQuizSolvingHistoryId: Int = 0
    var quizImageUrl: String = "https://pulley-cm-book-pdfs.s3.ap-northeast-2.amazonaws.com/test/cooking_only_quiz.png"
    var hintImageUrl: String = ""

    var answerFormat: String = "SELECTIVE1"
    var answerOptions: List<ExerciseQuizAnswerOption> = listOf()
    var answer: String = "3" // 문제의 정답
    var userAnswer: String? = null


    var isHintUsed: ObservableBoolean = ObservableBoolean(false)
    var afterTryAnswered: ObservableBoolean = ObservableBoolean(false)
    var isAnswerEntered: ObservableBoolean = ObservableBoolean(false)
    var isCorrectAnswer: ObservableBoolean = ObservableBoolean(false)
    var selectedQuizAnswerImageUrl = ObservableField<String>("")

    val correctAnswerImage: String
     get() {

         val imageUrl =  when (answer) {
             "1" -> {
                 if (answerOptions.size > 0) answerOptions[0].imageUrl else ""
             }
             "2" -> {
                 if (answerOptions.size > 1) answerOptions[1].imageUrl else ""
             }
             "3" -> {
                 if (answerOptions.size > 2) answerOptions[2].imageUrl else ""
             }
             "4" -> {
                 if (answerOptions.size > 3) answerOptions[3].imageUrl else ""
             }
             "5" ->
                 if (answerOptions.size > 4) answerOptions[4].imageUrl else ""
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

class CookingInfoItem: BaseDiffItem, Serializable {
    override fun getId(): String {
        return "${id}"
    }
    enum class ItemType {
        Video,
        Exercise,
//        QuizHint,
    }

    var type: ItemType = ItemType.Video
    var id: Int = 0
    var cookingImageUrl: String? = null
//    var videoUrl: String? = null // 풀리로 넘어오면서 변경된 값
    lateinit var video: CookingInfo.Video

    val youtubeKey: String
        get() {
            return video.uuid
        }
    val youtubeStartSecond: Int
        get() {
            return if (video.startTime == null) 0 else video.startTime!!.toInt()
        }

    // exercise
    var exerciseList: List<CookingExercise>? = null

    // Video
    constructor(id: Int, video: CookingInfo.Video) {
        this.id = id
        this.type = ItemType.Video
        this.video = video
        order = "A"
    }

    var order: String = ""
    constructor(id: Int, exerciseList: List<CookingExercise>) {
        this.id = id
        this.type = ItemType.Exercise
        this.exerciseList = exerciseList
        order = "B"
    }

}

class CookingQuizSelection: BaseDiffItem, Serializable {
    var id: Int = hashCode()
    var seq: Int = 0
    var imageUrl : String = ""
    override fun getId(): String {
        return "$id"
    }

    constructor(url: String, index: Int) {
        imageUrl = url
        seq = index
    }
}