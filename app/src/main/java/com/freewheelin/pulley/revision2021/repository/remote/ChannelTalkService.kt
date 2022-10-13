package com.freewheelin.pulley.revision2021.repository.remote

import com.freewheelin.pulley.revision2021.model.request.channelio.PostImageMessageReq
import com.freewheelin.pulley.revision2021.model.request.channelio.PostTextMessageReq
import com.freewheelin.pulley.revision2021.model.response.channelio.ChannelIOImageUploadRes
import com.freewheelin.pulley.revision2021.model.response.channelio.PostImageMessageRes
import com.freewheelin.pulley.revision2021.model.response.channelio.PostTestMessageRes
import com.freewheelin.pulley.revision2021.model.response.channelio.UserChats
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.*


object ChannelTalkApi {
    fun channelTalkMediaService(mimeType: String) : ChannelTalkMediaService  = Network.retrofitChannelIO(Network.Type.channelTalkMedia, mimeType).create(ChannelTalkMediaService::class.java)
    fun channelTalkApiService() : ChannelTalkApiService  = Network.retrofitChannelIO(Network.Type.channelTalkApi).create(ChannelTalkApiService::class.java)
}
interface ChannelTalkMediaService {

    @POST("pri-file/{channelId}/user-chats/{chatId}/message/{fileName}")
    fun uploadCaptureImage(
        @Path("channelId") channelId: String,
        @Path("chatId") chatId: String,
        @Path("fileName") fileName: String,
        @Body file: RequestBody
    ): Observable<ChannelIOImageUploadRes>
}
interface ChannelTalkApiService {

    @POST("front/v5/user-chats/{chatId}/messages")
    fun postCapturedImageMessage(
        @Path("chatId") chatId: String,
        @Query("page") pageName: String,
        @Body body: PostImageMessageReq
    ): Observable<PostImageMessageRes>

    @POST("front/v5/user-chats/{chatId}/messages")
    fun postTextMessage(
        @Path("chatId") chatId: String,
        @Query("page") pageName: String,
        @Body body: PostTextMessageReq
    ): Observable<PostTestMessageRes>

    //    @Headers("Content-Type: application/json")
    @GET("front/v5/user-chats")
    fun getChats(): Observable<UserChats>
}