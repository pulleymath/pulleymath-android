package com.freewheelin.pulley.legacy.activities.solve

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.utils.toPx
import com.freewheelin.pulley.legacy.views.memoView.MemoView

class SolveGestures: ProblemGestures {
    val context: Context
    var topContainer: View
    constructor(context: Context,
                imageView: ImageView,
                memoView: MemoView,
                container: View
                ) : super(context, imageView, memoView) {
        this.context = context
        this.topContainer = container
    }

    override fun getIvY(): Float {
        val topMargin = context.resources.getDimension(R.dimen.dp32) * scaleFactor
        return super.getIvY() + (8.toPx() + topContainer.height) * scaleFactor + topMargin
    }

    override fun setViewPosition() {
        super.setViewPosition()
        topContainer.x = getAnswerX()
        topContainer.y = getAnswerY()
    }

    override fun setViewScale() {
        super.setViewScale()
        topContainer.scaleX = scaleFactor
        topContainer.scaleY = scaleFactor

    }
    fun getAnswerX(): Float {
        val startMargin = context.resources.getDimension(R.dimen.dp32) * scaleFactor
        return memoView.x + 0.5f * (1 - scaleFactor) * (memoView.measuredWidth - topContainer.measuredWidth) + startMargin
    }

    fun getAnswerY(): Float {
        val topMargin = context.resources.getDimension(R.dimen.dp32) * scaleFactor
        return memoView.y + 0.5f * (1 - scaleFactor) * (memoView.measuredHeight - topContainer.measuredHeight) + topMargin
    }

    override fun init() {
        scaleFactor = 1f
        imageView.scaleX = 1f
        imageView.scaleY = 1f

        memoView.scaleX = 1f
        memoView.scaleY = 1f
        memoView.x = 0f
        memoView.y = 0f

        topContainer.scaleX = 1f
        topContainer.scaleY = 1f
        topContainer.x = getAnswerX()
        topContainer.y = getAnswerY()
        imageView.x = getIvX()
        imageView.y = getIvY()
    }
}
