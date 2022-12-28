package com.freewheelin.pulley.revision2023.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import com.facebook.FacebookSdk
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity

class CookingChromeClient(val activity: Activity): WebChromeClient() {

    // https://stackoverflow.com/questions/15768837/playing-html5-video-on-fullscreen-in-android-webview/56186877#56186877

    private var mCustomView: View? = null
    private var mCustomViewCallback: CustomViewCallback? = null
    private var mOriginalOrientation = 0
    private var mOriginalSystemUiVisibility = 0

    init {

    }

    override fun getDefaultVideoPoster(): Bitmap? {
        return if (mCustomView == null) {
            null
        } else BitmapFactory.decodeResource(FacebookSdk.getApplicationContext().resources, 2130837573)
    }

    override fun onHideCustomView() {
        val activity = (activity as LearningCourseActivity)
        (activity.window.decorView as FrameLayout).removeView(
            mCustomView
        )
        mCustomView = null
        activity.window.decorView.systemUiVisibility = mOriginalSystemUiVisibility
        activity.requestedOrientation = mOriginalOrientation
        mCustomViewCallback!!.onCustomViewHidden()
        mCustomViewCallback = null
    }

    override fun onShowCustomView(
        paramView: View?,
        paramCustomViewCallback: CustomViewCallback?
    ) {
        val activity = (activity as LearningCourseActivity)

        if (mCustomView != null) {
            onHideCustomView()
            return
        }
        mCustomView = paramView
        mOriginalSystemUiVisibility = activity.window.decorView.systemUiVisibility
        mOriginalOrientation = activity.requestedOrientation
        mCustomViewCallback = paramCustomViewCallback
        (activity.window.decorView as FrameLayout).addView(
            mCustomView,
            ViewGroup.LayoutParams(-1, -1)
        )
        activity.window.decorView.systemUiVisibility = 3846 or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
}