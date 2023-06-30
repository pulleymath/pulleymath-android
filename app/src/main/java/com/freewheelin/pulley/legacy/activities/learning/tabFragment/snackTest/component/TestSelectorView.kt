package com.freewheelin.pulley.legacy.activities.learning.tabFragment.snackTest.component

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.freewheelin.pulley.legacy.model.contents.Test


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

    fun setUpUI(test: Test?) {
        isEnableUI = test != null

        if(test != null) {
            setTestUI(test)
        }
    }

    abstract fun toEnableUI()
    abstract fun toDisableUI()
    abstract fun setTestUI(test: Test)
}