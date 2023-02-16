package com.freewheelin.pulley.revision2023.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewMissionStampBinding
import com.freewheelin.pulley.revision2021.utils.getLifecycleOwner
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeCourse

class MissionStampView: ConstraintLayout {
    constructor(context: Context, course: ChallengeCourse): super(context) { setCourse(course) }
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    var binding: ViewMissionStampBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_mission_stamp, this, true)

    init {
        binding.apply {
            lifecycleOwner = binding.root.findViewTreeLifecycleOwner()
        }
    }

    fun setCourse(course: ChallengeCourse) {
        binding.item = course
    }
}