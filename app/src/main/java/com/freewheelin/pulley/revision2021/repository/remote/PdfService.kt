package com.freewheelin.pulley.revision2021.repository.remote

import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.model.DummyCreatedUser
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.model.ResponseListBody
import com.freewheelin.pulley.revision2021.cookingmemo.storage.PulleyCookingMemo
import com.freewheelin.pulley.revision2021.model.response.*
import com.freewheelin.pulley.revision2021.model.response.base.BaseCookingResponse
import com.freewheelin.pulley.revision2021.model.response.base.BaseSingleResponseNode
import com.pulleymath.android.pdf.log.PdfMemoPostResponse
import com.pulleymath.android.pdf.log.PdfMemoResponse
import com.pulleymath.android.pdf.memo.storage.PdfMemo
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.components.ApplicationComponent
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

//@Module
//@InstallIn(ApplicationComponent::class)
object PdfApi {
//    @Provides
    fun pdfService() : PdfService  = Network.retrofit().create(PdfService::class.java)
}

interface PdfService {

    @GET("v1/pdf/{pdf_id}/single")
    suspend fun fetchPdf(
        @Path("pdf_id") pdfId:Int
    ): ResponseBody<Pdf>

    @GET("v1/pdf/list")
    fun list(@Query("title") title:String,
             @Query("page") page:Int,
             @Query("size") size:Int,
             @Query("subject_code") subject_code:String="",
             @Query("category") category:String="")
            : Observable<PdfListResponse>

    @GET("v2/pdf/list")
    fun listV2(@Query("title") title:String,
        @Query("page") page:Int,
        @Query("size") size:Int,
        @Query("subject_code") subject_code:String = "",
        @Query("category") category:String = "",
        @Query("schoolType") school: String? = MyApplication.schoolType.name
    ): Observable<PdfListResponse>

    @GET("v1/pdf/answers/{cm_book_id}")
    fun answer(@Path("cm_book_id") cm_book_id:Int) : Observable<PdfAnswerResponse>

    @POST("v1/pdf/reading")
    fun postReadLog() : Observable<PdfReadLogInsertResponse>

    @PATCH("v1/pdf/reading/{logId}")
    fun patchReadLog(@Path("logId") logId:Int) : Observable<PdfReadLogUpdateResponse>

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
//    ) : ResponseBody<Any>
//    ) : ResponseBody<PdfMemoPostResponse>

    @Multipart
    @POST("/v1/memo/pdf-image")
    suspend fun uploadMemoByteArray2(@Part image: MultipartBody.Part,
                            @Part("id") id: RequestBody,
                            @Part("student_id") student_id: RequestBody,
                            @Part("pdf_id") pdf_id: RequestBody,
                            @Part("updated_at") updated_at: RequestBody,
                            @Part("page_no") page_no: RequestBody,
    ) : ResponseBody<Any>
    @GET("/v1/memo/pdf")
    suspend fun downloadMemo(@Query("student_id") studentId:String,
                     @Query("type_id") typeId:Int?=null,
                     @Query("sub_id") subId:Int?=null,
                     @Query("updated_at") updatedAt:Long?=null) : ResponseListBody<PulleyCookingMemo>

}