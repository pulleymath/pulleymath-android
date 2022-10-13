package com.freewheelin.pulley.revision2021.model.request.channelio

import com.freewheelin.pulley.revision2021.model.response.channelio.ChannelIOImageUploadRes
import java.io.Serializable

class PostImageMessageReq: Serializable {

    var createdAt: Long = System.currentTimeMillis()
    lateinit var files: List<PostedFile>
    lateinit var personId: String
    var personType: String = "user"
    val requestId: String
        get() {
            return "${createdAt}Q5uE"
        }

    constructor(personId: String, res: ChannelIOImageUploadRes) {
        val file = PostedFile(res)
        files = listOf(file)
        this.personId = personId
    }
}


class PostedFile: Serializable {
    var bucket: String = "bin.channel.io"
    var contentType: String = "image/png"
    lateinit var id: String
    lateinit var key: String
    lateinit var name: String
    var type: String = "image"
    var size: Int = -1
    var height: Int = -1
    var width: Int = -1

    constructor(res: ChannelIOImageUploadRes) {
        this.id = res.id
        this.key = res.key
        this.name = res.name
        size = res.size
        height = res.height
        width = res.width
    }
}