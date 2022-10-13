package com.freewheelin.pulley.revision2021.repository.remote

import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.revision2021.channelio.channel.PChannelIO
import com.freewheelin.pulley.utils.APHelper
import com.freewheelin.pulley.utils.Preferences
import com.google.gson.GsonBuilder
import com.zoyi.channel.plugin.android.global.PrefSupervisor
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Network {
    var shopUrl     = Preferences.shopUrl.get()
    val baseNodeUrl = when (Preferences.onServerAPI.get()) {
        Server.live.toString() -> "https://pdf-live.pulleymath.com"
        Server.staging.toString() -> "https://pdf-staging.pulleymath.com"
        Server.dev.toString() -> "https://pdf-staging.pulleymath.com"
        else -> "https://pdf-live.pulleymath.com"
    }

    val mockTestUrl = when (Preferences.onServerAPI.get()) {
        Server.live.toString() -> "https://mock-live.pulleymath.com"
        Server.staging.toString() -> "https://mock-staging.pulleymath.com"
        Server.dev.toString() -> "https://mock-dev.pulleymath.com"
        else -> "https://mock-live.pulleymath.com"
    }

    val springUrl = when (Preferences.onServerAPI.get()) {
        Server.live.toString() -> URL.PULLEY_API
        Server.staging.toString() -> URL.PULLEY_STAGING_API
        Server.dev.toString() -> Preferences.testBaseURL.get()
        else -> URL.PULLEY_API
    }
    val cookingUrl = when (Preferences.onServerAPI.get()) {
        Server.live.toString() -> "https://pulley-cooking-dev.pulleymath.com"
        Server.staging.toString() -> "https://pulley-cooking-dev.pulleymath.com"
        Server.dev.toString() -> "https://pulley-cooking-dev.pulleymath.com"
        else -> "https://pulley-cooking-dev.pulleymath.com"
    }
    val channelTalkMediaUrl = when (Preferences.onServerAPI.get()) {
        Server.live.toString() -> "https://media.channel.io"
        else -> "https://media.channel.io"
    }
    val channelTalkApiUrl = when (Preferences.onServerAPI.get()) {
        Server.live.toString() -> "https://api.channel.io"
        else -> "https://api.channel.io"
    }

    var token = ""

    enum class Type {
        node, mockTest, spring, cooking, channelTalkMedia, channelTalkApi;

        val url: String
            get() {
                return when(this) {
                    node -> baseNodeUrl
                    mockTest -> mockTestUrl
                    spring -> springUrl
                    cooking -> cookingUrl
                    channelTalkMedia -> channelTalkMediaUrl
                    channelTalkApi -> channelTalkApiUrl
                }
            }
    }
    enum class Server {
        live, staging, dev
    }

    fun retrofit(type: Type = Type.node): Retrofit {
        return Retrofit.Builder().baseUrl(type.url).apply {

            val client = OkHttpClient.Builder().apply {

                val interceptor = HttpLoggingInterceptor()
                interceptor.level = HttpLoggingInterceptor.Level.BODY
                addInterceptor(interceptor)

                addInterceptor(
                        Interceptor { chain ->
                            val builder = chain.request().newBuilder()
                                .header("Authorization", "Bearer ${user?.token}")
                                .header("DeviceUid", APHelper.deviceId())
                                .header("DeviceName", APHelper.deviceName)
                                .header("Platform", "ANDROID")
                            val response = chain.proceed(builder.build())

//                            val authorization = response.header("Authorization")
//                            Log.d("Authorize", "Authorization=$authorization")

//                            if (authorization?.isNotEmpty() == true) {
//                                token = authorization
//                            }
                            return@Interceptor response
                        }
                )
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(15, TimeUnit.SECONDS)
                writeTimeout(15, TimeUnit.SECONDS)
            }.build()

            val gson = GsonBuilder().setLenient().create()
            client(client)
            addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            addConverterFactory(GsonConverterFactory.create(gson))

        }.build()
    }

    fun retrofitChannelIO(type: Type = Type.cooking, mimeType: String = "image/png"): Retrofit {
        return Retrofit.Builder().baseUrl(type.url).apply {

            val client = OkHttpClient.Builder().apply {

                val interceptor = HttpLoggingInterceptor()
                interceptor.level = HttpLoggingInterceptor.Level.BODY
                addInterceptor(interceptor)

                addInterceptor(
                    Interceptor { chain ->
                        val jwt = PrefSupervisor.getJwt(PChannelIO.getAppContext())

                        // 혹시 몰라서 분기
                        val builder = if (type == Type.channelTalkMedia) {
                            chain.request().newBuilder()
                                .header("x-session", jwt)
                                .header("X-Requested-With", "XMLHttpRequest")
                                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.87 Whale/3.16.138.22 Safari/537.36")
                                .header("content-type", mimeType)

                        } else if (type == Type.channelTalkApi) {
                            chain.request().newBuilder()
                                .header("x-session", jwt)
                                .header("X-Requested-With", "XMLHttpRequest")
                                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.87 Whale/3.16.138.22 Safari/537.36")

                        } else {
                            chain.request().newBuilder()
                        }

                        val response = chain.proceed(builder.build())

                        return@Interceptor response
                    }
                )
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(15, TimeUnit.SECONDS)
                writeTimeout(15, TimeUnit.SECONDS)
            }.build()

            client(client)
            addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            addConverterFactory(GsonConverterFactory.create())

        }.build()
    }
}