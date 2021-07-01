package com.freewheelin.pulley.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.vibrate
import kotlinx.android.synthetic.main.view_input_daebak_radio.view.*

class DaebakInputRadio: LinearLayout {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setTypedArray(attrs)
    }

    var label: String?
        get() {
            return labelTv.text.toString()
        }
        set(value) {
            labelTv.text = value
        }

    var errorMsg: String
        get() {
            return errorTv.text.toString()
        }
        set(value) {
            errorTv.text = value
        }

    var position: Int? = null
        set(value) {
            field = value
            when(value) {
                0 -> rg.check(R.id.rb1)
                1 -> rg.check(R.id.rb2)
                2 -> rg.check(R.id.rb3)
            }
        }

    var isShownError: Boolean
        get() {
            return errorContainerLl.visibility == View.VISIBLE
        }
        set(value) {
            if (value)
                errorContainerLl.visibility = View.VISIBLE
            else
                errorContainerLl.visibility = View.GONE
        }
    init {
        orientation = LinearLayout.VERTICAL
        LayoutInflater.from(context).inflate(R.layout.view_input_daebak_radio, this)
        isShownError = false
        rg.setOnCheckedChangeListener { group, checkedId ->
            isShownError = false
            when(checkedId) {
                R.id.rb1 -> position = 0
                R.id.rb2 -> position = 1
                R.id.rb3 -> position = 2
            }
        }
    }

    fun showErrMsg() {
        isShownError = true
        context.vibrate()
    }

    fun hideErrMsg() {
        isShownError = false
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.DaebakInputRadio)
        this.label = array.getString(R.styleable.DaebakInputRadio_DaebakInputRadio_Label)
        array.recycle()
    }
}