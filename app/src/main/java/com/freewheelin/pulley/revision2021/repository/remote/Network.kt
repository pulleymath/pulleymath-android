package com.freewheelin.pulley.revision2021.repository.remote

import android.util.Log
import com.freewheelin.pulley.legacy.assets.URL
import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.utils.APHelper
import com.freewheelin.pulley.legacy.utils.Preferences
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.pulleymath.android.pdf.log.Network
import com.pulleymath.android.pdf.log.PdfMemoPostResponse
import com.pulleymath.android.pdf.log.PdfMemoService
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

object Network {
    var shopUrl     = Preferences.shopUrl.get()
    val baseNodeUrl = when (Preferences.onServerAPI.get()) {
        Server.live.toString() -> "https://pdf-live.pulleymath.com"
        Server.staging.toString() -> "https://pdf-staging.pulleymath.com"
        Server.dev.toString() -> "https://pdf-staging.pulleymath.com"
        else -> "https://pdf-live.pulleymath.com"
    }

    val webAppUrl = when (Preferences.onServerAPI.get()) {
        Server.live.toString() -> "https://app.pulleymath.com"
        Server.staging.toString() -> "https://app-staging.pulleymath.com"
        Server.dev.toString() -> "https://app-staging.pulleymath.com"
        else -> "https://app.pulleymath.com"
//        else -> "http://192.168.0.54:3000"
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
        Server.live.toString() -> "https://pulley-cooking-live.pulleymath.com"
        Server.staging.toString() -> "https://pulley-cooking-dev.pulleymath.com"
        Server.dev.toString() -> "https://pulley-cooking-dev.pulleymath.com"
        else -> "https://pulley-cooking-live.pulleymath.com"
    }
//    var purchaseSubscriptionUrl = "${springUrl}/gateway?token="
    var webRedirectUrlOnShortToken = when (Preferences.onServerAPI.get()) {
        Server.live.toString() -> "${Preferences.shopUrl.get()}/ottway?token="
        else -> "${Preferences.devShopUrl.get()}/ottway?token="
    }
    var homePageUrl = when (Preferences.onServerAPI.get()) {
        Server.live.toString() -> Preferences.shopUrl.get()
        else -> Preferences.devShopUrl.get()
    }

    const val marketingUrl = "https://pulleymath.com/marketing"
    var token = ""

    enum class Type {
        node, mockTest, spring, cooking;

        val url: String
            get() {
                return when(this) {
                    node -> baseNodeUrl
                    mockTest -> mockTestUrl
                    spring -> springUrl
                    cooking -> cookingUrl
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

                            val token = user?.token ?: MyApplication.token
                            val builder = if (token != null) {
                                chain.request().newBuilder()
                                    .header("Authorization", "Bearer $token")
                                    .header("DeviceUid", APHelper.deviceId())
                                    .header("DeviceName", APHelper.deviceName)
                            } else {
                                chain.request().newBuilder()
                                    .header("DeviceUid", APHelper.deviceId())
                                    .header("DeviceName", APHelper.deviceName)
                            }
//                                .header("Platform", "ANDROID")
                            val response = chain.proceed(builder.build())

                            val authorization = response.header("Authorization")

                            if (authorization?.isNotEmpty() == true) {
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

            val gson = GsonBuilder().setLenient().create()
            client(client)
            addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            addConverterFactory(GsonConverterFactory.create(gson))

        }.build()
    }

    private val pdfService = retrofit().create(PdfService::class.java)


    fun uploadTestMemo(file: MultipartBody.Part,
                       id: RequestBody, student_id: RequestBody,
                       pdf_id: RequestBody, updated_at: RequestBody, page_no: RequestBody,
                       callback: (() -> Unit)? = null) {
        pdfService.uploadMemoByteArray(file, id, student_id, pdf_id, updated_at, page_no).enqueue(object: Callback<PdfMemoPostResponse>{
            override fun onResponse(call: Call<PdfMemoPostResponse>, response: Response<PdfMemoPostResponse>) {
                callback?.let{ it() }
            }
            override fun onFailure(call: Call<PdfMemoPostResponse>, t: Throwable) {
                Log.e("uploadMemo", "${t.localizedMessage}")
            }
        })
    }

}
class EnumConverterFactory : Converter.Factory() {

    override fun stringConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<Enum<*>, String>? =
        if (type is Class<*> && type.isEnum) {
            Converter { enum ->
                try {
                    enum.javaClass.getField(enum.name)
                        .getAnnotation(SerializedName::class.java)?.value
                } catch (exception: Exception) {
                    null
                } ?: enum.toString()
            }
        } else {
            null
        }
}