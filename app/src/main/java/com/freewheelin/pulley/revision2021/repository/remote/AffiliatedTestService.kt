package com.freewheelin.pulley.revision2021.repository.remote

import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.revision2021.model.response.*
import com.freewheelin.pulley.revision2021.model.response.base.BaseSingleResponseNode
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.components.ApplicationComponent
import io.reactivex.Observable
import retrofit2.http.*

//@Module
//@InstallIn(ApplicationComponent::class)
object AffiliatedTestApi {
//    @Provides
    fun univTestService() : AffiliatedTestService  = Network.retrofit(Network.Type.mockTest).create(AffiliatedTestService::class.java)
}

interface AffiliatedTestService {
    @GET("test/group/list")
    fun getGroupList(@Query("school_id") schoolID :Int,
             @Query("major_id") majorID: String)
            : Observable<AffiliatedTestResponse>

    @GET("test/workbook/list")
    fun getWorkbookList(@Query("group_ids") groupIdList :String) : Observable<AffiliatedTestWorkbookResponse>

    @GET("test/student/{studentId}/workbook/list")
    fun getWorkbookListOnStudentId(@Path("studentId") studentId: String) : Observable<AffiliatedTestWorkbookOnStudentResponse>

    @GET("test/problem/list")
    fun getAffiliatedTestProblems(@Query("workbook_id") workbookId: Int,
                                  @Query("version") version: Int) : Observable<AffiliatedTestProblemResponse>

    @GET("test/student/{student_id}/workbook/{workbook_id}/open")
    fun openWorkbook(@Path("student_id") studentId: String,
                     @Path("workbook_id") workbookId: Int,
                     @Query("version") version: Int) : Observable<AffiliatedTestAnswerResponse>

    @GET("test/student/{student_id}/workbook/{workbook_id}/problem/{problem_no}")
    fun openProblem(@Path("student_id") studentId: String,
                     @Path("workbook_id") workbookId: Int,
                     @Path("problem_no") problemNo: Int) : Observable<AffiliatedOpenProblemResponse>

    @GET("test/student/{student_id}/workbook/{workbook_id}/answer/list")
    fun getWorkbookAnswerList(@Path("student_id") studentId: String,
                              @Path("workbook_id") workbookId: Int) : Observable<AffiliatedTestAnswer2Response>

    @GET("test/student/{student_id}/workbook/current")
    fun getGroupList2(@Path("student_id") studentId: String,
                      @Query("school_id") schoolId: Int) : Observable<AffiliatedGroupResponse>


    @PATCH("test/student/{student_id}/workbook/{workbook_id}/finish")
    fun finish(@Path("student_id") studentId: String,
               @Path("workbook_id") workbookId: Int) : Observable<AffiliatedStudentWorkbookResponse>

    @PUT("test/student/{student_id}/workbook/{workbook_id}/problem/{problem_no}/answer")
    fun insertAnswer(@Path("student_id") studentId: String,
                     @Path("workbook_id") workbookId: Int,
                     @Path("problem_no") problemNo: Int,
                     @Body param: Parameter) : Observable<AffiliatedStudentWorkbookResponse>

    @GET("test/student/{student_id}/workbook/{workbook_id}/result")
    fun fetchScoringResult(@Path("student_id") studentId: String,
                           @Path("workbook_id") workbookId: Int,
                           @Query("version") version: Int) : Observable<AffiliatedScoringResultResponse>

    @GET("media/list")
    fun fetchMedia(@Query("problem_id") problemId: Int) : Observable<AffiliatedSolutionResponse>

    @POST("media/log")
    fun makeMediaLog(@Body params: AffiliatedMediaLog) : Observable<AffiliatedMediaLogResponse<Int>>
//    fun makeMediaLog(@Body params: AffiliatedMediaLog) : Observable<BaseSingleResponseNode<Int>>

    @PATCH("media/log/{response_media_id}/finish")
    fun finishMediaLog(@Path("response_media_id") responseMediaId: Int,
                       @Body params: AffiliatedMediaLog) : Observable<AffiliatedMediaLogResponse<String>>


}
