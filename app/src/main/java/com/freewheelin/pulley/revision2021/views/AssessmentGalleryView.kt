package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.freewheelin.pulley.databinding.ViewAssessmentGalleryBinding
import com.freewheelin.pulley.revision2021.activity.AssessmentSolveActivity
import com.freewheelin.pulley.revision2021.model.response.AssessmentProblem
import com.freewheelin.pulley.revision2021.model.response.AssessmentWorkbook
import com.freewheelin.pulley.revision2021.utils.getLifecycleOwner
import com.freewheelin.pulley.revision2021.viewmodel.AssessmentSolveViewModel
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel
import com.freewheelin.pulley.revision2021.views.adapters.AssessmentTestGalleryAdapter
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.views.GridMarginDecoration

interface AssessmentGalleryViewDelegate {
    fun onFoldBtnClicked()
    fun onProblemSelected(problem: AssessmentProblem?, autoFocus: Boolean = true)
}

class AssessmentGalleryView: ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var delegate: AssessmentGalleryViewDelegate? = null
//    var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    private var binding: ViewAssessmentGalleryBinding
    companion object {
        fun getGalleryViewWidth(context: Context): Int {
            return (DisplayUtils.getScreenWidth(context) * 0.34f).toInt()
        }
    }

    init {
        binding = ViewAssessmentGalleryBinding.inflate(LayoutInflater.from(context), this, true)
        binding.apply {
            lifecycleOwner = context.getLifecycleOwner()
        }
    }
    private val adapter = AssessmentTestGalleryAdapter {
        delegate?.onProblemSelected(it, false)
    }
    fun setContent(workbook: AssessmentWorkbook) {

        binding.apply {
            galleryHeaderTv.text = workbook.title
            val decoration = GridMarginDecoration(rowSpace = 8, columnCnt = 3, columnSpace = 6)
            galleryRv.addItemDecoration(decoration)
            galleryRv.adapter = adapter
            galleryRv.layoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)

            foldBtn.setOnClickListener {
                delegate?.onFoldBtnClicked()
            }
        }
    }
    fun setViewModel(vm: BaseViewModel) {

        (vm as AssessmentSolveViewModel).apply {
            binding.activityVM = this
            contentAdapter = adapter
            problemList.observe(context as AssessmentSolveActivity) {
                adapter.submitList(null)
                adapter.submitList(it)
            }
        }
    }

    fun scrollTo(problem: AssessmentProblem) {
        binding.activityVM?.contentAdapter?.getIndex(problem)?.let {
            binding.galleryRv.scrollToPosition(it)
        }
    }
}