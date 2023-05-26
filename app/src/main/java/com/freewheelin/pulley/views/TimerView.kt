package com.freewheelin.pulley.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewTimerHorizontalBinding
import com.freewheelin.pulley.databinding.ViewTimerSolveBinding
import com.freewheelin.pulley.databinding.ViewTimerVerticalBinding
import com.freewheelin.pulley.utils.hide
import com.freewheelin.pulley.utils.show
import java.util.*
import kotlin.concurrent.timerTask

interface TimerViewListener {
    fun onSubmitTypeChanged(submitType: TimerView.SubmitType)
    fun onTimerExpired(timerView: TimerView, type: TimerView.SubmitType)
    fun onTimerSwitchChecked() {}
    fun onTimerStopClicked() {}
}

class TimerView : ConstraintLayout {
    enum class State {
        progress,
        finish,
        expired
    }

    enum class SubmitType {
        strict,
        lenient
    }

    private val VERTICAL = 0
    private val HORIZONTAL = 1
    private val SOLVE = 2

    var orientation = VERTICAL

    val timeLimit = 6000
    var elapsedTime = 0
        set(value) {
            field = value
            val timeLimitSec = timeLimit - elapsedTime
            rootView.post {
                if (timeLimitSec == 0) {
                    if(submitType == SubmitType.lenient)
                        setLenientOvetimeUI()

                    listener?.onTimerExpired(this, this@TimerView.submitType)
                }

                if (timeLimitSec >= 0)
                    setTimerText(timeLimitSec)
                else
                    setOvertimerText(timeLimitSec)
            }
        }

    var timer: Timer? = null
    var listener: TimerViewListener? = null
    val checkedTextColor: Int
        get() {
            if(orientation == SOLVE)
                return ContextCompat.getColor(context, R.color.purple_300)
            else
                return ContextCompat.getColor(context, R.color.purple_200)
        }

    var submitType = SubmitType.strict
        set(value) {
            field = value
            listener?.onSubmitTypeChanged(value)
        }
    var overTimerTextView: TextView? = null
        get() {
            return when (orientation) {
                VERTICAL -> { (binding as ViewTimerVerticalBinding).overTimerTv }
                HORIZONTAL -> { (binding as ViewTimerHorizontalBinding).overTimerTv }
                SOLVE -> { null }
                else -> null
            }
        }

    var typeRadioGroup: RadioGroup? = null
        get() {
            return when (orientation) {
                VERTICAL -> { (binding as ViewTimerVerticalBinding).typeRg }
                HORIZONTAL -> { (binding as ViewTimerHorizontalBinding).typeRg }
                SOLVE -> { (binding as ViewTimerSolveBinding).typeRg }
                else -> (binding as ViewTimerSolveBinding).typeRg
            }
        }

    var timerSwitch: Switch
        get() {
            return when (orientation) {
                VERTICAL -> { (binding as ViewTimerVerticalBinding).timerSwitch }
                HORIZONTAL -> { (binding as ViewTimerHorizontalBinding).timerSwitch }
                SOLVE -> { (binding as ViewTimerSolveBinding).timerSwitch }
                else -> (binding as ViewTimerSolveBinding).timerSwitch
            }
        }
        set(value) {}
    var hourMinTv: TextView? = null
        get() {
            return when (orientation) {
                VERTICAL -> { (binding as ViewTimerVerticalBinding).hourMinTv }
                HORIZONTAL -> { (binding as ViewTimerHorizontalBinding).hourMinTv }
                SOLVE -> { (binding as ViewTimerSolveBinding).hourMinTv }
                else -> (binding as ViewTimerSolveBinding).hourMinTv
            }
        }
    var secTv: TextView? = null
        get() {
            return when (orientation) {
                VERTICAL -> { (binding as ViewTimerVerticalBinding).secTv }
                HORIZONTAL -> { (binding as ViewTimerHorizontalBinding).secTv }
                SOLVE -> { (binding as ViewTimerSolveBinding).secTv }
                else -> (binding as ViewTimerSolveBinding).secTv
            }
        }

    constructor(context: Context, orientation: Int) : super(context) {
        this.orientation = orientation
        initBinding()
        initView()

    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setTypedArray(attrs)
        initBinding()
        initView()
    }
    init {

    }
    lateinit var binding: ViewDataBinding

