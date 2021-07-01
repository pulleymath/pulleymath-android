package com.freewheelin.pulley.views.Buttons

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.ScreenTheme
import kotlinx.android.synthetic.main.item_study_plan_add.view.*

class DarkImageButton: ImageButton {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    var theme = ScreenTheme.Dark
        set(value) {
            field = value
            this.isSelected = this.isSelected
       }

    init {
        this.background = ContextCompat.getDrawable(context, R.drawable.bg_grey_3d3d3d_stroke_black_4c4c4c_round_18)
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        this.background = getBackgroundDrawble(selected)
    }

    fun getBackgroundDrawble(isSelected: Boolean): Drawable? {
        when(theme) {
            ScreenTheme.Bright -> {
                if(isSelected) {
                    return ContextCompat.getDrawable(context, R.drawable.bg_purple_ecebff_stroke_purple_acacff_round_18)
                } else  {
                    return ContextCompat.getDrawable(context, R.drawable.bg_white_fafafa_stroke_grey_e8e8e8_round_18)
                }
            }
            ScreenTheme.Dark -> {
                if(isSelected) {
                    return ContextCompat.getDrawable(context, R.drawable.bg_purple_acacff_round_18)
                } else  {
                    return ContextCompat.getDrawable(context, R.drawable.bg_grey_3d3d3d_stroke_black_4c4c4c_round_18)
                }
            }
        }

    }
}