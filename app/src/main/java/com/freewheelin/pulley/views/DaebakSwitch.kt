package com.freewheelin.pulley.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.freewheelin.pulley.R

class DaebakSwitch : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setTypedArray(attrs)
    }

    var label: String
        get() {
            return labelTv.text.toString()
        }
        set(value) {
            labelTv.text = value
        }


    var listener: CompoundButton.OnCheckedChangeListener? = null
        set(value) {
            field = value
            daebakSwitch.setOnCheckedChangeListener(value)
        }

    val isChecked: Boolean
        get() = daebakSwitch.isChecked

    var labelTv: TextView
    var daebakSwitch: Switch

    init {
        LayoutInflater.from(context).inflate(R.layout.view_switch_daebak, this)

        labelTv = findViewById(R.id.labelTv)
        daebakSwitch = findViewById(R.id.daebakSwitch)

        this.setOnClickListener {
            daebakSwitch.isChecked = !daebakSwitch.isChecked
        }
        labelTv.setOnClickListener {
            daebakSwitch.isChecked = !daebakSwitch.isChecked
        }
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.DaebakSwitch)
        label = array.getString(R.styleable.DaebakSwitch_DaebakSwitch_Label)?: ""
        array.recycle()
    }

    fun setOnCheckedChangeListener(listener: CompoundButton.OnCheckedChangeListener) {
        this.listener = listener
    }
}
