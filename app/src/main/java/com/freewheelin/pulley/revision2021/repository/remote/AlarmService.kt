package com.freewheelin.pulley.revision2021.repository.remote

import com.freewheelin.pulley.revision2021.model.response.AlarmReadResponse
import com.freewheelin.pulley.revision2021.model.response.AlarmResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

object AlarmApi {
    fun alarmService() : AlarmService  = Network.retrofit(Network.Type.spring).create(AlarmService::class.java)
}
interface AlarmService {
    @GET("v1/users/messages")
    fun fetchAlarmMessages(): Observable<AlarmResponse>

    @PATCH("v1/users/messages/{message_id}")
    fun readAlarmMessage(@Path("message_id") messageID: Int) : Observable<AlarmReadResponse>

    @PATCH("v1/users/messages")
    fun readAllAlarmMessages(): Observable<AlarmResponse>
}