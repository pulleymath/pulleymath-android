package com.freewheelin.pulley.activities.learning.tabFragment.snackTest


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.model.contents.Test
import com.freewheelin.pulley.utils.hide
import com.freewheelin.pulley.utils.show
import kotlinx.android.synthetic.main.fragment_test_main_daily_setup.*


class TestMainDailySetupFragment : TestMainBaseFragment() {

    override var test: Test? = null
    override var testType: Test.TestType = Test.TestType.daily

    companion object {
        fun newInstance(test: Test): TestMainDailySetupFragment {
            val fragment = TestMainDailySetupFragment()
            val args = Bundle()
            args.putSerializable(TestManager.ARG_TEST, test)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_test_main_daily_setup, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBtn.setOnClickListener {
            listener?.onSettingBtnClicked(test!!)
        }
    }

    override fun showMainContents() {
        guideTv.show(duration)
        setupBtn.show(duration)
    }

    override fun hideMainContents(cb: () -> Unit) {
        guideTv.hide(duration)
        setupBtn.hide(duration)
    }

    override fun configureUI(test: Test) {}

}
