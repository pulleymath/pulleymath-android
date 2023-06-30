package com.freewheelin.pulley.legacy.utils

import android.content.res.Resources
import android.util.TypedValue
import java.util.Random


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

// toDp의 리턴값은 dp단위이다.
// 예를들어 xml에서 15dp 라고 입력한 값을 출력해보면 35.625가 출력된다. (15 * 2.375 = 35.625)
// 이때 출력된 값에 toDp()를 하면 dp단위인 15가 리턴되는것이다.
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
