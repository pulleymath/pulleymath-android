package com.freewheelin.pulley.revision2021.repository.remote

import android.util.Log
import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.Version
import com.freewheelin.pulley.core.retrofit
import com.freewheelin.pulley.utils.APHelper
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

    val baseNodeUrl = if(BuildConfig.DEBUG || Preferences.onTestAPI.get()) "http://3.36.127.47:3000" else "https://pdf-live.pulleymath.net"
    var token = ""

    fun retrofit(): Retrofit {
        return Retrofit.Builder().baseUrl(baseNodeUrl).apply {

            val client = OkHttpClient.Builder().apply {

//                val interceptor = HttpLoggingInterceptor()
//                interceptor.level = HttpLoggingInterceptor.Level.BODY
//                addInterceptor(interceptor)

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