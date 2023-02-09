package com.freewheelin.pulley.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.core.manage.ProblemManager
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.databinding.ActivityTestReportWrongBinding
import com.freewheelin.pulley.databinding.ItemTestReportScoringWrongBinding
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.Result
import com.freewheelin.pulley.model.contents.Test
import com.freewheelin.pulley.utils.*

class WrongTestReportActivity : AppCompatActivity() {
    private val binding: ActivityTestReportWrongBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_test_report_wrong,null,false)
    }
    lateinit var test: Test
    var isFromSolve = false

    companion object {
        fun getIntent(context: Context, test: Test, isFromSolve: Boolean = false): Intent {
            val intent = Intent(context, WrongTestReportActivity::class.java)
            intent.putExtra(TestManager.ARG_TEST, test)
            intent.putExtra("FROM_SOLVE", isFromSolve)
            return intent
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initUI()
//        test = getSerializable(this@WrongTestReportActivity, TestManager.ARG_TEST, Test::class.java)
        test = intent.getSerializableExtra(TestManager.ARG_TEST) as Test
        isFromSolve = intent.getBooleanExtra("FROM_SOLVE", false)
    }

    override fun onResume() {
        super.onResume()
        syncTest(test)
    }

    fun syncTest(test: Test) {
        TestManager.getTestReport(this, user!!, test) {
            this.test = it
            with(binding) {
                scoreTv.text = "${it.score}"
                leftScoringRv.adapter = ScoringAdapter(this@WrongTestReportActivity,  true)
                rightScoringRv.adapter = ScoringAdapter(this@WrongTestReportActivity, false)

                leftCorrectRateTv.text = "${it.studentRating}등급 정답률"
                rightCorrectRateTv.text = "${it.studentRating}등급 정답률"

                scoreResultLabel.show()
                scoreResultContainerCl.show()
                leftScoringRv.show()
                rightScoringRv.show()

                configureCheckbox(it)
            }

        }
    }

    private fun initUI() {
        hideViews()
        with(binding) {
            leftScoringRv.layoutParams.height = resources.getDimensionPixelSize(R.dimen.dp48) * 5
            leftScoringRv.layoutManager = LinearLayoutManager(this@WrongTestReportActivity)
            rightScoringRv.layoutParams.height = resources.getDimensionPixelSize(R.dimen.dp48) * 5
            rightScoringRv.layoutManager = LinearLayoutManager(this@WrongTestReportActivity)

            xBtn.extensionTouchArea(24.toPx())
            xBtn.setOnClickListener {
                this@WrongTestReportActivity.onBackPressed()
            }

            reviewBtn.setOnClickListener { onReviewBtnClicked() }
        }
    }

    fun onReviewBtnClicked() {
        if(isFromSolve) {
            finish()
        } else {
            LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "테스트", "리뷰하기", test.getTestType().eventItemValue)
            val intent = SolveActivity.getReviewIntent(this, test)
            startActivity(intent)
        }
    }

    private fun hideViews() {
        with(binding) {
            scoreResultLabel.visibility = View.INVISIBLE
            scoreResultContainerCl.visibility = View.INVISIBLE
            leftScoringRv.visibility = View.INVISIBLE
            rightScoringRv.visibility = View.INVISIBLE
        }
    }

    private fun configureCheckbox(test: Test) {
        val correctProblems = test.problems.filter { it.getResultByScoring() == Result.correct }
        val correctAndNotClearedProblems = correctProblems.filter { !it.isClear }


        if(correctProblems.isEmpty()) {
            binding.clearCheckbox.isChecked = false
        } else {
            binding.clearCheckbox.isChecked = correctAndNotClearedProblems.isEmpty()

            binding.clearCheckbox.setOnCheckedChangeListener { button, isChecked ->
                if(isChecked)
                    ProblemManager.clearProblems(this, correctAndNotClearedProblems, user!!) {
                        correctAndNotClearedProblems.forEach { it.isClear = isChecked }
                        binding.leftScoringRv.adapter?.notifyDataSetChanged()
                        binding.rightScoringRv.adapter?.notifyDataSetChanged()
                        val intent = Intent(ProblemManager.EVENT_PROBLEM_CLEAR_CHANGED)
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                    }
                else
                    ProblemManager.unclearProblems(this, correctAndNotClearedProblems, user!!) {
                        correctAndNotClearedProblems.forEach { it.isClear = isChecked }
                        binding.leftScoringRv.adapter?.notifyDataSetChanged()
                        binding.rightScoringRv.adapter?.notifyDataSetChanged()
                        val intent = Intent(ProblemManager.EVENT_PROBLEM_CLEAR_CHANGED)
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                    }
            }
        }
    }

    inner class ScoringAdapter(val context: Context, val isLeftSide: Boolean = false): RecyclerView.Adapter<WrongReportScoringHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WrongReportScoringHolder {
            val itemBinding: ItemTestReportScoringWrongBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_test_report_scoring_wrong, parent, false)
            val holder = WrongReportScoringHolder(itemBinding)
            if(isLeftSide)
                holder.endBorder.visibility = View.INVISIBLE
            return holder
        }

        override fun getItemCount(): Int {
            return 5
        }

        override fun onBindViewHolder(holder: WrongReportScoringHolder, position: Int) {
            val problem = if(isLeftSide)
                test.problems.getOrNull(position)
            else
                test.problems.getOrNull(position + 5)
            holder.set(problem)
        }
    }
}

