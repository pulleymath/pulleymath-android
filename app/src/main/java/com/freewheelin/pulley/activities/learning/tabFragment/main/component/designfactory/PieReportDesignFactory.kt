package com.freewheelin.pulley.activities.learning.tabFragment.main.component.designfactory

import android.content.Context
import android.widget.LinearLayout
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.DessertType
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.utils.toPx

class PieReportDesignFactory(listener: ReportDesignListener) : ReportDesignFactory(listener) {
    override fun getIllustResource(): Int {
        return R.drawable.illust_pie
    }

    override fun getReportTopImageResource(): Int {
        return R.drawable.snack_image_top_pie
    }

    override fun getReportBottomImageResource(): Int {
        return R.drawable.snack_image_bottom_pie
    }

    override fun createReportTopUIComponent(context: Context): LinearLayout {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        val firstGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_1_pie, 468.toPx(), 212.toPx())

        val secondGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_2_pie, 377.toPx(), 20.toPx())

        val thirdGuideBtn = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_3_pie, 224.toPx(), 72.toPx())

        val fourthGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_4_pie, 480.toPx(), 20.toPx())

        val fifthGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_5_pie, 435.toPx(), 20.toPx())

        val sixthGuideBtn = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_6_pie, 224.toPx(), 72.toPx())

        layout.addView(firstGuide)
        layout.addView(secondGuide)
        layout.addView(thirdGuideBtn)
        layout.addView(fourthGuide)
        layout.addView(fifthGuide)
        layout.addView(sixthGuideBtn)

        setMarginTop(secondGuide, context.resources.getDimension(R.dimen.dp48).toInt())
        setMarginTop(thirdGuideBtn,8.toPx())
        setMarginTop(fourthGuide, context.resources.getDimension(R.dimen.dp24).toInt())
        setMarginTop(fifthGuide, context.resources.getDimension(R.dimen.dp32).toInt())
        setMarginTop(sixthGuideBtn, 8.toPx())

        thirdGuideBtn.setOnClickListener {
            LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "스낵보고서", "유형학습", DessertType.PIE.dessertName)
            listener.onUnitStudyBtnClicked()
        }
        sixthGuideBtn.setOnClickListener {
            LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "스낵보고서", " 내분석", DessertType.PIE.dessertName)
            listener.onAnalysisBtnClicked()
        }

        return layout
    }

    override fun createReportBottomUIComponent(context: Context): LinearLayout {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        val firstGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_1_pie, 427.toPx(), 186.toPx())

        val secondGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_2_pie, 554.toPx(), 20.toPx())

        val thirdGuideBtn = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_3_pie, 224.toPx(), 72.toPx())

        val fourthGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_4_pie, 480.toPx(), 20.toPx())


        layout.addView(firstGuide)
        layout.addView(secondGuide)
        layout.addView(thirdGuideBtn)
        layout.addView(fourthGuide)

        setMarginTop(secondGuide, context.resources.getDimension(R.dimen.dp48).toInt())
        setMarginTop(thirdGuideBtn, 8.toPx())
        setMarginTop(fourthGuide, context.resources.getDimension(R.dimen.dp24).toInt())

        thirdGuideBtn.setOnClickListener {
            LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "스낵보고서", "오답노트", DessertType.PIE.dessertName)
            listener.onWrongNoteBtnClicked()
        }

        return layout
    }
}