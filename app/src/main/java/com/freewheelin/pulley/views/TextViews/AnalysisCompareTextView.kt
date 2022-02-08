package com.freewheelin.pulley.views.textViews

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import kotlinx.android.synthetic.main.textview_analysis_compare.view.*

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
                    setDiffTextColor(ContextCompat.getColor(context!!, R.color.blue_2287ef))
                    changeIv.setImageResource(R.drawable.ic_up_blue)
                }
                UpDownTextView.Change.decrease -> {
                    setDiffTextColor(ContextCompat.getColor(context!!, R.color.red_fe7b67))
                    changeIv.setImageResource(R.drawable.ic_down_red)
                }
                UpDownTextView.Change.noChange -> {
                    setDiffTextColor(ContextCompat.getColor(context!!, R.color.grey_9f9f9f))
                    changeIv.setImageDrawable(null)
                }
            }
        }
    var valueText: String?
        get() {
            return valueTv.text.toString()
        }
        set(value) {
            valueTv.text = value
        }

    var diffText: String?
        get() {
            return diffTv.text.toString()
        }
        set(value) {
            diffTv.text = value
        }

    init {
        orientation = LinearLayout.VERTICAL
        LayoutInflater.from(context).inflate(R.layout.textview_analysis_compare, this)
        gravity = Gravity.CENTER
    }

    fun setDiffTextColor(color: Int) {
        diffTv.setTextColor(color)
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.AnalysisCompareTextView)

        topLabel.text = array.getString(R.styleable.AnalysisCompareTextView_AnalysisCompareTextView_TopLabel)
        bottomLabel.text = array.getString(R.styleable.AnalysisCompareTextView_AnalysisCompareTextView_BottomLabel)
        array.recycle()
    }
}