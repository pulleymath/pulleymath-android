package com.freewheelin.pulley.legacy.views.textViews

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.TextviewAnalysisCompareBinding
import io.channel.plugin.android.extension.setTint

class AnalysisCompareTextView: LinearLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setTypedArray(attrs)
    }

    var change: UpDownTextView.Change = UpDownTextView.Change.increase
        set(value) {
            field = value
            when(field) {
                UpDownTextView.Change.increase -> {
                    setDiffTextColor(ContextCompat.getColor(context!!, R.color.blue_500))
                    binding.changeIv.setImageResource(R.drawable.ic_up_blue)
                }
                UpDownTextView.Change.decrease -> {
                    setDiffTextColor(ContextCompat.getColor(context!!, R.color.red_300))
                    binding.changeIv.setImageResource(R.drawable.ic_blunt_triangle_bottom_8_6_gray_400)
                    binding.changeIv.setTint(ContextCompat.getColor(context!!, R.color.red_300))
                }
                UpDownTextView.Change.noChange -> {
                    setDiffTextColor(ContextCompat.getColor(context!!, R.color.gray_600))
                    binding.changeIv.setImageDrawable(null)
                }
            }
        }
    var valueText: String?
        get() {
            return binding.valueTv.text.toString()
        }
        set(value) {
            binding.valueTv.text = value
        }

    var diffText: String?
        get() {
            return binding.diffTv.text.toString()
        }
        set(value) {
            binding.diffTv.text = value
        }
    var binding: TextviewAnalysisCompareBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.textview_analysis_compare, this, true)

    init {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
    }

    fun setDiffTextColor(color: Int) {
        binding.diffTv.setTextColor(color)
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.AnalysisCompareTextView)

        binding.topLabel.text = array.getString(R.styleable.AnalysisCompareTextView_AnalysisCompareTextView_TopLabel)
        binding.bottomLabel.text = array.getString(R.styleable.AnalysisCompareTextView_AnalysisCompareTextView_BottomLabel)
        array.recycle()
    }
}