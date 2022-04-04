package com.freewheelin.pulley.activities.learning.tabFragment.snackTest.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.model.contents.Test

class WrongTestSelectorView: TestSelectorView {
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    var guideTv: TextView
    var titleTv: TextView
    var checkIv: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_selector_wrong_test, this)

        guideTv = findViewById(R.id.guideTv)
        titleTv = findViewById(R.id.titleTv)
        checkIv = findViewById(R.id.checkIv)
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

    override fun toEnableUI() {
        titleTv.setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
        guideTv.setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
    }

    override fun toDisableUI() {
        titleTv.setTextColor(ContextCompat.getColor(context, R.color.grey_c0c0c0))
        guideTv.setTextColor(ContextCompat.getColor(context, R.color.grey_c0c0c0))
    }

}