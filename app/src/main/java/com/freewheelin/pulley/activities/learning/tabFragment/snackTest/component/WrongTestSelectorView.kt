package com.freewheelin.pulley.activities.learning.tabFragment.snackTest.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.model.contents.Test
import kotlinx.android.synthetic.main.view_selector_wrong_test.view.*

class WrongTestSelectorView: TestSelectorView {
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_selector_wrong_test, this)
    }

    override fun setTestUI(test: Test) {
        if(isNeedToFinishUI(test)) {
            titleTv.text = "오답 테스트"
            titleTv.setTextColor(ContextCompat.getColor(context, R.color.green_70d000))
            guideTv.text = "오답 테스트는\n무제한 응시 가능합니다 :)"
            checkIv.visibility = View.VISIBLE
        } else {
            titleTv.text = "오답 테스트"
            titleTv.setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
            guideTv.text = "완벽을 위한 무제한 응시!\n여러번 반복해서 빈틈없는 실력을 만들어요 :)"
            checkIv.visibility = View.INVISIBLE
        }
    }

    private fun isNeedToFinishUI(test: Test): Boolean {
        return test.scoringTestPieceCount > 0
    }
}