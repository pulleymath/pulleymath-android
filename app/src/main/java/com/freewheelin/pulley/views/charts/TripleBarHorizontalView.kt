package com.freewheelin.pulley.views.charts

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewTripleBarHorizontalBinding
import com.freewheelin.pulley.utils.TextUtils

class TripleBarHorizontalView: ConstraintLayout {
    var secondBarColor: Int
        set(value) {
            field = value
            binding.secondBar.progressColor = value
            binding.secondLabelTv.setTextColor(value)
            binding.secondRateTv.setTextColor(value)
        }

    var title: String?
        get() {
            return binding.titleTv.text.toString()
        }
        set(value) {
            field = value
            binding.titleTv.text = value
        }
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    var binding: ViewTripleBarHorizontalBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_triple_bar_horizontal, this, true)

    init {
        secondBarColor = ContextCompat.getColor(context, R.color.blue_30a4ff)
        title = null
    }

    fun setValue(firstVal: Float, secondVal: Float, thirdVal: Float) {
        binding.firstBar.set(firstVal)
        binding.secondBar.set(secondVal)
        binding.thirdBar.set(thirdVal)

        binding.firstRateTv.text = TextUtils.percentFormat.format(firstVal)
        binding.secondRateTv.text = TextUtils.percentFormat.format(secondVal)
        binding.thirdRateTv.text = TextUtils.percentFormat.format(thirdVal)

        if(firstVal >= secondVal)
            secondBarColor = ContextCompat.getColor(context, R.color.red_fe7b67)
        else
            secondBarColor = ContextCompat.getColor(context, R.color.blue_30a4ff)
    }

    fun setLabel(firstVal: String, secondVal: String, thirdVal: String) {
        binding.firstLabelTv.text = firstVal
        binding.secondLabelTv.text = secondVal
        binding.thirdLabelTv.text = thirdVal
    }
}