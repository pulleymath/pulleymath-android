package com.freewheelin.pulley.views.v2

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import kotlinx.android.synthetic.main.view_policy_layout_v2.view.*

interface PolicyLayoutV2Listener {
    fun onCheckChangedListener(cb: CheckBox?, flag: Boolean)
}

class PolicyLayoutV2: ConstraintLayout, View.OnClickListener {
    companion object {
        val Essential = 0
        val Optional = 1
    }

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setTypedArray(attrs)
    }
    var checkBox: CheckBox

    init {
        LayoutInflater.from(context).inflate(R.layout.view_policy_layout_v2, this)
        checkBox = findViewById(R.id.checkBox)
        checkBox.setOnClickListener(this)
        essentialOptionalText = findViewById(R.id.essentialOptionalText)
        allDocuTextTv = findViewById(R.id.allDocuTextTv)

    }
    var listener: PolicyLayoutV2Listener? = null
    var essentialOptionalText: TextView
    var allDocuTextTv: TextView

    var type = Essential
        set(value) {
            field = value

            when(value) {
                Essential -> {
                    essentialOptionalText.text = "[필수]"
                    essentialOptionalText.setTextColor(ContextCompat.getColor(context, R.color.red_300))
                }
                Optional -> {
                    essentialOptionalText.text = "[선택]"
                    essentialOptionalText.setTextColor(ContextCompat.getColor(context, R.color.grey_9f9f9f))
                }
            }

        }
    var checked: Boolean = false
        get() {
            return checkBox.isChecked
        }
        set(value) {
            field = value
            checkBox.isChecked = value
        }

    var text: CharSequence = ""
        set(value) {
            field = value
            textTv.text = value
        }
    var allDocuText: CharSequence = "전문 보기"
        set(value) {
            field = value
            allDocuTextTv.text = value
        }

    fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.PolicyLayoutV2)
        this.type = array.getInt(R.styleable.PolicyLayoutV2_PolicyLayoutV2_Type, Essential)
        this.checked = array.getBoolean(R.styleable.PolicyLayoutV2_PolicyLayoutV2_Checked, false)
        allDocuTextTv.visibility = array.getInt(R.styleable.PolicyLayoutV2_PolicyLayoutV2_ShowAllDocuText, VISIBLE)
        essentialOptionalText.visibility = array.getInt(R.styleable.PolicyLayoutV2_PolicyLayoutV2_ShowEssOpText, VISIBLE)

        val set = intArrayOf(
            android.R.attr.background, // idx 0
            android.R.attr.text// idx 1
        )

        val androidAttrs = context.obtainStyledAttributes(attrs, set)
        text = androidAttrs.getText(set.indexOf(android.R.attr.text))

    }

    override fun onClick(view: View?) {
        val cb = view as CheckBox
        val isChecked: Boolean = cb.isChecked
        listener?.onCheckChangedListener(cb, isChecked)
    }
}