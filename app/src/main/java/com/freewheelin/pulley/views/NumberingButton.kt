package com.freewheelin.pulley.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.Button
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R

interface NumberingButtonListener {
    fun onNumberingButtonClicked(button: NumberingButton, isSelected: Boolean)
}

class NumberingButton: Button {
    companion object {
        val THEME_WHITE = 0
        val THEME_BLACK = 1
        val THEME_OMR = 2
        val THEME_GREY = 3
        val THEME_RED = 4
    }
    var listener: NumberingButtonListener? = null
    var theme = NumberingButton.THEME_WHITE
    set(value) {
        field = value
        setBackgroundResource(getBgResource(isSelected))
        setTextColor(getTextColor(isSelected))
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
    }

    constructor(context: Context): super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setTypedArray(attrs)
        initView()
        if (text == "4")
            this.setPadding(paddingLeft, paddingTop, 1, paddingBottom)
        if (text == "5")
            this.setPadding(paddingLeft, paddingTop, 2, paddingBottom)

    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int): super(context, attrs, defStyle)


    fun initView() {
        setPadding(0,0,0,0)
        this.setOnClickListener {
            isSelected = !isSelected
            listener?.onNumberingButtonClicked(this, this.isSelected)
        }
    }

    fun setOnNumberingButtonListener(listener: NumberingButtonListener) {
        this.listener = listener
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.NumberingButton)
        theme = array.getInt(R.styleable.NumberingButton_NumberingTheme, THEME_WHITE)
        array.recycle()
    }

    private fun getTextColor(isSelected: Boolean):Int {
        return when(theme) {
            THEME_WHITE -> {
                if (isSelected)
                    ContextCompat.getColor(context, R.color.black_200)
                else
                    ContextCompat.getColor(context, R.color.gray_200)
            }
            THEME_BLACK -> {
                if(isSelected)
                    ContextCompat.getColor(context, R.color.white)
                else
                    ContextCompat.getColor(context, R.color.gray_800)
            }
            THEME_OMR -> {
                if(isSelected)
                    ContextCompat.getColor(context, R.color.gray_700)
                else
                    ContextCompat.getColor(context, R.color.red_200)
            }
            THEME_GREY -> {
                if (isSelected)
                    ContextCompat.getColor(context, R.color.white)
                else
                    ContextCompat.getColor(context, R.color.gray_400)
            }
            THEME_RED -> {
                if (isSelected)
                    ContextCompat.getColor(context, R.color.red_300)
                else
                    ContextCompat.getColor(context, R.color.red_300)
            }
            else -> {
                if (isSelected)
                    ContextCompat.getColor(context, R.color.black_200)
                else
                    ContextCompat.getColor(context, R.color.gray_200)
            }

        }
    }

    private fun getBgResource(isSelected: Boolean): Int {
        return when(theme) {
            THEME_WHITE -> {
                if (isSelected)
                    R.drawable.bg_gray_200_circle
                else
                    R.drawable.bg_transparent_stroke_gray_200_circle
            }
            THEME_BLACK -> {
                if(isSelected)
                    R.drawable.bg_gray_800_circle
                else
                    R.drawable.bg_transparent_stroke_gray_800_circle
            }
            THEME_OMR -> {
                if(isSelected)
                    R.drawable.bg_gray_700_round_8
                else
                    R.drawable.bg_yellow_200_stroke_2_red_100_round_8
            }
            THEME_GREY -> {
                if (isSelected)
                    R.drawable.bg_gray_400_circle
                else
                    R.drawable.bg_transparent_stroke_gray_400_circle
            }
            THEME_RED -> {
                if (isSelected)
                    R.drawable.bg_transparent_stroke_red_300_circle
                else
                    R.drawable.bg_transparent_stroke_red_300_circle
            }
            else -> {
                if (isSelected)
                    ContextCompat.getColor(context, R.color.black_200)
                else
                    ContextCompat.getColor(context, R.color.white)
            }
        }
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        this.setBackgroundResource(getBgResource(isSelected))
        this.setTextColor(getTextColor(isSelected))
    }
}