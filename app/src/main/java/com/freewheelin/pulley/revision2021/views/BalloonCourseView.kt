package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewBalloonCourseRoadBinding
import com.freewheelin.pulley.revision2021.model.CourseType
import com.freewheelin.pulley.revision2021.model.response.SingleCourseDesc

class BalloonCourseView(private val context: Context) : PopupWindow(context) {

//    constructor(context: Context) : super(context)
//    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {

    }

//    var context: Context? = null
    fun show(selectedType: CourseType, courseList: List<SingleCourseDesc>, currentCourse: SingleCourseDesc?) {
        context.let {
            val roadView = BalloonCourseRoadView(it)
            roadView.id = View.generateViewId()
            roadView.setPeakViewBias(selectedType)
            roadView.setCourseList(courseList, currentCourse) {

            }

            contentView = roadView
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            showAtLocation(contentView, Gravity.NO_GRAVITY, 0, 0)

        }
    }

    open fun onBalloonMeasured() {
    }

    class RoadView: ConstraintLayout {

        constructor(context: Context) : super(context)
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
        val binding: ViewBalloonCourseRoadBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_balloon_course_road, this, true)


        init {
            binding.apply {
                val color = ContextCompat.getColor(context, R.color.white)
                peakIv.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
                rootLl.background.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            }
        }


        fun setCourseList(courseList: List<SingleCourseDesc>, currentCourse: SingleCourseDesc?) {
            binding.apply {
                courseList.forEach {
                    val isEqualType = it.courseType == currentCourse?.courseType
                    val isEqualId = it.learningCourseDetailId == currentCourse?.learningCourseDetailId
                    roadCourseTextView(it, isEqualType && isEqualId).let { tv ->
                        tv.setOnClickListener {
                            // TODO dismiss()
                            (it as LCNaviTextView).course?.let { course ->
//                            itemCallback(course)
                            }
                        }
                        rootLl.addView(tv)
                    }
                }
            }
        }
        private fun roadCourseTextView(course: SingleCourseDesc, isCurrentCourse: Boolean): TextView {
            return LCNaviTextView(context).apply {
                this.course = course
                val isStudied = false
                background = when {
                    isCurrentCourse -> ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_road_view_studied
                    )
                    isStudied -> ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_road_view_studied
                    ) // SCD에 값이 없다.
                    else -> ContextCompat.getDrawable(context, R.drawable.bg_road_view_common)
                }
//            text = "${course.name}"
                text = when (course.courseType) {
                    CourseType.priorConcept -> "복습 0${course.sequence}. ${course.name}"
                    CourseType.cooking -> "개념 0${course.sequence}. ${course.name}"
                    CourseType.pattern -> "유형 0${course.sequence}. ${course.name}"
                    else -> {
                        "유형 0${course.sequence}. ${course.name}"
                    }
                }
            }
        }
        fun setPeakViewBias(selectedType: CourseType) {
            binding.apply {
                val biasValue = when (selectedType) {
                    CourseType.priorConcept -> 0.15f
                    CourseType.cooking -> 0.5f
                    CourseType.pattern -> 0.85f
                    else -> 0.5f
                }

                val cs = ConstraintSet()
                cs.clone(rootView)
                cs.setHorizontalBias(peakView.id, biasValue)
                cs.applyTo(rootView)
            }
        }
        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//            this@BalloonCourseWindow.onBalloonMeasured()
        }
    }

}