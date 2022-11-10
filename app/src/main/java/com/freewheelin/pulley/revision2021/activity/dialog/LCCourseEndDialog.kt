package com.freewheelin.pulley.revision2021.activity.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogLcWrongNoteEndBinding
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.model.response.CourseSummary
import com.freewheelin.pulley.revision2021.viewmodel.learningcourse.LCCourseEndDialogViewModel

class LCCourseEndDialog(context: Context,
                        val chapterId: Int?,
                        private val exitBtnCallback: () -> Unit,
                        private val moreStudyBtnCallback: (CourseSummary.MainMessageStatus) -> Unit,
): DialogFragment() {

    val binding: DialogLcWrongNoteEndBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_lc_wrong_note_end, null, false)
    }
    val viewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
            LCCourseEndDialogViewModel::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            viewModel.fetchCourseSummary(chapterId)


            viewModel.showSprinkleView.observe(viewLifecycleOwner) { showView ->
                if (showView) sprinkleLottieView.playAnimation()
            }

            lifecycleOwner = this@LCCourseEndDialog
            vm = viewModel
//            count = "$solvedPatternCount"

            exitBtn.setOnClickListener {
                exitBtnCallback()
                dismiss()
            }

            exitIconBtn.setOnClickListener {
                dismiss()
            }

            moreStudyBtn.setOnClickListener {
                viewModel.courseSummary.value?.mainMessageStatus?.let {
                    moreStudyBtnCallback(it)
                    dismiss()
                }

            }
        }
    }
}