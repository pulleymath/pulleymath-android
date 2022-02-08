package com.freewheelin.pulley.revision2021.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.CompoundButton
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.lib.ObservableHashSet
import com.freewheelin.pulley.revision2021.model.response.AffiliatedStudentWorkbook
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestProblem
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestWorkbook
import com.freewheelin.pulley.revision2021.repository.AffiliatedTestRepository
import com.freewheelin.pulley.utils.DialogUtils
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AffiliatedTestSolveViewModel : BaseViewModel(), LifecycleObserver {
    private val affiliatedTestRepository: AffiliatedTestRepository by lazy { AffiliatedTestRepository() }

    var isSubmitBtnActive = MutableLiveData(false)

    var testStartedAt: String? = null
    var testFinishedAt: String? = null
    var showDimBgView = MutableLiveData(true)
    var showDimView = MutableLiveData(false)
    var isFixedStartTime = MutableLiveData(false)

    val minInDimDialog by lazy { MutableLiveData<String>("00") }
    val secInDimDialog by lazy { MutableLiveData<String>("00") }

    var isReview = MutableLiveData(false)
    var showCommentaryView = MutableLiveData(false)

    var answeredSet: ObservableHashSet<AffiliatedTestProblem> = ObservableHashSet()

    var selectedWorkbook: AffiliatedTestWorkbook? = null
    var studentWorkbook: AffiliatedStudentWorkbook? = null
    val problemList by lazy { MutableLiveData<List<AffiliatedTestProblem>>() }
    val currentProblem by lazy { MutableLiveData<AffiliatedTestProblem>() }

    val problemIndex by lazy { MutableLiveData<Int>(0) }

    var workbookId: Int = 0
    var version: Int = 0
    var showTimer: Boolean = false
    var testPeriodMinutes: Int = 0
    var workbookSeq: Int = 0

    fun onCommentaryShowChanged(buttonView: CompoundButton, isChecked: Boolean) {
        println("buttonView = [$buttonView], isChecked = [$isChecked]")
        showCommentaryView.postValue(isChecked)
    }

    @SuppressLint("CheckResult")
    fun getTestResult (callback: ((problem: AffiliatedTestProblem)-> Unit)?) {
        val studentId = user?.studentID ?: return
        affiliatedTestRepository.fetchScoringResult(studentId, workbookId, version)
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
    @SuppressLint("CheckResult")
    fun getTestProblems (workbookId: Int, version: Int, callback: ((problem: AffiliatedTestProblem)-> Unit)?) {
        val studentId = user?.studentID ?: return
        affiliatedTestRepository.openWorkbook(studentId, workbookId, version)
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

    @SuppressLint("CheckResult")
    fun finishTest(callback: (()->Unit)) {
        val studentId = user?.studentID
        if (studentId != null) {
            affiliatedTestRepository.finishTest(studentId, workbookId)
                .subscribeOn(Schedulers.io())
                .timeout(3, TimeUnit.SECONDS)
                .subscribe({ res ->
                    callback()
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

    @SuppressLint("CheckResult")
    fun openProblem(problemNo: Int) {
        val studentId = user?.studentID
        if (studentId != null) {
            affiliatedTestRepository.openProblem(studentId, workbookId, problemNo)
                .subscribeOn(Schedulers.io())
                .timeout(3, TimeUnit.SECONDS)
                .subscribe({ res ->
                    println("tpehf , ${problemNo}번 문제 열기 ")

                }, { error ->
                    Log.e(javaClass.simpleName, "openProblem error=${error.localizedMessage}")
                })
        }
    }

    fun answerChanged(answer: String?, problem: AffiliatedTestProblem?) {
        val problem = problem ?: currentProblem.value ?: return
        problem.user_answer = if(answer != null && answer.isNotEmpty()) answer else null

        if (problem.user_answer == null) {
            answeredSet.remove(problem)
        } else {
            answeredSet.add(problem)
        }

        insertAnswer(problem)
        isSubmitBtnActive.value = answeredSet.isNotEmpty()

    }

    @SuppressLint("CheckResult")
    fun insertAnswer(problem: AffiliatedTestProblem) {
        val studentId = user?.studentID
        val problemNo = problem.no
        val userAnswer = problem.user_answer ?: ""
        val param: Parameter = Parameter(
            "answer" to userAnswer
        )
        if (studentId != null) {

            affiliatedTestRepository.insertAnswer(studentId, workbookId, problemNo, param)
                .subscribeOn(Schedulers.io())
                .timeout(3, TimeUnit.SECONDS)
                .subscribe({ res ->
                    println("tpehf , ${problemNo}번 문제 답 입력! ")

                }, { error ->
                    Log.e(javaClass.simpleName, "insertAnswer error=${error.localizedMessage}")
                })
        }
    }
    val sdf by lazy { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA) }

    fun getFinishedTime(): String? {
        return when (workbookSeq) {
            1 -> { testFinishedAt }
            else -> {
                val cal = Calendar.getInstance()
                val startedAt = studentWorkbook?.started_at
                val startedDate = sdf.parse(startedAt)
                cal.time = startedDate
                cal.add(Calendar.MINUTE, testPeriodMinutes)
                return sdf.format(cal.time)
            }
        }
    }

    fun getStartedTime(): String? {
        return when (workbookSeq) {
            1 -> { testStartedAt }
            else -> { studentWorkbook?.started_at }
        }
    }

    fun get5MinBeforeFinishedTimeEnds(): String? {
        val cal = Calendar.getInstance()

        return when (workbookSeq) {
            1 -> {
                val finishedDate = sdf.parse(testFinishedAt)
                cal.time = finishedDate
                cal.add(Calendar.MINUTE, -5)
                return sdf.format(cal.time)
            }
            else -> {
//                val cal = Calendar.getInstance()
                val startedAt = studentWorkbook?.started_at
                val startedDate = sdf.parse(startedAt)
                cal.time = startedDate
                cal.add(Calendar.MINUTE, testPeriodMinutes - 5)
                return sdf.format(cal.time)
            }
        }
    }
}