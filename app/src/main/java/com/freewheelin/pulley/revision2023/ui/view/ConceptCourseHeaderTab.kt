package com.freewheelin.pulley.revision2023.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.legacy.bases.isTablet
import com.freewheelin.pulley.databinding.ViewLayoutConceptCourseHeaderTabBinding
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.revision2021.model.response.LCSubject.SubjectIndicator
import com.freewheelin.pulley.revision2021.viewmodel.ConceptCourseViewModel
import com.freewheelin.pulley.revision2023.SchoolType

class ConceptCourseHeaderTab : FrameLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)

    var binding: ViewLayoutConceptCourseHeaderTabBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_layout_concept_course_header_tab, this, true)

    var selectedSubjectId: Int = 1

    init {
//        LayoutInflater.from(context).inflate(R.layout.view_layout_concept_course_header_tab, this)

        binding.apply {
            isTablet = context.isTablet
            schoolType = MyApplication.schoolType
        }
    }

    fun setViewModel(viewModel: ConceptCourseViewModel) {
        binding.vm = viewModel
    }
    fun setLifecycleOwner(owner: LifecycleOwner) {
        binding.lifecycleOwner = owner
    }

    fun changeSchoolType(type: SchoolType) {
        binding.schoolType = type
    }
    fun setMiddleAvailableSubjects(list: List<LCSubject>) {
        if (schoolType.isMiddle) {
            list.forEach {


            }
        }
    }

    private fun getSubjectType(tv: TextView): SubjectIndicator {
        return SubjectIndicator.convertStrToSubject(tv.text.toString())
    }

    fun getTabByIndex(subjectRaw: Int): SubjectIndicator {
        return SubjectIndicator.convertRawToSubject(subjectRaw)
    }
}