package com.freewheelin.pulley.revision2021.repository.remote

import com.freewheelin.pulley.core.Version
import com.freewheelin.pulley.core.retrofit
import com.freewheelin.pulley.revision2021.model.response.CityResponse
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.components.ApplicationComponent
import io.reactivex.Observable
import retrofit2.http.GET

//@Module
//@InstallIn(ApplicationComponent::class)
object CityApi {
//    @Provides
    fun cityService(): CityService = retrofit(Version.v2).create(CityService::class.java)
}

interface CityService {
    @GET("region")
    fun getCities(): Observable<CityResponse>
}
