package com.freewheelin.pulley.legacy.activities.learning.tabFragment.affiliatedTest

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
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.revision2023.ui.fragment.AffiliatedTestFragment.Companion.SHOW_ADDITIONAL_LEARNING
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.manage.PieceManager
import com.freewheelin.pulley.databinding.DialogAffiliatedTestReportDialogBinding
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.revision2021.activity.AffiliatedTestSolveActivity
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestProblem
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestWorkbook
import com.freewheelin.pulley.revision2021.repository.AffiliatedTestRepository
import com.freewheelin.pulley.revision2023.model.AffiliatedUniv
import com.freewheelin.pulley.revision2023.ui.view.CommonButton
import com.freewheelin.pulley.revision2023.ui.view.MainTab
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class AffiliatedTestReportDialog(context: Context, workbookId: Int, version: Int, workbook: AffiliatedTestWorkbook?): Dialog(context) {

    private val TAG = this.javaClass.name

    data class ProblemInfo(val number:Int, val subject :String, val singleScore: Int, val result: Int)
    private var problemInfoList = listOf<ProblemInfo>()

    private val binding: DialogAffiliatedTestReportDialogBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_affiliated_test_report_dialog, null, false)
    }

    lateinit var affiliatedTestRepository: AffiliatedTestRepository
    var workbook: AffiliatedTestWorkbook?
    lateinit var subject: String
    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        affiliatedTestRepository = AffiliatedTestRepository.instance
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

                    val workbookScore = studentWorkbook.score ?: return@subscribe


                    val problemDataList = answerList.map { that ->
                        val problem = problemList.filter { problem -> problem.no == that.problem_no }[0]
                        ProblemInfo(problem.no, problem.intention, problem.point, that.result)
                    }

                    val problemHeader = ProblemInfo(-1, "과목", -1, -1)
                    val problemTop = ProblemInfo(-1, "과목", -1, -1)
                    problemInfoList = listOf(problemHeader, problemTop) + problemDataList
                    binding.root.post {
                        val problem = problemList[0]
                        setStudentListView(workbookScore, problem)
                    }
                }

            }, { error ->
                Log.e(javaClass.simpleName, "fetchScoringResult error=${error.localizedMessage}")
            })
    }


    private fun initUI() {
        setCancelable(true)
        binding.apply {

            btnClose.setOnClickListener {
                close()
            }
        }
    }

    private fun close() {
        dismiss()
    }

    private fun setStudentListView(score: Int, problem: AffiliatedTestProblem) {
        val adapter = ProblemInfoListAdapter(score, problem)
        binding.reportRv.adapter = adapter
        binding.reportRv.layoutManager = LinearLayoutManager(context)
        adapter.notifyDataSetChanged()

    }

    inner class ProblemInfoListAdapter(score: Int, problem: AffiliatedTestProblem): RecyclerView.Adapter<RecyclerView.ViewHolder> () {
        private val typeHeader = 0
        private val typeTop = 1
        private val typeItem = 2
        private val typeBottom = 3
        private val score: Int = score
        private val problem: AffiliatedTestProblem = problem
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                typeHeader -> {
                    ProblemInfoHeaderHolder(LayoutInflater.from(context).inflate(R.layout.item_affiliated_test_report_problem_info_header, parent, false))
                }
                typeTop -> {
                    ProblemInfoTopHolder(LayoutInflater.from(context).inflate(R.layout.item_affiliated_test_report_problem_info_top, parent, false))
                }
                typeItem -> {
                    ProblemInfoItemHolder(LayoutInflater.from(context).inflate(R.layout.item_affiliated_test_report_problem_info_middle, parent, false))
                }
                else -> {
                    ProblemInfoItemHolder(LayoutInflater.from(context).inflate(R.layout.item_affiliated_test_report_problem_info_bottom, parent, false))
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (position) {
                0 -> (holder as ProblemInfoHeaderHolder).set(score, problem)
                1 -> (holder as ProblemInfoTopHolder).set()
                else -> (holder as ProblemInfoItemHolder).set(problemInfoList[position])
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when(position) {
                0 -> typeHeader
                1 -> typeTop
                (problemInfoList.size - 1) -> typeBottom
                else -> typeItem
            }
        }

        override fun getItemCount(): Int {
            return problemInfoList.size
        }
    }

    inner class ProblemInfoHeaderHolder(val view: View): RecyclerView.ViewHolder(view) {
        var baseCl = view.findViewById<ConstraintLayout>(R.id.baseCl)
        var resultCl = view.findViewById<ConstraintLayout>(R.id.resultCl)
        var stepScoreCl = view.findViewById<ConstraintLayout>(R.id.stepScoreCl)
        var supportLearnCl = view.findViewById<ConstraintLayout>(R.id.supportLearnCl)
        var resultTv = view.findViewById<TextView>(R.id.resultTv)
        var scoreTv = view.findViewById<TextView>(R.id.scoreTv)
        var resultBodyTv = view.findViewById<TextView>(R.id.resultBodyTv)
        var scoreStandardTv = view.findViewById<TextView>(R.id.scoreStandardTv)
        var tooltipIv = view.findViewById<ImageView>(R.id.tooltipIv)
        var supportLearnIv = view.findViewById<ImageView>(R.id.supportLearnIv)
        var step1Tv = view.findViewById<TextView>(R.id.step1Tv)
        var step2Tv = view.findViewById<TextView>(R.id.step2Tv)
        var step3Tv = view.findViewById<TextView>(R.id.step3Tv)
        var stepPointIv = view.findViewById<ImageView>(R.id.stepPointIv)
        var stepStick1 = view.findViewById<View>(R.id.stepStick1)
        var stepStick2 = view.findViewById<View>(R.id.stepStick2)
        var stepStick3 = view.findViewById<View>(R.id.stepStick3)
        var tooltipView = view.findViewById<View>(R.id.tooltipView)
        var reviewStartBtn = view.findViewById<CommonButton>(R.id.reviewStartBtn)
        var additionalLearningBtn = view.findViewById<CommonButton>(R.id.additionalLearningBtn)
        var label2Tv = view.findViewById<TextView>(R.id.label2Tv)
        var label3Tv = view.findViewById<TextView>(R.id.label3Tv)
        var supportLearnTitleTv = view.findViewById<TextView>(R.id.supportLearnTitleTv)
        var supportLearnContentTv = view.findViewById<TextView>(R.id.supportLearnContentTv)

        fun set(score: Int, problem: AffiliatedTestProblem) {
            scoreTv.text = "${score}점"
            releaseStepStick()
            val univ = AffiliatedUniv.schoolIdOfNonNull(user?.schoolID)
            val userStep = problem.getStepOnScore(score, univ)
            setStepStick(userStep)
            setResultBodyTv(userStep, univ)
            setSubjectView(problem)

            val toolTipOutViewList = listOf<View>(baseCl, resultCl, supportLearnCl)
            toolTipOutViewList.forEach {
                it.setOnClickListener {
                    tooltipView.visibility = View.GONE
                }
            }
            scoreStandardTv.setOnClickListener {
                val value = tooltipView.visibility != View.VISIBLE
                tooltipView.findViewById<ConstraintLayout>(univ.tooltipReportClId).visibleIf(value)
                tooltipView.visibleIf(value)
            }

            tooltipIv.setOnClickListener {
                val value = tooltipView.visibility != View.VISIBLE
                tooltipView.findViewById<ConstraintLayout>(univ.tooltipReportClId).visibleIf(value)
                tooltipView.visibleIf(value)
            }

            reviewStartBtn.setOnClickListener {
                val intent = AffiliatedTestSolveActivity.getReviewIntent(context, workbook)
                context.startActivity(intent)
            }
            additionalLearningBtn.setOnClickListener {
                if (subject == "물리학") {
                    val intent = Intent(SHOW_ADDITIONAL_LEARNING)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    close()
                } else {
                    val intent = Intent(PieceManager.EVENT_MOVE_TAB)
                    intent.putExtra(PieceManager.EVENT_MOVE_TAB_INDEX, MainTab.문제풀이.indexOnTablet)
                    intent.putExtra(PieceManager.EVENT_ADDITIONAL_ACTION, "WRONG_NOTE")
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    close()
                }
            }

            val messageByUniv = "${univ.inShortTermEnglish}진단 결과에 맞는 보완학습을 진행해보세요."
            supportLearnTitleTv.text = messageByUniv
        }

        private fun releaseStepStick() {
            stepStick1.setBackgroundResource(R.color.gray_300)
            stepStick2.setBackgroundResource(R.color.gray_300)
            stepStick3.setBackgroundResource(R.color.gray_300)
        }
        private fun setResultBodyTv(userStep: Int, univ: AffiliatedUniv) {
            resultBodyTv.text = univ.reportResultBodyText(userStep)
        }

        private fun setStepStick(userStep: Int) {
            val cs = ConstraintSet()
            cs.clone(stepScoreCl)

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

        private fun setSubjectView(problem: AffiliatedTestProblem) {
            subject = problem.subject.toString()
            Handler(Looper.getMainLooper()).post {
                binding.apply {
                    if (AffiliatedUniv.schoolIdOfNonNull(user?.schoolID).isSoongsil) {
                        label2Tv.visibility = View.VISIBLE
                        supportLearnCl.visibility = View.VISIBLE
                    } else {
                        when (subject) {
                            "확률과 통계", "미적분", "물리학" -> {
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
        }
    }

    inner class ProblemInfoTopHolder(val view: View): RecyclerView.ViewHolder(view) {
        var problemNumTv = view.findViewById<TextView>(R.id.problemNumTv)
        var subjectTv = view.findViewById<TextView>(R.id.subjectTv)
        var scoreTv = view.findViewById<TextView>(R.id.scoreTv)
        var resultTv = view.findViewById<TextView>(R.id.reportResultTv)

        fun set() {
            problemNumTv.text = "번호"
            subjectTv.text = "문제 유형"
            scoreTv.text = "배점"
            resultTv.text = "결과"
        }
    }

    inner class ProblemInfoItemHolder(val view: View): RecyclerView.ViewHolder(view) {
        var problemNumTv = view.findViewById<TextView>(R.id.problemNumTv)
        var subjectTv = view.findViewById<TextView>(R.id.subjectTv)
        var scoreTv = view.findViewById<TextView>(R.id.scoreTv)
        var resultIv = view.findViewById<ImageView>(R.id.reportResultIv)

        fun set(info: ProblemInfo) {
            problemNumTv.text = "${info.number}"
            subjectTv.text = info.subject
            scoreTv.text = "${info.singleScore}"
            if (info.result == 1) {
                resultIv.setBackgroundResource(R.drawable.ic_mock_report_result_o)
            } else {
                resultIv.setBackgroundResource(R.drawable.ic_mock_report_result_x)
            }
        }
    }

}

class UnivTestReportAdapter(val problems: List<AffiliatedTestReportDialog.ProblemInfo>, val parent: LinearLayout) {
    fun notifyDataSetChanged() {
        parent.removeAllViews()
        for((idx, problem) in problems.withIndex()) {
            val view = getView(problem, idx, problems.size - 1)
            parent.addView(view)
        }
    }

    private fun getView(problem: AffiliatedTestReportDialog.ProblemInfo, idx:Int, endIdx: Int): View {
        val view = when (idx) {
            0 -> LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_affiliated_test_report_problem_info_top, parent, false)
            endIdx -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_affiliated_test_report_problem_info_bottom, parent, false)
            }
            else -> {
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_affiliated_test_report_problem_info_middle, parent, false)
            }
        }
        set(view, problem, idx)

        return view
    }

    fun set(view:View, info: AffiliatedTestReportDialog.ProblemInfo, idx: Int) {
        val problemNumTv = view.findViewById<TextView>(R.id.problemNumTv)
        val subjectTv = view.findViewById<TextView>(R.id.subjectTv)
        val scoreTv = view.findViewById<TextView>(R.id.scoreTv)

        if (idx == 0) {
            val resultTv = view.findViewById<TextView>(R.id.reportResultTv)
            problemNumTv.text = "번호"
            subjectTv.text = "문제 유형"
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

