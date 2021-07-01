package com.freewheelin.pulley.assets

enum class Major(val id: Int) {
    none(-1),
    common(0),
    natural_sciences(1),
    liberal_arts(2);

    val title: String
        get() {
            return when (this) {
                common -> "공통"
                liberal_arts -> "문과"
                natural_sciences -> "이과"
                else -> ""
            }
        }

    val value: String
        get() {
            return when (this) {
                common -> "U"
                liberal_arts -> "A"
                natural_sciences -> "B"
                none -> ""
            }
        }


    companion object {
        val list = listOf(common, liberal_arts, natural_sciences)

        fun init(value: String): Major {
            return when (value) {
                "U" -> common
                "B" -> natural_sciences
                "A" -> liberal_arts
                else -> none
            }
        }

        fun getValue(position:Int) : String {
            return if(position < 0) "" else list.get(position).value
        }
    }

}