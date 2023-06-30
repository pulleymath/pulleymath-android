package com.freewheelin.pulley.legacy.activities.learning.tabFragment.snackTest

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.core.manage.TestManager
import com.freewheelin.pulley.legacy.model.contents.Test


interface TestMainBaseListener {
    fun onReviewBtnClicked(test: Test)
    fun onSolveBtnClicked(test: Test)
    fun onSettingBtnClicked(test: Test)
    fun onReportBtnClicked(test: Test, fromGift: Boolean = false)
    fun onMoveBtnClikced(test: Test)
}

abstract class TestMainBaseFragment: Fragment() {
    var isHiding = false
    var listener: TestMainBaseListener? = null
    val duration: Long = 200
    abstract var test: Test?
    abstract var testType: Test.TestType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        test = arguments?.getSerializable(TestManager.ARG_TEST) as? Test
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showMainContents()
    }

    abstract fun showMainContents()
    abstract fun hideMainContents(cb: () -> Unit)

    fun add(manager: FragmentManager) {
        val transaction = manager.beginTransaction()
        transaction.replace(R.id.mainContentsCl, this)

        transaction.commitAllowingStateLoss()
    }

    fun remove(manager: FragmentManager) {
        if(isHiding || context == null)
            return

        isHiding = true
        hideMainContents {
            val tran = manager.beginTransaction()
            tran.remove(this)
            tran.commit()
        }

    }

    open fun tick() {}

    abstract fun configureUI(test: Test)
}