package com.freewheelin.pulley.core.API

import com.freewheelin.pulley.core.API.RequestModel.sign.AuthPhoneRequest
import com.freewheelin.pulley.core.API.RequestModel.sign.ConfirmCodeRequest
import com.freewheelin.pulley.core.API.ResponseModel.sign.CountryCodeResponse
import com.freewheelin.pulley.model.ResponseBody
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface Anonymous {

    @GET("anonymous/v1/auth/country")
    fun listCountryCodes(): Single<CountryCodeResponse>

    @POST("anonymous/v1/auth/send")
    fun getAuthCode(@Body authPhoneRequest: AuthPhoneRequest): Single<ResponseBody<Any>>

    @POST("anonymous/v1/auth/check")
    fun confirmAuthCode(@Body confirmCodeRequest: ConfirmCodeRequest): Single<ResponseBody<Any>>

}