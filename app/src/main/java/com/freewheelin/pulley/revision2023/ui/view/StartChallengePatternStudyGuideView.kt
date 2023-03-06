package com.freewheelin.pulley.revision2023.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.freewheelin.pulley.databinding.ViewStartChallengePatternStudyGuideBinding
import com.freewheelin.pulley.revision2021.activity.base.CustomBaseView
import com.freewheelin.pulley.revision2023.viewmodel.PatternStudyViewModel

class StartChallengePatternStudyGuideView: CustomBaseView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    lateinit var binding: ViewStartChallengePatternStudyGuideBinding
    lateinit var parentViewModel: PatternStudyViewModel


    init {

    }

    override fun onViewCreated() {
        binding = ViewStartChallengePatternStudyGuideBinding.inflate(LayoutInflater.from(context), this, true)
    }
    override fun doOnAttached() {
        binding.apply {
            rootView.setOnClickListener {
                parentViewModel.showGuideView.postValue(false)
            }

        }
    }

    override fun doOnDetached() {}

    fun setViewModel(vm: PatternStudyViewModel) {
        parentViewModel = vm
    }

}