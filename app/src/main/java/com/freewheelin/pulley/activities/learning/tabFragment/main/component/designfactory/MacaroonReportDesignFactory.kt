package com.freewheelin.pulley.activities.learning.tabFragment.main.component.designfactory

import android.content.Context
import android.widget.LinearLayout
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.DessertType
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.utils.toPx

class MacaroonReportDesignFactory(listener: ReportDesignListener) : ReportDesignFactory(listener) {
    override fun getIllustResource(): Int {
        return R.drawable.illust_macaron
    }

    override fun getReportTopImageResource(): Int {
        return R.drawable.snack_image_top_macaroon
    }

    override fun getReportBottomImageResource(): Int {
        return R.drawable.snack_image_bottom_macaroon
    }

    override fun createReportTopUIComponent(context: Context): LinearLayout {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        val firstGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_1_macaroon, 435.toPx(), 186.toPx())

        val secondGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_2_macaroon, 195.toPx(), 20.toPx())

        val thirdGuideBtn = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_3_macaroon, 224.toPx(), 72.toPx())

        val fourthGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_4_macaroon, 429.toPx(), 52.toPx())

        layout.addView(firstGuide)
        layout.addView(secondGuide)
        layout.addView(thirdGuideBtn)
        layout.addView(fourthGuide)

        setMarginTop(secondGuide, context.resources.getDimension(R.dimen.dp48).toInt())
        setMarginTop(thirdGuideBtn,8.toPx())
        setMarginTop(fourthGuide, context.resources.getDimension(R.dimen.dp24).toInt())

        thirdGuideBtn.setOnClickListener {
            LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "스낵보고서", "테스트", DessertType.MACAROON.dessertName)
            listener.onTestBtnClicked()
        }

        return layout
    }

    override fun createReportBottomUIComponent(context: Context): LinearLayout {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        val firstGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_1_macaroon, 548.toPx(), 160.toPx())

        val secondGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_2_macaroon, 499.toPx(), 20.toPx())

        val thirdGuideBtn = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_3_macaroon, 224.toPx(), 72.toPx())

        val fourthGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_4_macaroon, 368.toPx(), 52.toPx())

        val fifthGuideBtn = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_5_macaroon, 224.toPx(), 72.toPx())

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
            LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "스낵보고서", "테스트2", DessertType.MACAROON.dessertName)
            listener.onTestBtnClicked()
        }
        fifthGuideBtn.setOnClickListener {
            LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "스낵보고서", "유형학습", DessertType.MACAROON.dessertName)
            listener.onUnitStudyBtnClicked()
        }
        return layout
    }
}