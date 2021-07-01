package com.freewheelin.pulley.views

import android.content.Context
import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.utils.hide
import com.freewheelin.pulley.utils.show
//import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import kotlinx.android.synthetic.main.view_timer_vertical.view.*
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
            return ContextCompat.getColor(context, R.color.purple_6D6DFF)
        else
            return ContextCompat.getColor(context, R.color.purple_ACACFF)
    }

    var submitType = SubmitType.strict
        set(value) {
            field = value
            listener?.onSubmitTypeChanged(value)
        }

    constructor(context: Context, orientation: Int) : super(context) {
        this.orientation = orientation
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setTypedArray(attrs)
        initView()
    }

    fun setTimerViewListener(listener: TimerViewListener) {
        this.listener = listener
    }

    private fun initView() {
        when (orientation) {
            VERTICAL -> LayoutInflater.from(context).inflate(R.layout.view_timer_vertical, this)
            HORIZONTAL -> LayoutInflater.from(context).inflate(R.layout.view_timer_horizontal, this)
            SOLVE -> LayoutInflater.from(context).inflate(R.layout.view_timer_solve, this)
        }

        timerSwitch.setOnCheckedChangeListener { button, isChecked ->
            if(isChecked) {
                hourMinTv.visibility = View.VISIBLE
                secTv.visibility = View.VISIBLE
                timerPlayBtn.visibility = View.VISIBLE
                hideGuideLabel?.visibility = View.INVISIBLE

                val timeLimitSec = timeLimit - elapsedTime
                if(timeLimitSec < 0 )
                    overTimerTv?.visibility = View.VISIBLE
            } else {
                if(orientation == SOLVE) {
                    hourMinTv.visibility = View.GONE
                    secTv.visibility = View.GONE
                    timerPlayBtn.visibility = View.GONE
                } else {
                    hourMinTv.visibility = View.INVISIBLE
                    secTv.visibility = View.INVISIBLE
                    timerPlayBtn.visibility = View.INVISIBLE
                    hideGuideLabel?.visibility = View.VISIBLE
                }

                val timeLimitSec = timeLimit - elapsedTime
                if(timeLimitSec < 0 )
                    overTimerTv?.visibility = View.GONE
            }
            this.listener?.onTimerSwitchChecked()
        }

        timerSwitch.isChecked = true
        timerPlayBtn.setOnClickListener {
            it.isSelected = !it.isSelected

            if (it.isSelected)
                stop()
            else
                runTimer()
            this.listener?.onTimerStopClicked()
        }
        hideGuideLabel?.visibility = View.GONE
        overTimerTv?.visibility = View.GONE

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

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.TimerView)
        val rawValueForOrientation = array.getInt(R.styleable.TimerView_orientation, VERTICAL)
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
        typeRg.hide()
    }

    fun showOverTimerView() {
        overTimerTv?.show()
    }

    fun isTimerShown(): Boolean {
        return timerSwitch.isChecked
    }

    private fun setTimerText(timeLimitSec: Int) {
        val hour = timeLimitSec / 3600
        val min = (timeLimitSec - (hour * 3600)) / 60
        val sec = timeLimitSec % 60

        if (timeLimitSec <= 300) {
            hourMinTv.setTextColor(Color.parseColor("#fe7b67"))
            secTv.setTextColor(Color.parseColor("#fe7b67"))
        }
        hourMinTv.text = hour.toString() + ":" + String.format("%02d", min)
        secTv.text = ":" + String.format("%02d", sec)
    }

    private fun setOvertimerText(timeLimitSec: Int) {
        val overtimeSec = timeLimitSec * -1
        val hour = overtimeSec / 3600
        val min = (overtimeSec - (hour * 3600)) / 60
        val sec = overtimeSec % 60

        if (timeLimitSec <= 300) {
            hourMinTv.setTextColor(Color.parseColor("#fe7b67"))
            secTv.setTextColor(Color.parseColor("#fe7b67"))
        }
        hourMinTv.text = "0:00"
        secTv.text = ":00"

        overTimerTv?.text = "+ ${hour}:${String.format("%02d",min)}:${String.format("%02d",sec)}"
        if(orientation == SOLVE) {
            hourMinTv.text = "- ${hour}:${String.format("%02d", min)}"
            secTv.text = ":" + String.format("%02d", sec)
        }
    }

    fun deinitTimer() {
        timer?.cancel()
        timer = null
    }

    fun setLenientOvetimeUI() {
        hideTypeRadio()

        if(isTimerShown())
            overTimerTv?.visibility = View.VISIBLE
    }

}
