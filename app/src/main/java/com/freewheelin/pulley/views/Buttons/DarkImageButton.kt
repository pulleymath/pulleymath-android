package com.freewheelin.pulley.views.buttons

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.ScreenTheme

class DarkImageButton: ImageButton {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    var theme = ScreenTheme.Dark
        set(value) {
            field = value
            this.isSelected = this.isSelected
       }

    init {
        this.background = ContextCompat.getDrawable(context, R.drawable.bg_black_100_stroke_gray_800_round_18)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        this.background = getBackgroundDrawble(selected)
    }

    fun getBackgroundDrawble(isSelected: Boolean): Drawable? {
        when(theme) {
            ScreenTheme.Bright, ScreenTheme.BrightOutside-> {
                if(isSelected) {
                    return ContextCompat.getDrawable(context, R.drawable.bg_purple_100_stroke_purple_200_round_18)
                } else  {
                    return ContextCompat.getDrawable(context, R.drawable.bg_gray_100_stroke_gray_300_round_18)
                }
            }
            ScreenTheme.Dark -> {
                if(isSelected) {
                    return ContextCompat.getDrawable(context, R.drawable.bg_purple_200_round_18)
                } else  {
                    return ContextCompat.getDrawable(context, R.drawable.bg_black_100_stroke_gray_800_round_18)
                }
            }
        }

    }
}