package com.freewheelin.pulley.views.bars

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewBarPenBinding
import com.freewheelin.pulley.utils.TextUtils

class PenBar : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setTypedArray(attrs)
    }

    companion object {
        const val grey = 0
        const val red = 1
        const val yellow = 2
        const val green = 3
        const val blue = 4
    }

    var progressColor: Int = ContextCompat.getColor(context, R.color.grey_e0e0e0)
        set(value) {
            field = value
            val drawableResource = when(value) {
                grey -> R.drawable.bg_grey_e0e0e0_round_2
                red -> R.drawable.bg_red_fe7b67_round_2
                yellow -> R.drawable.bg_yellow_ffd545_round_2
                green -> R.drawable.bg_green_70d000_round_2
                blue -> R.drawable.bg_blue_30a4ff_round_2
                else -> R.drawable.bg_grey_e0e0e0_round_2
            }
            binding.progressBar.background = ContextCompat.getDrawable(context, drawableResource)
        }


    var value: Float = 0f
        set(value) {
            field = value
            binding.valueTv.text = TextUtils.percentFormat.format(value)
            val layoutParams = binding.progressBar.layoutParams as LinearLayout.LayoutParams
            layoutParams.weight = value * 100
        }

    var title: String
        get() = binding.titleTv.text.toString()
        set(value) {
            binding.titleTv.text = value
        }
    var binding: ViewBarPenBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_bar_pen, this, true)

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.PenBar)

        progressColor = array.getInt(R.styleable.PenBar_progressColorCase, 0)
        value = array.getFloat(R.styleable.PenBar_value, 0f)
    }

    fun setValue(value: Float, withColor: Boolean = false) {
        this.value = value

        if(withColor) {
            progressColor = if (value in 0f..0.5f)
                red
            else if(value >0.5f && value <=0.8f)
                yellow
            else
                green
        }
    }
}