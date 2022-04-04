package com.freewheelin.pulley.activities.learning.tabFragment.snackTest

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.freewheelin.pulley.core.manage.TestManager
import com.freewheelin.pulley.model.contents.Test
import com.freewheelin.pulley.model.curation.TestCuration
import com.freewheelin.pulley.utils.LogUtils

abstract class TestPageBaseFragment: Fragment() {
    val curation: TestCuration
        get() = TestCuration(requireContext())
    abstract var test: Test?


    companion object {
        fun newInstance(test: Test): TestPageBaseFragment {
            return when(test.getTestType()) {
                Test.TestType.wrong -> {
                    if(test.assignID == null)
                        TestPageFragment.newInstance(test)
                    else {
                        if(test.isReStudy == true)
                            TestPageFragment.newInstance(test)
                        else
                            TestPageFinishFragment.newInstance(test)
                    }
                }
                Test.TestType.daily -> {
                    TestPageDailyFragment.newInstance(test)
                }
                Test.TestType.weekly -> {
                   if (test.isCompleted()) {
                        TestPageFinishFragment.newInstance(test)
                    } else {
                        TestPageFragment.newInstance(test)
                    }
                }
                else -> {
                    LogUtils.assert(false, "예상치 못한 경우")
                    TestPageFragment.newInstance(test)
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        test = arguments?.getSerializable(TestManager.ARG_TEST) as Test
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureBy(test!!)
    }

    abstract fun configureBy(test: Test)

    open fun tick() {}
}