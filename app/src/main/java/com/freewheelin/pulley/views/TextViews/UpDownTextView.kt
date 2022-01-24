package com.freewheelin.pulley.views.TextViews

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.utils.toPx
import kotlinx.android.synthetic.main.view_tv_updown.view.*

class UpDownTextView : ConstraintLayout {

    var textSize: Float
        get() = tv.textSize
        set(value) {
            tv.textSize = value
        }
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setTypedArray(attrs)
    }

    enum class Change {
        increase,
        decrease,
        noChange
    }

    var change = Change.increase
        set(value) {
            field = value
            when (value) {
                Change.increase -> {
                    tv.setTextColor(ContextCompat.getColor(context, R.color.blue_30a4ff))
                    arrowIv.visibility = View.VISIBLE
                    arrowIv.setImageResource(R.drawable.ic_arrow_blue_top)
                }

                Change.decrease -> {
                    tv.setTextColor(ContextCompat.getColor(context, R.color.red_fe7b67))
                    arrowIv.visibility = View.VISIBLE
                    arrowIv.setImageResource(R.drawable.ic_arrow_red_bottom)
                }

                Change.noChange -> {
                    tv.setTextColor(ContextCompat.getColor(context, R.color.grey_c0c0c0))
                    arrowIv.visibility = View.GONE
                }
            }
        }

    var text: String
        get() = tv.text.toString()
        set(value) {
            tv.text = value
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_tv_updown, this)
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array= context.obtainStyledAttributes(attrs, R.styleable.UpDownTextView)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, array.getDimension(R.styleable.UpDownTextView_udTv_textSize, 16f.toPx()))
        array.recycle()
    }
}