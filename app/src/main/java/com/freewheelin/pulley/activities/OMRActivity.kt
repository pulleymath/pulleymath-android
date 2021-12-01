package com.freewheelin.pulley.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.BaseNavActivity
import com.freewheelin.pulley.activities.learning.tabFragment.mockExam.MockExamFragment
import com.freewheelin.pulley.bases.isSPYMode
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.AppUsageMonitor
import com.freewheelin.pulley.core.manage.AppUsageMonitorListener
import com.freewheelin.pulley.core.manage.ContentManager
import com.freewheelin.pulley.core.manage.MockExamManager
import com.freewheelin.pulley.dialogs.CompleteDialog
import com.freewheelin.pulley.model.contents.MockExam
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.ProblemType
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.*
import com.freewheelin.pulley.views.OMRView.OMRViewType
import kotlinx.android.synthetic.main.activity_omr.*
import kotlinx.android.synthetic.main.dialog_daebak.*
import kotlinx.android.synthetic.main.view_timer_vertical.view.*
import kotlin.collections.ArrayList


class OMRActivity : BaseNavActivity(), NumberKeypadListener, OMRViewListener, TimerViewListener, AppUsageMonitorListener {

    lateinit var mockExam: MockExam
    lateinit var problems: List<Problem>
//    var isRestart: Boolean = false

    companion object {
        fun getIntent(context: Context, exam: MockExam, isRestart: Boolean = false): Intent {
            val intent = Intent(context, OMRActivity::class.java)
            intent.putExtra(MockExamManager.ARG_MOCK_EXAM, exam)
            intent.putExtra(MockExamManager.ARG_MOCK_IS_RESTART, isRestart)
            return intent
        }
    }

    override val backTintColor: Int
        get() = ContextCompat.getColor(this, R.color.black_4c4c4c)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.mockExam = intent.getSerializableExtra(MockExamManager.ARG_MOCK_EXAM) as MockExam
//        this.isRestart = intent.getBooleanExtra(MockExamManager.ARG_MOCK_IS_RESTART, false)
        setContentView(R.layout.activity_omr)

