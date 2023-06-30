package com.freewheelin.pulley.legacy.core.API

import com.freewheelin.pulley.legacy.core.API.RequestModel.mypage.NotificationSettingRequest
import com.freewheelin.pulley.legacy.core.API.ResponseModel.mypage.*
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.model.ResponseForceBody
import com.freewheelin.pulley.legacy.model.ResponseListBody
import com.freewheelin.pulley.legacy.model.coupon.NewCoupon
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface  AppService {

    // FCM 토큰 등록
    @PUT("app/v1/users/devices/token")
    fun putToken(@Query("appPushToken") appPushToken: String): Single<ResponseBody<Any>>
    // 쿠폰
    @POST("app/v2/users/coupons")
    fun addCoupon(@Body coupon: NewCoupon): Single<ResponseBody<Any>>

    @POST("v2/coupons/pulley")
    fun useCoupon(@Query("couponId") couponId: Long): Single<ResponseBody<UseCouponResponse>>

    @GET("app/v2/users/coupons")
    fun fetchCoupons(): Single<ResponseListBody<CouponItem>>
    // 마이페이지
    @GET("app/v1/users/lesson/summary")
    fun summaryLesson(): Single<SummaryLessonResponse>

    @GET("app/v1/users/books/summary")
    fun summaryBooks(): Single<SummaryBooksResponse>

    @GET("app/v1/users/plus/summary")
    fun summaryPlus(): Single<SummaryPlusResponse>
    // 알림 설정
    @GET("app/v1/users/notification")
    fun getNotificationSetting(): Single<NotificationResponse>

    @PUT("app/v1/users/notification")
    fun setNotificationSetting(@Body notification: NotificationSettingRequest): Single<NotificationResponse>
}