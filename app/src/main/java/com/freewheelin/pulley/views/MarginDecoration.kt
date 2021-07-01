package com.freewheelin.pulley.views

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class MarginDecoration: RecyclerView.ItemDecoration {
    val margin: Int
    var startMargin: Int? = null
    var lastMargin: Int? = null

    constructor(margin: Int) {
        this.margin = margin
    }

    constructor(margin: Int, startMargin: Int? = null, lastMargin: Int? = null) {
        this.margin = margin
        this.startMargin = startMargin
        this.lastMargin = lastMargin
    }
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildLayoutPosition(view)
        val lastPosition = parent.adapter?.itemCount
        var margin = Math.round(margin * 0.5f)

        if (position == 0) {
            outRect.left = startMargin ?: margin
            outRect.right = margin
        } else if (position + 1 == lastPosition) {
            outRect.left = margin
            outRect.right = lastMargin ?: margin
        } else {
            outRect.right = margin
            outRect.left = margin
        }
    }
}

class GridMarginDecoration: RecyclerView.ItemDecoration {
    val rowSpace: Int
    val columnSpace: Int
    val columnCnt: Int

    constructor(rowSpace: Int, columnSpace: Int, columnCnt: Int) {
        this.rowSpace = rowSpace
        this.columnSpace = columnSpace
        this.columnCnt = columnCnt
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildLayoutPosition(view)

        val perSpace = (columnSpace * (columnCnt - 1)) / columnCnt
        if(columnCnt == 3) {
            when (position % columnCnt) {
                0 -> {
                    outRect.right = perSpace
                }
                columnCnt - 1 -> {
                    outRect.left = perSpace
                }

                else -> {
                    outRect.left =  perSpace / 2
                    outRect.right =  perSpace / 2
                }
            }
        } else {
            when (position % columnCnt) {
                0 -> {
                    outRect.right = perSpace
                }

                1  -> {
                    outRect.left = columnSpace - perSpace
                    outRect.right = perSpace - (columnSpace - perSpace)
                }

                2 -> {
                    outRect.right = columnSpace - perSpace
                    outRect.left = perSpace - (columnSpace - perSpace)
                }

                3 -> {
                    outRect.left = perSpace
                }
            }
        }
        outRect.top = rowSpace / 2
        outRect.bottom = rowSpace / 2
    }
}