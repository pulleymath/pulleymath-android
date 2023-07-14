package com.freewheelin.pulley.revision2021.activity.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogLcWrongNoteEndBinding
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.model.response.CourseSummary
import com.freewheelin.pulley.revision2021.viewmodel.learningcourse.LCCourseEndDialogViewModel

class LCCourseEndDialog(): DialogFragment() {

    val binding: DialogLcWrongNoteEndBinding by lazy {
        DataBindingUtil.inflate(layoutInflater.cloneInContext(requireContext()), R.layout.dialog_lc_wrong_note_end, null, false)
    }
    val viewModel: LCCourseEndDialogViewModel by viewModels()
    var exitBtnCallback: () -> Unit = {}
    var moreStudyBtnCallback: (CourseSummary.MainMessageStatus) -> Unit = {}
    companion object {
        const val DIALOG_CHAPTER_ID = "DIALOG_CHAPTER_ID"
        fun newInstance(chapterId: Int?): LCCourseEndDialog {
            val args = Bundle().apply {
                putInt(DIALOG_CHAPTER_ID, chapterId ?: -1)
            }
            val instance = LCCourseEndDialog()
            instance.arguments = args
            return instance
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.apply {
            viewModel.chapterId = getInt(DIALOG_CHAPTER_ID)
        }
        return binding.root
    }
    override fun onStop() {
        super.onStop()
        viewModel.run {
            clearCompositeDisposable()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            viewModel.fetchCourseSummary()


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