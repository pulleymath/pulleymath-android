package com.freewheelin.pulley.activities.learning.tabFragment.snackTest.component

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.model.contents.Test
import kotlinx.android.synthetic.main.view_selector_daily_test.view.*


abstract class TestSelectorView: ConstraintLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    var isEnableUI: Boolean = false
    set(value) {
        field = value

        if(value) {
            toEnableUI()
        } else {
            toDisableUI()
        }
    }


    open fun toEnableUI() {
        tagTv?.background = ContextCompat.getDrawable(context, R.drawable.bg_yellow_ffb300_round)
        titleTv?.setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
        guideTv?.setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
    }

    open fun toDisableUI() {
        tagTv?.background = ContextCompat.getDrawable(context, R.drawable.bg_grey_e0e0e0_round)
        titleTv?.setTextColor(ContextCompat.getColor(context, R.color.grey_c0c0c0))
        guideTv?.setTextColor(ContextCompat.getColor(context, R.color.grey_c0c0c0))
    }

    fun setUpUI(test: Test?) {
        isEnableUI = test != null

        if(test != null) {
            setTestUI(test)
        }
    }

    abstract fun setTestUI(test: Test)
}