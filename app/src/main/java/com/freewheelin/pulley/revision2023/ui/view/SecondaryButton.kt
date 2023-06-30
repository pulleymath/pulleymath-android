package com.freewheelin.pulley.revision2023.ui.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R

class SecondaryButton: CommonButton {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setTypedArray(attrs)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        setTypedArray(attrs)
    }


//    override fun setEnabled(enabled: Boolean) {
//        super.setEnabled(enabled)
//        if (enabled) {
//            rootCl.setBackgroundResource(getEnabledBgByType())
//            val contentsColor = getContentsColorByType()
//            setIconColor(contentsColor)
//            setTextColor(contentsColor)
//        } else {
//            rootCl.setBackgroundResource(getDisabledBgByType())
//            val contentsColor = getContentsDisabledColorByType()
//            setIconColor(contentsColor)
//            setTextColor(contentsColor)
//        }
//    }

    override fun getDisabledBgByType(): Int {
        return R.drawable.bg_purple_100_round_disabled
    }
    override fun getEnabledBgByType(): Int {
        return R.drawable.bg_purple_100_round_ripple
    }
    override fun getContentsColorByType(): Int {

        return R.color.purple_300
    }
    override fun getContentsDisabledColorByType(): Int {
        return R.color.purple_300_disabled
    }
    override fun setTextColorFromTypedArray(array: TypedArray) {
        val textColor = array.getColor(R.styleable.CommonButton_android_textColor, ContextCompat.getColor(context, R.color.purple_300))
        title.setTextColor(textColor)
    }
}
