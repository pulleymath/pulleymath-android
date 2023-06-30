package com.freewheelin.pulley.revision2021.repository.remote

import com.freewheelin.pulley.legacy.core.Version
import com.freewheelin.pulley.legacy.core.retrofit
import com.freewheelin.pulley.revision2021.model.response.SchoolResponse
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.components.ApplicationComponent
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

//@Module
//@InstallIn(ApplicationComponent::class)
object SchoolApi {

//    @Provides
    fun schoolService(): SchoolService = retrofit(Version.v2).create(SchoolService::class.java)
}

interface SchoolService {
    @GET("school")
    fun searchSchool(@Query("name") name: String
                     , @Query("page") page: Int
                     , @Query("size") size: Int): Observable<SchoolResponse>
}
