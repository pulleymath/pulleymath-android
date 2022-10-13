package com.freewheelin.pulley.utils

import android.content.res.Resources
import android.util.TypedValue
import java.util.*


class NumberUtils {
    companion object {
        private val random = Random(System.currentTimeMillis())

        fun rand(from: Int, to: Int): Int {
            if(from >= to)
                return from
            return random.nextInt(to - from) + from
        }


        fun getSizeText(size: Int): String {
            return if(size == 0)
                "-"
            else size.toString()
        }
    }
}


fun Int.toDp():Float {
    return this / Resources.getSystem().displayMetrics.density
}

fun Float.toDp(): Float {
    return this / Resources.getSystem().displayMetrics.density
}

fun Int.toPx(): Int {
    return Math.round(this * Resources.getSystem().displayMetrics.density)
}
fun Int.dpToPx(): Int {
    val metrics = Resources.getSystem().displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), metrics).toInt()
}
fun Float.toPx(): Float {
    return this * Resources.getSystem().displayMetrics.density
}

fun Int.spToPx(): Float {
    return this * Resources.getSystem().displayMetrics.scaledDensity
}

fun Float.spToPx(): Float {
    return this * Resources.getSystem().displayMetrics.scaledDensity
}

fun Int.pxToSp(): Float {
    return this / Resources.getSystem().displayMetrics.scaledDensity
}

fun Float.pxToSp(): Float {
    return this / Resources.getSystem().displayMetrics.scaledDensity
}
