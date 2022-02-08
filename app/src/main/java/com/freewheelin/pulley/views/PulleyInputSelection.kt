package com.freewheelin.pulley.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R

interface PulleyInputSelectionListener {
    fun onSelectionChanged(view:PulleyInputSelection)
}
class PulleyInputSelection: ConstraintLayout, View.OnClickListener {

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
        get() = listOf(btn1.isSelected, btn2.isSelected, btn3.isSelected)
        set(value) {
            btn1.isSelected = value[0]
            btn2.isSelected = value[1]
            btn3.isSelected = value[2]
        }

    var excludedViews = mutableListOf<View>()

    var listener: PulleyInputSelectionListener? = null


    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setTypedArray(attrs)
    }

    var btn1: SelectionButton
    var btn2: SelectionButton
    var btn3: SelectionButton
    var labelTv: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_input_pulley_selection, this)
        btn1 = findViewById(R.id.btn1)
        btn2 = findViewById(R.id.btn2)
        btn3 = findViewById(R.id.btn3)
        labelTv = findViewById(R.id.labelTv)
        buttonTitles = listOf("A","B","C")

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
        view.isSelected = !view.isSelected

        if(!view.isSelected) excludedViews.add(view)
        else excludedViews.remove(view)

        listener?.onSelectionChanged(this)
    }

    fun getExcluded() : List<Boolean> {
        return listOf(excludedViews.contains(btn1), excludedViews.contains(btn2), excludedViews.contains(btn3))
    }

    fun set(list:List<Boolean>) {
        result = list
        setHide()
    }

    fun exclude(list:List<Boolean>) {
        if(list[0] && btn1.visibility != View.GONE) {
            btn1.isSelected = false
            excludedViews.add(btn1)
        }
        if(list[1] && btn2.visibility != View.GONE) {
            btn2.isSelected = false
            excludedViews.add(btn2)
        }
        if(list[2] && btn3.visibility != View.GONE) {
            btn3.isSelected = false
            excludedViews.add(btn3)
        }
    }

    fun setHide() {
        if(!btn1.isSelected) btn1.visibility = View.GONE
        if(!btn2.isSelected) btn2.visibility = View.GONE
        if(!btn3.isSelected) btn3.visibility = View.GONE

        if(!btn1.isSelected && !btn2.isSelected && !btn3.isSelected) visibility = View.GONE
    }

}

