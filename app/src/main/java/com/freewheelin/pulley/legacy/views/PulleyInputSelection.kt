package com.freewheelin.pulley.legacy.views

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.revision2023.model.response.SubjectChapter

interface PulleyInputSelectionListener {
    fun onSelectionChanged(view:PulleyInputSelection)
}
class PulleyInputSelection: ConstraintLayout, View.OnClickListener {

    var label: String
        get() {
            return labelTv.text.toString()
        }
        set(value) {
            labelTv.text = value
        }

    var btnList = mutableListOf<SelectionButton>()

    var chapterList: List<SubjectChapter> = listOf()
        set(value) {
            field = value

            val list = when (value.size) {
                1 -> listOf(btn1)
                2 -> listOf(btn1, btn2)
                3 -> listOf(btn1, btn2, btn3)
                4 -> listOf(btn1, btn2, btn3, btn4)
                5 -> listOf(btn1, btn2, btn3, btn4, btn5)
                6 -> listOf(btn1, btn2, btn3, btn4, btn5, btn6)
                7 -> listOf(btn1, btn2, btn3, btn4, btn5, btn6, btn7)
                8 -> listOf(btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8)
                9 -> listOf(btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9)
                else -> listOf()
            }
            btnList.addAll(list)

            value.forEachIndexed { index, subjectChapter ->
                btnList[index].isSelected = subjectChapter.isSelected
                btnList[index].visibleIf(true)
            }

        }


    var buttonTitles: List<String> = listOf("")
        set(value) {
            field = value
            val btnList = listOf(btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9)
            value.forEachIndexed { index, text ->
                btnList[index].text = text
            }
        }

    var listener: PulleyInputSelectionListener? = null

    constructor(context: Context): super(context)

    var btn1: SelectionButton
    var btn2: SelectionButton
    var btn3: SelectionButton
    var btn4: SelectionButton
    var btn5: SelectionButton
    var btn6: SelectionButton
    var btn7: SelectionButton
    var btn8: SelectionButton
    var btn9: SelectionButton
    var labelTv: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_input_pulley_selection, this)
        btn1 = findViewById(R.id.btn1)
        btn2 = findViewById(R.id.btn2)
        btn3 = findViewById(R.id.btn3)
        btn4 = findViewById(R.id.btn4)

        btn5 = findViewById(R.id.btn5)
        btn6 = findViewById(R.id.btn6)
        btn7 = findViewById(R.id.btn7)
        btn8 = findViewById(R.id.btn8)
        btn9 = findViewById(R.id.btn9)

        labelTv = findViewById(R.id.labelTv)
    }
    override fun onClick(view: View) {
        view.isSelected = !view.isSelected
        listener?.onSelectionChanged(this)
    }

    fun init(list: List<SubjectChapter>) {
        val btnList = listOf(btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9)
        btnList.forEach {
            it.setOnClickListener(this)
            it.visibleIf(false)
        }
        chapterList = list
        buttonTitles = list.map { it.chapterName }

    }
}

