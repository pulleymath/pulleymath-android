package com.freewheelin.pulley.legacy.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.solve.SolveActivity
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.manage.TestManager
import com.freewheelin.pulley.databinding.ActivityTestReportDailyBinding
import com.freewheelin.pulley.databinding.ItemTestReportDailyScoringBinding
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.model.Result
import com.freewheelin.pulley.legacy.model.contents.Test
import com.freewheelin.pulley.legacy.model.curation.TestCuration
import com.freewheelin.pulley.legacy.utils.*
import com.google.android.material.tabs.TabLayout

class DailyTestReportActivity : AppCompatActivity() {
    private val binding: ActivityTestReportDailyBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_test_report_daily,null,false)
    }

    var tests: List<Test>? = null
    val test: Test?
        get() = tests?.getOrNull(binding.tabLayout.selectedTabPosition)
    var isFromSolve = false

    val curation: TestCuration
        get() = TestCuration(this)

    companion object {
        fun getIntent(context: Context, test: Test, isFromSolve: Boolean = false): Intent {
            val intent = Intent(context, DailyTestReportActivity::class.java)
            intent.putExtra(TestManager.ARG_TEST, test)
            intent.putExtra("FROM_SOLVE", isFromSolve)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
//        val test = getSerializable(this@DailyTestReportActivity, TestManager.ARG_TEST, Test::class.java)
        val test = intent.getSerializableExtra(TestManager.ARG_TEST) as Test
        isFromSolve = intent.getBooleanExtra("FROM_SOLVE", false)
        initUI(test.scoringTestPieceCount - 1)

        TestManager.getDailyTestReport(this, user!!) {
            tests = it
            if(it.isNotEmpty()) configureUI(it.last()) // 목록이 없는 경우가 있음
        }
    }

    private fun configureUI(test: Test?) {
        with(binding) {
            if(test == null) {
                questionCl.visibility = View.VISIBLE
                questionTv.text = "앗! 아직 ${tabLayout.selectedTabPosition + 1}회차 테스트를 하지 않았네요 :)"
            } else {
                questionCl.visibility = View.INVISIBLE
                scoreTv.text = "${test.score}"
                scoreGuideTv.text = curation.getDailyReportGuideQ(test.scoreLastTime, test.scoreBeforeLastTime, test.problems)
                scoringRv.adapter = ScoringAdapter()
                scoringRv.layoutManager = LinearLayoutManager(this@DailyTestReportActivity)
                sameCorrectRateLabel.text = "${test.studentRating}등급 정답률"

                sameCorrectRateLabel.visibleIf(schoolType.isHigh)
                sameCorrectRateBorder.visibleIf(schoolType.isHigh)

                scoreTv.show()
                scoreLabel.show()
                scoreGuideTv.show()
                reviewBtn.show()
            }
        }
    }

    private fun initUI(initialIndex: Int) {
        with(binding) {
            val dp24 = resources.getDimension(R.dimen.dp24)
            xBtn.extensionTouchArea(dp24.toInt())
            xBtn.setOnClickListener {
                finish()
            }

            val tabs = listOf(
                tabLayout.newTab().setText("1회차"),
                tabLayout.newTab().setText("2회차"),
                tabLayout.newTab().setText("3회차")
            )

            tabLayout.addTab(tabs[0])
            tabLayout.addTab(tabs[1])
            tabLayout.addTab(tabs[2])
            tabs[initialIndex].select()
            tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
                override fun onTabReselected(p0: TabLayout.Tab?) {}

                override fun onTabUnselected(p0: TabLayout.Tab?) {}

                override fun onTabSelected(p0: TabLayout.Tab?) {
                    val eventValue = "결과-${tabLayout.selectedTabPosition + 1}회"
                    LogUtils.logEvent(this@DailyTestReportActivity, user, PulleyEvent.BUTTON_CLICK, "테스트", eventValue)
                    Handler(Looper.getMainLooper()).postDelayed({
                        configureUI(test)
                    }, 250)

                }
            })

            reviewBtn.setOnClickListener { onReviewBtnClikced() }
            scoreTv.visibility = View.INVISIBLE
            scoreLabel.visibility = View.INVISIBLE
            reviewBtn.visibility = View.INVISIBLE
        }
    }

    inner class ScoringAdapter: RecyclerView.Adapter<com.freewheelin.pulley.legacy.activities.DailyScoringHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): com.freewheelin.pulley.legacy.activities.DailyScoringHolder {
//            val view = LayoutInflater.from(this@DailyTestReportActivity).inflate(R.layout.item_test_report_daily_scoring, parent, false)
            val itemBinding: ItemTestReportDailyScoringBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_test_report_daily_scoring, parent, false)
            return com.freewheelin.pulley.legacy.activities.DailyScoringHolder(itemBinding)
        }

        override fun getItemCount(): Int {
            if (test != null) {
                return test!!.problems.size
            } else
                return 0
        }

        override fun onBindViewHolder(holder: com.freewheelin.pulley.legacy.activities.DailyScoringHolder, position: Int) {
            val problem = test?.problems?.getOrNull(position)
            if(problem != null)
                holder.set(problem)
        }
    }

    fun onReviewBtnClikced() {
        if(test != null) {
            if(isFromSolve) {
                finish()
            } else {
                LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "테스트", "리뷰하기", test!!.getTestType().eventItemValue)
                val intent = SolveActivity.getReviewIntent(this, test!!)
                startActivity(intent)
            }
        }
    }
}

class DailyScoringHolder(val itemBinding: ItemTestReportDailyScoringBinding): RecyclerView.ViewHolder(itemBinding.root) {
    val numberTv = itemBinding.numberTv
    val resultIv = itemBinding.resultIv
    val totalCorrectRateTv = itemBinding.totalCorrectRateTv
    val sameCorrectRateTv = itemBinding.sameCorrectRateTv
    val sameCorrectRateBorder = itemBinding.sameCorrectRateBorder
    val subjectTv = itemBinding.subjectTv
//    val tagLl = view.tagLl

    fun set(problem: Problem) {
        numberTv.text = problem.problemNum.toString()
        if(problem.getResultByScoring() == Result.correct)
            resultIv.setImageResource(R.drawable.ic_result_correct)
        else
            resultIv.setImageResource(R.drawable.ic_result_incorrect)

        val correctRate = problem.correctRate
        if(correctRate == null)
            totalCorrectRateTv.text = "-"
        else
            totalCorrectRateTv.text = TextUtils.percentFormat.format(correctRate)

        if(problem.standardCorrectRate != null) {
            sameCorrectRateTv.text = TextUtils.percentFormat.format(problem.standardCorrectRate!! * 0.01f)
        }

        sameCorrectRateTv.visibleIf(schoolType.isHigh)
        sameCorrectRateBorder.visibleIf(schoolType.isHigh)
        subjectTv.text = problem.subject
//        tagLl.removeAllViewsInLayout()
//
//        problem.tag.forEach {
//            val tagLabel = TagTextView(view.context, it, view.context.resources.getDimension(R.dimen.sp14))
//            tagLl.addView(tagLabel)
//            (tagLabel.layoutParams as? ViewGroup.MarginLayoutParams)?.marginStart = 8.toPx()
//        }
    }
}