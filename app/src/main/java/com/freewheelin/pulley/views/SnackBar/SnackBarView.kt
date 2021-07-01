package com.freewheelin.pulley.views.SnackBar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.freewheelin.pulley.utils.toPx
import kotlinx.android.synthetic.main.view_snack_bar.view.*


interface SnackBarViewListener {
    fun onXBtnClicked(view: SnackBarView)
    fun onActionBtnClicked(view: SnackBarView)
}

class SnackBarView : ConstraintLayout {
    var listener: SnackBarViewListener? = null
    val snackBarViewHeight = 48

    var contentText: String? = null
    var actionText: String? = null
    constructor(context: Context, contentText: String, actionText: String): super(context) {
        this.contentText = contentText
        this.actionText = actionText
        snackActionBtn.text = actionText
        snackContentTv.text = contentText
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setTypedArray(attrs)
        snackActionBtn.text = actionText
        snackContentTv.text = contentText
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_snack_bar, this)

        this.background = ContextCompat.getDrawable(context, R.drawable.bg_black_333333_round)

        val lParams = ViewGroup.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, snackBarViewHeight.toPx())
        this.layoutParams = lParams
        xBtn.setOnClickListener {
            listener?.onXBtnClicked(this)
        }

        snackActionBtn.setOnClickListener {
            listener?.onActionBtnClicked(this)
        }
    }

    fun setSnackBarViewListener(listener: SnackBarViewListener) {
        this.listener = listener
    }

    private fun setTypedArray(attrs: AttributeSet) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.SnackBarView)
        contentText = array.getString(R.styleable.SnackBarView_contentText)
        actionText = array.getString(R.styleable.SnackBarView_actionText)
        array.recycle()

    }
}