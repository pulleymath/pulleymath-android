package com.freewheelin.pulley.revision2021.activity.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogCourseRoadViewBinding
import com.freewheelin.pulley.revision2021.model.CourseType
import com.freewheelin.pulley.revision2021.model.response.SingleCourseDesc
import com.freewheelin.pulley.revision2021.views.LCNaviTextView
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.utils.dpToPx

class CourseRoadViewDialog(context: Context,
                           private val courseList: List<SingleCourseDesc>,
                           private val currentCourse: SingleCourseDesc?,
                           private val selectedType: CourseType,
                           private val sourceView: View,
                           private val itemCallback: (course: SingleCourseDesc) -> Unit,
                           private val mapCallback: (type: CourseType) -> Unit,
): DialogFragment() {

//    private val viewModel by lazy {
//        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(LessonRoadViewViewModel::class.java)
//    }

    private val binding: DialogCourseRoadViewBinding by lazy {
        DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_course_road_view,
            null,
            false
        )
    }
    val screenWidth by lazy { DisplayUtils.getScreenWidth(context) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setDialogPosition(selectedType)
        return binding.root
    }

    fun setDialogPosition(selectedType: CourseType) {
        val location = IntArray(2)
        sourceView.getLocationInWindow(location)
        val sourceX = location[0]
        val sourceY = location[1]
        val dialogXPosition = getDialogXWithCourseType(selectedType, sourceX)

        dialog?.window?.let {
            it.setGravity(Gravity.LEFT or Gravity.TOP)
            val p = it.attributes
            p.width = ViewGroup.LayoutParams.MATCH_PARENT
            p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            p.x = dialogXPosition
            p.y = sourceY + 50.dpToPx()
            it.attributes = p
        }
    }

    private fun getDialogXWithCourseType(type: CourseType, sourceX: Int): Int {
        val centerOfWidth = screenWidth / 2
        val dialogWidth = 368.dpToPx()
        return when (selectedType) {
            CourseType.PriorConcept -> sourceX
            CourseType.Pattern -> {
                val sourceWidth = sourceView.width
                val targetX = sourceX + sourceWidth - dialogWidth
                targetX
            }
            else -> {
                centerOfWidth - 184.dpToPx()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AppCompatDialog(requireContext(), R.style.TransparentFragmentDialog)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = this@CourseRoadViewDialog
//            vm = viewModel
            courseType = selectedType
            setPeakViewBias()

            courseList.forEach {
                val isEqualType = it.courseType == currentCourse?.courseType
                val isEqualId = it.learningCourseDetailId == currentCourse?.learningCourseDetailId
                roadCourseTextView(it, isEqualType && isEqualId).let { tv ->
                    tv.setOnClickListener {
                        dismiss()
                        (it as LCNaviTextView).course?.let { course ->
                            itemCallback(course)
                        }
                    }
                    rootLl.addView(tv)
                }
            }


            goMapBtn.setOnClickListener {
                dismiss()
                val selectedMap = when (selectedType) {
                    CourseType.PriorConcept -> CourseType.PriorConceptMap
                    CourseType.Pattern -> CourseType.PatternMap
                    else -> CourseType.PriorConceptMap
                }
                mapCallback(selectedMap)
            }
        }
    }

    private fun roadCourseTextView(course: SingleCourseDesc, isCurrentCourse: Boolean): TextView {
        return LCNaviTextView(requireContext()).apply {
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
            text = when (course.courseType) {
                CourseType.PriorConcept -> "복습 0${course.sequence}. ${course.name}"
                CourseType.Cooking -> "개념 0${course.sequence}. ${course.name}"
                CourseType.Pattern -> "유형 0${course.sequence}. ${course.name}"
                else -> {
                    "유형 0${course.sequence}. ${course.name}"
                }
            }
        }
    }

    private fun setPeakViewBias() {
        binding.apply {
            val biasValue = when (selectedType) {
                CourseType.PriorConcept, CourseType.PriorConcept -> 0.15f
                CourseType.Pattern -> 0.85f
                else -> 0.5f
            }

            val cs = ConstraintSet()
            cs.clone(rootView)
            cs.setHorizontalBias(peakView.id, biasValue)
            cs.applyTo(rootView)
        }
    }

}
