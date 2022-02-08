package com.freewheelin.pulley.revision2021.repository.remote

import android.util.Log
import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Version
import com.freewheelin.pulley.core.retrofit
import com.freewheelin.pulley.utils.APHelper
import com.freewheelin.pulley.utils.APPreference
import com.freewheelin.pulley.utils.Preferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named

object Network {
    var shopUrl     = Preferences.shopUrl.get()
    val baseNodeUrl = if(Preferences.onTestAPI.get()) "http://3.36.127.47:3000" else "https://pdf-live.pulleymath.net"
    val mockTestUrl = if(Preferences.onTestAPI.get()) "https://mock-dev.pulleymath.com" else "https://mock-live.pulleymath.com"
    var token = ""

    enum class Type {
        node, mockTest;

        val url: String
            get() {
                return when(this) {
                    node -> baseNodeUrl
                    mockTest -> mockTestUrl
                }
            }
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

            client(client)
            addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            addConverterFactory(GsonConverterFactory.create())

        }.build()
    }
}