package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewBalloonCourseRoadBinding
import com.freewheelin.pulley.revision2021.model.CourseType
import com.freewheelin.pulley.revision2021.model.response.SingleCourseDesc
import com.freewheelin.pulley.utils.dpToPx

class BalloonCourseRoadView: ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    val binding: ViewBalloonCourseRoadBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_balloon_course_road, this, true)


    init {
        id = View.generateViewId()
    }


    fun setCourseList(courseList: List<SingleCourseDesc>, currentCourse: SingleCourseDesc?, itemCallback: (SingleCourseDesc) -> Unit) {
        binding.apply {
            courseList.forEach {
                val isEqualType = it.courseType == currentCourse?.courseType
                val isEqualId = it.learningCourseDetailId == currentCourse?.learningCourseDetailId
                roadNaviView(it, isEqualType && isEqualId).let { tv ->
                    tv.setOnClickListener {
                        (it as LCNaviView).course?.let { course ->
                            itemCallback(course)
                        }
                    }
                    rootLl.addView(tv)
                }
            }
        }
    }
    fun setOnMapBtnClickListener(selectedType: CourseType, callback: (CourseType) -> Unit) {
        binding.goMapBtn.setOnClickListener {
            val selectedMap = when (selectedType) {
                CourseType.priorConcept -> CourseType.priorConceptMap
                CourseType.pattern -> CourseType.patternMap
                else -> CourseType.priorConceptMap
            }
            callback(selectedMap)
        }
    }
    private fun roadNaviView(course: SingleCourseDesc, isCurrentCourse: Boolean): ConstraintLayout {
        return LCNaviView(context).apply {
            this.course = course
            val isStudied = false
            background = when {
                isCurrentCourse -> ContextCompat.getDrawable(context, R.drawable.bg_road_view_studied)
                isStudied -> ContextCompat.getDrawable(context, R.drawable.bg_road_view_studied) // SCD에 값이 없다.
                else -> ContextCompat.getDrawable(context, R.drawable.bg_road_view_common)
            }

            binding.contentTv.text = when (course.courseType) {
                CourseType.priorConcept -> "${course.name}"
                CourseType.cooking -> "개념 0${course.sequence}. ${course.name}"
                CourseType.pattern -> "유형 0${course.sequence}. ${course.name}"
                else -> { "유형 0${course.sequence}. ${course.name}" }
            }
            binding.arrowIv.visibility = if (isCurrentCourse) View.GONE else View.VISIBLE
        }
    }
    fun setPeakViewBias(selectedType: CourseType) {
        binding.apply {
            courseType = selectedType
            val biasValue = when (selectedType) {
                CourseType.priorConcept -> 0.15f
                CourseType.cooking -> 0.3f
                CourseType.pattern -> 0.7f
                else -> 0.5f
            }

            val cs = ConstraintSet()
            cs.clone(rootView)
            cs.setHorizontalBias(peakView.id, biasValue)
            cs.applyTo(rootView)
        }
    }

    fun getBalloonWidth(): Int {
        return if(this.width == 0) {
            ViewGroup.LayoutParams.WRAP_CONTENT
        } else {
            this.width.dpToPx()
        }
    }
    fun getBalloonHeight(): Int {
        return if(this.height == 0) {
            ViewGroup.LayoutParams.WRAP_CONTENT
        } else {
            this.height.dpToPx()
        }
    }
    fun getXOffset(selectedType: CourseType): Int {
        return when (selectedType) {
            CourseType.cooking -> -50
            CourseType.pattern -> -188
//            CourseType.pattern -> -468
            else -> 0
        }
    }

}