    private fun initBinding() {
        binding = when (orientation) {
            VERTICAL -> { DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_timer_vertical, null, false) }
            HORIZONTAL -> { DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_timer_horizontal, null, false) }
            SOLVE -> { DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_timer_solve, null, false) }
            else -> DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_timer_vertical, null, false)
        }
    }

    private fun initView () {
        // hideGuideLabel, overTimerTv 는 solve에 없음
        when (orientation) {
            VERTICAL -> {
                with(binding as ViewTimerVerticalBinding) {
                    timerSwitch.setOnCheckedChangeListener { button, isChecked ->
                        if(isChecked) {
                            hourMinTv.visibility = View.VISIBLE
                            secTv.visibility = View.VISIBLE
                            timerPlayBtn.visibility = View.VISIBLE

                            hideGuideLabel.visibility = View.INVISIBLE
                            val timeLimitSec = timeLimit - elapsedTime
                            if(timeLimitSec < 0 )
                                overTimerTv.visibility = View.VISIBLE
                        } else {
                            if(orientation == SOLVE) {
                                hourMinTv.visibility = View.GONE
                                secTv.visibility = View.GONE
                                timerPlayBtn.visibility = View.GONE
                            } else {
                                hourMinTv.visibility = View.INVISIBLE
                                secTv.visibility = View.INVISIBLE
                                timerPlayBtn.visibility = View.INVISIBLE
                                hideGuideLabel.visibility = View.VISIBLE
                            }

                            val timeLimitSec = timeLimit - elapsedTime
                            if(timeLimitSec < 0 )
                                overTimerTv.visibility = View.GONE
                        }
                        listener?.onTimerSwitchChecked()
                    }

                    timerSwitch.isChecked = true
                    timerPlayBtn.setOnClickListener {
                        it.isSelected = !it.isSelected

                        if (it.isSelected)
                            stop()
                        else
                            runTimer()
                        listener?.onTimerStopClicked()
                    }
                    hideGuideLabel.visibility = View.GONE
                    overTimerTv.visibility = View.GONE

                    strictRb.setOnCheckedChangeListener { button, isChecked ->
                        val color = if (isChecked) checkedTextColor else Color.parseColor("#ffffff")
                        button.setTextColor(color)
                        if(isChecked)
                            submitType = SubmitType.strict
                    }

                    lenientRb.setOnCheckedChangeListener { button, isChecked ->
                        val color = if (isChecked) checkedTextColor else Color.parseColor("#ffffff")
                        button.setTextColor(color)
                        if(isChecked)
                            submitType = SubmitType.lenient
                    }
                }
            }
            HORIZONTAL -> {
                with(binding as ViewTimerHorizontalBinding) {
                    timerSwitch.setOnCheckedChangeListener { button, isChecked ->
                        if(isChecked) {
                            hourMinTv.visibility = View.VISIBLE
                            secTv.visibility = View.VISIBLE
                            timerPlayBtn.visibility = View.VISIBLE

                            hideGuideLabel.visibility = View.INVISIBLE
                            val timeLimitSec = timeLimit - elapsedTime
                            if(timeLimitSec < 0 )
                                overTimerTv.visibility = View.VISIBLE
                        } else {
                            if(orientation == SOLVE) {
                                hourMinTv.visibility = View.GONE
                                secTv.visibility = View.GONE
                                timerPlayBtn.visibility = View.GONE
                            } else {
                                hourMinTv.visibility = View.INVISIBLE
                                secTv.visibility = View.INVISIBLE
                                timerPlayBtn.visibility = View.INVISIBLE
                                hideGuideLabel.visibility = View.VISIBLE
                            }

                            val timeLimitSec = timeLimit - elapsedTime
                            if(timeLimitSec < 0 )
                                overTimerTv.visibility = View.GONE
                        }
                        listener?.onTimerSwitchChecked()
                    }

                    timerSwitch.isChecked = true
                    timerPlayBtn.setOnClickListener {
                        it.isSelected = !it.isSelected

                        if (it.isSelected)
                            stop()
                        else
                            runTimer()
                        listener?.onTimerStopClicked()
                    }
                    hideGuideLabel.visibility = View.GONE
                    overTimerTv.visibility = View.GONE

                    strictRb.setOnCheckedChangeListener { button, isChecked ->
                        val color = if (isChecked) checkedTextColor else Color.parseColor("#ffffff")
                        button.setTextColor(color)
                        if(isChecked)
                            submitType = SubmitType.strict
                    }

                    lenientRb.setOnCheckedChangeListener { button, isChecked ->
                        val color = if (isChecked) checkedTextColor else Color.parseColor("#ffffff")
                        button.setTextColor(color)
                        if(isChecked)
                            submitType = SubmitType.lenient
                    }
                }
            }
            SOLVE -> {
                with(binding as ViewTimerSolveBinding) {

                    timerSwitch.setOnCheckedChangeListener { button, isChecked ->
                        if(isChecked) {
                            hourMinTv.visibility = View.VISIBLE
                            secTv.visibility = View.VISIBLE
                            timerPlayBtn.visibility = View.VISIBLE

                        } else {
                            if(orientation == SOLVE) {
                                hourMinTv.visibility = View.GONE
                                secTv.visibility = View.GONE
                                timerPlayBtn.visibility = View.GONE
                            } else {
                                hourMinTv.visibility = View.INVISIBLE
                                secTv.visibility = View.INVISIBLE
                                timerPlayBtn.visibility = View.INVISIBLE
                            }
                        }
                        listener?.onTimerSwitchChecked()
                    }

                    timerSwitch.isChecked = true
                    timerPlayBtn.setOnClickListener {
                        it.isSelected = !it.isSelected

                        if (it.isSelected)
                            stop()
                        else
                            runTimer()
                        listener?.onTimerStopClicked()
                    }

                    strictRb.setOnCheckedChangeListener { button, isChecked ->
                        val color = if (isChecked) checkedTextColor else Color.parseColor("#ffffff")
                        button.setTextColor(color)
                        if(isChecked)
                            submitType = SubmitType.strict
                    }

                    lenientRb.setOnCheckedChangeListener { button, isChecked ->
                        val color = if (isChecked) checkedTextColor else Color.parseColor("#ffffff")
                        button.setTextColor(color)
                        if(isChecked)
                            submitType = SubmitType.lenient
                    }
                }
            }
            else -> {

            }
        }

        timerSwitch = when (orientation) {
            VERTICAL -> { (binding as ViewTimerVerticalBinding).timerSwitch }
            HORIZONTAL -> { (binding as ViewTimerHorizontalBinding).timerSwitch }
            SOLVE -> { (binding as ViewTimerSolveBinding).timerSwitch }
            else -> (binding as ViewTimerSolveBinding).timerSwitch
        }
    }

    fun setTimerViewListener(listener: TimerViewListener) {
        this.listener = listener
    }
    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.PulleyTimerView)
        val rawValueForOrientation = array.getInt(R.styleable.PulleyTimerView_orientation_subname, VERTICAL)
        orientation = rawValueForOrientation
        array.recycle()
    }

    fun runTimer() {
        timer = Timer()
        val task = timerTask {
            elapsedTime += 1
        }

        timer?.schedule(task, 1000, 1000)
    }

    fun stop() {
        timer?.cancel()
    }

    fun hideTypeRadio() {
        typeRadioGroup?.hide()
    }

    fun showOverTimerView() {
        overTimerTextView?.show()
    }

    fun isTimerShown(): Boolean {
        return timerSwitch.isChecked
    }

    private fun setTimerText(timeLimitSec: Int) {
        val hour = timeLimitSec / 3600
        val min = (timeLimitSec - (hour * 3600)) / 60
        val sec = timeLimitSec % 60

        if (timeLimitSec <= 300) {
            hourMinTv?.setTextColor(Color.parseColor("#fe7b67"))
            secTv?.setTextColor(Color.parseColor("#fe7b67"))
        }
        hourMinTv?.text = hour.toString() + ":" + String.format("%02d", min)
        secTv?.text = ":" + String.format("%02d", sec)
    }

    private fun setOvertimerText(timeLimitSec: Int) {
        val overtimeSec = timeLimitSec * -1
        val hour = overtimeSec / 3600
        val min = (overtimeSec - (hour * 3600)) / 60
        val sec = overtimeSec % 60

        if (timeLimitSec <= 300) {
            hourMinTv?.setTextColor(Color.parseColor("#fe7b67"))
            secTv?.setTextColor(Color.parseColor("#fe7b67"))
        }
        hourMinTv?.text = "0:00"
        secTv?.text = ":00"

        overTimerTextView?.text = "+ ${hour}:${String.format("%02d",min)}:${String.format("%02d",sec)}"
        if(orientation == SOLVE) {
            hourMinTv?.text = "- ${hour}:${String.format("%02d", min)}"
            secTv?.text = ":" + String.format("%02d", sec)
        }
    }

    fun deinitTimer() {
        timer?.cancel()
        timer = null
    }

    fun setLenientOvetimeUI() {
        hideTypeRadio()

        if(isTimerShown())
            overTimerTextView?.visibility = View.VISIBLE
    }

}
