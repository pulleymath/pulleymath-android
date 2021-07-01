package com.freewheelin.pulley.activities.learning.tabFragment.main.component.designfactory

import android.content.Context
import android.widget.LinearLayout
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.DessertType
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.utils.toPx

class CandyReportDesignFactory(listener: ReportDesignListener): ReportDesignFactory(listener) {
    override fun getIllustResource(): Int {
        return R.drawable.illust_candy
    }

    override fun getReportTopImageResource(): Int {
        return R.drawable.snack_image_top_candy
    }

    override fun getReportBottomImageResource(): Int {
        return R.drawable.snack_image_bottom_candy
    }

    override fun createReportTopUIComponent(context: Context): LinearLayout {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        val guideImageView = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_1_candy, 589.toPx(), 186.toPx())

        val firstGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_2_candy, 382.toPx(), 52.toPx())

        val secondGuideBtn = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_3_candy, 224.toPx(), 72.toPx())

        val thirdGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_4_candy, 422.toPx(), 52.toPx())

        val fourthGuideBtn = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_5_candy, 224.toPx(), 72.toPx())

        layout.addView(guideImageView)
        layout.addView(firstGuide)
        layout.addView(secondGuideBtn)
        layout.addView(thirdGuide)
        layout.addView(fourthGuideBtn)

        setMarginTop(firstGuide, context.resources.getDimension(R.dimen.dp48).toInt())
        setMarginTop(secondGuideBtn, 8.toPx())
        setMarginTop(thirdGuide, context.resources.getDimension(R.dimen.dp24).toInt())
        setMarginTop(fourthGuideBtn, 8.toPx())

        secondGuideBtn.setOnClickListener {
            LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "스낵보고서", "오답노트", DessertType.CANDY.dessertName)
            listener.onWrongNoteBtnClicked()
        }
        fourthGuideBtn.setOnClickListener {
            LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "스낵보고서", "오답노트2", DessertType.CANDY.dessertName)
            listener.onWrongNoteBtnClicked()
        }

        return layout
    }

    override fun createReportBottomUIComponent(context: Context): LinearLayout {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        val guideImageView = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_1_candy, 373.toPx(), 186.toPx())

        val firstGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_2_candy, 352.toPx(), 20.toPx())

        val secondGuideBtn = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_3_candy, 224.toPx(), 72.toPx())

        val thirdGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_4_candy, 470.toPx(), 20.toPx())

        layout.addView(guideImageView)
        layout.addView(firstGuide)
        layout.addView(secondGuideBtn)
        layout.addView(thirdGuide)

        setMarginTop(firstGuide, context.resources.getDimension(R.dimen.dp48).toInt())
        setMarginTop(secondGuideBtn, 8.toPx())
        setMarginTop(thirdGuide, context.resources.getDimension(R.dimen.dp24).toInt())

        secondGuideBtn.setOnClickListener {
            LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "스낵보고서", "유형학습", DessertType.CANDY.dessertName)
            listener.onUnitStudyBtnClicked()
        }

        return layout
    }

}