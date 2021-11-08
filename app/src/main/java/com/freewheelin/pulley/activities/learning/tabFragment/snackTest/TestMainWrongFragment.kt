package com.freewheelin.pulley.activities.learning.tabFragment.snackTest

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.model.contents.Test
import com.freewheelin.pulley.utils.*
import kotlinx.android.synthetic.main.fragment_test_main_wrong.*

class TestMainWrongFragment : TestMainBaseFragment() {
    companion object {
        fun newInstance(test: Test?): TestMainWrongFragment {
            val fragment = TestMainWrongFragment()
            val args = Bundle()
            args.putSerializable(TestManager.ARG_TEST, test)
            fragment.arguments = args
            return fragment
        }
    }

    override var test: Test? = null
    override var testType: Test.TestType = Test.TestType.wrong

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_test_main_wrong, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        test?.let { test ->
            configureUI(test)
            startBtn.setPermissionClickListener { listener?.onSolveBtnClicked(test) }
            reportTv.setOnClickListener { listener?.onReportBtnClicked(test) }
        }
    }

    override fun configureUI(test: Test) {
        if(isNeedToFinishUI()) {
            resultHeaderTv.visibility = View.VISIBLE
            scoreTv.visibility = View.VISIBLE
            scoreLabel.visibility = View.VISIBLE
            reportTv.visibility = View.VISIBLE

            dateTv.visibility = View.INVISIBLE
            headerTv.visibility = View.INVISIBLE
            startBtn.text = "테스트 다시보기"
        } else {
            resultHeaderTv.visibility = View.INVISIBLE
            scoreTv.visibility = View.INVISIBLE
            scoreLabel.visibility = View.INVISIBLE
            reportTv.visibility = View.INVISIBLE

            dateTv.visibility = View.VISIBLE
            headerTv.visibility = View.VISIBLE
            startBtn.text = "테스트 시작하기"
        }
        scoreTv.text = "${test.score}"
        dateTv.text = test.wrongInfo.monthAndWeek
        durationTv.text = "${test.startDate.month()}월 ${test.startDate.day()}일 - ${test.endDate.month()}월 ${test.endDate.day()}일"
        numberOfTestTv.text = "${test.scoringTestPieceCount}회"

        val cnt = test.wrongInfo.wrongProblemCount - test.wrongInfo.clearedProblemCount
        if(cnt != 0) {
            wrongCntTv.text = "${cnt}개"
        } else {
            wrongCntTv.text = "-"
        }
    }

    override fun showMainContents() {
        val duration: Long = 200

        if(isNeedToFinishUI()) {
            resultHeaderTv.show(duration)
            scoreTv.show(duration)
            scoreLabel.show(duration)
            reportTv.show(duration) {
                val duration = duration * 2
                guideTv.show(duration)
                contentsCl.show(duration)
                startBtn.show(duration)
            }
        } else {
            dateTv.show(duration)
            headerTv.show(duration) {
                val duration = duration * 2
                guideTv.show(duration)
                contentsCl.show(duration)
                startBtn.show(duration)
            }
        }
    }

    override fun hideMainContents(cb: () -> Unit) {
        val duration: Long = 200
        dateTv.hide(duration)
        headerTv.hide(duration)
        guideTv.hide(duration)
        contentsCl.hide(duration)
        startBtn.hide(duration) {
            cb()
        }

        resultHeaderTv.hide(duration)
        scoreTv.hide(duration)
        scoreLabel.hide(duration)
        reportTv.hide(duration)
    }

    private fun isNeedToFinishUI(): Boolean {
        return test!!.scoringTestPieceCount > 0
    }

}
