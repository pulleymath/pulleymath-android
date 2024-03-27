package com.freewheelin.pulley.revision2021.repository.remote

import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.model.ResponseForceBody
import com.freewheelin.pulley.revision2021.model.response.*
import com.freewheelin.pulley.revision2021.model.response.base.BaseIntResponseNode
import com.freewheelin.pulley.revision2023.model.ServerTimeNow
import com.freewheelin.pulley.revision2023.model.response.AssessmentMetadata
import io.reactivex.Observable
import retrofit2.http.*

//@Module
//@InstallIn(ApplicationComponent::class)
object AssessmentApi {
//    @Provides
    fun univTestService() : AssessmentService  = Network.retrofit(Network.Type.mockTest).create(AssessmentService::class.java)
    fun springService() : SpringService2  = Network.retrofit(Network.Type.spring).create(SpringService2::class.java)
}
interface SpringService2 {
    @GET("now")
    fun getServerTime()
        : Observable<ServerTimeNow>
}
interface AssessmentService {
    @GET("test/group/list")
    fun getGroupList(@Query("school_id") schoolID :Int,
             @Query("major_id") majorID: String)
            : Observable<AssessmentResponse>

    @GET("test/workbook/list")
    fun getWorkbookList(@Query("group_ids") groupIdList :String) : Observable<AssessmentWorkbookResponse>

    @GET("test/student/{studentId}/workbook/list")
    fun getWorkbookListOnStudentId(@Path("studentId") studentId: String) : Observable<AssessmentWorkbookOnStudentResponse>

    @GET("test/problem/list")
    fun getAssessmentProblems(@Query("workbook_id") workbookId: Int,
                              @Query("version") version: Int) : Observable<AssessmentProblemResponse>

    @GET("test/student/{student_id}/workbook/{workbook_id}/open")
    fun openWorkbook(@Path("student_id") studentId: String,
                     @Path("workbook_id") workbookId: Int,
                     @Query("version") version: Int) : Observable<AssessmentAnswerResponse>

    @GET("test/student/{student_id}/workbook/{workbook_id}/problem/{problem_no}")
    fun openProblem(@Path("student_id") studentId: String,
                     @Path("workbook_id") workbookId: Int,
                     @Path("problem_no") problemNo: Int) : Observable<AssessmentOpenProblemResponse>

    @GET("test/student/{student_id}/workbook/{workbook_id}/answer/list")
    fun getWorkbookAnswerList(@Path("student_id") studentId: String,
                              @Path("workbook_id") workbookId: Int) : Observable<AssessmentAnswer2Response>

    @GET("test/student/{student_id}/workbook/current")
    fun getGroupList2(@Path("student_id") studentId: String,
                      @Query("school_id") schoolId: Int) : Observable<AssessmentGroupResponse>
    @GET("test/student/{student_id}/workbook/current")
    suspend fun getGroupList(@Path("student_id") studentId: String,
                      @Query("school_id") schoolId: Int) : ResponseBody<AssessmentTestGroup>

    @GET("/metadata/school/{school_id}")
    suspend fun fetchAssessmentGroupMetadata(@Path("school_id") schoolId: Int) : ResponseBody<AssessmentMetadata>


    @PATCH("test/student/{student_id}/workbook/{workbook_id}/finish")
    fun finish(@Path("student_id") studentId: String,
               @Path("workbook_id") workbookId: Int) : Observable<AssessmentStudentWorkbookResponse>

    @PUT("test/student/{student_id}/workbook/{workbook_id}/problem/{problem_no}/answer")
    fun insertAnswer(@Path("student_id") studentId: String,
                     @Path("workbook_id") workbookId: Int,
                     @Path("problem_no") problemNo: Int,
                     @Body param: Parameter) : Observable<AssessmentStudentWorkbookResponse>

    @GET("test/student/{student_id}/workbook/{workbook_id}/result")
    fun fetchScoringResult(@Path("student_id") studentId: String,
                           @Path("workbook_id") workbookId: Int,
                           @Query("version") version: Int) : Observable<AssessmentScoringResultResponse>

    @GET("media/list")
    fun fetchMedia(@Query("problem_id") problemId: Int) : Observable<AssessmentSolutionResponse>

    @POST("media/log")
    fun makeMediaLog(@Body params: AssessmentMediaLog) : Observable<AssessmentMediaLogResponse<Int>>

    @PATCH("media/log/{response_media_id}/finish")
    fun finishMediaLog(@Path("response_media_id") responseMediaId: Int,
                       @Body params: AssessmentMediaLog) : Observable<AssessmentMediaLogResponse<String>>

    @GET("study/student/{student_id}/workbook/count")
    fun fetchAdditionalLearningBySchool(@Path("student_id") studentId: String,
                           @Query("school_id") schoolId: Int,
                           @Query("subject") subject: String) : Observable<BaseIntResponseNode>

}
