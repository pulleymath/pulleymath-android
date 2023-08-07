package com.freewheelin.pulley.legacy.views

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.freewheelin.pulley.legacy.utils.toDp

class DividerDecoration(private val margin: Int, private val colorString: String = "#e8e8e8"): RecyclerView.ItemDecoration() {
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)

        val paint = Paint().apply {
            color = Color.parseColor(colorString)
        }
        val height = 1.toDp()

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (i != parent.childCount - 1) {

                c.drawRect(child.left.toFloat() + margin, child.bottom.toFloat() + margin, child.right.toFloat() - margin, child.bottom.toFloat() + height + margin, paint)
            }
        }
    }
}