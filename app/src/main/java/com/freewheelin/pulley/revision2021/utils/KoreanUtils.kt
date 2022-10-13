package com.freewheelin.pulley.revision2021.utils

class KoreanUtil {
    companion object {
        fun getCompleteWordBy종성(name: String, 받침있을때: String, 받침없을때: String): String {
            val lastName = name.toCharArray().last()
            if (lastName < 0xAC00.toChar() || lastName > 0xD7A3.toChar()) {
                return name
            }

            val selectedValue = if ((lastName - 0xAC00.toChar()) % 28 > 0) 받침있을때 else 받침없을때
            return name + selectedValue
        }
    }
}