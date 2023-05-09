package com.freewheelin.pulley.revision2023.ui.view

import android.content.Context
import android.graphics.drawable.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CompoundButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.databinding.ViewSchoolSwitchBinding
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.repository.UserRepository

class SchoolSwitch: ConstraintLayout {

    private val userRepository by lazy { UserRepository.instance }

    var binding: ViewSchoolSwitchBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_school_switch, this, true)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    lateinit var checkedAnim: AnimationDrawable
    lateinit var uncheckedAnim: AnimationDrawable

    init {

        binding.apply {
            isMiddleSchool = schoolType.isMiddle
            schoolSwitch.setOnCheckedChangeListener { btn, isChecked ->
                val type = if (isChecked) {
                    SchoolType.MIDDLE
                } else {
                    SchoolType.HIGH
                }
                isMiddleSchool = isChecked
                userRepository.updateSchoolType(type)
            }


        }
    }
    fun changeSchoolType(isMiddle: Boolean) {
        binding.schoolSwitch.isChecked = isMiddle
    }

    fun setTransition(isChecked: Boolean, btn: CompoundButton) {

        val drawables: Array<Drawable> = if (isChecked) arrayOf(
            ContextCompat.getDrawable(context, R.drawable.switch_track_off_highschool)!!,
            ContextCompat.getDrawable(context, R.drawable.switch_track_on_middleschool)!!
        ) else {
            arrayOf(
                ContextCompat.getDrawable(context, R.drawable.switch_track_on_middleschool)!!,
                ContextCompat.getDrawable(context, R.drawable.switch_track_off_highschool)!!
            )
        }
        val transitionDrawable = TransitionDrawable(drawables)
        btn.background = transitionDrawable
        transitionDrawable.startTransition(400)
    }
}