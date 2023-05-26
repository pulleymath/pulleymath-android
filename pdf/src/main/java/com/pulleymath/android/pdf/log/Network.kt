package com.pulleymath.android.pdf.log

import android.util.Log
import com.pulleymath.android.pdf.BuildConfig
import com.pulleymath.android.pdf.PdfViewerActivity
import com.pulleymath.android.pdf.memo.storage.PdfMemo
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

object Network {
    private val BASE_URL = when(PdfViewerActivity.onServerApi) {
        Server.live.toString() -> "https://pdf-live.pulleymath.com"
        Server.staging.toString() -> "https://pdf-staging.pulleymath.com"
        Server.dev.toString() -> "https://pdf-staging.pulleymath.com"
        else -> "https://pdf-live.pulleymath.com"
    }
    var token = ""

    enum class Server {
        live, staging, dev
    }

    private val retrofit = Retrofit.Builder().baseUrl(BASE_URL).apply {
        val client = OkHttpClient.Builder().apply {
            // add logging
//            if(BuildConfig.DEBUG) { // 개발모드에서만 로깅처리
                val interceptor = HttpLoggingInterceptor()
                interceptor.level = HttpLoggingInterceptor.Level.BODY
                addInterceptor(interceptor)
//            }
            // add bearer token
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                        .header("Authorization", "Bearer $token")
//                        .header("Platform", "ANDROID")
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

    fun uploadMemo(request:List<PdfMemo>, callback: (() -> Unit)? = null) {
        pdfMemoService.uploadMemo(request).enqueue(object: Callback<PdfMemoPostResponse>{
            override fun onResponse(call: Call<PdfMemoPostResponse>, response: Response<PdfMemoPostResponse>) {
                callback?.let{ it() }
            }
            override fun onFailure(call: Call<PdfMemoPostResponse>, t: Throwable) {
                Log.e("uploadMemo", "${t.localizedMessage}")
            }
        })
    }

    fun uploadTestMemo(file: MultipartBody.Part,
                       id: RequestBody, student_id: RequestBody,
                       pdf_id: RequestBody, updated_at: RequestBody, page_no: RequestBody,
                       callback: (() -> Unit)? = null) {
        pdfMemoService.uploadMemoByteArray(file, id, student_id, pdf_id, updated_at, page_no).enqueue(object: Callback<PdfMemoPostResponse>{
            override fun onResponse(call: Call<PdfMemoPostResponse>, response: Response<PdfMemoPostResponse>) {
                callback?.let{ it() }
            }
            override fun onFailure(call: Call<PdfMemoPostResponse>, t: Throwable) {
                Log.e("uploadMemo", "${t.localizedMessage}")
            }
        })
    }

    fun downloadMemo(studentId:String, pdfId:Int?=null, pageNo:Int?=null, updatedAt:Long?=null, onResponse:(PdfMemoResponse?)->Unit, onFailure:(String)->Unit) {
        pdfMemoService.downloadMemo(studentId, pdfId, pageNo, updatedAt).enqueue(object: Callback<PdfMemoResponse>{
            override fun onResponse(call: Call<PdfMemoResponse>, response: Response<PdfMemoResponse>) {
                onResponse(response.body())
            }
            override fun onFailure(call: Call<PdfMemoResponse>, t: Throwable) {
                t.localizedMessage?.let { onFailure(it) }
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

    @Multipart
    @POST("/v1/memo/pdf-image")
    fun uploadMemoByteArray(@Part image: MultipartBody.Part,
                            @Part("id") id: RequestBody,
                            @Part("student_id") student_id: RequestBody,
                            @Part("pdf_id") pdf_id: RequestBody,
                            @Part("updated_at") updated_at: RequestBody,
                            @Part("page_no") page_no: RequestBody,
    ) : Call<PdfMemoPostResponse>

    @GET("/v1/memo/pdf")
    fun downloadMemo(@Query("student_id") studentId:String,
                     @Query("pdf_id") pdfId:Int?=null,
                     @Query("page_no") pageNo:Int?=null,
                     @Query("updated_at") updatedAt:Long?=null) : Call<PdfMemoResponse>
}