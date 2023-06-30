package com.freewheelin.pulley.legacy.activities.learning.tabFragment.snackTest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.core.manage.TestManager
import com.freewheelin.pulley.databinding.FragmentTestPageBinding
import com.freewheelin.pulley.legacy.model.contents.Test
import com.freewheelin.pulley.legacy.utils.LogUtils
import java.util.*

class TestPageFragment : TestPageBaseFragment() {
    lateinit var binding: FragmentTestPageBinding
    override var test: Test? = null
    companion object {
        fun newInstance(test: Test): TestPageFragment {
            val fragment = TestPageFragment()
            val args = Bundle()
            args.putSerializable(TestManager.ARG_TEST, test)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_test_page, container, false)
        return binding.root
    }

    override fun configureBy(test: Test) {
        with(binding) {
            when(test.getTestType()) {
                Test.TestType.daily -> {
                    titleTv.text = "데일리\n테스트"
                    descTv.text = test.description
                    wrongGuideTv.visibility = View.INVISIBLE
                }

                Test.TestType.weekly -> {
                    titleTv.text = "주간\n테스트"
                    descTv.text = test.description
                    wrongGuideTv.visibility = View.INVISIBLE
                }

                Test.TestType.wrong -> {
                    titleTv.text = "오답\n테스트"
                    wrongGuideTv.visibility = View.VISIBLE
                    warningTimeLabel.visibility = View.INVISIBLE
                    warningGuideLabel.visibility = View.INVISIBLE
                    wrongGuideTv.text = test.wrongInfo.headline
                    descTv.text = test.description
                }
                else -> {
                    LogUtils.assert(false, "unexpected case ${test.getTestType()}")
                }
            }
            tick()
        }
    }

    override fun tick() {
        if(test == null
                || test!!.getTestType() == Test.TestType.wrong
                || test!!.getTestType() == Test.TestType.theme
                || test!!.getTestType() == Test.TestType.initial)
            return


        val now = Date()
        val targetDate = test!!.endDate

        if(now > targetDate) {
            binding.warningTimeLabel.text = "00:00"
        } else {
            val timeLimit = (targetDate.time - now.time) / 1000
            val hour = timeLimit / 3600
            val min = (timeLimit - (hour * 3600)) / 60

            binding.warningTimeLabel.text = "${String.format("%02d", hour)}:${String.format("%02d", min)}"
        }
    }

}
