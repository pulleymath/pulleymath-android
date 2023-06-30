package com.freewheelin.pulley.revision2023.ui.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R

class TertiaryButton: CommonButton {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setTypedArray(attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        setTypedArray(attrs)
    }

    override fun getDisabledBgByType(): Int {
        return R.drawable.bg_gray_300_round_disabled
    }
    override fun getEnabledBgByType(): Int {
        return R.drawable.bg_gray_300_round_ripple
    }
    override fun getContentsColorByType(): Int {
        return R.color.gray_800
    }
    override fun getContentsDisabledColorByType(): Int {
        return R.color.gray_800_disabled
    }

    override fun setTextColorFromTypedArray(array: TypedArray) {
        val textColor = array.getColor(R.styleable.CommonButton_android_textColor, ContextCompat.getColor(context, R.color.gray_800))
        title.setTextColor(textColor)
    }

}
