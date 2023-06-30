package com.freewheelin.pulley.legacy.activities.learning.tabFragment.snackTest.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.model.contents.Test

class WeeklyTestSelectorView : TestSelectorView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    var tagTv: TextView
    var guideTv: TextView
    var titleTv: TextView
    var needMoreTv: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_selector_weekly_test, this)
        tagTv = findViewById(R.id.tagTv)
        guideTv = findViewById(R.id.guideTv)
        titleTv = findViewById(R.id.titleTv)
        needMoreTv = findViewById(R.id.needMoreTv)
    }

    override fun setTestUI(test: Test) {
        if (test.isCompleted()) {
            titleTv.text = "주간 테스트 완료"
            titleTv.setTextColor(ContextCompat.getColor(context, R.color.green_300))
            guideTv.text = "다음 주간 테스트는\n" +
                    "토요일 오전 6시에 공개됩니다 :)"
            needMoreTv.visibility = View.INVISIBLE
        } else {
            titleTv.text = "주간 테스트"
            titleTv.setTextColor(ContextCompat.getColor(context, R.color.gray_800))
            guideTv.text = "이번 주 공부한 내용을 점검할 시간!\n" +
                    "토-일, 주말에만 응시가능합니다."
            needMoreTv.visibility = View.INVISIBLE
            if(test.isPossibleToSolve() == false) {
                needMoreTv.visibility = View.VISIBLE
                needMoreTv.text = "${30 - test.weeklyInfo.weeklyProblemCount}문제 더 풀면 오픈!"
            }
        }
    }

    override fun toEnableUI() {
        tagTv.background = ContextCompat.getDrawable(context, R.drawable.bg_yellow_300_round)
        titleTv.setTextColor(ContextCompat.getColor(context, R.color.gray_800))
        guideTv.setTextColor(ContextCompat.getColor(context, R.color.gray_800))
    }

    override fun toDisableUI() {
        tagTv.background = ContextCompat.getDrawable(context, R.drawable.bg_gray_400_round)
        titleTv.setTextColor(ContextCompat.getColor(context, R.color.gray_500))
        guideTv.setTextColor(ContextCompat.getColor(context, R.color.gray_500))
        guideTv.text = "다음 주간 테스트는\n토요일 오전 6시에 공개됩니다 :)"
    }
}