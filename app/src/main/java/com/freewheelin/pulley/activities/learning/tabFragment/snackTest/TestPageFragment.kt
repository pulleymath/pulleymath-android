package com.freewheelin.pulley.activities.learning.tabFragment.snackTest


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.manage.PieceManager
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.model.contents.Piece
import com.freewheelin.pulley.model.contents.PieceCategory
import com.freewheelin.pulley.model.contents.Test
import com.freewheelin.pulley.model.curation.TestCuration
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.show
import kotlinx.android.synthetic.main.fragment_test_page.*
import java.util.*

class TestPageFragment : TestPageBaseFragment() {

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
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_test_page, container, false)
    }

    override fun configureBy(test: Test) {
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

    override fun tick() {
        if(test == null
                || test!!.getTestType() == Test.TestType.wrong
                || test!!.getTestType() == Test.TestType.theme
                || test!!.getTestType() == Test.TestType.initial)
            return


        val now = Date()
        val targetDate = test!!.endDate

        if(now > targetDate) {
            warningTimeLabel?.text = "00:00"
        } else {
            val timeLimit = (targetDate.time - now.time) / 1000
            val hour = timeLimit / 3600
            val min = (timeLimit - (hour * 3600)) / 60

            warningTimeLabel?.text = "${String.format("%02d", hour)}:${String.format("%02d", min)}"
        }
    }

}
