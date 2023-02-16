package com.freewheelin.pulley.revision2023.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.revision2023.model.PaidServiceType

class MainUserStatusChip: androidx.appcompat.widget.AppCompatTextView {
    lateinit var title:String
    lateinit var color:String

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
//        setTypedArray(attrs)
    }

//    private fun setTypedArray(attrs: AttributeSet?) {
//        val array = context.obtainStyledAttributes(attrs, R.styleable.MainUserStatusChip)
//
//        val paidServiceTypeRawValue = array.getInt(R.styleable.MainUserStatusChip_status, 0)
//        type = PaidServiceType.ConvertToType(paidServiceTypeRawValue)
//        array.recycle()
//    }

    var type: PaidServiceType? = null
        set(value) {
            field = value
            value?.let {
                this.text = it.convertTextOnMainChip()
                setTextAppearance(R.style.h6)
                setTextColor(it.convertColorOnMainChip(context))
                background = it.convertDrawableOnMainChip(context)
            }
        }

}