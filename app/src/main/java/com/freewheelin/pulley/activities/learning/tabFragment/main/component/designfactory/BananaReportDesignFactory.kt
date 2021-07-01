package com.freewheelin.pulley.activities.learning.tabFragment.main.component.designfactory

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.DessertType
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.utils.setImageResourceWithSpecificSize
import com.freewheelin.pulley.utils.toPx


class BananaReportDesignFactory(listener: ReportDesignListener): ReportDesignFactory(listener) {

    override fun getIllustResource(): Int {
        return R.drawable.illust_banana
    }

    override fun getReportTopImageResource(): Int {
        return R.drawable.snack_image_top_banana
    }

    override fun getReportBottomImageResource(): Int {
        return R.drawable.snack_image_bottom_banana
    }

    override fun createReportTopUIComponent(context: Context): LinearLayout {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL


        val guideImageView = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_banana, 435.toPx(), 186.toPx())

        val firstGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_1_banana, 195.toPx(), 20.toPx())

        val secondGuideBtn = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_2_banana, 224.toPx(), 72.toPx())

        val thirdGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_top_3_banana, 429.toPx(), 52.toPx())

        layout.addView(guideImageView)
        layout.addView(firstGuide)
        layout.addView(secondGuideBtn)
        layout.addView(thirdGuide)

        setMarginTop(firstGuide,  context.resources.getDimension(R.dimen.dp48).toInt())
        setMarginTop(secondGuideBtn, 8.toPx())
        setMarginTop(thirdGuide, context.resources.getDimension(R.dimen.dp24).toInt())

        secondGuideBtn.setOnClickListener {
            LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "스낵보고서", "테스트", DessertType.BANANA.dessertName)
            listener.onTestBtnClicked()
        }

        return layout
    }

    override fun createReportBottomUIComponent(context: Context): LinearLayout {
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL


        val guideImageView = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_banana, 548.toPx(), 160.toPx())

        val firstGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_1_banana, 499.toPx(), 20.toPx())

        val secondGuideBtn = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_2_banana, 224.toPx(), 72.toPx())

        val thirdGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_3_banana, 368.toPx(), 52.toPx())

        val fourthGuide = createImageViewWithResourceSize(context, R.drawable.snack_guide_bottom_4_banana, 224.toPx(), 72.toPx())


        layout.addView(guideImageView)
        layout.addView(firstGuide)
        layout.addView(secondGuideBtn)
        layout.addView(thirdGuide)
        layout.addView(fourthGuide)

        setMarginTop(firstGuide, context.resources.getDimension(R.dimen.dp48).toInt())
        setMarginTop(secondGuideBtn, 8.toPx())
        setMarginTop(thirdGuide, context.resources.getDimension(R.dimen.dp24).toInt())
        setMarginTop(fourthGuide, 8.toPx())

        secondGuideBtn.setOnClickListener {
            LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "스낵보고서", "테스트2", DessertType.BANANA.dessertName)
            listener.onTestBtnClicked()
        }

        fourthGuide.setOnClickListener {
            LogUtils.logEvent(context, user!!, PulleyEvent.BUTTON_CLICK, "스낵보고서", "유형학습", DessertType.BANANA.dessertName)
            listener.onUnitStudyBtnClicked()
        }

        return layout
    }
}