package com.freewheelin.pulley.revision2023.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.assets.BigUnitV3
import com.freewheelin.pulley.assets.SubjectV3
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.databinding.ViewMiddleSchoolUnitSelectionBinding
import com.freewheelin.pulley.revision2023.model.response.RecommendSubject
import com.freewheelin.pulley.utils.visibleIf

interface MiddleSchoolUnitSelectionListener {
    fun onSelectionChanged(view: View)
}
class MiddleSchoolUnitSelection: ConstraintLayout, View.OnClickListener {

    var listener: MiddleSchoolUnitSelectionListener? = null

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setTypedArray(attrs)
        initUI()
    }
    var binding: ViewMiddleSchoolUnitSelectionBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_middle_school_unit_selection, this, true)

    fun initUI () {
        binding.apply {

            unitTotal.setOnClickListener {
                it.isSelected = !it.isSelected
                if(it.isSelected) {
                    btnList.forEach { it.isSelected = false }
                }
                listener?.onSelectionChanged(it)
            }
            btnList.forEach { it.setOnClickListener(this@MiddleSchoolUnitSelection) }
        }
    }

    var btnList = mutableListOf<SubjectSelectionButton>()

    var label: Int = 0
        set(value) {
            field = value
            binding.apply {
                val list = when (value) {
                    SubjectV3.중1_1.id -> listOf(unit0, unit1, unit2, unit3)
                    SubjectV3.중1_2.id -> listOf(unit0, unit1, unit2, unit3)
                    SubjectV3.중2_1.id -> listOf(unit0, unit1, unit2, unit3)
                    SubjectV3.중2_2.id -> listOf(unit0, unit1, unit2)
                    SubjectV3.중3_1.id -> listOf(unit0, unit1, unit2, unit3)
                    SubjectV3.중3_2.id -> listOf(unit0, unit1, unit2)
                    else -> { listOf() }
                }
                btnList.addAll(list)
            }
        }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.MiddleSchoolUnitSelection)
        this.label = array.getInt(R.styleable.MiddleSchoolUnitSelection_unitType, 0)
        array.recycle()
    }

    override fun onClick(view: View) {
        binding.unitTotal.isSelected = false
        view.isSelected = !view.isSelected
        listener?.onSelectionChanged(view)
    }

    fun getAllBtn(): List<SubjectSelectionButton> {
        return btnList.plus(binding.unitTotal)

    }
    fun release() {
        binding.selectionLl.children.forEach {
            it.isSelected = false
        }
    }
    fun initSelected(subject: RecommendSubject) {
        binding.apply {
            unitTitleTv.text = subject.subjectName
            val isSelectedAll = subject.chapters
                .map { it.isSelected }
                .reduce { p1, p2 ->
                    p1 && p2
                }

            unitTotal.isSelected = isSelectedAll


            val withoutUnSortedChapters = subject.chapters
                .filterNot { it.chapterName == "미분류" }
            withoutUnSortedChapters
                .forEachIndexed { index, chapter ->
                    btnList[index].visibleIf(true)
                    btnList[index].text = chapter.chapterName
                    btnList[index].isSelected = !isSelectedAll && chapter.isSelected
                    btnList[index].bigUnits.add(BigUnitV3.idOfNonNull(chapter.chapterId))
                }

            val bigUnits = withoutUnSortedChapters.map { BigUnitV3.idOfNonNull(it.chapterId) }
            unitTotal.bigUnits.addAll(bigUnits)
        }

    }
}

class SubjectSelectionButton: androidx.appcompat.widget.AppCompatButton {
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {

    }
    var bigUnits: MutableList<BigUnitV3> = mutableListOf()
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