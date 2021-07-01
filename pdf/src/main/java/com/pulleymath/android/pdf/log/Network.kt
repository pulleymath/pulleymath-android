package com.pulleymath.android.pdf.log

import android.util.Log
import com.pulleymath.android.pdf.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

object Network {
    private val BASE_URL = if(BuildConfig.DEBUG) "http://3.36.127.47:3000" else "https://pdf-live.pulleymath.net"
    var token = ""

    private val retrofit = Retrofit.Builder().baseUrl(BASE_URL).apply {
        val client = OkHttpClient.Builder().apply {
            // add logging
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            addInterceptor(interceptor)
            // add bearer token
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                        .header("Authorization", "Bearer $token")
                        .header("Platform", "ANDROID")
                    val response = chain.proceed(builder.build())
                    return@Interceptor response
                }
            )
            connectTimeout(3, TimeUnit.SECONDS)
            readTimeout(3, TimeUnit.SECONDS)
            writeTimeout(3, TimeUnit.SECONDS)
        }.build()
        client(client)
        addConverterFactory(GsonConverterFactory.create())
    }.build()

    private val pdfLogService = retrofit.create(PdfLogService::class.java)

    fun sendLog(body: PdfReadLog?, logId: Int?, callback:(Int)->Unit) {
        return if(body != null) {
            pdfLogService.postLog(body).enqueue(object: Callback<PdfReadLogInsertResponse> {
                override fun onResponse(call: Call<PdfReadLogInsertResponse>, response: Response<PdfReadLogInsertResponse>) {

                    Log.d(javaClass.simpleName, "read after=${response.body()}")

                    response.body()?.let { body ->
                        Log.d(javaClass.simpleName, "read logId=${body.data?.id}")
                        body.data?.id?.let { callback(it) }
                    }
                }
                override fun onFailure(call: Call<PdfReadLogInsertResponse>, t: Throwable) {
                    Log.e(javaClass.simpleName, "read error=${t.localizedMessage}")
                    callback(0) // error
                }
            })
        } else {
            pdfLogService.patchLog(logId).enqueue(object: Callback<PdfReadLogUpdateResponse> {
                override fun onResponse(call: Call<PdfReadLogUpdateResponse>, response: Response<PdfReadLogUpdateResponse>) {
                    callback(1) // 1 true
                }
                override fun onFailure(call: Call<PdfReadLogUpdateResponse>, t: Throwable) {
                    callback(0) // 0 false
                }
            })
        }
    }
}

interface PdfLogService {
    @POST("/v1/pdf/reading")
    fun postLog(@Body body:PdfReadLog) : Call<PdfReadLogInsertResponse>

    @PATCH("/v1/pdf/reading/{logId}")
    fun patchLog(@Path("logId") logId: Int?) : Call<PdfReadLogUpdateResponse>
}