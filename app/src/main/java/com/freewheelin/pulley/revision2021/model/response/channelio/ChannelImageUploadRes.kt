package com.freewheelin.pulley.revision2021.model.response.channelio

import java.io.Serializable

class ChannelIOImageUploadRes: Serializable {
    lateinit var type: String
    lateinit var id: String
    lateinit var name: String
    var size: Int = -1
    lateinit var contentType: String
    var width: Int = -1
    var height: Int = -1
    lateinit var bucket: String
    lateinit var key: String
}