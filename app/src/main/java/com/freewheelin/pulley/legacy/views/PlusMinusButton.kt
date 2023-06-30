package com.freewheelin.pulley.legacy.views

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.freewheelin.pulley.R

class PlusMinusButton: ConstraintLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    var repeatUpdateHandler = Handler(Looper.getMainLooper())
    var autoIncrement = false
    var autoDecrement = false

    var cnt: Int
        get() {
            return cntTv.text.toString().toInt()
        }
        set(value) {
            cntTv.text = value.toString()
        }

    var plusBtn: ImageButton
    var cntTv: TextView
    var minusBtn: ImageButton

    init {
        LayoutInflater.from(context).inflate(R.layout.button_plus_minus, this)

        plusBtn = findViewById(R.id.plusBtn)
        cntTv = findViewById(R.id.cntTv)
        minusBtn = findViewById(R.id.minusBtn)

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