package com.freewheelin.pulley.model

import com.freewheelin.pulley.utils.DateUnit
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

open class Notice {

    @Expose @SerializedName("noticeID")
    val id: Int = 0
    var subject: String = ""
    var headline: String = ""
    var contents: String = ""
    var imageUrl: String? = null
    var dateTime: Date = Date()


    fun isNeedUpdateTag(): Boolean {
        return Date().time < dateTime.time + DateUnit.Week.unit
    }
}