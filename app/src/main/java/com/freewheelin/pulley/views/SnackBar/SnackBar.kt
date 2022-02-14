package com.freewheelin.pulley.views.snackBar

import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import android.view.*
import android.widget.PopupWindow
import com.freewheelin.pulley.utils.DisplayUtils
import com.freewheelin.pulley.utils.toPx

class SnackBar : PopupWindow {

    var context: Context
    var contentText: String = ""
        set(value) {
            field = value
            (contentView as SnackBarView).snackContentTv.text = value
        }
    var actionText: String = ""
        set(value) {
            field = value
            (contentView as SnackBarView).snackActionBtn.text = value
        }

    var listener: SnackBarViewListener? = null


    private val bottomMargin = 16
    private val leftMargin = 24

    constructor(context: Context, contentText: String, actionText: String) : super(context) {
        this.context = context
        this.contentView = SnackBarView(context,contentText, actionText)
        this.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
        this.width = ConstraintLayout.LayoutParams.WRAP_CONTENT

        setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context, android.R.color.transparent)))
    }

    fun setSnackBarViewListener(listener: SnackBarViewListener) {
        this.listener = listener
        (contentView as SnackBarView).setSnackBarViewListener(listener)
    }

    fun show() {
        var screenHeight = DisplayUtils.getScrenHeight(context)


        val yOffset = screenHeight - bottomMargin.toPx() - (contentView as SnackBarView).snackBarViewHeight.toPx()
        showAtLocation(contentView, Gravity.NO_GRAVITY, leftMargin.toPx(), yOffset)
    }
}