        loadInitData()
        initUI()
        setTimer()
    }

    private fun loadInitData() {
        MockExamManager.getMockProblems(this, mockExam, user!!) {
            mockExam = it
            problems = it.problems

            if (mockExam.year < 2021 || mockExam.selectOptional.isEmpty()) {
                showOmrViewTopLabel(false)
                omrViewLeft.setAnswer(it.problems.filter { problem -> problem.problemType != ProblemType.short }, OMRViewType.LEFT)
                omrViewRight.setAnswer(it.problems.filter { problem -> problem.problemType == ProblemType.short }, OMRViewType.RIGHT)
            } else {
                showOmrViewTopLabel(true)
                omrViewLeft.setAnswer(it.problems.filter { problem -> problem.problemNum!! < 23 }, OMRViewType.LEFT)
                omrViewRight.setAnswer(it.problems.filter { problem -> problem.problemNum!! >= 23 }, OMRViewType.RIGHT)
            }
            omrViewLeft.setOMRViewListener(this)
            omrViewRight.setOMRViewListener(this)
        }
    }

    private fun initUI() {
        submitBtn.toDisableUI()
        setToolbar(toolbar)
        showOmrViewTopLabel(false)
        keypadView.visibility = View.INVISIBLE
        keypadView.setNumberKeypadListener(this)
        timerView.setTimerViewListener(this)

        tvTitle.text = mockExam.getMockTitle()

        if(isSPYMode) {
            spyBtn.visibility = View.VISIBLE
            spyBtn.setOnClickListener {
                onSpyBtnClikced()
            }
        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        submitBtn.setOnClickListener {
            onSubmitBtnClicked()
        }

        frontDimmedLl.setOnClickListener {
            hideKeypad()
        }

        dimOfLeftOmr.referencedIds.forEach { id ->
            findViewById<View>(id).setOnClickListener { hideKeypad() }
        }
    }

    private fun setTimer() {
        if (mockExam.time != null && !mockExam.isRestart) {
            val time = mockExam.time!!
            if(time >= 6000) {
                timerView.submitType = TimerView.SubmitType.lenient
                timerView.setLenientOvetimeUI()
            }
            timerView.elapsedTime = time
        }

        AnimationUtils.showTimer(this, 3, "시험 시작!", object:AnimationListener {
            override fun onAnimationEnd() {
                timerView.runTimer()
            }

            override fun onAnimationCancel() {
                timerView.runTimer()
            }
        })
    }

    private fun showOmrViewTopLabel(isShow: Boolean) {

        val visibility = if (isShow) View.VISIBLE else View.GONE
        lbCommonSubject.visibility = visibility
        dividerCommonSubject.visibility = visibility
        lbSelectedSubject.visibility = visibility
        dividerSelectedSubject.visibility = visibility
        (dividerSecondOmrLeft.layoutParams as? ConstraintLayout.LayoutParams)?.setMargins(0, if (isShow) 32.toPx() else 0, 0, 0)

        // 선택과목 없을 시 공통 레이블 숨기기
        val labelHide = if(mockExam.selectOptional.isEmpty()) View.GONE else View.VISIBLE

        lbCommonSubject.visibility = labelHide
        lbSelectedSubject.visibility = labelHide
        dividerCommonSubject.visibility = labelHide
        dividerSelectedSubject.visibility = labelHide
    }

    override fun onDestroy() {
        AppUsageMonitor.finishStudy(this)
        super.onDestroy()
        timerView.deinitTimer()
    }

    override fun onSupportNavigateUp(): Boolean {
        if (keypadView.visibility == View.VISIBLE) hideKeypad()
        this.onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (keypadView.visibility == View.VISIBLE) {
            hideKeypad()
            return
        }

        val dialog = DialogUtils.makeDialog(this,
                "종료하실 건가요?",
                "제출하지 않고 종료 시\n시험기록이 삭제되며,\n보고서를 볼 수 없습니다.",
                "취소",
                "종료하기")
        dialog.type = DialogType.alert
        dialog.rightBtn.setOnClickListener {
            dialog.dismiss()
            super.onBackPressed()
        }
        dialog.show()

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForeground() {
        AppUsageMonitor.startStudy(this)
//        VersionManager.requestVersionInfo(this) {
//            handleUser()
//        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackground() {
        AppUsageMonitor.finishStudy(this)
    }

    override fun onNumberBtnClicked(button: Button, text: String) {
        getKeypadFocusOmrView().addTextOnFocusedAnswer(text)
    }

    override fun onDeleteBtnClicked(button: ImageButton) {
        getKeypadFocusOmrView().deleteLastTextOnFocusedAnswer()
    }

    override fun onNextBtnClicked(button: Button) {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            getKeypadFocusOmrView().focusOnNext()
        }, 200)
    }

    override fun onFinishBtnClicked(button: Button) {
        hideKeypad()
    }

    private var currentFocusEditTextOMRViewType: OMRViewType = OMRViewType.RIGHT
        set(value) {
            if (value == field) return
            field = value
        }
    private fun getKeypadFocusOmrView() = if (currentFocusEditTextOMRViewType == OMRViewType.LEFT) omrViewLeft else omrViewRight

    override fun onShortAnswerFocusChanged(editText: EditText, omrViewType: OMRViewType, hasFocus: Boolean, isLast: Boolean) {
        keypadView.type = if (isLast) NumberKeypadView.Type.FINISH else NumberKeypadView.Type.NEXT
        keypadView.visibility = View.VISIBLE
        currentFocusEditTextOMRViewType = omrViewType

        if (hasFocus)
            showKeypad()

        if (editText.text.isNotEmpty() && editText.text.toString().toCharArray()[0] == '0') {
            editText.text.delete(0, 1)
        }
    }

    override fun onAnswerChanged(answers: ArrayList<Int?>) {
        val answeredSet1 = omrViewLeft.getAnswers().filterNotNull()
        val answeredSet2 = omrViewRight.getAnswers().filterNotNull()

        if (answeredSet1.isEmpty() && answeredSet2.isEmpty())
            submitBtn.toDisableUI()
        else
            submitBtn.toEnableUI()
    }

    private fun showKeypad() {
        val keypadMarginLayout = keypadView.layoutParams as ViewGroup.MarginLayoutParams
        keypadMarginLayout.rightMargin =
                if (currentFocusEditTextOMRViewType == OMRViewType.LEFT)
                    omrViewRight.width * 2 + 22.toPx()
                else
                    omrViewRight.width + 8
        keypadView.visibility = View.VISIBLE

        when(currentFocusEditTextOMRViewType) {
            OMRViewType.LEFT -> {
                dimOfLeftOmr.visibility = View.VISIBLE
            }
            OMRViewType.RIGHT -> {
                frontDimmedLl.visibility = View.VISIBLE
            }
        }
    }

    private fun hideKeypad() {
        omrViewLeft.clearFocus()
        omrViewRight.clearFocus()
        keypadView.visibility = View.GONE

        dimOfLeftOmr.visibility = View.GONE
        frontDimmedLl.visibility = View.GONE
    }

    fun onSubmitBtnClicked() {
        if(!submitBtn.isEnableUI())
            return

        val answerList = omrViewLeft.getAnswers().plus(omrViewRight.getAnswers())

        for (i in answerList.indices) {
            val answer = answerList[i]
            if(answer != null)
                problems[i].userAnswer = answer.toString()
            else
                problems[i].userAnswer = null
        }

        val notSolvedProblem = answerList.filter { it == null }

        val time = timerView.elapsedTime

        if(notSolvedProblem.isNotEmpty()) {
            DialogUtils.showExamSubmitDialog(this, notSolvedProblem.size) {
                ContentManager.score(this, user!!, mockExam, problems.filter { it.userAnswer != null }.toSet(), time) {
                    setResult(MockExamFragment.RESULT_MOCK_FINISH, intent)
                    finish()
                }
            }
        } else {
            val dialog = CompleteDialog(this, "수고하셨습니다!", "분석 보고서로 이동합니다.")
            dialog.setCancelable(false)
            dialog.showFor {
                ContentManager.score(this, user!!, mockExam, problems.toSet(), time) {
                    val intent = MockReportActivity.getIntent(this, mockExam, it)
                    startActivity(intent)
                    setResult(MockExamFragment.RESULT_MOCK_FINISH, intent)
                    finish()
                }
            }
        }
    }

    fun onSpyBtnClikced() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("SPY")
        builder.setItems(listOf(
                "컨닝",
                "3번으로 찍기",
                "타이머 세팅: 5분전",
                "타이머 세팅: 제출직전"
        ).toTypedArray()) { dialog, position ->
            when (position) {
                0 -> {
                    problems.forEach { it.userAnswer = it.answerData }
                    omrViewLeft.adapter?.notifyDataSetChanged()
                    omrViewLeft.adapter?.notifyDataSetChanged()
                }

                1 -> {
                    problems.forEach { it.userAnswer = "3" }
                    omrViewLeft.adapter?.notifyDataSetChanged()
                    omrViewRight.adapter?.notifyDataSetChanged()
                }

                2 -> {
                    timerView?.elapsedTime = 5695
                    timerView?.typeRg?.visibility = View.VISIBLE
                    timerView?.overTimerTv?.visibility = View.GONE
                }
                3 -> {
                    timerView?.elapsedTime = 5995
                    timerView?.typeRg?.visibility = View.VISIBLE
                    timerView?.overTimerTv?.visibility = View.GONE
                }
            }
        }

        builder.show()
    }
    

    override fun onSubmitTypeChanged(submitType: TimerView.SubmitType) {
        val itemName = if(submitType == TimerView.SubmitType.lenient) "시간제한없음" else "100분자동제출"
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "모의고사", itemName)
    }

    override fun onTimerSwitchChecked() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "모의고사", "타이머표시")
    }

    override fun onTimerStopClicked() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "모의고사", "일시정지")
    }

    override fun onTimerExpired(timerView: TimerView, type: TimerView.SubmitType) {
        if(type == TimerView.SubmitType.lenient)
            return

        timerView.stop()
        val notSolvedProblem = omrViewLeft.getAnswers().plus(omrViewRight.getAnswers()).filter { it == null }

        DialogUtils.showExamExpiredDialog(this, notSolvedProblem.size,
                onSolveClicked = {
                    if(timerView.isTimerShown())
                        timerView.showOverTimerView()
                    timerView.hideTypeRadio()
                    timerView.runTimer()
                },
                onSubmitClicked = {
                    val answerList = omrViewLeft.getAnswers().plus(omrViewRight.getAnswers())

                    for (i in answerList.indices) {
                        val answer = answerList[i]
                        if(answer != null)
                            problems[i].userAnswer = answer.toString()
                        else
                            problems[i].userAnswer = null
                    }

                    val time = timerView.elapsedTime

                    ContentManager.score(this, user!!, mockExam, problems.toSet(), time) {
                        val intent = MockReportActivity.getIntent(this, mockExam, it)
                        startActivity(intent)
                        setResult(MockExamFragment.RESULT_MOCK_FINISH, intent)
                        finish()

                    }
                }
        )
    }

    override fun monitoringTick() {
        var sec = AppUsageMonitor.accumulatedStudyTime
        val min = sec / 60
        sec = sec % 60

        runOnUiThread {
            spyBtn.text =  String.format("%02d", min) + ":" + String.format("%02d", sec)
        }


//        Log.d("MONITOR", "[SOLVE] TICK - ${AppUsageMonitor.accumulatedStudyTime}")
    }
}

