package com.pulleymath.android.pdf.utils

import android.os.Handler
import android.os.Looper
import android.view.View

class OnDebounceClickListener(
    private val clickListener: View.OnClickListener,
    private val interval: Long = 1000
) :
    View.OnClickListener {

    private var isWaitingExecutionSignal = false

    private var handler: Handler? = null
    override fun onClick(v: View?) {
        if (isWaitingExecutionSignal) handler?.removeCallbacksAndMessages(null)
        else isWaitingExecutionSignal = true

        addHandler(v)
    }

    private fun addHandler(v: View?) {
        handler = Handler(Looper.getMainLooper())
        handler!!.postDelayed({
            isWaitingExecutionSignal = false
            clickListener.onClick(v)
        }, interval)
    }
}
