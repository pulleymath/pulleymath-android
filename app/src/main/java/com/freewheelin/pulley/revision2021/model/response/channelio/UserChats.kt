package com.freewheelin.pulley.revision2021.model.response.channelio

import com.zoyi.channel.plugin.android.model.rest.Message
import com.zoyi.channel.plugin.android.model.rest.Session
import io.channel.plugin.android.model.api.Bot
import io.channel.plugin.android.model.api.Manager
import java.io.Serializable

public class UserChats: Serializable {
    lateinit var userChats: List<UserChat>
    lateinit var sessions: List<Session>
    lateinit var messages: List<Message>
    lateinit var managers: List<Manager>
    lateinit var bots: List<Bot>

    inner class UserChat {
        lateinit var id: String
        lateinit var channelId: String
        lateinit var userId: String
        lateinit var source: Source
        inner class Source {
            lateinit var page: String
        }
    }
}