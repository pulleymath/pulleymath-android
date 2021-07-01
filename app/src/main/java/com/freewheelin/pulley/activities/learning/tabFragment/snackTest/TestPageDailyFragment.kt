package com.freewheelin.pulley.activities.learning.tabFragment.snackTest


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.model.contents.Test
import kotlinx.android.synthetic.main.fragment_test_page_daily.*


/**
 * A simple [Fragment] subclass.
 */
class TestPageDailyFragment : TestPageBaseFragment() {
    override var test: Test? = null

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
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        if(test?.isCompleted() == true)
            return inflater.inflate(R.layout.fragment_test_page_daily_complete, container, false)
        else
            return inflater.inflate(R.layout.fragment_test_page_daily, container, false)
    }
    override fun configureBy(test: Test) {
        when(test.scoringTestPieceCount) {
            1 -> {
                firstTestIv.setImageResource(R.drawable.ic_check_green_circle_24)
                remainTv.setTextColor(ContextCompat.getColor(context!!,R.color.green_70d000))
                remainTv.text = "오늘 남은 횟수 : 2회"
            }
            2 -> {
                firstTestIv.setImageResource(R.drawable.ic_check_green_circle_24)
                secondTestIv.setImageResource(R.drawable.ic_check_green_circle_24)
                remainTv.setTextColor(ContextCompat.getColor(context!!,R.color.green_70d000))
                remainTv.text = "오늘 남은 횟수 : 1회"
            }
        }
    }
}
