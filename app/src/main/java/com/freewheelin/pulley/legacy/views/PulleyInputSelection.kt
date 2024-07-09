package com.freewheelin.pulley.legacy.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.assets.BigUnitV3
import com.freewheelin.pulley.legacy.assets.SubjectV3
import com.freewheelin.pulley.revision2023.model.response.SubjectChapter
import com.freewheelin.pulley.legacy.utils.visibleIf

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
            val subject = SubjectV3.convertStrToSubject(value)
            bigUnits = subject.bigUnits
        }

    var bigUnits: List<BigUnitV3> = listOf(BigUnitV3.지수함수와_로그함수)
        set(value) {
            field = value
            buttonTitles = value.map { it.title }
        }
    var buttonTitles: List<String> = listOf("")
        set(value) {
            field = value
            val btnList = listOf(btn1, btn2, btn3, btn4)
            value.forEachIndexed { index, text ->
                btnList[index].text = text
            }
        }

    var result: List<Boolean>
        get() = listOf(btn1.isSelected, btn2.isSelected, btn3.isSelected, btn4.isSelected)
        set(value) {
            val btnList = listOf(btn1, btn2, btn3, btn4)
            value.forEachIndexed { index, isSelected ->
                btnList[index].isSelected = isSelected
            }
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
    var btn4: SelectionButton
    var labelTv: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_input_pulley_selection, this)
        btn1 = findViewById(R.id.btn1)
        btn2 = findViewById(R.id.btn2)
        btn3 = findViewById(R.id.btn3)
        btn4 = findViewById(R.id.btn4)
        labelTv = findViewById(R.id.labelTv)
        bigUnits = listOf()
        buttonTitles = listOf("A","B","C","D")

        listOf(btn1, btn2, btn3, btn4).forEach {
            it.setOnClickListener(this)
            it.visibleIf(false)
        }
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.DaebakInputSelection)
        this.label = array.getString(R.styleable.DaebakInputSelection_DaebakInputSelection_Label) ?: "교육과정 외"
        array.recycle()
    }

    override fun onClick(view: View) {
        view.isSelected = !view.isSelected

        if(!view.isSelected) excludedViews.add(view)
        else excludedViews.remove(view)

        listener?.onSelectionChanged(this)
    }

    fun getExcluded() : List<Boolean> {
        return listOf(excludedViews.contains(btn1), excludedViews.contains(btn2), excludedViews.contains(btn3), excludedViews.contains(btn4))
    }

    fun setSubject(list: List<SubjectChapter>) {
        val bigUnitList = list.map { BigUnitV3.idOfNonNull(it.chapterId) }

        val includedFlagList = bigUnits.map { bigUnitList.contains(it) }
        set(includedFlagList)
    }

    fun set(list:List<Boolean>) {
        list.forEachIndexed { index, flag ->
            val btnList = listOf(btn1, btn2, btn3, btn4)
            btnList[index].isSelected = flag
            btnList[index].visibleIf(flag)

        }

        setHide()
    }

    fun excludeSubjects(list: List<SubjectChapter>) {
        val bigUnitList = list.filter { !it.isSelected }
            .map { BigUnitV3.idOfNonNull(it.chapterId) }
        val excludedFlagList = bigUnits.map { bigUnitList.contains(it) }

        exclude(excludedFlagList)
    }
    fun excludeBigUnit(includedUnits: List<BigUnitV3>) {
        val excludedFlagList = bigUnits.map { !includedUnits.contains(it) }
        exclude(excludedFlagList)

    }
    fun exclude(list:List<Boolean>) {
        val btnList = listOf(btn1, btn2, btn3, btn4)
        list.forEachIndexed { index, flag ->
            val btn = btnList[index]
            if (flag) {
                btn.isSelected = false
                excludedViews.add(btn)
            }
        }
    }

    fun setHide() {
        if(!btn1.isSelected && !btn2.isSelected && !btn3.isSelected && !btn4.isSelected) {
            visibility = View.GONE
        }
    }

}

