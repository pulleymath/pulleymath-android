package com.freewheelin.pulley.legacy.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
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
        fun setFullScreen (activity: Activity) {
            val window = activity.window
            val actionBar = activity.actionBar
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
                if (window.insetsController != null) {
                    window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    window.insetsController?.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            }


            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN)
//                hideSystemUI()
            } else {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
                actionBar?.hide()
            }
        }
    }
}