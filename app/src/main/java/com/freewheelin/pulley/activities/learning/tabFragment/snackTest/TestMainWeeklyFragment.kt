package com.freewheelin.pulley.activities.learning.tabFragment.snackTest


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.databinding.FragmentTestMainUnavailableTestBinding
import com.freewheelin.pulley.databinding.FragmentTestMainWeeklyBinding
import com.freewheelin.pulley.databinding.FragmentTestMainWeeklyFinishBinding
import com.freewheelin.pulley.model.contents.Test
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.buttons.ButtonLockImage
import java.util.*


class TestMainWeeklyFragment : TestMainBaseFragment() {
    lateinit var binding: ViewDataBinding

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
        binding = if(test == null) DataBindingUtil.inflate(inflater, R.layout.fragment_test_main_unavailable_test, container, false)
        else if(test?.isCompleted() == true) DataBindingUtil.inflate(inflater, R.layout.fragment_test_main_weekly_finish, container, false)
        else  DataBindingUtil.inflate(inflater, R.layout.fragment_test_main_weekly, container, false)
        return binding.root
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
            with(binding as FragmentTestMainUnavailableTestBinding) {
                imageView.show(duration)
                textView.show(duration)
            }
        } else if(test?.isCompleted() == true) {
            with(binding as FragmentTestMainWeeklyFinishBinding) {
                titleTv.show(duration)
                scoreTv.show(duration)
                reportTv.show(duration)
                scoreLabel.show(duration) {
                    val duration = (200 * 2).toLong()
                    contentTv.show(duration)
                    fullClearCl.show(duration)
                }
                reportTv.setOnClickListener { listener?.onReportBtnClicked(test!!) }
            }
        } else {
            with(binding as FragmentTestMainWeeklyBinding) {
                val duration: Long = 200
                headerTv.show(duration)
                titleTv.show(duration) {
                    val duration = (200 * 2).toLong()
                    contentTv.show(duration)
                    settingContainerCl.show(duration)
                    timerGuideTv.show(duration)
                    startBtn.show(duration)
                }

                val showLockIv = user?.serviceType?.isTypeEqualOrHigher(PaidServiceType.BASIC_P) == false
                if (showLockIv) startBtn.setLock(ButtonLockImage.mid20)
                else startBtn.setUnlock()

                startBtn.setOnPaidUserClickListener(cb = {
                    listener?.onSolveBtnClicked(test!!)
                }, deniedCb = {
                    LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "주간테스트", "결제유도", "시작")
                    val dialog = PurchaseGuideDialog()
                    childFragmentManager.let { dialog.show(it, "purchaseGuideDialog") }
                })
            }
        }
    }

    override fun hideMainContents(cb: () -> Unit) {
        if(test?.isCompleted() == true) {
            with(binding as FragmentTestMainWeeklyFinishBinding) {
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
            }
        } else {
            with(binding as FragmentTestMainWeeklyBinding) {
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
    }

    override fun configureUI(test: Test) {
        if(test.isCompleted()) {
            with(binding as FragmentTestMainWeeklyFinishBinding) {
                scoreTv.text = "${test.score}"
                coverTv.text = test.weeklyInfo.testRange
                durationTv.text = test.weeklyInfo.getDurationText()
            }
        } else {
            with(binding as FragmentTestMainWeeklyBinding) {
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
    }

    private fun setUnavailableUI() {
        with(binding as FragmentTestMainUnavailableTestBinding) {
            imageView.setImageResource(R.drawable.ic_weekly_unavailable)
            textView.text = "주간 테스트는 주말에만 제공됩니다.\n" +
                "평일에는 데일리 테스트를 풀어보세요 :)"
        }
    }

    override fun tick() {
        test?.let {
            if (!it.isCompleted()) {
                with(binding as FragmentTestMainWeeklyBinding) {
                    val now = Date()
                    val targetDate = test!!.endDate
                    if (now > targetDate) {
                        startBtn.text = "남은시간 00:00:00"
                        startBtn.toDisableUI()
                    } else {
                        val timeLimit = (targetDate.time - now.time) / 1000
                        val hour = timeLimit / 3600
                        val min = (timeLimit - (hour * 3600)) / 60
                        val sec = timeLimit % 60


                        startBtn.text = "남은시간 ${String.format("%02d", hour)}:${String.format("%02d", min)}:${String.format("%02d", sec)}"
                        startBtn.toEnableUI()
                    }
                }
            }
        }


    }
}
