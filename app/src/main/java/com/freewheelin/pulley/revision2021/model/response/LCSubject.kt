package com.freewheelin.pulley.revision2021.model.response

import java.io.Serializable

class LCSubject: Serializable {
    var subjectId = -1
    var seq: Int = -1
    lateinit var name: String
    lateinit var unitcode: String

}