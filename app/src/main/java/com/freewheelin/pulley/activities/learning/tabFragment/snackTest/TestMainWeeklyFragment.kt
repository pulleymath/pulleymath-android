package com.freewheelin.pulley.activities.learning.tabFragment.snackTest


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.model.contents.Test
import kotlinx.android.synthetic.main.fragment_test_main_weekly.*
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.Buttons.ButtonLockImage
import com.freewheelin.pulley.views.Buttons.ButtonMode
import kotlinx.android.synthetic.main.fragment_test_main.*
import kotlinx.android.synthetic.main.fragment_test_main_unavailable_test.*
import kotlinx.android.synthetic.main.fragment_test_main_weekly.contentTv
import kotlinx.android.synthetic.main.fragment_test_main_weekly.headerTv
import kotlinx.android.synthetic.main.fragment_test_main_weekly.levelLabel
import kotlinx.android.synthetic.main.fragment_test_main_weekly.levelTv
import kotlinx.android.synthetic.main.fragment_test_main_weekly.rangeLabel
import kotlinx.android.synthetic.main.fragment_test_main_weekly.rangeTv
import kotlinx.android.synthetic.main.fragment_test_main_weekly.settingBtn
import kotlinx.android.synthetic.main.fragment_test_main_weekly.settingContainerCl
import kotlinx.android.synthetic.main.fragment_test_main_weekly.startBtn
import kotlinx.android.synthetic.main.fragment_test_main_weekly.timerGuideTv
import kotlinx.android.synthetic.main.fragment_test_main_weekly.titleTv
import kotlinx.android.synthetic.main.fragment_test_main_weekly_finish.*
import java.util.*


class TestMainWeeklyFragment : TestMainBaseFragment() {

    override var test: Test? = null
    override var testType: Test.TestType = Test.TestType.weekly
    companion object {
        fun newInstance(test: Test?): TestMainWeeklyFragment {
            val fragment = TestMainWeeklyFragment()
            val args = Bundle()
            args.putSerializable(TestManager.ARG_TEST, test)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        if(test == null)
            return inflater.inflate(R.layout.fragment_test_main_unavailable_test, container, false)
        else if(test?.isCompleted() == true)
            return inflater.inflate(R.layout.fragment_test_main_weekly_finish, container, false)
        else
            return inflater.inflate(R.layout.fragment_test_main_weekly, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(test == null) {
            setUnavailableUI()
        } else {
            configureUI(test!!)
        }

    }

    override fun showMainContents() {
        if(test == null) {
            imageView.show(duration)
            textView.show(duration)
        } else if(test?.isCompleted() == true) {
            titleTv.show(duration)
            scoreTv.show(duration)
            reportTv.show(duration)
            scoreLabel.show(duration) {
                val duration = (200 * 2).toLong()
                contentTv.show(duration)
                fullClearCl.show(duration)
            }
            reportTv.setOnClickListener { listener?.onReportBtnClicked(test!!) }
        } else {
            val duration: Long = 200
            headerTv.show(duration)
            titleTv.show(duration) {
                val duration = (200 * 2).toLong()
                contentTv.show(duration)
                settingContainerCl.show(duration)
                timerGuideTv.show(duration)
                startBtn.show(duration)
            }

//            startBtn.setLock(user!!.hasPulleyPlus, ButtonLockImage.mid24, ButtonMode.pulley_plus)
            startBtn.setOnClickListener {
                listener?.onSolveBtnClicked(test!!)
            }
        }
    }

    override fun hideMainContents(cb: () -> Unit) {
        if(test?.isCompleted() == true) {
            titleTv.hide(duration)
            scoreTv.hide(duration)
            contentTv.hide(duration)
            settingContainerCl.hide(duration)
            reportTv.hide(duration)
            startBtn.hide(duration)
            fullClearCl.hide(duration)
            scoreLabel.hide(duration) {
                cb()
            }
        } else {
            val duration: Long = 200
            headerTv.hide(duration)
            titleTv.hide(duration)
            contentTv.hide(duration)
            settingContainerCl.hide(duration)
            timerGuideTv.hide(duration)
            startBtn.hide(duration) {
                cb()
            }
        }
    }

    override fun configureUI(test: Test) {
        if(test.isCompleted()) {
            scoreTv.text = "${test.score}"
            coverTv.text = test.weeklyInfo.testRange
            durationTv.text = test.weeklyInfo.getDurationText()
        } else {
            contentTv.text = "주간 테스트는 이번 주 ${user!!.fullName}님이 학습한 범위 내에서 출제됩니다."
            headerTv.text = "이번 주 점검을 위한"
            levelLabel.text = "기간"
            levelTv.text = test.weeklyInfo.getDurationText()
            rangeLabel.text = "범위"
            rangeTv.text = test.weeklyInfo.testRange
            settingBtn.visibility = View.INVISIBLE

            titleTv.text = test.subject
            tick()
        }
    }

    private fun setUnavailableUI() {
        imageView.setImageResource(R.drawable.ic_weekly_unavailable)
        textView.text = "주간 테스트는 주말에만 제공됩니다.\n" +
                "평일에는 데일리 테스트를 풀어보세요 :)"
    }

    override fun tick() {
        if(test == null)
            return
        val now = Date()
        val targetDate = test!!.endDate
        if(now > targetDate) {
            startBtn?.text = "남은시간 00:00:00"
            startBtn?.toDisableUI()
        } else {
            val timeLimit = (targetDate.time - now.time) / 1000
            val hour = timeLimit / 3600
            val min = (timeLimit - (hour * 3600)) / 60
            val sec = timeLimit % 60


            startBtn?.text = "남은시간 ${String.format("%02d", hour)}:${String.format("%02d", min)}:${String.format("%02d", sec)}"
            startBtn?.toEnableUI()
        }
    }
}
