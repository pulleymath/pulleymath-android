package com.freewheelin.pulley.revision2023.model

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.freewheelin.pulley.R
import com.google.gson.annotations.SerializedName

enum class PaidServiceType(val rawValue: Int) {
    NONE(0),
    PAID_ING(1),
    BASIC_C(2),
    BASIC_P(3),
    STANDARD(4),
    PREMIUM(5),
    ALL(100);

    val isPaidUser: Boolean
        get() { return this != NONE }
    val isFreeUser: Boolean
        get() { return this == NONE }

    fun convertTextOnMainChip(): String {
        return when (this) {
            BASIC_C -> "개념 Basic"
            BASIC_P -> "유형 Basic"
            STANDARD -> "Standard"
            PREMIUM -> "Premium"
            PAID_ING -> "Subscriber"
            NONE -> "Free"
            else -> "Free"
        }
    }

    fun convertDrawableOnMainChip(context: Context): Drawable {
        return when (this) {
            BASIC_C, BASIC_P -> ContextCompat.getDrawable(context, R.drawable.bg_bronze_round_13)!!
            STANDARD -> ContextCompat.getDrawable(context, R.drawable.bg_gray_600_round_13)!!
            PREMIUM -> ContextCompat.getDrawable(context, R.drawable.bg_yellow_300_round_13)!!
            PAID_ING -> ContextCompat.getDrawable(context, R.drawable.bg_bronze_round_13)!!
            NONE -> ContextCompat.getDrawable(context, R.drawable.bg_gray_200_round_13)!!
            else -> ContextCompat.getDrawable(context, R.drawable.bg_gray_200_round_13)!!
        }
    }
    fun convertColorOnMainChip(context: Context): Int {
        return when (this) {
            NONE -> ContextCompat.getColor(context, R.color.gray_700)
            else -> ContextCompat.getColor(context, R.color.white_ffffff)
        }
    }
    fun isTypeEqualOrHigher(target: PaidServiceType): Boolean {
        val serviceTypeValue = this.rawValue
        return serviceTypeValue >= target.rawValue
    }
    companion object {
        fun ConvertToType(value: Int): PaidServiceType {
            return when (value) {
                NONE.rawValue -> NONE
                PAID_ING.rawValue -> PAID_ING
                BASIC_C.rawValue -> BASIC_C
                BASIC_P.rawValue -> BASIC_P
                STANDARD.rawValue -> STANDARD
                PREMIUM.rawValue -> PREMIUM
                else -> NONE
            }
        }
    }
}