package com.freewheelin.pulley.legacy.activities.learning.tabFragment.snackTest


import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.core.manage.TestManager
import com.freewheelin.pulley.databinding.FragmentTestMainNeedMoreBinding
import com.freewheelin.pulley.legacy.model.contents.Test
import com.freewheelin.pulley.legacy.utils.*

class TestMainNeedMoreFragment : TestMainBaseFragment() {
    lateinit var binding: FragmentTestMainNeedMoreBinding
    override var test: Test? = null
    override var testType: Test.TestType = Test.TestType.weekly
    companion object {
        fun newInstance(test: Test): TestMainNeedMoreFragment {
            val fragment = TestMainNeedMoreFragment()
            val args = Bundle()
            args.putSerializable(TestManager.ARG_TEST, test)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_test_main_need_more, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureUI(test!!)
    }

    override fun showMainContents() {
        with(binding) {
            val duration: Long = 200
            dateGuideTv.show(duration)
            remainGuideTv.show(duration)
            containerCl.show(duration) {
                val value = test!!.weeklyInfo.weeklyProblemCount / 30f
                progressBar.set(value, true, 400, 0, object: Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator) {}
                    override fun onAnimationCancel(p0: Animator) {}
                    override fun onAnimationStart(p0: Animator) {}

                    override fun onAnimationEnd(p0: Animator) {
                        if(context == null) return

                        val width = progressBar.measuredWidth * value
                        val marginLeft = resources.getDimension(R.dimen.dp120)
                        guideline3.setGuidelineBegin(width.toInt() + marginLeft.toInt() )
                        balloonIv.show()
                        balloonTv.show()
                    }
                })
            }
        }
    }

    override fun configureUI(test: Test) {
        with(binding) {
            progressBar.set(0f)
            remainGuideTv.text = "주간 테스트를 시작하려면\n${30 - test.weeklyInfo.weeklyProblemCount}문제를 더 풀어주세요."
            balloonTv.text = "${test.weeklyInfo.weeklyProblemCount}"
            val startDate = test.weeklyInfo.weekStartDate
            val endDate = test.weeklyInfo.weekEndDate

            dateGuideTv.text = "${startDate.month()}월 ${startDate.day()}일" +
                " - " +
                "${endDate.month()}월 ${endDate.day()}일까지 ${test.weeklyInfo.weeklyProblemCount}문제 풀었습니다."
        }
    }

    override fun hideMainContents(cb: () -> Unit) {
        with(binding) {
            val duration: Long = 200
            dateGuideTv.hide(duration)
            remainGuideTv.hide(duration)
            containerCl.hide(duration)
        }
    }
}
