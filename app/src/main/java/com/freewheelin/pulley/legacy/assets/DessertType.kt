package com.freewheelin.pulley.legacy.assets

import android.R.style
import java.io.Serializable


enum class DessertType: Serializable {
    PIE, LEMONADE, CANDY, CHEESE_BALL, BANANA, MACAROON;

    companion object {
        fun getType(intelligence: Int, technique: Int, planning: Int, style: Int): DessertType {
            val param1 = intelligence + technique
            val param2 = planning + style

            val score = param1 * param2
            return if (score <= 23) {
                DessertType.MACAROON
            } else if (score <= 63) {
                if (style < 4) {
                    DessertType.MACAROON
                } else {
                    DessertType.BANANA
                }
            } else if (score <= 71) {
                DessertType.BANANA
            } else if (score <= 88) {
                if (technique < 5) {
                    DessertType.BANANA
                } else {
                    DessertType.CHEESE_BALL
                }
            } else if (score <= 125) {
                DessertType.CHEESE_BALL
            } else if (score <= 150) {
                if (technique < 10) {
                    DessertType.CHEESE_BALL
                } else {
                    DessertType.CANDY
                }
            } else if (score <= 169) {
                DessertType.CANDY
            } else if (score <= 234) {
                if (style < 7) {
                    DessertType.CANDY
                } else {
                    DessertType.LEMONADE
                }
            } else if (score <= 285) {
                DessertType.LEMONADE
            } else if (score <= 320) {
                if (technique < 15) {
                    DessertType.LEMONADE
                } else {
                    DessertType.PIE
                }
            } else {
                DessertType.PIE
            }
        }
    }
    val dessertName: String
        get() {
            return when(this){
                PIE -> "찰떡파이"
                LEMONADE -> "레모네이드"
                CANDY -> "솜사탕"
                CHEESE_BALL -> "치즈볼"
                BANANA -> "바나나우유"
                MACAROON -> "마카롱"
            }
        }
}
