package com.freewheelin.pulley.views

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import com.freewheelin.pulley.R
import kotlinx.android.synthetic.main.button_plus_minus.view.*
import kotlinx.android.synthetic.main.dialog_wrong_management.*

class PlusMinusButton: ConstraintLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    var repeatUpdateHandler = Handler(Looper.getMainLooper())
    var autoIncrement = false
    var autoDecrement = false

    var cnt: Int = 5
    set(value) {
        field = value
        cntTv.text = cnt.toString()
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.button_plus_minus, this)

        plusBtn.setOnClickListener {
            increment()
        }

        minusBtn.setOnClickListener {
            decrement()
        }

        plusBtn.setOnLongClickListener {
            autoIncrement = true
            repeatUpdateHandler.post(RptUpdater())
            false
        }

        plusBtn.setOnTouchListener { view, motionEvent ->
            if ((motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) && autoIncrement) {
                autoIncrement = false
            }
            false
        }

        minusBtn.setOnLongClickListener {
            autoDecrement = true
            repeatUpdateHandler.post(RptUpdater())
            false
        }

        minusBtn.setOnTouchListener{ _, motionEvent ->
            if ((motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) && autoDecrement) {
                autoDecrement = false
            }
            false
        }
    }


    fun increment() {
        if (cnt < 5)
            cnt += 1
    }

    fun decrement() {
        if (cnt > 1)
            cnt -= 1
    }

    inner class RptUpdater : Runnable {
        private var delay: Long

        constructor(delay: Long=300): super() {
            this.delay = delay
        }

        override fun run() {
            val postDelay = if (delay < 0)  50 else delay
            if (autoIncrement) {
                increment()
                repeatUpdateHandler.postDelayed(RptUpdater(delay - 150), postDelay)
            } else if (autoDecrement) {
                decrement()
                repeatUpdateHandler.postDelayed(RptUpdater(delay - 150), postDelay)
            }
        }
    }
}