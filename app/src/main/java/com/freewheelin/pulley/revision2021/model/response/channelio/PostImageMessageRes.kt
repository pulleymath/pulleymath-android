package com.freewheelin.pulley.revision2021.model.response.channelio

import com.freewheelin.pulley.revision2021.model.request.channelio.PostedFile
import java.io.Serializable

class PostImageMessageRes: Serializable {

    lateinit var message: PostedMessage

    inner class PostedMessage {
        lateinit var chatKey: String // "userChat-62fd8d76dad426809b6c"
        lateinit var id: String // "62feeca88b0b45fd4ae4"
        lateinit var mainKey: String // "62feeca88b0b45fd4ae4"
        var root: Boolean = false
        lateinit var channelId: String // "16440"
        lateinit var chatType: String // "userChat"
        lateinit var chatId: String // "62fd8d76dad426809b6c"
        lateinit var personType: String // "user"
        lateinit var personId: String // "62fb4f11addea3c7ef58"
        lateinit var requestId: String // "1660873888938Q5uE"
        var createdAt: Long = 1660873896569
        var version: Int = -1
        var updatedAt: Long = 1660873896566
        lateinit var files: List<PostedFile>
        var threadMsg: Boolean = false
        var removed: Boolean = false
        var broadcastedMsg: Boolean = false
    }

}


class PostTestMessageRes: Serializable {

    lateinit var message: PostedMessage

    inner class PostedMessage {
        lateinit var chatKey: String // "userChat-62fd8d76dad426809b6c"
        lateinit var id: String // "62feeca88b0b45fd4ae4"
        lateinit var mainKey: String // "62feeca88b0b45fd4ae4"
        var root: Boolean = false
        lateinit var channelId: String // "16440"
        lateinit var chatType: String // "userChat"
        lateinit var chatId: String // "62fd8d76dad426809b6c"
        lateinit var personType: String // "user"
        lateinit var personId: String // "62fb4f11addea3c7ef58"
        lateinit var requestId: String // "1660873888938Q5uE"

        var language: String = ""
        var version: Int = -1
        lateinit var plainText: String
        var createdAt: Long = 1660873896569
        var updatedAt: Long = 1660873896566

        var threadMsg: Boolean = false
        var removed: Boolean = false
        var broadcastedMsg: Boolean = false

    }

}

