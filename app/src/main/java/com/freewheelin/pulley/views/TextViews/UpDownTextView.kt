package com.freewheelin.pulley.views.textViews

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewTvUpdownBinding
import com.freewheelin.pulley.utils.toPx

class UpDownTextView : ConstraintLayout {

    var textSize: Float
        get() = binding.tv.textSize
        set(value) {
            binding.tv.textSize = value
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
                    binding.tv.setTextColor(ContextCompat.getColor(context, R.color.blue_30a4ff))
                    binding.arrowIv.visibility = View.VISIBLE
                    binding.arrowIv.setImageResource(R.drawable.ic_arrow_blue_top)
                }

                Change.decrease -> {
                    binding.tv.setTextColor(ContextCompat.getColor(context, R.color.red_fe7b67))
                    binding.arrowIv.visibility = View.VISIBLE
                    binding.arrowIv.setImageResource(R.drawable.ic_arrow_red_bottom)
                }

                Change.noChange -> {
                    binding.tv.setTextColor(ContextCompat.getColor(context, R.color.grey_c0c0c0))
                    binding.arrowIv.visibility = View.GONE
                }
            }
        }

    var text: String
        get() = binding.tv.text.toString()
        set(value) {
            binding.tv.text = value
        }
    var binding: ViewTvUpdownBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_tv_updown, this, true)

    init {
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array= context.obtainStyledAttributes(attrs, R.styleable.UpDownTextView)
        binding.tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, array.getDimension(R.styleable.UpDownTextView_udTv_textSize, 16f.toPx()))
        array.recycle()
    }
}