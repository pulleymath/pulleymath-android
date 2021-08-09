package com.pulleymath.android.pdf.log

import com.pulleymath.android.pdf.BuildConfig
import com.pulleymath.android.pdf.memo.storage.PdfMemo
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

object Network {
    private val BASE_URL = if(BuildConfig.DEBUG) "http://3.36.127.47:3000" else "https://pdf-live.pulleymath.net"
    var token = ""

    private val retrofit = Retrofit.Builder().baseUrl(BASE_URL).apply {
        val client = OkHttpClient.Builder().apply {
            // add logging
            if(BuildConfig.DEBUG) { // 개발모드에서만 로깅처리
                val interceptor = HttpLoggingInterceptor()
                interceptor.level = HttpLoggingInterceptor.Level.BODY
                addInterceptor(interceptor)
            }
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
                    response.body()?.let { body -> body.data?.id?.let { callback(it) } }
                }
                override fun onFailure(call: Call<PdfReadLogInsertResponse>, t: Throwable) {
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

    fun sendPageLog(request:PdfPageLog) {
        pdfLogService.pageLog(request).enqueue(object: Callback<PdfPageLogResponse>{
            override fun onResponse(p0: Call<PdfPageLogResponse>, p1: Response<PdfPageLogResponse>) {}
            override fun onFailure(p0: Call<PdfPageLogResponse>, p1: Throwable) {}
        })
    }

    private val pdfMemoService = retrofit.create(PdfMemoService::class.java)

    fun uploadMemo(request:List<PdfMemo>) {
        pdfMemoService.uploadMemo(request).enqueue(object: Callback<PdfMemoPostResponse>{
            override fun onResponse(call: Call<PdfMemoPostResponse>, response: Response<PdfMemoPostResponse>) {}
            override fun onFailure(call: Call<PdfMemoPostResponse>, t: Throwable) {}
        })
    }

    fun downloadMemo(studentId:String, pdfId:Int?=null, pageNo:Int?=null, updatedAt:Long?=null, onResponse:(PdfMemoResponse?)->Unit, onFailure:(String)->Unit) {
        pdfMemoService.downloadMemo(studentId, pdfId, pageNo, updatedAt).enqueue(object: Callback<PdfMemoResponse>{
            override fun onResponse(call: Call<PdfMemoResponse>, response: Response<PdfMemoResponse>) {
                onResponse(response?.body())
            }
            override fun onFailure(call: Call<PdfMemoResponse>, t: Throwable) {
                onFailure(t.localizedMessage)
            }
        })
    }
}

interface PdfLogService {
    @POST("/v1/pdf/reading")
    fun postLog(@Body body:PdfReadLog) : Call<PdfReadLogInsertResponse>

    @PATCH("/v1/pdf/reading/{logId}")
    fun patchLog(@Path("logId") logId: Int?) : Call<PdfReadLogUpdateResponse>

    @POST("/v1/pdf/page-log")
    fun pageLog(@Body body:PdfPageLog) : Call<PdfPageLogResponse>
}

interface PdfMemoService {
    @POST("/v1/memo/pdf")
    fun uploadMemo(@Body body:List<PdfMemo>) : Call<PdfMemoPostResponse>

    @GET("/v1/memo/pdf")
    fun downloadMemo(@Query("student_id") studentId:String,
                     @Query("pdf_id") pdfId:Int?=null,
                     @Query("page_no") pageNo:Int?=null,
                     @Query("updated_at") updatedAt:Long?=null) : Call<PdfMemoResponse>
}