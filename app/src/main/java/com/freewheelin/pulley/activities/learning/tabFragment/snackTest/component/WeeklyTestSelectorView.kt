package com.freewheelin.pulley.activities.learning.tabFragment.snackTest.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.model.contents.Test
import kotlinx.android.synthetic.main.view_selector_daily_test.view.*
import kotlinx.android.synthetic.main.view_selector_daily_test.view.guideTv
import kotlinx.android.synthetic.main.view_selector_daily_test.view.titleTv
import kotlinx.android.synthetic.main.view_selector_weekly_test.view.*

class WeeklyTestSelectorView : TestSelectorView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_selector_weekly_test, this)
    }

    override fun setTestUI(test: Test) {
        if (test.isCompleted()) {
            titleTv.text = "주간 테스트 완료"
            titleTv.setTextColor(ContextCompat.getColor(context, R.color.green_70d000))
            guideTv.text = "다음 주간 테스트는\n" +
                    "토요일 오전 6시에 공개됩니다 :)"
            needMoreTv.visibility = View.INVISIBLE
        } else {
            titleTv.text = "주간 테스트"
            titleTv.setTextColor(ContextCompat.getColor(context, R.color.black_4c4c4c))
            guideTv.text = "이번 주 공부한 내용을 점검할 시간!\n" +
                    "토-일, 주말에만 응시가능합니다."
            needMoreTv.visibility = View.INVISIBLE
            if(test.isPossibleToSolve() == false) {
                needMoreTv.visibility = View.VISIBLE
                needMoreTv.text = "${30 - test.weeklyInfo.weeklyProblemCount}문제 더 풀면 오픈!"
            }
        }
    }

    override fun toDisableUI() {
        super.toDisableUI()
        guideTv.text = "다음 주간 테스트는\n토요일 오전 6시에 공개됩니다 :)"
    }
}