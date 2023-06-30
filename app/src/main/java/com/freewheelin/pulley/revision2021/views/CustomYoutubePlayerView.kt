package com.freewheelin.pulley.revision2021.views

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Insets
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import android.view.WindowInsets
import android.os.Build
import android.util.TypedValue
import com.freewheelin.pulley.R
import com.freewheelin.pulley.legacy.utils.toPx
import kotlinx.coroutines.*


class CustomYoutubePlayerView: LinearLayout {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    var youtubePlayerId : Int = 0
    var dummyViewId : Int = 0

    init {
        (context as? Activity)?.let {
            val v = View(context)

            val (width, _) = getScreenSize(it)

            // videoContainer의 margin 좌우의 합
            val marginHorizontal = 64.toPx()
            val vWidth = (((width * 0.55) - marginHorizontal) / 16 * 9).toInt()
            v.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, vWidth)

            dummyViewId = View.generateViewId()
            v.background = ContextCompat.getDrawable(context, R.drawable.bg_round)
            v.id = dummyViewId
            this.addView(v)
        }

    }

    fun getPlayerId() = youtubePlayerId
    fun getPlayerView(): YouTubePlayerView? {
        this.children.forEach { child ->
            if (child.id == youtubePlayerId) {
                val yp = (child as YouTubePlayerView)
                return yp
            }
        }
        return null
    }
    fun addPlayerView() {
        val yp = YouTubePlayerView(context)
//        yp.enableAutomaticInitialization = false // initialize를 수동으로 할때 필요
        yp.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        youtubePlayerId = View.generateViewId()
//        yp.background = ContextCompat.getDrawable(context, R.drawable.bg_round)
        yp.id = youtubePlayerId
        this.addView(yp)
    }

    fun removePlayerView() {
        this.children.forEach { child ->
            if (child.id == youtubePlayerId) {
                val yp = (child as YouTubePlayerView)
                yp.release()
                this.removeAllViews()
            }
        }
    }

    fun setDummyPlayerView() {
        (context as? Activity)?.let {
            val v = View(context)
            val (width, _) = getScreenSize(it)
            val marginHorizontal = 64.toPx()
            val videoHeight = (((width * 0.55) - marginHorizontal) / 16 * 9).toInt()

            v.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, videoHeight)
            dummyViewId = View.generateViewId()
            v.background = ContextCompat.getDrawable(context, R.drawable.bg_round)

            v.id = dummyViewId
            this.addView(v)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(ev)
    }
    fun getScreenSize(activity: Activity): Pair<Int, Int> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            Pair(windowMetrics.bounds.width() - insets.left - insets.right, windowMetrics.bounds.height() - insets.bottom - insets.top)
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }
    }
}
