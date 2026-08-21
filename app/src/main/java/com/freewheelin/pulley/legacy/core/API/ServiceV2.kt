package com.freewheelin.pulley.legacy.core.API

import com.freewheelin.pulley.legacy.bases.MyApplication
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestAgreeInfo
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestChangeEmail
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestChangePassword
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestChangePhone
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestCheckCode
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestLogin
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestReset
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestResetPassword
import com.freewheelin.pulley.legacy.core.API.RequestModel.RequestSignup
import com.freewheelin.pulley.legacy.core.API.RequestModel.SchoolInfo
import com.freewheelin.pulley.legacy.core.API.ResponseModel.CommercialBookPageResponse
import com.freewheelin.pulley.legacy.core.API.ResponseModel.DDay
import com.freewheelin.pulley.legacy.core.API.ResponseModel.DailyRecommend
import com.freewheelin.pulley.legacy.core.API.ResponseModel.DailyStudy
import com.freewheelin.pulley.legacy.core.API.ResponseModel.DailySummary
import com.freewheelin.pulley.legacy.core.API.ResponseModel.MainProfile
import com.freewheelin.pulley.legacy.core.API.ResponseModel.MyBookList
import com.freewheelin.pulley.legacy.core.API.ResponseModel.RecommendBookList
import com.freewheelin.pulley.legacy.core.API.ResponseModel.ResponseDevice
import com.freewheelin.pulley.legacy.core.API.ResponseModel.ScoredStudentGoalInfo
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.legacy.core.manage.ResponseBookInfo
import com.freewheelin.pulley.legacy.core.manage.ResponseBookInfo2
import com.freewheelin.pulley.legacy.core.manage.ResponseBookList
import com.freewheelin.pulley.legacy.model.Problem
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.model.ResponseForceBody
import com.freewheelin.pulley.legacy.model.Template
import com.freewheelin.pulley.legacy.model.User
import com.freewheelin.pulley.legacy.model.contents.Book
import com.freewheelin.pulley.legacy.model.contents.Content
import com.freewheelin.pulley.legacy.model.contents.MockExam
import com.freewheelin.pulley.legacy.model.contents.Piece
import com.freewheelin.pulley.legacy.model.contents.Test
import com.freewheelin.pulley.revision2023.model.response.NoteStudyDetailResponse
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface  ServiceV2 {

    @POST("users/login")
    fun login(@Body params: RequestLogin): Call<Template<User>>

    @GET("me/app")
    fun getUser(): Call<Template<User>>

    @PATCH("users/{studentID}/grade")
    fun plusGrade(@Path("studentID") studentID: String,
                  @Query("updateGrade") upgradeGrade: Int): Call<Void>

    @PATCH("users/{studentID}/service/free")
    fun startFree(@Path("studentID") studentID: String): Call<Template<User>>

    @POST("users/{studentID}/init-setting")
    fun setUserInitInfo(
            @Path("studentID") studentID: String,
            @Body params: Parameter): Call<Void>

    @POST("errors/problems")
    fun reportProblem(@Body params: Parameter): Call<Void>

    @GET("mo")
    fun getMoList(@Query("publicSearch") publicSearch: Boolean, @Query("studentID") studentID: String): Call<List<MockExam>>

    @GET("mo/{pieceID}")
    fun getMo(@Path("pieceID") pieceID: Int,
              @Query("studentID") studentID: String,
              @Query("isRestart") isRestart: Boolean): Call<MockExam>

    @GET("books/plan/{studentID}")
    fun getBookPlans(@Path("studentID") studentID: String): Call<ResponseBookList>

    @GET("books/{studentID}")
    fun getBooks(@Path("studentID") studentID: String): Call<List<Book>>

    @GET("books/{studentID}/plans")
    fun getMyBookList(@Path("studentID") studentID: String): Call<MyBookList>

    @GET("books/recommend/{studentID}")
    fun getRecommendBookList(@Path("studentID") studentID: String): Call<List<RecommendBookList>>

    @GET("books/{studentID}/{assignID}/problems")
    fun getBook(@Path("studentID") studentID: String, @Path("assignID") assignID: Int): Call<ResponseBookInfo2>

    @GET("books/all")
    fun getBooks(@Query("studentID") studentID: String,
                 @Query("filter") filter: String): Call<List<Book>>

    @GET("books/all/ios/book")
    fun getBooksNew(@Query("studentID") studentID: String,
                 @Query("filter") filter: String,
                 @Query("order") order: String,
                 @Query("category") category: String): Call<List<Book>>

    @DELETE("books/{studentID}/plans/pieces/{pieceID}")
    fun deleteFromMyBook(@Path("studentID") studentID: String,
                         @Path("pieceID") pieceID: Int): Call<Void>


    @GET("init-test/{studentID}/reports")
    fun getReportUrl(@Path("studentID") studentID: String): Call<Map<String, String>>

    @POST("init-test/{studentID}/reports/mails")
    fun postReportMail(@Path("studentID") studentID: String): Call<Void>

    @PATCH("users/{studentID}")
    fun setUserInfo(@Path("studentID") studentID: String, @Body params: Parameter): Call<Void>

    @GET("init-test/{studentID}")
    fun getTestProblems(@Path("studentID") studentID: String): Call<Map<String, Any>>

    @POST("init-test/{studentID}")
    fun setUserType(@Path("studentID") studentID: String, @Body params: Parameter): Call<Void>

    @GET("init-test/pieces/{studentID}")
    fun getInitTest(@Path("studentID") studentID: String): Call<Test>

    @POST("init-test/pieces/problems/similar")
    fun getInitSimilarProblem(@Body params: Parameter): Call<Template<Problem>>

    @GET("test/{studentID}")
    fun getTestList(@Path("studentID") studentID: String):Call<List<Test>>

    @POST("daily-test/{studentID}")
    fun getDailyTest(@Path("studentID") studentID: String): Call<Template<Test>>

    @PUT("daily-test/{studentID}")
    fun setUserDailySetup(@Path("studentID") studentID: String, @Body params: Parameter): Call<Void>

    //deprecated
    @GET("test/{studentID}/reports")
    fun getDailyTestReport(@Path("studentID") studentID: String): Call<List<Test>>

    @GET("notes/problem/{problemId}")
    fun getProblemDetail(
        @Path("problemId") problemId: Int
    ): Call<ResponseForceBody<NoteStudyDetailResponse>>

    @PATCH("books/{studentID}/pins")
    fun setPin(@Path("studentID") studentID: String,
               @Query("pieceID") pieceID: Int,
               @Query("isPin") isPin: Boolean): Call<Void>

    @POST("scoring")
    fun score(@Body params: Parameter): Call<ScoredStudentGoalInfo>

    // deprecated
    @GET("commercials/{pieceID}/pages")
    fun getCommercialBookPage(@Path("pieceID") pieceID: Int): Call<CommercialBookPageResponse>

    // deprecated
    @POST("commercials/{pieceID}/similar/problems")
    fun getCommercialSimilarCnt(
            @Path("pieceID") pieceID: Int,
            @Body params: Parameter): Call<Int>


    // deprecated
    @POST("commercials/{pieceID}/custom")
    fun makeCustomBook(@Path("pieceID") pieceID: Int,
                       @Body params: Parameter): Call<Book>

    @POST("daily/study-time")
    fun postStudyTime(
        @Body params: Parameter,
        @Query("schoolType") school: String? = MyApplication.schoolType.name
    ): Call<Void>

    @GET("daily/study-time/{studentID}")
    fun getDailyStudyTime(@Path("studentID") studentID: String,
                          @Query("now") now: String)

    @PUT("users/{studentID}/init-setting/subjects/common")
    fun setInitStudied(
            @Path("studentID") studentID: String,
            @Body params: Parameter): Call<Void>

    @PUT("daily-test/{studentID}/exclude")
    fun setExcludeStudied(
            @Path("studentID") studentID: String,
            @Body params: Parameter): Call<Void>

    @PUT("users/{studentID}/init-setting/subjects/optional")
    fun setInitOptional(
            @Path("studentID") studentID: String,
            @Body params: Parameter): Call<Void>

    @POST("users/{studentID}/init-setting/subjects/optional")
    fun addInitOptionalSubject(
            @Path("studentID") studentID: String,
            @Body params: Parameter): Call<Void>

    @GET("profiles/{studentID}")
    fun getProfile(@Path("studentID") studentID: String): Call<MainProfile>

    @PATCH("profiles/{studentID}/goals")
    fun setUserGoalCount(
            @Path("studentID") studentID: String,
            @Query("value") value: Int): Call<Void>


    @GET("profiles/ddays/defaults")
    fun getDDays(): Call<List<DDay>>

    @GET("daily-summary/{studentID}")
    fun getDailySummary(@Path("studentID") studentID: String): Call<DailySummary>

//    @GET("search/user")
//    fun getUserListByUserName(@Query("name") value: String): Call<Template<List<StudentManagerDialog.Student>>>

    @GET("daily-summary/{studentID}/pieces/all")
    fun getStudyList(@Path("studentID") studentID: String): Call<List<Content>>

    // @Deprecated
    @POST("daily-summary/{studentID}/notes")
    fun makeWrongNote(@Path("studentID") studentID: String): Call<Piece>

    //Deprecated
    @POST("daily-summary/{studentID}/weak")
    fun makeRecommend(@Path("studentID") studentID: String): Call<Book>

    // Deprecated
    @GET("daily-summary/{studentID}/studies")
    fun getDailyStudy(@Path("studentID") studentID: String): Call<DailyStudy>

    @GET("daily-summary/{studentID}/pieces")
    fun getDailyPiece(@Path("studentID") studentID: String): Call<List<Content>>

    @GET("daily-summary/{studentID}/recommend")
    fun getDailyRecommend(@Path("studentID") studentID: String): Call<DailyRecommend?>

    @POST("problems/similar")
    fun getSimilarProblem(@Body param :Parameter): Call<Template<Problem>>

    @POST("signin/app")
    fun loginApp(@Body params: RequestLogin): Call<Template<User?>>

    @GET("new-user/loginID")
    fun existId(@Query("value") value: String): Call<Template<String?>>

    @GET("find/email")
    fun findEmail(@Query("name") name: String, @Query("cellphone") cellphone: String): Call<Template<String>>

    @POST("verify/password/send")
    fun requestReset(@Body params: RequestReset): Call<ResponseBody<String>>

    @PUT("find/reset/password")
    fun requestResetPassword(@Body params: RequestResetPassword): Call<Template<String?>>

    @POST("verify/signup/send")
    fun requestPhoneCode(@Query("cellphone") cellphone: String): Call<Template<String?>>

    @POST("verify/check")
    fun checkPhoneCode(@Body checkCode: RequestCheckCode): Call<Template<String?>>

    @POST("signup/app")
    fun signup(@Body request: RequestSignup): Call<Template<String?>>

    @POST("signout/app")
    fun signout(): Call<Template<String?>>

    @PATCH("me/name")
    fun rename(@Query("name") name: String): Single<Template<String?>>

    @POST("verify/update-email/send")
    fun requestChangeMailCode(@Query("email") email: String): Single<Template<String?>>

    @POST("verify/certify-email/send")
    fun requestCertifyMailCode(@Query("email") email: String): Single<Template<String?>>

    @PATCH("me/email")
    fun requestChangeMail(@Body requestChangeEmail:RequestChangeEmail): Single<Template<String?>>

    @PATCH("me/email/certify")
    fun requestCertifyMail(@Body requestChangeEmail:RequestChangeEmail): Single<Template<String?>>

    @POST("verify/update-cellphone/send")
    fun requestChangePhoneCode(@Query("cellphone") cellphone: String): Single<Template<String?>>

    @PATCH("me/cellphone")
    fun requestChangePhone(@Body requestChangePhone:RequestChangePhone): Single<Template<String?>>

    @PATCH("me/password")
    fun requestChangePassword(@Body request:RequestChangePassword): Single<Template<String?>>

    @GET("users/devices")
    fun getDevices(): Single<ResponseDevice>

    @DELETE("users/devices/all")
    fun deleteAllDevice(): Single<Template<String?>>

    @DELETE("users/devices/{id}/app")
    fun deleteDevice(@Path("id") id:Int): Single<Template<String?>>

    @PUT("me/school")
    fun updateSchoolInfo(@Body request: SchoolInfo): Single<Template<String?>>

    @PUT("me/agree")
    fun updateAgreeInfo(@Body request: RequestAgreeInfo): Single<Template<String?>>

    @GET("lesson/check")
    fun isPurchasedLesson(@Query("studentID") studentID:String)

    @POST("book/review/book")
    fun reviewBook(@Body param: Parameter): Call<Template<ResponseBookInfo>>

    @POST("book/review/custom-book")
    fun reviewCustomBook(@Body param: Parameter): Call<ResponseBody<Book>>

    @POST("users/{studentId}/init-setting/default")
    fun defaultInitSetting(@Path("studentId") studentId: String): Single<ResponseBody<Any?>>

    @GET("notes/")
    fun getWrongNotes(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("noteMode") mode: String,
    ): Single<Template<List<Problem>>>

}