class WrongReportScoringHolder(val itemBinding: ItemTestReportScoringWrongBinding): RecyclerView.ViewHolder(itemBinding.root) {
    val numTv = itemBinding.numTv
    val containerCl = itemBinding.containerCl
    val endBorder = itemBinding.endBorder
    val resultIv = itemBinding.resultIv
    val correctRateTv = itemBinding.correctRateTv
    val wrongCntTv = itemBinding.wrongCntTv
    val resultTv = itemBinding.resultTv
    val clearContainerCl = itemBinding.clearContainerCl
    val viewContext = itemBinding.root.context

    fun set(problem: Problem?) {
        if(problem == null) {
            numTv.text = "-"
            correctRateTv.text = "-"
            wrongCntTv.text = "-"
            numTv.setBackgroundColor(ContextCompat.getColor(viewContext, R.color.white_fafafa))

            wrongCntTv.setTextColor(ContextCompat.getColor(viewContext, R.color.black_4c4c4c))
            wrongCntTv.typeface = Theme.regular(viewContext)

            itemView.setBackgroundColor(ContextCompat.getColor(viewContext, R.color.white_ffffff))
            resultTv.visibility = View.VISIBLE
            resultIv.visibility = View.INVISIBLE
            clearContainerCl.visibility = View.GONE
        } else {
            resultTv.visibility = View.INVISIBLE
            resultIv.visibility = View.VISIBLE
            numTv.text = "${problem.problemNum}"
            wrongCntTv.text = "${problem.wrongCount!!}회"
            correctRateTv.text = "${problem.standardCorrectRate}%"
            if (problem.getResultByScoring() == Result.correct) {
                resultIv.setImageResource(R.drawable.ic_result_correct)
                wrongCntTv.setTextColor(ContextCompat.getColor(viewContext, R.color.black_4c4c4c))
                wrongCntTv.typeface = Theme.regular(viewContext)
            } else {
                resultIv.setImageResource(R.drawable.ic_result_incorrect)
                wrongCntTv.setTextColor(ContextCompat.getColor(viewContext, R.color.red_fe7b67))
                wrongCntTv.typeface = Theme.extraBold(viewContext)
            }

            if(problem.isClear) {
                clearContainerCl.visibility = View.VISIBLE
            } else {
                clearContainerCl.visibility = View.GONE
            }
        }
    }
}
