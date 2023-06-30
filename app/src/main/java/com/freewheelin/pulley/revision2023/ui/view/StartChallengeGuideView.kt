package com.freewheelin.pulley.revision2023.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.activities.learning.LearningTabActivity
import com.freewheelin.pulley.revision2023.viewmodel.MainFViewModel
import com.freewheelin.pulley.legacy.core.Theme
import com.freewheelin.pulley.databinding.ViewStartChallengeGuideBinding
import com.freewheelin.pulley.revision2021.activity.base.CustomBaseView
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.legacy.utils.partialFontAndColored
import com.freewheelin.pulley.legacy.utils.underline
import com.freewheelin.pulley.revision2023.ui.activity.MainActivity

class StartChallengeGuideView: CustomBaseView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    lateinit var binding: ViewStartChallengeGuideBinding
    lateinit var mainViewModel: MainFViewModel


    init {

    }

    override fun onViewCreated() {
        binding = ViewStartChallengeGuideBinding.inflate(LayoutInflater.from(context), this, true)
    }
    override fun doOnAttached() {
        binding.apply {

            startChallengeBtn.setOnClickListener {
                (context as MainActivity).let {
                    it.tabMove(1)
                    it.moveConceptCourseSubject(LCSubject.SubjectIndicator.Tutorial.rawValue)
                    mainViewModel.disappearStartChallengeGuide()
                }
            }
            anotherMissionTv.underline()
            startChallengeGuideTv.text = startChallengeGuideTv.text
                .partialFontAndColored(
                    Theme.extraBold(context),
                    ContextCompat.getColor(context, R.color.red_300),
                    "빨간 도장"
                )
        }
    }

    override fun doOnDetached() {}

    fun setViewModel(vm: MainFViewModel) {
        mainViewModel = vm
        binding.vm = vm
    }

}