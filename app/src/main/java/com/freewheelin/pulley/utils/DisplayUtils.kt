package com.freewheelin.pulley.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager


class DisplayUtils {
    companion object {
        val statusbarHeight = 24

        fun getStatusbarHeight(context: Context): Int {
            var result = 0
            val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = context.resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

        fun getScreenWidth(context: Context): Int {
            var metrics = DisplayMetrics()
            var display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            display.getMetrics(metrics)

            return metrics.widthPixels
        }

        fun getScreenHeight(context: Context): Int {
            var metrics = DisplayMetrics()
            var display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            display.getMetrics(metrics)

            return metrics.heightPixels

        }


        fun getBgImgFolder(context: Context): String {
            val density = context.resources.displayMetrics.density
            if(density > 0 && density <= 1) {
                return "1x"
            } else if(density > 1 && density <= 1.5)
                return "1.5x"
            else if(density > 1.5 && density <= 2)
                return "2x"
            else
                return "3x"
        }
    }
}