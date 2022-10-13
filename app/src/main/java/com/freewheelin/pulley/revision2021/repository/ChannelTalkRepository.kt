package com.freewheelin.pulley.revision2021.repository

import com.freewheelin.pulley.revision2021.model.request.channelio.PostImageMessageReq
import com.freewheelin.pulley.revision2021.model.request.channelio.PostTextMessageReq
import com.freewheelin.pulley.revision2021.repository.remote.ChannelTalkApi
import com.freewheelin.pulley.revision2021.repository.remote.ChannelTalkApiService
import com.freewheelin.pulley.revision2021.repository.remote.ChannelTalkMediaService
import okhttp3.RequestBody

class ChannelTalkRepository {
    private val imageService: ChannelTalkMediaService by lazy { ChannelTalkApi.channelTalkMediaService("image/png") }
    private val apiService: ChannelTalkApiService by lazy { ChannelTalkApi.channelTalkApiService() }

//    fun fetchCourseList(chapterId: Int) = mediaService.fetchCourseList(chapterId)
//    fun fetchCourseList(chapterId: Int) = apiService.fetchCourseList(chapterId)

    fun getChats() = apiService.getChats()
    fun uploadCaptureImage(channelId: String, chatId: String, fileName: String, file: RequestBody) =
        imageService.uploadCaptureImage(channelId, chatId, fileName, file)

    fun postCapturedImageMessage(chatId: String, pageName: String, body: PostImageMessageReq) =
        apiService.postCapturedImageMessage(chatId, pageName, body)

    fun postTextMessage(chatId: String, pageName: String, body: PostTextMessageReq) =
        apiService.postTextMessage(chatId, pageName, body)

}