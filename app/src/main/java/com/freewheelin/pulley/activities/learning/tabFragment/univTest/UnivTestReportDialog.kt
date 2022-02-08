package com.freewheelin.pulley.activities.learning.tabFragment.univTest

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.PieceManager
import com.freewheelin.pulley.databinding.DialogUnivTestReportDialogBinding
import com.freewheelin.pulley.revision2021.activity.AffiliatedTestSolveActivity
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestProblem
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestWorkbook
import com.freewheelin.pulley.revision2021.repository.AffiliatedTestRepository
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_book.*
import kotlinx.android.synthetic.main.tooltip_analysis.view.*
import java.util.concurrent.TimeUnit

class UnivTestReportDialog(context: Context, workbookId: Int, version: Int, workbook: AffiliatedTestWorkbook): Dialog(context) {

    private val TAG = this.javaClass.name

    data class ProblemInfo(val number:Int, val subject :String, val singleScore: Int, val result: Int)
    private var problemInfoList = listOf<ProblemInfo>()

    private val binding: DialogUnivTestReportDialogBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_univ_test_report_dialog, null, false)
    }

    lateinit var affiliatedTestRepository: AffiliatedTestRepository
    lateinit var workbook: AffiliatedTestWorkbook
    lateinit var subject: String
    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        affiliatedTestRepository = AffiliatedTestRepository()
        setContentView(binding.root)
        this.workbook = workbook
        initData(workbookId, version)
        initUI()
    }

    @SuppressLint("CheckResult")
    private fun initData(workbookId: Int, version: Int) {
        val studentId = user?.studentID ?: return
        affiliatedTestRepository.fetchScoringResult(studentId, workbookId, version)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ res ->
                Log.d(javaClass.simpleName, "fetchScoringResult list=>${res.data}")
                res.data?.let {
                    val studentWorkbook = it.student_workbook
                    val answerList = it.answer_list
                    val problemList = it.problem_list
                    setSubjectView(problemList)

                    val workbookScore = studentWorkbook.score ?: return@subscribe

                    val problemHeader = ProblemInfo(-1, "과목", -1, -1)

                    val problemDataList = answerList.map { that ->
                        val problem = problemList.filter { problem -> problem.no == that.problem_no }[0]
                        ProblemInfo(problem.no, problem.intention, problem.point, that.result)
                    }
                    problemInfoList = listOf(problemHeader) + problemDataList
                    binding.root.post {

                        binding.apply {
                            scoreTv.text = "${workbookScore}점"
                            releaseStepStick(this)
                            val problem = problemList[0] ?: return@apply
                            setStepStick(this, workbookScore, problem)

                        }
                        setStudentListView()
                    }
                }


            }, { error ->
                Log.e(javaClass.simpleName, "fetchScoringResult error=${error.localizedMessage}")
            })
    }
    private fun setSubjectView(problemList: List<AffiliatedTestProblem>) {
        subject = problemList[0].subject.toString()
        Handler(Looper.getMainLooper()).post {
            binding.apply {
                when (subject) {
                    "확률과 통계", "미적분" -> {
                        label2Tv.visibility = View.VISIBLE
                        supportLearnCl.visibility = View.VISIBLE
                    }
                    else -> {
                        label2Tv.visibility = View.GONE
                        supportLearnCl.visibility = View.GONE
                    }
                }
            }
        }
    }
    private fun releaseStepStick(view: DialogUnivTestReportDialogBinding) {
        view.apply {
            stepStick1.setBackgroundResource(R.color.gray_300)
            stepStick2.setBackgroundResource(R.color.gray_300)
            stepStick3.setBackgroundResource(R.color.gray_300)
        }
    }

    private fun setStepStick(view: DialogUnivTestReportDialogBinding, score: Int, problem: AffiliatedTestProblem) {
        view.apply {
            val cs = ConstraintSet()
            cs.clone(stepScoreCl)

            val userStep = problem.getStepOnScore(score)
            when (userStep) {
                1 -> {
                    stepStick1.setBackgroundResource(R.color.purple_300)
                    cs.setHorizontalBias(R.id.stepPointIv, 0.15f)
                    cs.setHorizontalBias(R.id.scoreTv, 0.12f)
                    cs.applyTo(stepScoreCl)
                }
                2 -> {
                    stepStick2.setBackgroundResource(R.color.purple_300)
                    cs.setHorizontalBias(R.id.stepPointIv, 0.5f)
                    cs.setHorizontalBias(R.id.scoreTv, 0.5f)
                    cs.applyTo(stepScoreCl)
                }
                3 -> {
                    stepStick3.setBackgroundResource(R.color.purple_300)
                    cs.setHorizontalBias(R.id.stepPointIv, 0.85f)
                    cs.setHorizontalBias(R.id.scoreTv, 0.92f)
                    cs.applyTo(stepScoreCl)
                }
                else -> {
                    stepStick2.setBackgroundResource(R.color.purple_300)
                    cs.setHorizontalBias(R.id.stepPointIv, 0.5f)
                    cs.setHorizontalBias(R.id.scoreTv, 0.5f)
                    cs.applyTo(stepScoreCl)
                }
            }

        }
    }

    private fun initUI() {
        setCancelable(true)
//        setStudentListView()
        binding.apply {
            val toolTipOutViewList = listOf<View>(dialogContainer, dialogHeader, reportScrollView, baseCl, resultCl, supportLearnCl)
            toolTipOutViewList.forEach {
                it.setOnClickListener {
                    tooltipView.visibility = View.GONE
                }
            }

            btnClose.setOnClickListener {
                close()
            }

            scoreStandardTv.setOnClickListener {
                tooltipView.visibility = when (tooltipView.visibility) {
                    View.VISIBLE -> View.GONE
                    else -> View.VISIBLE
                }
            }
            tooltipIv.setOnClickListener {
                tooltipView.visibility = when (tooltipView.visibility) {
                    View.VISIBLE -> View.GONE
                    else -> View.VISIBLE
                }
            }

            reviewStartBtn.setOnClickListener {
                val intent = AffiliatedTestSolveActivity.getReviewIntent(context, workbook)
                context.startActivity(intent)
            }
            addtionalLearningBtn.setOnClickListener {
                val intent = Intent(PieceManager.EVENT_MOVE_TAB)
                intent.putExtra(PieceManager.EVENT_MOVE_TAB_INDEX, 3)
                intent.putExtra(PieceManager.EVENT_SCROLL, true)
                intent.putExtra(PieceManager.EVENT_SCROLL_UNIT_TOTAL_LABEL, true)
                intent.putExtra(PieceManager.EVENT_FILTER, subject)
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                close()
            }
        }
    }

    private fun close() {
        dismiss()
    }

    private fun setStudentListView() {
        val adapter1 = UnivTestReportAdapter(problemInfoList, binding.problemRv)
        adapter1.notifyDataSetChanged()
    }
}

