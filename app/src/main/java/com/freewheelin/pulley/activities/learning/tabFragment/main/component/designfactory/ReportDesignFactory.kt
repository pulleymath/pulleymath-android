package com.freewheelin.pulley.activities.learning.tabFragment.main.component.designfactory

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.freewheelin.pulley.assets.DessertType
import com.freewheelin.pulley.bases.is10InchUI
import com.freewheelin.pulley.utils.setImageResourceWithSpecificSize
interface ReportDesignListener {
    fun onAnalysisBtnClicked()
    fun onTestBtnClicked()
    fun onWrongNoteBtnClicked()
    fun onUnitStudyBtnClicked()
    fun onMockExamBtnClicked()
}
abstract class ReportDesignFactory(val listener: ReportDesignListener) {

    companion object {
        fun createReportDesignFactory(type: DessertType, listener: ReportDesignListener): ReportDesignFactory {
            return when(type) {
                DessertType.BANANA -> BananaReportDesignFactory(listener)
                DessertType.LEMONADE -> LemonadeReportDesignFactory(listener)
                DessertType.CANDY -> CandyReportDesignFactory(listener)
                DessertType.CHEESE_BALL -> CheeseReportDesignFactory(listener)
                DessertType.MACAROON -> MacaroonReportDesignFactory(listener)
                DessertType.PIE -> PieReportDesignFactory(listener)
            }
        }
    }

    fun getScaleFactor(context: Context): Float {
        return if(context.is10InchUI) return 1f else 0.7f
    }

    fun getScaleSize(context: Context, size: Int): Int {
        return (size * getScaleFactor(context)).toInt()
    }

    abstract fun getIllustResource(): Int

    abstract fun getReportTopImageResource(): Int

    abstract fun getReportBottomImageResource(): Int

    abstract fun createReportTopUIComponent(context: Context): LinearLayout

    abstract fun createReportBottomUIComponent(context: Context): LinearLayout

    fun createImageViewWithResourceSize(context: Context, res: Int, widget: Int, height: Int): ImageView {
        return ImageView(context).apply {
            setImageResourceWithSpecificSize(res, getScaleSize(context, widget), getScaleSize(context, height))
            scaleType = ImageView.ScaleType.FIT_START
        }
    }

    fun setMarginTop(imageView: ImageView, size: Int) {
        val lp = imageView.layoutParams as ViewGroup.MarginLayoutParams
        lp.topMargin = size
        imageView.layoutParams = lp
    }
}