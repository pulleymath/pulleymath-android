package com.freewheelin.pulley.activities.learning.tabFragment.snackTest


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.databinding.FragmentTestPageDailyBinding
import com.freewheelin.pulley.databinding.FragmentTestPageDailyCompleteBinding
import com.freewheelin.pulley.model.contents.Test

class TestPageDailyFragment : TestPageBaseFragment() {
    override var test: Test? = null
    lateinit var binding: ViewDataBinding

    companion object {
        fun newInstance(test: Test): TestPageDailyFragment {
            val fragment = TestPageDailyFragment()
            val args = Bundle()
            args.putSerializable(TestManager.ARG_TEST, test)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = if(test?.isCompleted() == true) DataBindingUtil.inflate(inflater, R.layout.fragment_test_page_daily_complete, container, false)
        else DataBindingUtil.inflate(inflater, R.layout.fragment_test_page_daily, container, false)
        return binding.root
    }

    override fun configureBy(test: Test) {
        if (!test.isCompleted()) {
            with(binding as FragmentTestPageDailyBinding) {
                when(test.scoringTestPieceCount) {
                    1 -> {
                        firstTestIv.setImageResource(R.drawable.ic_check_green_circle_24)
                        remainTv.setTextColor(ContextCompat.getColor(requireContext(),R.color.green_70d000))
                        remainTv.text = "오늘 남은 횟수 : 2회"
                    }
                    2 -> {
                        firstTestIv.setImageResource(R.drawable.ic_check_green_circle_24)
                        secondTestIv.setImageResource(R.drawable.ic_check_green_circle_24)
                        remainTv.setTextColor(ContextCompat.getColor(requireContext(),R.color.green_70d000))
                        remainTv.text = "오늘 남은 횟수 : 1회"
                    }
                }
            }
        }
    }
}
