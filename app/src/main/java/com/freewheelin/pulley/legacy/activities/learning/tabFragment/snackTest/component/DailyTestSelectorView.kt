package com.freewheelin.pulley.legacy.activities.learning.tabFragment.snackTest.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.legacy.bases.isMobile
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.model.contents.Test

class DailyTestSelectorView: TestSelectorView {
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    var tagTv: TextView
    var guideTv: TextView
    var titleTv: TextView

    private var firstTestIv: ImageView
    private var secondTestIv: ImageView
    private var thirdTestIv: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_selector_daily_test, this)
        tagTv = findViewById(R.id.tagTv)
        guideTv = findViewById(R.id.guideTv)
        titleTv = findViewById(R.id.titleTv)
        firstTestIv = findViewById(R.id.firstTestIv)
        secondTestIv = findViewById(R.id.secondTestIv)
        thirdTestIv = findViewById(R.id.thirdTestIv)
    }

    override fun setTestUI(test: Test) {
        firstTestIv.setImageResource(R.drawable.ic_1_grey_24)
        secondTestIv.setImageResource(R.drawable.ic_2_grey_24)
        thirdTestIv.setImageResource(R.drawable.ic_3_grey_24)

        if(test.scoringTestPieceCount >= 1)
            firstTestIv.setImageResource(R.drawable.ic_check_green_circle_24)

        if(test.scoringTestPieceCount >= 2)
            secondTestIv.setImageResource(R.drawable.ic_check_green_circle_24)

        if(test.scoringTestPieceCount >= 3)
            thirdTestIv.setImageResource(R.drawable.ic_check_green_circle_24)

        if(test.isCompleted()) {
            titleTv.text = "데일리 테스트 완료!"
            titleTv.setTextColor(ContextCompat.getColor(context, R.color.green_300))
            guideTv.text = "다음 데일리 테스트가 공개되는\n" +
                    "내일 오전 6시에 또 만나요!"
        } else {
            titleTv.text = "데일리 테스트"
            titleTv.setTextColor(ContextCompat.getColor(context, R.color.gray_800))
            guideTv.text = if(test.scoringTestPieceCount == 0) "5문제 데일리 테스트는 하루에 딱 3번만 풀 수 있어요!\n지금 바로 풀어볼까요?"
                else "응시할수록 데이터가 쌓여\n나에게 꼭 필요한 문제를 제공해요 :)"
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
        guideTv.text = "다음 데일리 테스트는\n월요일 오전 6시에 공개됩니다 :)"
    }
}