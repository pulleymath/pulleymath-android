package com.freewheelin.pulley.views

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.cardview.widget.CardView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewLearningProgressBinding


class LearningProgressView: CardView {
    var completeColor: Int = ContextCompat.getColor(context, R.color.purple_300)
    var progressColor: Int =  ContextCompat.getColor(context, R.color.purple_300)
    set(value) {
        field = value
        binding.progressView.setBackgroundColor(value)
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setTypedArray(attrs)
    }

    var value: Float = 0f
    set(value) {
        field = value
        binding.progressView.layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, value)
    }
    var binding: ViewLearningProgressBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_learning_progress, this, true)

    init {
        elevation = 0f
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.LearningProgressView)

        progressColor = array.getColor(R.styleable.LearningProgressView_progressColor, ContextCompat.getColor(context, R.color.purple_300))
        val backgroundColor = array.getColor(R.styleable.LearningProgressView_backgroundColor, ContextCompat.getColor(context, R.color.white))
        completeColor = array.getColor(R.styleable.LearningProgressView_completeColor, ContextCompat.getColor(context, R.color.purple_300))

        binding.backgroundLl.setBackgroundColor(backgroundColor)
        array.recycle()
    }


    fun set(percentage: Float, withAnim: Boolean = false, duration: Long = 800, delay: Long = 700, listener: Animator.AnimatorListener? = null) {
        if(withAnim) {
            set(0f)
            val animator = ObjectAnimator.ofFloat(this, "value", 0f, percentage)
            animator.duration = duration
            animator.startDelay = delay
            animator.addListener(listener)
            animator.start()
        } else {
            value = percentage
            binding.progressView.requestLayout()
            if (percentage >= 1f)
                binding.progressView.setBackgroundColor(completeColor)
            else
                binding.progressView.setBackgroundColor(progressColor)
        }
    }
}


