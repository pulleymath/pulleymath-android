package com.freewheelin.pulley.activities.learning.tabFragment.snackTest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.databinding.FragmentTestMainWrongBinding
import com.freewheelin.pulley.model.contents.Test
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.revision2023.ui.dialogs.PurchaseGuideDialog
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.buttons.ButtonLockImage
import com.freewheelin.pulley.views.buttons.ButtonMode

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
    lateinit var binding: FragmentTestMainWrongBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_test_main_wrong, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        test?.let { test ->
            configureUI(test)
            binding.apply {
                val showLockIv = user?.serviceType?.isTypeEqualOrHigher(PaidServiceType.BASIC_P) == false
                if (showLockIv) startBtn.setLock(ButtonLockImage.mid20)
                else startBtn.setUnlock()

                startBtn.setOnPaidUserClickListener(cb = {
                    listener?.onSolveBtnClicked(test)
                }, deniedCb = {
                    val dialog = PurchaseGuideDialog()
                    childFragmentManager.let { dialog.show(it, "purchaseGuideDialog") }
                })

                reportTv.setOnPaidUserClickListener(cb = { listener?.onReportBtnClicked(test) },
                    deniedCb = {
                        val dialog = PurchaseGuideDialog()
                        childFragmentManager.let { dialog.show(it, "purchaseGuideDialog") }
                    })
            }
        }
    }

    override fun configureUI(test: Test) {
        binding.apply {
            if (isNeedToFinishUI()) {
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
            durationTv.text =
                "${test.startDate.month()}월 ${test.startDate.day()}일 - ${test.endDate.month()}월 ${test.endDate.day()}일"
            numberOfTestTv.text = "${test.scoringTestPieceCount}회"

            val cnt = test.wrongInfo.wrongProblemCount - test.wrongInfo.clearedProblemCount
            if (cnt != 0) {
                wrongCntTv.text = "${cnt}개"
            } else {
                wrongCntTv.text = "-"
            }
        }
    }

    override fun showMainContents() {
        binding.apply {

            val duration: Long = 200

            if (isNeedToFinishUI()) {
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
    }

    override fun hideMainContents(cb: () -> Unit) {
        binding.apply {
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
    }

    private fun isNeedToFinishUI(): Boolean {
        return test!!.scoringTestPieceCount > 0
    }

}
