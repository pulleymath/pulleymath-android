package com.freewheelin.pulley.revision2023.ui.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewElementarySchoolUnitSelectionBinding
import com.freewheelin.pulley.legacy.utils.visibleIf
import com.freewheelin.pulley.revision2023.model.response.RecommendSubject
import com.freewheelin.pulley.revision2023.model.response.SubjectChapter

interface ElementarySchoolUnitSelectionListener {
    fun onSelectionChanged(view: View)
}
class ElementarySchoolUnitSelection: ConstraintLayout, View.OnClickListener {

    var listener: ElementarySchoolUnitSelectionListener? = null

    constructor(context: Context): super(context) {

    }
    var binding: ViewElementarySchoolUnitSelectionBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_elementary_school_unit_selection, this, true)

    var btnList = mutableListOf<SubjectSelectionButton>()

    var chapterList: List<SubjectChapter> = listOf()
        set(value) {
            field = value

            binding.apply {
                val list = when (value.size) {
                    1 -> listOf(unit0)
                    2 -> listOf(unit0, unit1)
                    3 -> listOf(unit0, unit1, unit2)
                    4 -> listOf(unit0, unit1, unit2, unit3)
                    5 -> listOf(unit0, unit1, unit2, unit3, unit4)
                    6 -> listOf(unit0, unit1, unit2, unit3, unit4, unit5)
                    7 -> listOf(unit0, unit1, unit2, unit3, unit4, unit5, unit6)
                    8 -> listOf(unit0, unit1, unit2, unit3, unit4, unit5, unit6, unit7)
                    9 -> listOf(unit0, unit1, unit2, unit3, unit4, unit5, unit6, unit7, unit8)
                    else -> listOf()
                }
                btnList.addAll(list)
            }
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
        chapterList = subject.chapters
        binding.apply {
            unitTitleTv.text = subject.subjectName
            unitTotal.setOnClickListener {
                it.isSelected = !it.isSelected
                if(it.isSelected) {
                    btnList.forEach { it.isSelected = false }
                }
                listener?.onSelectionChanged(it)
            }
            btnList.forEach { it.setOnClickListener(this@ElementarySchoolUnitSelection) }

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
                }
        }

    }
}