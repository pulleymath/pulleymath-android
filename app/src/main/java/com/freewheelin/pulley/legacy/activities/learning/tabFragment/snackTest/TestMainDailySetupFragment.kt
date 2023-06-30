package com.freewheelin.pulley.legacy.activities.learning.tabFragment.snackTest


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.core.manage.TestManager
import com.freewheelin.pulley.databinding.FragmentTestMainDailySetupBinding
import com.freewheelin.pulley.legacy.model.contents.Test
import com.freewheelin.pulley.legacy.utils.hide
import com.freewheelin.pulley.legacy.utils.show


class TestMainDailySetupFragment : TestMainBaseFragment() {
    lateinit var binding: FragmentTestMainDailySetupBinding
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
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_test_main_daily_setup, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupBtn.setOnClickListener {
            listener?.onSettingBtnClicked(test!!)
        }
    }

    override fun showMainContents() {
        binding.guideTv.show(duration)
        binding.setupBtn.show(duration)
    }

    override fun hideMainContents(cb: () -> Unit) {
        binding.guideTv.hide(duration)
        binding.setupBtn.hide(duration)
    }

    override fun configureUI(test: Test) {}

}
