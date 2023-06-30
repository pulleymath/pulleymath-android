package com.freewheelin.pulley.legacy.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

class FAQ {

    @Expose @SerializedName("noticeID")
    val id: Int = 0
    var subject: String = ""
    var headline: String = ""
    var contents: String = ""
    var imageUrl: String? = null
    var dateTime: Date = Date()
}