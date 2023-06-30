package com.freewheelin.pulley.legacy.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.View

class BoongthEffect: View.OnTouchListener {
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        if (motionEvent.action == MotionEvent.ACTION_CANCEL || motionEvent.action == MotionEvent.ACTION_UP) {
            AnimUtils.startExpandAnim(view)
        } else {
            AnimUtils.startScaleAnim(view)
        }
        return false
    }

}
object AnimUtils {

    var scaleAnim: ValueAnimator? = null
    var expandAnim: ValueAnimator? = null

    fun startExpandAnim(itemView: View, cb: (() -> Unit)? = null) {
        val fromScale = itemView.scaleX
        scaleAnim?.cancel()
        scaleAnim = null
        if (expandAnim == null) {
            expandAnim = ValueAnimator.ofFloat(fromScale, 1f)
            expandAnim?.addUpdateListener {
                val value = it.animatedValue as Float
                itemView.scaleX = value
                itemView.scaleY = value
            }
            expandAnim?.duration = (3000 * (1 - fromScale)).toLong()
            expandAnim?.start()
        }

        expandAnim?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator) {}

            override fun onAnimationEnd(p0: Animator) {
                if (cb != null)
                    cb()
            }

            override fun onAnimationCancel(p0: Animator) {}

            override fun onAnimationStart(p0: Animator) {}

        })
    }

    fun startScaleAnim(itemView: View, cb: (() -> Unit)? = null) {
        val fromScale = itemView.scaleX
        expandAnim?.cancel()
        expandAnim = null
        if (scaleAnim == null) {
            scaleAnim = ValueAnimator.ofFloat(fromScale, 0.95f)
            scaleAnim?.addUpdateListener {
                val value = it.animatedValue as Float
                itemView.scaleX = value
                itemView.scaleY = value
            }
            scaleAnim?.duration = (3000 * (fromScale - 0.95)).toLong()
            scaleAnim?.start()
        }

        scaleAnim?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator) {}

            override fun onAnimationEnd(p0: Animator) {
                if (cb != null)
                    cb()
            }

            override fun onAnimationCancel(p0: Animator) {}

            override fun onAnimationStart(p0: Animator) {}

        })
    }

    fun smoothAppearAnim(v: View, cb: (() -> Unit)? = null) {
        v.alpha = 0f
        v.animate()
            .alpha(1f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    cb?.invoke()
                }
            })
            .duration = v.context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
    }
    fun smoothDisappearAnim(v: View, cb: (() -> Unit)? = null) {
        v.alpha = 1f
        v.animate()
            .alpha(0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    v.visibility = View.GONE
                    cb?.invoke()
                }
            })
            .duration = v.context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

    }
}