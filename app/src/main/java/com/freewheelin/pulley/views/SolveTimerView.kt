package com.freewheelin.pulley.views

import android.content.Context
import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewTimerSolveBinding
import com.freewheelin.pulley.utils.hide
import java.util.*
import kotlin.concurrent.timerTask

interface SolveTimerViewListener {
    fun onSubmitTypeChanged(submitType: SolveTimerView.SubmitType)
    fun onTimerExpired(solveTimerView: SolveTimerView, type: SolveTimerView.SubmitType)
    fun onTimerSwitchChecked() {}
    fun onTimerStopClicked() {}
}

class SolveTimerView : ConstraintLayout {
    enum class State {
        progress,
        finish,
        expired
    }

    enum class SubmitType {
        strict,
        lenient
    }

    var orientation = 0

    val timeLimit = 6000
    var elapsedTime = 0
        set(value) {
            field = value
            val timeLimitSec = timeLimit - elapsedTime
            rootView.post {
                if (timeLimitSec == 0) {
                    if(submitType == SubmitType.lenient)
                        setLenientOvetimeUI()

                    listenerSolve?.onTimerExpired(this, this@SolveTimerView.submitType)
                }

                if (timeLimitSec >= 0)
                    setTimerText(timeLimitSec)
                else
                    setOvertimerText(timeLimitSec)
            }
        }

    var timer: Timer? = null
    var listenerSolve: SolveTimerViewListener? = null
    val checkedTextColor: Int
        get() {
            return ContextCompat.getColor(context, R.color.purple_6D6DFF)
        }

    var submitType = SubmitType.strict
        set(value) {
            field = value
            listenerSolve?.onSubmitTypeChanged(value)
        }

    constructor(context: Context, orientation: Int) : super(context) {
        this.orientation = orientation
        initView()

    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setTypedArray(attrs)
        initView()
    }

    var binding: ViewTimerSolveBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_timer_solve, this, true)

    init {
//        LayoutInflater.from(context).inflate(R.layout.view_timer_solve, this)
    }

    private fun initView () {
        with(binding) {
            timerSwitch.setOnCheckedChangeListener { button, isChecked ->
                if(isChecked) {
                    hourMinTv.visibility = View.VISIBLE
                    secTv.visibility = View.VISIBLE
                    timerPlayBtn.visibility = View.VISIBLE

                } else {
                    hourMinTv.visibility = View.GONE
                    secTv.visibility = View.GONE
                    timerPlayBtn.visibility = View.GONE
                }
                listenerSolve?.onTimerSwitchChecked()
            }

            timerSwitch.isChecked = true
            timerPlayBtn.setOnClickListener {
                it.isSelected = !it.isSelected

                if (it.isSelected)
                    stop()
                else
                    runTimer()
                listenerSolve?.onTimerStopClicked()
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

    fun setTimerViewListener(listenerSolve: SolveTimerViewListener) {
        this.listenerSolve = listenerSolve
    }
    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.PulleyTimerView)
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
        binding.typeRg.hide()
    }

    fun isTimerShown(): Boolean {
        return binding.timerSwitch.isChecked
    }

    private fun setTimerText(timeLimitSec: Int) {
        val hour = timeLimitSec / 3600
        val min = (timeLimitSec - (hour * 3600)) / 60
        val sec = timeLimitSec % 60

        if (timeLimitSec <= 300) {
            binding.hourMinTv.setTextColor(Color.parseColor("#fe7b67"))
            binding.secTv.setTextColor(Color.parseColor("#fe7b67"))
        }
        binding.hourMinTv.text = hour.toString() + ":" + String.format("%02d", min)
        binding.secTv.text = ":" + String.format("%02d", sec)
    }

    private fun setOvertimerText(timeLimitSec: Int) {
        val overtimeSec = timeLimitSec * -1
        val hour = overtimeSec / 3600
        val min = (overtimeSec - (hour * 3600)) / 60
        val sec = overtimeSec % 60

        if (timeLimitSec <= 300) {
            binding.hourMinTv.setTextColor(Color.parseColor("#fe7b67"))
            binding.secTv.setTextColor(Color.parseColor("#fe7b67"))
        }
        binding.hourMinTv.text = "0:00"
        binding.secTv.text = ":00"

        binding.hourMinTv.text = "- ${hour}:${String.format("%02d", min)}"
        binding.secTv.text = ":" + String.format("%02d", sec)
    }

    fun deinitTimer() {
        timer?.cancel()
        timer = null
    }

    fun setLenientOvetimeUI() {
        hideTypeRadio()
    }

}
