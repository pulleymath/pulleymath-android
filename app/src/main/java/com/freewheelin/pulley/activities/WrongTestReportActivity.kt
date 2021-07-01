package com.freewheelin.pulley.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.solve.SolveActivity
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.core.manage.ProblemManager
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.model.Problem
import com.freewheelin.pulley.model.Result
import com.freewheelin.pulley.model.contents.Test
import com.freewheelin.pulley.utils.*
import kotlinx.android.synthetic.main.activity_test_report_wrong.*
import kotlinx.android.synthetic.main.item_test_report_scoring_wrong.view.*

class WrongTestReportActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_test_report_wrong)
        initUI()
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
            scoreTv.text = "${it.score}"
            leftScoringRv.adapter = ScoringAdapter(this,  true)
            rightScoringRv.adapter = ScoringAdapter(this, false)

            leftCorrectRateTv.text = "${it.studentRating}등급 정답률"
            rightCorrectRateTv.text = "${it.studentRating}등급 정답률"

            scoreResultLabel.show()
            scoreResultContainerCl.show()
            leftScoringRv.show()
            rightScoringRv.show()

            configureCheckbox(it)

        }
    }

    private fun initUI() {
        hideViews()
        leftScoringRv.layoutParams.height = resources.getDimensionPixelSize(R.dimen.dp48) * 5
        leftScoringRv.layoutManager = LinearLayoutManager(this)
        rightScoringRv.layoutParams.height = resources.getDimensionPixelSize(R.dimen.dp48) * 5
        rightScoringRv.layoutManager = LinearLayoutManager(this)

        xBtn.extensionTouchArea(24.toPx())
        xBtn.setOnClickListener {
            this.onBackPressed()
        }

        reviewBtn.setPermissionClickListener { onReviewBtnClicked() }
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
        scoreResultLabel.visibility = View.INVISIBLE
        scoreResultContainerCl.visibility = View.INVISIBLE
        leftScoringRv.visibility = View.INVISIBLE
        rightScoringRv.visibility = View.INVISIBLE
    }

    private fun configureCheckbox(test: Test) {
        val correctProblems = test.problems.filter { it.getResultByScoring() == Result.correct }
        val correctAndNotClearedProblems = correctProblems.filter { !it.isClear }


        if(correctProblems.isEmpty()) {
            clearCheckbox.isChecked = false
        } else {
            clearCheckbox.isChecked = correctAndNotClearedProblems.isEmpty()

            clearCheckbox.setOnCheckedChangeListener { button, isChecked ->
                if(isChecked)
                    ProblemManager.clearProblems(this, correctAndNotClearedProblems, user!!) {
                        correctAndNotClearedProblems.forEach { it.isClear = isChecked }
                        leftScoringRv.adapter?.notifyDataSetChanged()
                        rightScoringRv.adapter?.notifyDataSetChanged()
                        val intent = Intent(ProblemManager.EVENT_PROBLEM_CLEAR_CHANGED)
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                    }
                else
                    ProblemManager.unclearProblems(this, correctAndNotClearedProblems, user!!) {
                        correctAndNotClearedProblems.forEach { it.isClear = isChecked }
                        leftScoringRv.adapter?.notifyDataSetChanged()
                        rightScoringRv.adapter?.notifyDataSetChanged()
                        val intent = Intent(ProblemManager.EVENT_PROBLEM_CLEAR_CHANGED)
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                    }
            }
        }
    }

    inner class ScoringAdapter(val context: Context, val isLeftSide: Boolean = false): RecyclerView.Adapter<WrongReportScoringHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WrongReportScoringHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_test_report_scoring_wrong, parent, false)
            val holder = WrongReportScoringHolder(view)
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

class WrongReportScoringHolder(val view: View): RecyclerView.ViewHolder(view) {
    val numTv = view.numTv
    val containerCl = view.containerCl
    val endBorder = view.endBorder
    val resultIv = view.resultIv
    val correctRateTv = view.correctRateTv
    val wrongCntTv = view.wrongCntTv
    val resultTv = view.resultTv
    val clearContainerCl = view.clearContainerCl

    fun set(problem: Problem?) {
        if(problem == null) {
            numTv.text = "-"
            correctRateTv.text = "-"
            wrongCntTv.text = "-"
            numTv.setBackgroundColor(ContextCompat.getColor(view.context, R.color.white_fafafa))

            wrongCntTv.setTextColor(ContextCompat.getColor(view.context, R.color.black_4c4c4c))
            wrongCntTv.typeface = Theme.regular(view.context)

            itemView.setBackgroundColor(ContextCompat.getColor(view.context, R.color.white_ffffff))
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
                wrongCntTv.setTextColor(ContextCompat.getColor(view.context, R.color.black_4c4c4c))
                wrongCntTv.typeface = Theme.regular(view.context)
            } else {
                resultIv.setImageResource(R.drawable.ic_result_incorrect)
                wrongCntTv.setTextColor(ContextCompat.getColor(view.context, R.color.red_fe7b67))
                wrongCntTv.typeface = Theme.extraBold(view.context)
            }

            if(problem.isClear) {
                clearContainerCl.visibility = View.VISIBLE
            } else {
                clearContainerCl.visibility = View.GONE
            }
        }
    }
}
