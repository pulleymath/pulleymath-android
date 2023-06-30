package com.freewheelin.pulley.legacy.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.vibrate
import com.freewheelin.pulley.databinding.ViewInputDaebakRadioBinding
import com.freewheelin.pulley.databinding.ViewPolicyLayoutV2Binding

class DaebakInputRadio: LinearLayout {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setTypedArray(attrs)
    }

    var label: String?
        get() {
            return binding.labelTv.text.toString()
        }
        set(value) {
            binding.labelTv.text = value
        }

    var errorMsg: String
        get() {
            return binding.errorTv.text.toString()
        }
        set(value) {
            binding.errorTv.text = value
        }

    var position: Int? = null
        set(value) {
            field = value
            when(value) {
                0 -> binding.rg.check(R.id.rb1)
                1 -> binding.rg.check(R.id.rb2)
                2 -> binding.rg.check(R.id.rb3)
            }
        }

    var isShownError: Boolean
        get() {
            return binding.errorContainerLl.visibility == View.VISIBLE
        }
        set(value) {
            if (value)
                binding.errorContainerLl.visibility = View.VISIBLE
            else
                binding.errorContainerLl.visibility = View.GONE
        }
    var binding: ViewInputDaebakRadioBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_input_daebak_radio, this, true)

    init {
        orientation = LinearLayout.VERTICAL
        LayoutInflater.from(context).inflate(R.layout.view_input_daebak_radio, this)
        isShownError = false
        binding.rg.setOnCheckedChangeListener { group, checkedId ->
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