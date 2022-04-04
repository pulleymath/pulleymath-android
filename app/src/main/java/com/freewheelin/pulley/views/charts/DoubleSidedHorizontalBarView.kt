package com.freewheelin.pulley.views.charts

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ViewBarDoublesidedHorizontalBinding
import com.freewheelin.pulley.utils.TextUtils
import com.freewheelin.pulley.utils.show

// 220331 안쓰는것같다.
class DoubleSidedHorizontalBarView: ConstraintLayout {
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    var animator: ValueAnimator? = null
    var binding: ViewBarDoublesidedHorizontalBinding =
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_bar_doublesided_horizontal, this, true)

    init {
//        LayoutInflater.from(context).inflate(R.layout.view_bar_doublesided_horizontal, this)
        binding.positiveArrowIv.setColorFilter(ContextCompat.getColor(context, R.color.blue_30a4ff))
        binding.negativeArrowIv.setColorFilter(ContextCompat.getColor(context, R.color.red_fe7b67))
    }

    fun setValue(value: Float?, withAnim: Boolean = false) {
        with(binding) {
            zeroLabel.visibility = View.GONE

            if(value == null) {
                hideNegativeSide()
                hidePositiveSide()
                return
            }

            val widthValue: Int = (resources.getDimension(R.dimen.doubly_side_bar_width) * value).toInt()

            if(value > 0) {
                hideNegativeSide()
                showPositiveSide()
                if(withAnim == false) {
                    positiveTv.text = TextUtils.percentFormat.format(value)
                    positiveBar.layoutParams.width = widthValue
                } else {
                    positiveTv.visibility = View.INVISIBLE
                    positiveArrowIv.visibility = View.INVISIBLE

                    animator = ValueAnimator.ofInt(0, widthValue)

                    animator!!.duration = 500
                    animator!!.addUpdateListener {
                        val widthVal = it.animatedValue as Int
                        positiveBar.layoutParams.width = widthVal
                        this@DoubleSidedHorizontalBarView.requestLayout()
                    }
                    animator!!.start()
                }
            } else if(value < 0) {
                hidePositiveSide()
                showNegatvieSide()
                if(withAnim == false) {
                    negativeTv.text = TextUtils.percentFormat.format(value)
                    negativeBar.layoutParams.width = widthValue * -1
                } else {
                    negativeTv.visibility = View.INVISIBLE
                    negativeArrowIv.visibility = View.INVISIBLE

                    animator = ValueAnimator.ofInt(0, widthValue * -1)

                    animator!!.duration = 500
                    animator!!.addUpdateListener {
                        val widthVal = it.animatedValue as Int
                        negativeBar.layoutParams.width = widthVal
                        this@DoubleSidedHorizontalBarView.requestLayout()
                    }
                    animator!!.start()
                }
            } else {
                hidePositiveSide()
                hideNegativeSide()
                zeroLabel.show()
            }
        }
    }


    private fun showNegatvieSide() {
        with(binding) {
            negativeArrowIv.visibility = View.VISIBLE
            negativeTv.visibility = View.VISIBLE
            negativeBar.visibility = View.VISIBLE
        }
    }

    private fun hideNegativeSide() {
        with(binding) {
            negativeArrowIv.visibility = View.GONE
            negativeTv.visibility = View.GONE
            negativeBar.visibility = View.GONE
        }
    }

    private fun showPositiveSide() {
        with(binding) {
            positiveArrowIv.visibility = View.VISIBLE
            positiveTv.visibility = View.VISIBLE
            positiveBar.visibility = View.VISIBLE
        }
    }

    private fun hidePositiveSide() {
        with(binding) {
            positiveArrowIv.visibility = View.GONE
            positiveTv.visibility = View.GONE
            positiveBar.visibility = View.GONE
        }
    }

}