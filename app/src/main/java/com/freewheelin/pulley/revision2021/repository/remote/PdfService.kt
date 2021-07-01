package com.freewheelin.pulley.revision2021.repository.remote

import com.freewheelin.pulley.revision2021.model.response.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import io.reactivex.Observable
import retrofit2.http.*

@Module
@InstallIn(ApplicationComponent::class)
object PdfApi {
    @Provides
    fun pdfService() : PdfService  = Network.retrofit().create(PdfService::class.java)
}

interface PdfService {
    @GET("v1/pdf/list")
    fun list(@Query("title") title:String,
             @Query("page") page:Int,
             @Query("size") size:Int,
             @Query("subject_code") subject_code:String="",
             @Query("category") category:String="")
            : Observable<PdfListResponse>

    @GET("v1/pdf/answers/{cm_book_id}")
    fun answer(@Path("cm_book_id") cm_book_id:Int) : Observable<PdfAnswerResponse>

    @POST("v1/pdf/reading")
    fun postReadLog() : Observable<PdfReadLogInsertResponse>

    @PATCH("v1/pdf/reading/{logId}")
    fun patchReadLog(@Path("logId") logId:Int) : Observable<PdfReadLogUpdateResponse>
}
