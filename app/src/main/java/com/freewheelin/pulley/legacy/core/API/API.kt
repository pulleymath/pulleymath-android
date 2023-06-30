package com.freewheelin.pulley.legacy.core

import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.legacy.activities.learning.LearningTabActivity
import com.freewheelin.pulley.legacy.assets.URL
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.API.*
import com.freewheelin.pulley.legacy.core.manage.*
import com.freewheelin.pulley.legacy.model.*
import com.freewheelin.pulley.legacy.model.contents.*
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.legacy.utils.APHelper
import com.freewheelin.pulley.legacy.utils.APPreference
import com.freewheelin.pulley.legacy.utils.DialogUtils
import com.freewheelin.pulley.legacy.utils.Preferences
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.IOException
import java.util.concurrent.TimeUnit


typealias Parameter = HashMap<String, Any>

fun <K, V> Parameter(vararg pairs: Pair<K, V>): HashMap<K, V> = HashMap<K, V>().apply { putAll(pairs) }

enum class Version {
    v1, v2, v3, app, anonymous;

    val base = when (Preferences.onServerAPI.get()) {
        Network.Server.live.toString() -> URL.PULLEY_API
        Network.Server.staging.toString() -> URL.PULLEY_STAGING_API
        Network.Server.dev.toString() -> Preferences.testBaseURL.get()
        else -> URL.PULLEY_API
    }

    val url: String
    get() {
        return when(this) {
            v1 -> base
            v2 -> "$base/v2/"
            v3 -> "$base/v3/"
            app -> base
            anonymous -> base
        }
    }
}

val API_V1: ServiceV1 by lazy {
    retrofit(Version.v1).create(ServiceV1::class.java)
}

val API_V2: ServiceV2 by lazy {
    retrofit(Version.v2).create(ServiceV2::class.java)
}

val API_V3: ServiceV3 by lazy {
    retrofit(Version.v3).create(ServiceV3::class.java)
}

val API_APP: AppService by lazy {
    retrofit(Version.app).create(AppService::class.java)
}

val API_ANONYMOUS: Anonymous by lazy {
    retrofit(Version.anonymous).create(Anonymous::class.java)
}

fun retrofit(apiVersion: Version): Retrofit {
    return Retrofit.Builder().baseUrl(apiVersion.url).apply {

        val client = OkHttpClient.Builder().apply {

            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
//            val interceptor = LoggingInterceptor()
            addInterceptor(interceptor)

            addInterceptor(
                Interceptor { chain ->
                    val token = user?.token ?: MyApplication.token
                    Log.d("인증", "intercepter ==================> $token")
                    Log.d("인증", "intercepter ==================> DeviceUid : ${APHelper.deviceId()}, Name : ${APHelper.deviceName}")

                    val builder = chain.request().newBuilder()
                            .header("Authorization", "Bearer $token")
                            .header("DeviceUid", APHelper.deviceId())
                            .header("DeviceName", APHelper.deviceName)
//                            .header("Platform", "ANDROID")
                    val response = chain.proceed(builder.build())

                    val authorization = response.header("Authorization")
                    Log.d("인증", "Authorization=$authorization")


                    val path = chain.request().url().encodedPath()

                    Log.d(javaClass.simpleName, "Api path=${path}, code=${response.code()}")

                    // 401 시 세션 만료 처리
                    var exceptionUrl = listOf("/v3/me/app", "/v2/versions/android", "/v2/signin/app", "/log/user", "/v2/daily/study-time")

                    if(response.code() == 401 && !exceptionUrl.contains(path)) {

                        Log.d(javaClass.simpleName, "Api path=${path}, code=${response.code()}")
                        Log.d(javaClass.simpleName, "Api path referActivity=${LearningTabActivity.referActivity}")

                        LearningTabActivity.referActivity?.run {
                            Log.d(javaClass.simpleName, "Api path called dialog")
                            this.sendBroadcast(Intent(LearningTabActivity.FILTER_SESSION_EXPIRED))
                        }
                    }

                    if(authorization?.isNotEmpty() == true) {
                        user?.token = authorization
                        MyApplication.token = authorization
                        user?.commit("replace authorization token")
                    }
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

//class LoggingInterceptor : Interceptor {
//    @Throws(IOException::class)
//    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
//        val request: Request = chain.request()
//        val t1 = System.nanoTime()
//        Log.d("OkHttp", java.lang.String.format("--> Sending request %s on %s%n%s", request.url(), chain.connection(), request.headers()))
//        val requestBuffer = Buffer()
//        request.body()?.writeTo(requestBuffer)
//        Log.d("OkHttp", requestBuffer.readUtf8())
//        val response: Response = chain.proceed(request)
//        val t2 = System.nanoTime()
//        Log.d("OkHttp", java.lang.String.format("<-- Received response for %s in %.1fms%n%s", response.request().url(), (t2 - t1) / 1e6, response.headers()))
//        val contentType: MediaType? = response.body()?.contentType()
//        val content: String? = response.body()?.string()
//        Log.d("OkHttp", content)
//        val wrappedBody = ResponseBody.create(contentType, content)
//        return response.newBuilder().body(wrappedBody).build()
//    }
//}

