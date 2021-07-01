package com.freewheelin.pulley.activities.learning.tabFragment.snackTest

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.model.contents.Test
import com.freewheelin.pulley.utils.*
import kotlinx.android.synthetic.main.fragment_test_main.*
import kotlinx.android.synthetic.main.fragment_test_main.contentTv
import kotlinx.android.synthetic.main.fragment_test_main.levelLabel
import kotlinx.android.synthetic.main.fragment_test_main.levelTv
import kotlinx.android.synthetic.main.fragment_test_main.rangeLabel
import kotlinx.android.synthetic.main.fragment_test_main.rangeTv
import kotlinx.android.synthetic.main.fragment_test_main.settingBtn
import kotlinx.android.synthetic.main.fragment_test_main.settingContainerCl
import kotlinx.android.synthetic.main.fragment_test_main.startBtn
import kotlinx.android.synthetic.main.fragment_test_main.titleTv
import kotlinx.android.synthetic.main.fragment_test_main.subjectTv
import kotlinx.android.synthetic.main.fragment_test_main_result.*
import kotlinx.android.synthetic.main.fragment_test_main_unavailable_test.*
import java.util.*

class TestMainDailyFragment : TestMainBaseFragment() {

    override var test: Test? = null
    override var testType: Test.TestType = Test.TestType.daily
    companion object {
        fun newInstance(test: Test?): TestMainDailyFragment {
            val fragment = TestMainDailyFragment()
            val args = Bundle()
            args.putSerializable(TestManager.ARG_TEST, test)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (test == null)
            return inflater.inflate(R.layout.fragment_test_main_unavailable_test, container, false)
        else if(test?.scoringTestPieceCount == 0) {
            return inflater.inflate(R.layout.fragment_test_main, container, false)
        } else {
            return inflater.inflate(R.layout.fragment_test_main_result, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(test == null)
            setUnavailableUI()
        else
            configureUI(test!!)
    }

    override fun showMainContents() {
        if(test == null) {
            imageView.show(duration)
            textView.show(duration)
        } else if(test?.scoringTestPieceCount == 0) {
            showInitContents()
        } else if(test?.isCompleted() == false){
            showMiddleContents()
        } else {
            showAllFinishContents()
        }
    }

    private fun showInitContents() {
        headerTv.show(duration)
        titleTv.show(duration) {
            val duration = (200 * 2).toLong()
            contentTv.show(duration)
            settingContainerCl.show(duration)
            startBtn.show(duration)
        }
    }

    private fun showMiddleContents() {
        titleTv.show(duration)
        scoreTv.show(duration)
        reportTv.show(duration)
        scoreLabel.show(duration) {
            val duration = (200 * 2).toLong()
            contentTv.show(duration)
            settingContainerCl.show(duration)
            startBtn.show(duration)
        }
    }

    private fun showAllFinishContents() {
        giftIv.show(duration) {
            clearTv.show(duration * 2)
            reportTv2.show(duration * 2)
        }

        if(TestManager.isNeedToFullDailyResultInTab) {
            titleTv.show(duration)
            scoreTv.show(duration)
            reportTv.show(duration)
            scoreLabel.show(duration) {
                val duration = (200 * 2).toLong()
                contentTv.show(duration)
                fullClearCl.show(duration)
            }
        }
    }

    override fun hideMainContents(cb: () -> Unit) {
        if(test == null) {
            imageView.show(duration)
            textView.show(duration)
        } else if(test?.scoringTestPieceCount == 0) {
            hideInitContents(cb)
        } else if(test?.isCompleted() == false){
            hideMiddleContents(cb)
        } else {
            hideAllFinishContents()
        }
    }

    private fun hideInitContents(cb: () -> Unit) {
        headerTv.hide(duration)
        titleTv.hide(duration)
        contentTv.hide(duration)
        settingContainerCl.hide(duration)
        startBtn.hide(duration) {
            cb()
        }
    }

    private fun hideMiddleContents(cb: () -> Unit) {
        titleTv.hide(duration)
        scoreTv.hide(duration)
        contentTv.hide(duration)
        settingContainerCl.hide(duration)
        reportTv.hide(duration)
        reportTv2.hide(duration)
        startBtn.hide(duration)
        scoreLabel.hide(duration) {
            cb()
        }
    }

    private fun setUnavailableUI() {
        imageView.setImageResource(R.drawable.ic_daily_unavailable)
        textView.text = "데일리 테스트는 평일에만 제공됩니다.\n" +
                "주말에는 주간 테스트를 풀어보세요 :)"
    }

    private fun hideAllFinishContents() {
        clearTv.hide(duration)
        giftIv.hide(duration)
        reportTv2.hide(duration)
        fullClearCl.hide(duration)
        titleTv.hide(duration)
        scoreTv.hide(duration)
        contentTv.hide(duration)
        settingContainerCl.hide(duration)
        reportTv.hide(duration)
        reportTv2.hide(duration)
        startBtn.hide(duration)
        scoreLabel.hide(duration)
    }

    override fun configureUI(test: Test) {

        Log.d("테스트","===> TestMainDailyFragment")
        user?.log()

        if(test.scoringTestPieceCount == 0) {
            configureInitUI(test)
        } else if (test.isCompleted() == false){
            configureMiddleUI(test)
        } else {
            configureFinishUI(test)
        }

        startBtn.setPermissionClickListener {
            listener?.onSolveBtnClicked(test)
        }

        settingBtn.setOnClickListener {
            listener?.onSettingBtnClicked(test)
        }

        reportTv?.setOnClickListener { listener?.onReportBtnClicked(test) }
        reportTv2?.setOnClickListener { listener?.onReportBtnClicked(test, true) }
    }

    private fun configureInitUI(test: Test) {
        val date = Date()
        contentTv.text = "데일리 테스트는 응시할 때마다 문항이 새로 출제됩니다.\n문항 추천 기준은 아래와 같습니다."
        headerTv.text = "${date.month()}월 ${date.day()}일"
        levelLabel.text = "난이도"
        levelTv.text = user!!.getRecommendLevelText()
        rangeLabel.text ="출제 범위"
        rangeTv.text = user!!.getRecommendRangeText()
        settingBtn.visibility = View.VISIBLE
        titleTv.text = test.subject
        startBtn.text = "${test.scoringTestPieceCount + 1}회차 테스트 시작하기"

        setSubjectRangeText()
    }

    private fun configureMiddleUI(test: Test) {
        giftContainerCl.visibility = View.INVISIBLE

        titleTv.text = "${test.scoringTestPieceCount}회차 결과"
        scoreTv.text = test.score.toString()
        contentTv.text = "자세한 내용은 보고서에서 확인해주세요 :)\n문항 추천 기준은 아래와 같습니다."
        levelLabel.text = "난이도"
        levelTv.text = user!!.getRecommendLevelText()
        rangeLabel.text ="출제 범위"
        rangeTv.text = user!!.getRecommendRangeText()
        startBtn.text = "${test.scoringTestPieceCount + 1}회차 테스트 시작하기"
        reportTv.extensionTouchArea(24.toPx())

        setSubjectRangeText()
    }

    private fun setSubjectRangeText() {
        subjectTv.text = if(user!!.recommendChapter == 0 && user!!.recentSubjectCode.isNotEmpty()) {
            user!!.getRecentSubjectText()
        } else if(user!!.recommendChapter == 1) {
            "수학1, 수학2" + if(user!!.rawInitOptional.isNotEmpty()) ", "+user!!.getOptionalSubjectText() else ""
        } else {
            user!!.getAllSubjectText()
        }
    }

    private fun configureFinishUI(test: Test) {
        if(TestManager.isNeedToFullDailyResultInTab) {
            scoreTv.text = test.score.toString()
            titleTv.text = "${test.scoringTestPieceCount}회차 결과"
            contentTv.text = "자세한 내용은 보고서에서 확인해주세요 :)\n데일리테스트는 월~금 오전 6시에 공개됩니다!"
            giftContainerCl.visibility = View.INVISIBLE
            settingContainerCl.visibility = View.INVISIBLE
            startBtn.visibility = View.INVISIBLE

            TestManager.getDailyTestReport(requireContext(), user!!) {

                firstScoreTv.text = "${it[0].score}점"
                secondScoreTv.text = "${it[1].score}점"
                thirdScoreTv.text = "${it[2].score}점"

                firstScoreTv?.show(duration)
                secondScoreTv?.show(duration)
                thirdScoreTv?.show(duration)
                firstLabelTv?.show(duration)
                secondLabelTv?.show(duration)
                thirdLabelTv?.show(duration)
                fullClearGuideTv?.show(duration)
                TestManager.isNeedToFullDailyResultInTab = false
            }
        } else {
            giftContainerCl.visibility = View.VISIBLE
        }
    }
}
