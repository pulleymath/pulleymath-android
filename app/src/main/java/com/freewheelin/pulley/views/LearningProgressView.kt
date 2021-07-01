package com.freewheelin.pulley.views

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.cardview.widget.CardView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.freewheelin.pulley.R
import kotlinx.android.synthetic.main.view_learning_progress.view.*


class LearningProgressView: CardView {
    var completeColor: Int = ContextCompat.getColor(context, R.color.purple_6D6DFF)
    var progressColor: Int =  ContextCompat.getColor(context, R.color.purple_6D6DFF)
    set(value) {
        field = value
        progressView.setBackgroundColor(value)
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setTypedArray(attrs)
    }

    var value: Float = 0f
    set(value) {
        field = value
        val layoutParams = progressView.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = value
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_learning_progress, this)
        elevation = 0f
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.LearningProgressView)

        progressColor = array.getColor(R.styleable.LearningProgressView_progressColor, ContextCompat.getColor(context, R.color.purple_6D6DFF))
        val backgroundColor = array.getColor(R.styleable.LearningProgressView_backgroundColor, ContextCompat.getColor(context, R.color.white_ffffff))
        completeColor = array.getColor(R.styleable.LearningProgressView_completeColor, ContextCompat.getColor(context, R.color.purple_6D6DFF))

        backgroundLl.setBackgroundColor(backgroundColor)
        array.recycle()
    }


    fun set(percentage: Float, withAnim: Boolean = false, duration: Long = 800, delay: Long = 700, listener: Animator.AnimatorListener? = null) {
        if(withAnim) {
            set(0f)
            val animator = ObjectAnimator.ofFloat(this, "value", 0f, percentage)
            animator.duration = duration
            animator.startDelay = delay
            animator.addUpdateListener {
                progressView.requestLayout()
            }
            animator.addListener(listener)
            animator.start()
        } else {
            value = percentage
            progressView.requestLayout()
            if (percentage >= 1f)
                progressView.setBackgroundColor(completeColor)
            else
                progressView.setBackgroundColor(progressColor)
        }
    }
}


