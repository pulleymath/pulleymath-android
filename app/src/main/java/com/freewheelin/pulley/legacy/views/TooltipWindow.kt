package com.freewheelin.pulley.legacy.views

import android.R
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.views.balloonWindow.BalloonWindow

class TooltipWindow: BalloonWindow {


    constructor(context: Context, targetView: View, position: Position, offset: Int = 0) : super(context, targetView, position, offset) {
        this.windowLayoutType = WindowManager.LayoutParams.LAST_APPLICATION_WINDOW
    }

    override fun getTargetAbsolutePosition(view: View): Pair<Int, Int> {
        val value =  super.getTargetAbsolutePosition(view)
        return Pair(value.first, value.second - DisplayUtils.getStatusbarHeight(context))
    }

}