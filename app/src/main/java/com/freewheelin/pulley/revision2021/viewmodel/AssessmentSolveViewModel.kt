package com.freewheelin.pulley.revision2021.viewmodel

import android.content.Context
import android.util.Log
import android.widget.CompoundButton
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.legacy.lib.ObservableHashSet
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.revision2021.model.response.AssessmentStudentWorkbook
import com.freewheelin.pulley.revision2021.model.response.AssessmentProblem
import com.freewheelin.pulley.revision2021.model.response.AssessmentWorkbook
import com.freewheelin.pulley.revision2021.repository.AssessmentRepository
import com.freewheelin.pulley.revision2021.utils.replace
import com.freewheelin.pulley.revision2021.views.adapters.AssessmentTestGalleryAdapter
import com.freewheelin.pulley.legacy.utils.DialogUtils
import com.freewheelin.pulley.revision2023.model.ChatBotInfo
import com.freewheelin.pulley.revision2023.model.ChatBotInfoImage
import com.freewheelin.pulley.revision2023.model.ChatBotInfoImageType
import com.freewheelin.pulley.revision2023.model.ChatBotInitViewType
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AssessmentSolveViewModel : BaseViewModel(), LifecycleObserver {
    val assessmentRepository: AssessmentRepository by lazy { AssessmentRepository.instance }

    var isSubmitBtnActive = MutableLiveData(false)

    var testStartedAt: String? = null
    var testFinishedAt: String? = null
    var showDimBgView = MutableLiveData(true)
    var showDimView = MutableLiveData(false)
    var isFixedStartTime = MutableLiveData(false)

    val minInDimDialog by lazy { MutableLiveData<String>("00") }
    val secInDimDialog by lazy { MutableLiveData<String>("00") }

    var isReview = MutableLiveData(false)
    var showSolutionView = MutableLiveData(false)
    var showGalleryView = MutableLiveData(false)

    var isEnableSolutionSwitch = MutableLiveData(false)

    var answeredSet: ObservableHashSet<AssessmentProblem> = ObservableHashSet()

    var selectedWorkbook: AssessmentWorkbook? = null
    var studentWorkbook: AssessmentStudentWorkbook? = null
    val problemList by lazy { MutableLiveData<List<AssessmentProblem>>() }
    val currentProblem by lazy { assessmentRepository.currentProblem }

    val problemIndex by lazy { MutableLiveData<Int>(0) }
    lateinit var contentAdapter: AssessmentTestGalleryAdapter

    var workbookId: Int = 0
    var version: Int = 0
    var showTimer: Boolean = false
    var testPeriodMinutes: Int = 0
    var workbookSeq: Int = 0

    var chatBotInfo: ChatBotInfo? = null

    fun onCommentaryShowChanged(buttonView: CompoundButton, isChecked: Boolean) {
        println("buttonView = [$buttonView], isChecked = [$isChecked]")
        showSolutionView.postValue(isChecked)
    }

    fun getTestResult (callback: ((problem: AssessmentProblem)-> Unit)?) {
        val studentId = user?.studentID ?: return
        compositeDisposable += assessmentRepository.fetchScoringResult(studentId, workbookId, version)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ res ->
                Log.d(javaClass.simpleName, "getTestResult list=>${res.data}")
                res.data?.let { resData ->
                    studentWorkbook = resData.student_workbook
                    val answerList = resData.answer_list
                    var pbList = resData.problem_list.toMutableList()
                    pbList = pbList.map {
                        val ans = answerList.filter { that -> that.problem_no == it.no }
                        if (ans.isNotEmpty()) {
                            it.user_answer = ans[0].user_answer
                            it.is_correct = ans[0].is_correct
                        }
                        it
                    }.toMutableList()

                    problemList.postValue(pbList)
                    callback?.invoke(pbList[0])

                }


            }, { error ->
                Log.e(javaClass.simpleName, "fetchScoringResult error=${error.localizedMessage}")
            })
    }

    fun getTestProblems (workbookId: Int, version: Int, callback: ((problem: AssessmentProblem)-> Unit)?) {
        val studentId = user?.studentID ?: return
        compositeDisposable += assessmentRepository.openWorkbook(studentId, workbookId, version)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ res ->
                Log.d(javaClass.simpleName, "testproblem list=>${res.data}")
                // 원본
                res.data?.let { resData ->

                    studentWorkbook = resData.user_workbook
                    val ansList = resData.answer_list

                    var pbList = resData.problem_list.toMutableList()

                    pbList = pbList.map {
                        val ans = ansList.filter { that -> that.problem_no == it.no }
                        if (ans.isNotEmpty()) {
                            it.user_answer = ans[0].user_answer
                            it.is_correct = ans[0].is_correct
                            answeredSet.add(it)
                        }
                        it
                    }.toMutableList()

                    val answeredProblem = answeredSet.filter { !it.user_answer.isNullOrEmpty() }
                    if (isReview.value == false) isSubmitBtnActive.postValue(answeredProblem.isNotEmpty()) // 대답된 값이 있으면 submit btn active

                    problemList.postValue(pbList)
                    callback?.invoke(pbList[0])
                }
            }, { error ->
                Log.e(javaClass.simpleName, "getTestProblems error=${error.localizedMessage}")
            })


    }

    fun onSubmit(context: Context, cb: (()-> Unit)) {
        if (isSubmitBtnActive.value == true) {
            callSubmitDialog(context, cb)
        }
    }

    fun finishTest(callback: (()->Unit)) {
        val studentId = user?.studentID
        if (studentId != null) {
            compositeDisposable += assessmentRepository.finishTest(studentId, workbookId)
                .subscribeOn(Schedulers.io())
                .timeout(3, TimeUnit.SECONDS)
                .subscribe({ res ->
                    CoroutineScope(Dispatchers.Main).launch {
                        callback()
                    }
                }, { error ->
                    Log.e(javaClass.simpleName, "finishTest error=${error.localizedMessage}")
                })

        }
    }

    fun callSubmitDialog(context: Context, cb: (() -> Unit)) {
        val answeredSetSize = answeredSet.size
        val problemSize = problemList.value?.size
        val remainingCount = problemSize?.minus(answeredSetSize) ?: return
        when (answeredSetSize == problemSize) {
            true -> DialogUtils.v2SubmitDialog(context) { cb() }
            false -> DialogUtils.v2SubmitUnCompletedDialog(context, remainingCount) { cb() }
        }
    }

    fun openProblem(problemNo: Int) {
        val studentId = user?.studentID
        if (studentId != null) {
            compositeDisposable += assessmentRepository.openProblem(studentId, workbookId, problemNo)
                .subscribeOn(Schedulers.io())
                .timeout(3, TimeUnit.SECONDS)
                .subscribe({ res ->

                }, { error ->
                    Log.e(javaClass.simpleName, "openProblem error=${error.localizedMessage}")
                })
        }
    }

    fun answerChanged(answer: String?, problem: AssessmentProblem?) {
        val problem = problem ?: currentProblem.value ?: return
        problem.user_answer = if(answer != null && answer.isNotEmpty()) answer else null

        if (problem.user_answer == null) {
            answeredSet.remove(problem)
        } else {
            answeredSet.add(problem)
        }

        insertAnswer(problem)
        isSubmitBtnActive.value = answeredSet.isNotEmpty()

        problemList.value?.replace(problem)?.let { newList ->
            problemList.postValue(newList)
        }

    }

    fun getServerTime(cb: (String) -> Unit) {
        compositeDisposable += assessmentRepository.getServerTimeNow()
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ serverTime ->
                cb(serverTime.now)
            }, { error ->
                Log.e(javaClass.simpleName, "insertAnswer error=${error.localizedMessage}")
            })
    }
    fun insertAnswer(problem: AssessmentProblem) {
        val studentId = user?.studentID
        val problemNo = problem.no
        val userAnswer = problem.user_answer ?: ""
        val param: Parameter = Parameter(
            "answer" to userAnswer
        )
        if (studentId != null) {

            compositeDisposable += assessmentRepository.insertAnswer(studentId, workbookId, problemNo, param)
                .subscribeOn(Schedulers.io())
                .timeout(3, TimeUnit.SECONDS)
                .subscribe({ res ->

                }, { error ->
                    Log.e(javaClass.simpleName, "insertAnswer error=${error.localizedMessage}")
                })
        }
    }
    val sdf by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA) }

    fun getFinishedTime(): String? {
//        println("aspasp getFinishedTime : ${isFixedStartTime.value}")
        return if (isFixedStartTime.value == true) {
            testFinishedAt
        } else {
            val cal = Calendar.getInstance()
            val startedAt = studentWorkbook?.started_at ?: return null
            val startedDate = sdf.parse(startedAt) ?: return null
            cal.time = startedDate
            cal.add(Calendar.MINUTE, testPeriodMinutes)
            sdf.format(cal.time)
        }
    }

    fun getStartedTime(): String? {
        return if (isFixedStartTime.value == true) {
            testStartedAt
        } else {
            studentWorkbook?.started_at
        }
    }

    fun get5MinBeforeFinishedTimeEnds(): String? {
        val cal = Calendar.getInstance()
        return if (isFixedStartTime.value == true) {
            if (testFinishedAt == null) return null
            val finishedDate = sdf.parse(testFinishedAt!!) ?: return null
            cal.time = finishedDate
            cal.add(Calendar.MINUTE, -5)
            sdf.format(cal.time)
        } else {
            val startedAt = studentWorkbook?.started_at ?: return null
            val startedDate = sdf.parse(startedAt) ?: return null
            cal.time = startedDate
            cal.add(Calendar.MINUTE, testPeriodMinutes - 5)
            sdf.format(cal.time)
        }
    }
    fun makeChatBotInfo(problem: AssessmentProblem) {
        chatBotInfo = ChatBotInfo(
            ChatBotInitViewType.SOLVE_PROBLEM, listOf(
                ChatBotInfoImage(
                    ChatBotInfoImageType.SOLVE_PROBLEM, 0, listOf(problem.img_url))
            ))
    }

}