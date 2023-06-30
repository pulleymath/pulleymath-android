package com.freewheelin.pulley.legacy.views

import android.content.Context
import android.graphics.*
import android.graphics.Color.parseColor
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.graphics.Path.FillType
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode


class TransparentCircleView : View {

    lateinit var bm: Bitmap
    lateinit var cv: Canvas
    lateinit var eraser: Paint

    constructor(context: Context) : super(context) {
        Init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        Init()
    }

    constructor(context: Context, attrs: AttributeSet,
                defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        Init()
    }

    private fun Init() {

        eraser = Paint()
        eraser!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        eraser!!.isAntiAlias = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        if (w != oldw || h != oldh) {
            bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            cv = Canvas(bm)
        }
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {

        val w = width.toFloat()
        val h = height.toFloat()
        val radius = if (w > h) h / 2 else w / 2

        bm?.eraseColor(Color.TRANSPARENT)
        cv?.drawColor(Color.WHITE)
        cv?.drawCircle(w / 2, h / 2, radius, eraser)
        canvas.drawBitmap(bm, 0f, 0f, null)
        super.onDraw(canvas)
    }
}