package com.freewheelin.pulley.legacy.activities.learning.tabFragment.assessment

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
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.revision2023.ui.fragment.AssessmentFragment.Companion.SHOW_ADDITIONAL_LEARNING
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.manage.PieceManager
import com.freewheelin.pulley.databinding.DialogAssessmentReportDialogBinding
import com.freewheelin.pulley.legacy.assets.SubjectV3
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.revision2021.activity.AssessmentSolveActivity
import com.freewheelin.pulley.revision2021.model.response.AssessmentProblem
import com.freewheelin.pulley.revision2021.model.response.AssessmentWorkbook
import com.freewheelin.pulley.revision2021.repository.AssessmentRepository
import com.freewheelin.pulley.revision2023.model.AssessmentDesignSkin
import com.freewheelin.pulley.revision2023.ui.view.CommonButton
import com.freewheelin.pulley.revision2023.ui.view.MainTab
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class AssessmentReportDialog(context: Context, workbookId: Int, version: Int, skin: AssessmentDesignSkin, workbook: AssessmentWorkbook?): Dialog(context) {

    private val TAG = this.javaClass.name

    data class ProblemInfo(val number:Int, val subject :String, val singleScore: Int, val result: Int)
    private var problemInfoList = listOf<ProblemInfo>()

    private val binding: DialogAssessmentReportDialogBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_assessment_report_dialog, null, false)
    }

    lateinit var assessmentRepository: AssessmentRepository
    var workbook: AssessmentWorkbook?
    var skin: AssessmentDesignSkin
    lateinit var subject: String
    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        assessmentRepository = AssessmentRepository.instance
        setContentView(binding.root)
        this.workbook = workbook
        this.skin = skin
        initData(workbookId, version)
        initUI()
    }

    @SuppressLint("CheckResult")
    private fun initData(workbookId: Int, version: Int) {
        val studentId = user?.studentID ?: return
        assessmentRepository.fetchScoringResult(studentId, workbookId, version)
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
                        ProblemInfo(problem.no, problem.unit, problem.point, that.result)
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

    private fun setStudentListView(score: Int, problem: AssessmentProblem) {
        val adapter = ProblemInfoListAdapter(score, problem)
        binding.reportRv.adapter = adapter
        binding.reportRv.layoutManager = LinearLayoutManager(context)
        adapter.notifyDataSetChanged()

    }

    inner class ProblemInfoListAdapter(score: Int, problem: AssessmentProblem): RecyclerView.Adapter<RecyclerView.ViewHolder> () {
        private val typeHeader = 0
        private val typeTop = 1
        private val typeItem = 2
        private val typeBottom = 3
        private val score: Int = score
        private val problem: AssessmentProblem = problem
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                typeHeader -> {
                    ProblemInfoHeaderHolder(LayoutInflater.from(context).inflate(R.layout.item_assessment_test_report_problem_info_header, parent, false))
                }
                typeTop -> {
                    ProblemInfoTopHolder(LayoutInflater.from(context).inflate(R.layout.item_assessment_test_report_problem_info_top, parent, false))
                }
                typeItem -> {
                    ProblemInfoItemHolder(LayoutInflater.from(context).inflate(R.layout.item_assessment_test_report_problem_info_middle, parent, false))
                }
                else -> {
                    ProblemInfoItemHolder(LayoutInflater.from(context).inflate(R.layout.item_assessment_test_report_problem_info_bottom, parent, false))
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

        fun set(score: Int, problem: AssessmentProblem) {
            scoreTv.text = "${score}점"
            releaseStepStick()
//            val skin = AssessmentDesignSkin.convertSkinBySchoolIdOfNonNull(user?.schoolID)
            val userStep = problem.getStepOnScore(score, skin)
            setStepStick(userStep)
            setResultBodyTv(userStep, skin)
            setSubjectView(problem)

            val toolTipOutViewList = listOf<View>(baseCl, resultCl, supportLearnCl)
            toolTipOutViewList.forEach {
                it.setOnClickListener {
                    tooltipView.visibility = View.GONE
                }
            }
            scoreStandardTv.setOnClickListener {
                val value = tooltipView.visibility != View.VISIBLE
                tooltipView.findViewById<ConstraintLayout>(skin.tooltipReportClId).visibleIf(value)
                tooltipView.visibleIf(value)
            }

            tooltipIv.setOnClickListener {
                val value = tooltipView.visibility != View.VISIBLE
                tooltipView.findViewById<ConstraintLayout>(skin.tooltipReportClId).visibleIf(value)
                tooltipView.visibleIf(value)
            }

            reviewStartBtn.setOnClickListener {
                val intent = AssessmentSolveActivity.getReviewIntent(context, workbook)
                context.startActivity(intent)
            }
            additionalLearningBtn.setOnClickListener {

                if (SubjectV3.isMathticSubject(subject)) {
                    val intent = Intent(PieceManager.EVENT_MOVE_TAB).apply {
                        putExtra(PieceManager.EVENT_MOVE_TAB_INDEX, MainTab.문제풀이.indexOnTablet)
                        val schoolTypeAsSubject = SubjectV3.nameToSchoolType(subject)
                        putExtra(PieceManager.EVENT_ADDITIONAL_ACTION, schoolTypeAsSubject.name)
                    }

//                    intent.putExtra(PieceManager.EVENT_ADDITIONAL_ACTION, "WRONG_NOTE")
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    close()
                } else {
                    val intent = Intent(SHOW_ADDITIONAL_LEARNING)
                    intent.putExtra("subject", subject)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    close()
                }
            }

//            val messageByUniv = "진단결과에 맞는 보완학습을 진행해보세요."
//            supportLearnTitleTv.text = messageByUniv
        }

        private fun releaseStepStick() {
            stepStick1.setBackgroundResource(R.color.gray_300)
            stepStick2.setBackgroundResource(R.color.gray_300)
            stepStick3.setBackgroundResource(R.color.gray_300)
        }
        private fun setResultBodyTv(userStep: Int, skin: AssessmentDesignSkin) {
            resultBodyTv.text = skin.reportResultBodyText(userStep)
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

        @SuppressLint("CheckResult")
        private fun fetchAdditionalLearningCount(subject: String?, cb: (Boolean) -> Unit) {
            val studentId = user?.studentID ?: return
            val schoolId = user?.schoolID ?: return
            subject ?: return

            assessmentRepository.fetchAdditionalLearningBySchool(studentId, schoolId, subject)
                .subscribeOn(Schedulers.io())
                .timeout(3, TimeUnit.SECONDS)
                .subscribe({ res ->
                    Log.d(javaClass.simpleName, "fetchAdditionalLearningCount res=>${res}")
                    cb(res.data > 0)
                }, { error ->
                    Log.e(javaClass.simpleName, "fetchAdditionalLearningCount error=${error.localizedMessage}")
                })
        }
        private fun setSubjectView(problem: AssessmentProblem) {
            subject = problem.subject.toString()
            if (SubjectV3.isMathticSubject(subject)) {
                binding.apply {
                    label2Tv.visibleIf(true)
                    supportLearnCl.visibleIf(true)
                }
            } else {
                fetchAdditionalLearningCount(problem.subject) { isVisible ->
                    Handler(Looper.getMainLooper()).post {
                        binding.apply {
                            label2Tv.visibleIf(isVisible)
                            supportLearnCl.visibleIf(isVisible)
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
            subjectTv.text = "단원"
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