class UnivTestReportAdapter(val problems: List<UnivTestReportDialog.ProblemInfo>, val parent: LinearLayout) {
    fun notifyDataSetChanged() {
        parent.removeAllViews()
        for((idx, problem) in problems.withIndex()) {
            val view = getView(problem, idx, problems.size - 1)
            parent.addView(view)
        }
    }

    private fun getView(problem: UnivTestReportDialog.ProblemInfo, idx:Int, endIdx: Int): View {
        val view = when (idx) {
            0 -> LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_univ_report_problem_info_top, parent, false)
            endIdx -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_univ_report_problem_info_bottom, parent, false)
            }
            else -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_univ_report_problem_info_middle, parent, false)
            }
        }
        set(view, problem, idx)

        return view
    }

    fun set(view:View, info: UnivTestReportDialog.ProblemInfo, idx: Int) {
        val problemNumTv = view.findViewById<TextView>(R.id.problemNumTv)
        val subjectTv = view.findViewById<TextView>(R.id.subjectTv)
        val scoreTv = view.findViewById<TextView>(R.id.scoreTv)

        if (idx == 0) {
            val resultTv = view.findViewById<TextView>(R.id.reportResultTv)
            problemNumTv.text = "번호"
            subjectTv.text = "과목"
            scoreTv.text = "배점"
            resultTv.text = "결과"
        } else {
            val reportResultIv = view.findViewById<ImageView>(R.id.reportResultIv)
            problemNumTv.text = "${info.number}"
            subjectTv.text = info.subject
            scoreTv.text = "${info.singleScore}"

            if (info.result == 1) {
                reportResultIv.setBackgroundResource(R.drawable.ic_mock_report_result_o)
            } else {
                reportResultIv.setBackgroundResource(R.drawable.ic_mock_report_result_x)
            }
        }
    }
}

