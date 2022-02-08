package com.freewheelin.pulley.revision2021.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewAffiliatedTestGalleryBinding
import com.freewheelin.pulley.revision2021.viewmodel.AffiliatedTestSolveViewModel
import com.freewheelin.pulley.utils.DisplayUtils

class AffiliatedTestGalleryView: ConstraintLayout, LifecycleOwner {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    var vm: AffiliatedTestSolveViewModel? = null
    private val binding: ViewAffiliatedTestGalleryBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_affiliated_test_gallery, null, false)
    }
    companion object {
        fun getGalleryViewWidth(context: Context): Int {
            return (DisplayUtils.getScreenWidth(context) * 0.693f).toInt()
        }
    }

    init {
        binding.apply {
            lifecycleOwner = this@AffiliatedTestGalleryView
            activityVM = vm
        }
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

}