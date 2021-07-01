package com.freewheelin.pulley.activities.learning.tabFragment.main.component.designfactory

import android.content.Context
import android.widget.LinearLayout
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.DessertType
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.utils.toPx

class LemonadeReportDesignFactory(listener: ReportDesignListener): ReportDesignFactory(listener) {
    override fun getIllustResource(): Int {
        return R.drawable.illust_lemonade
    }

    override fun getReportTopImageResource(): Int {
        return R.drawable.snack_image_top_lemonade
    }

    override fun getReportBottomImageResource(): Int {
        return R.drawable.snack_image_bottom_lemonade
    }

    override fun createReportTopUIComponent(context: Context): LinearLayout {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        val guideImageView = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_1_lemonade, 414.toPx(), 186.toPx())

        val firstGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_2_lemonade, 378.toPx(), 20.toPx())

        val secondGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_3_lemonade, 582.toPx(), 20.toPx())

        val thirdGuideBtn = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_4_lemonade, 224.toPx(), 72.toPx())

        layout.addView(guideImageView)
        layout.addView(firstGuide)
        layout.addView(secondGuide)
        layout.addView(thirdGuideBtn)

        setMarginTop(firstGuide, context.resources.getDimension(R.dimen.dp48).toInt())
        setMarginTop(secondGuide, context.resources.getDimension(R.dimen.dp32).toInt())
        setMarginTop(thirdGuideBtn,8.toPx())

        thirdGuideBtn.setOnClickListener {
            LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "스낵보고서", "모의고사", DessertType.LEMONADE.dessertName)
            listener.onMockExamBtnClicked()
        }

        return layout
    }

    override fun createReportBottomUIComponent(context: Context): LinearLayout {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        val firstGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_1_lemonade, 490.toPx(), 186.toPx())

        val secondGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_2_lemonade, 412.toPx(), 20.toPx())

        val thirdGuideBtn = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_3_lemonade, 224.toPx(), 72.toPx())

        val fourthGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_4_lemonade, 489.toPx(), 20.toPx())

        val fifthGuideBtn = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_5_lemonade, 224.toPx(), 72.toPx())

        layout.addView(firstGuide)
        layout.addView(secondGuide)
        layout.addView(thirdGuideBtn)
        layout.addView(fourthGuide)
        layout.addView(fifthGuideBtn)

        setMarginTop(secondGuide, context.resources.getDimension(R.dimen.dp48).toInt())
        setMarginTop(thirdGuideBtn, 8.toPx())
        setMarginTop(fourthGuide, context.resources.getDimension(R.dimen.dp24).toInt())
        setMarginTop(fifthGuideBtn, 8.toPx())

        thirdGuideBtn.setOnClickListener {
            LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "스낵보고서", "유형학습", DessertType.LEMONADE.dessertName)
            listener.onUnitStudyBtnClicked()
        }
        fifthGuideBtn.setOnClickListener {
            LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "스낵보고서", "오답노트", DessertType.LEMONADE.dessertName)
            listener.onWrongNoteBtnClicked()
        }

        return layout
    }

}