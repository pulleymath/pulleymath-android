package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.content.ContextWrapper
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import com.freewheelin.pulley.databinding.ViewAffiliatedTestGalleryBinding
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.revision2021.activity.AffiliatedTestSolveActivity
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestProblem
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestWorkbook
import com.freewheelin.pulley.revision2021.utils.getLifecycleOwner
import com.freewheelin.pulley.revision2021.viewmodel.AffiliatedTestSolveViewModel
import com.freewheelin.pulley.revision2021.viewmodel.BaseViewModel
import com.freewheelin.pulley.revision2021.views.adapters.AffiliatedTestGalleryAdapter
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.views.GridMarginDecoration

interface AffiliatedGalleryViewDelegate {
    fun onFoldBtnClicked()
    fun onProblemSelected(problem: AffiliatedTestProblem?, autoFocus: Boolean = true)
}

class AffiliatedTestGalleryView: ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var delegate: AffiliatedGalleryViewDelegate? = null
//    var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    private var binding: ViewAffiliatedTestGalleryBinding
    companion object {
        fun getGalleryViewWidth(context: Context): Int {
            return (DisplayUtils.getScreenWidth(context) * 0.34f).toInt()
        }
    }

    init {
        binding = ViewAffiliatedTestGalleryBinding.inflate(LayoutInflater.from(context), this, true)
        binding.apply {
            lifecycleOwner = context.getLifecycleOwner()
        }
    }
    private val adapter = AffiliatedTestGalleryAdapter {
        delegate?.onProblemSelected(it, false)
    }
    fun setContent(workbook: AffiliatedTestWorkbook) {

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

        (vm as AffiliatedTestSolveViewModel).apply {
            binding.activityVM = this
            contentAdapter = adapter
            problemList.observe(context as AffiliatedTestSolveActivity) {
                adapter.submitList(null)
                adapter.submitList(it)
            }
        }
    }

    fun scrollTo(problem: AffiliatedTestProblem) {
        binding.activityVM?.contentAdapter?.getIndex(problem)?.let {
            binding.galleryRv.scrollToPosition(it)
        }
    }
}