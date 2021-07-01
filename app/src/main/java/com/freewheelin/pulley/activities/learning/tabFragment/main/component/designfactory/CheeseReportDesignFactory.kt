package com.freewheelin.pulley.activities.learning.tabFragment.main.component.designfactory

import android.content.Context
import android.widget.LinearLayout
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.DessertType
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.utils.toPx
import kotlin.*

class CheeseReportDesignFactory(listener: ReportDesignListener) : ReportDesignFactory(listener) {
    override fun getIllustResource(): Int {
        return R.drawable.illust_cheese
    }

    override fun getReportTopImageResource(): Int {
        return R.drawable.snack_image_top_cheese
    }

    override fun getReportBottomImageResource(): Int {
        return R.drawable.snack_image_bottom_cheese
    }

    override fun createReportTopUIComponent(context: Context): LinearLayout {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        val firstGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_1_cheese, 542.toPx(), 186.toPx())

        val secondGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_2_cheese, 458.toPx(), 20.toPx())

        val thirdGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_3_cheese, 368.toPx(), 52.toPx())

        layout.addView(firstGuide)
        layout.addView(secondGuide)
        layout.addView(thirdGuide)

        setMarginTop(secondGuide, context.resources.getDimension(R.dimen.dp48).toInt())
        setMarginTop(thirdGuide, context.resources.getDimension(R.dimen.dp32).toInt())

        return layout
    }

    override fun createReportBottomUIComponent(context: Context): LinearLayout {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        val firstGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_1_cheese, 424.toPx(), 186.toPx())

        val secondGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_2_cheese, 493.toPx(), 20.toPx())

        val thirdGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_3_cheese, 224.toPx(), 72.toPx())

        val fourthGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_4_cheese, 457.toPx(), 20.toPx())

        val fifthGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_5_cheese, 224.toPx(), 72.toPx())

        layout.addView(firstGuide)
        layout.addView(secondGuide)
        layout.addView(thirdGuide)
        layout.addView(fourthGuide)
        layout.addView(fifthGuide)

        setMarginTop(secondGuide, context.resources.getDimension(R.dimen.dp48).toInt())
        setMarginTop(thirdGuide, 8.toPx())
        setMarginTop(fourthGuide, context.resources.getDimension(R.dimen.dp24).toInt())
        setMarginTop(fifthGuide, 8.toPx())

        thirdGuide.setOnClickListener {
            LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "스낵보고서", "유형학습", DessertType.CHEESE_BALL.dessertName)
            listener.onUnitStudyBtnClicked()
        }
        fifthGuide.setOnClickListener {
            LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "스낵보고서", "테스트", DessertType.CHEESE_BALL.dessertName)
            listener.onTestBtnClicked()
        }

        return layout
    }

}