package com.freewheelin.pulley.legacy.views.editText

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatEditText
import com.freewheelin.pulley.R

interface PulleyTextFieldListener {
    fun onTextChanged(text: String)
}
class PulleyTextField : AppCompatEditText {
    var placeholderTextSize: Float = 0f
    var pulleyTextSize: Float
    var listener: PulleyTextFieldListener? = null
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setTypedArray(attrs)
    }
    init {
        pulleyTextSize = resources.getDimension(R.dimen.sp16)
        this.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(sequence: CharSequence, p1: Int, p2: Int, p3: Int) {
                if(sequence.isEmpty()) {
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, placeholderTextSize)
                } else {
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, pulleyTextSize)
                }
                listener?.onTextChanged(text.toString())
            }
        })
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.PulleyTextField)
        placeholderTextSize = array.getFloat(R.styleable.PulleyTextField_PulleyTextField_placeholderSize, 0f)
        val set = intArrayOf(
                android.R.attr.textSize
        )
        val androidAttrs = context.obtainStyledAttributes(attrs, set)
        pulleyTextSize = androidAttrs.getFloat(set.indexOf(android.R.attr.textSize), pulleyTextSize)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, pulleyTextSize)
        array.recycle()
    }
}