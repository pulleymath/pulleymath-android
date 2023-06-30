package com.freewheelin.pulley.revision2023.utils

class StringUtils {
    companion object {

        fun getEmojiByUnicode(unicode: Int): String {
            return String(Character.toChars(unicode))
        }
    }
}