package com.freewheelin.pulley.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.Theme

interface DaebakInputSelectionListener {
    fun onSelectionChanged(view:DaebakInputSelection)
}
class DaebakInputSelection: ConstraintLayout, View.OnClickListener {


    var label: String?
        get() {
            return labelTv.text.toString()
        }
        set(value) {
            labelTv.text = value
        }


    var buttonTitles: List<String>
        set(value) {
            field = value
            btn1.text = value[0]
            btn2.text = value[1]
            btn3.text = value[2]
        }

    var result: List<Boolean>
        get() = listOf(btn0.isSelected, btn1.isSelected, btn2.isSelected, btn3.isSelected)
        set(value) {
            btn0.isSelected = value[0]
            btn1.isSelected = value[1]
            btn2.isSelected = value[2]
            btn3.isSelected = value[3]
        }

    var listener: DaebakInputSelectionListener? = null


    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setTypedArray(attrs)
    }

    var labelTv: TextView
    var btn0: SelectionButton
    var btn1: SelectionButton
    var btn2: SelectionButton
    var btn3: SelectionButton

    init {
        LayoutInflater.from(context).inflate(R.layout.view_input_daebak_selection, this)

        labelTv = findViewById(R.id.labelTv)
        btn0 = findViewById(R.id.btn0)
        btn1 = findViewById(R.id.btn1)
        btn2 = findViewById(R.id.btn2)
        btn3 = findViewById(R.id.btn3)

        buttonTitles = listOf("A","B","C")
        btn0.setOnClickListener {
            it.isSelected = !it.isSelected
            if(it.isSelected) {
                btn1.isSelected = false
                btn2.isSelected = false
                btn3.isSelected = false
            }
            listener?.onSelectionChanged(this)
        }
        btn1.setOnClickListener(this)
        btn2.setOnClickListener(this)
        btn3.setOnClickListener(this)
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.DaebakInputSelection)
        this.label = array.getString(R.styleable.DaebakInputSelection_DaebakInputSelection_Label)
        array.recycle()
    }

    override fun onClick(view: View) {
        btn0.isSelected = false
        view.isSelected = !view.isSelected
        listener?.onSelectionChanged(this)
    }

    fun release() {
        btn0.isSelected = false
        btn1.isSelected = false
        btn2.isSelected = false
        btn3.isSelected = false
    }

}

class SelectionButton: androidx.appcompat.widget.AppCompatButton {
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if(isSelected) {
            typeface = Theme.extraBold(context)
            setTextColor(ContextCompat.getColor(context,R.color.purple_6D6DFF))
            background = ContextCompat.getDrawable(context, R.drawable.bg_purple_ecebff_stroke_purple_acacff_round_24)
        } else {
            typeface = Theme.bold(context)
            setTextColor(ContextCompat.getColor(context,R.color.black_4c4c4c))
            background = ContextCompat.getDrawable(context, R.drawable.bg_white_ffffff_stroke_grey_e8e8e8_round_24)
        }
    }
}