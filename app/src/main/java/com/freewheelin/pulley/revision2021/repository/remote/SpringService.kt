package com.freewheelin.pulley.revision2021.repository.remote

import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.revision2021.model.response.*
import com.freewheelin.pulley.revision2021.model.response.base.BaseSingleResponseNode
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.*

//@Module
//@InstallIn(ApplicationComponent::class)
object SpringApi {
//    @Provides
    fun springService() : SpringService  = Network.retrofit(Network.Type.spring).create(SpringService::class.java)
}

interface SpringService {
    // 이 api의 성공시 리턴은 전부 null. res type이 큰 의미가있지는 않다.
    @POST("v1/user/books")
    fun eventBookCheck(@Body body: EventBook) : Observable<PdfAnswerResponse>
}