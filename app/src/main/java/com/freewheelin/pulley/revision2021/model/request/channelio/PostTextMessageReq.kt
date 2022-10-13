package com.freewheelin.pulley.revision2021.model.request.channelio

import java.io.Serializable

class PostTextMessageReq: Serializable {

    var createdAt: Long = System.currentTimeMillis()
    lateinit var personId: String
    val personType: String = "user"
    lateinit var plainText: String

    val requestId: String
        get() {
            return "${createdAt}Q5uE"
        }


    constructor(personId: String, text: String) {
        this.personId = personId
        plainText = text
    }
}