package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.revision2021.repository.remote.Network

object MainFApi {
    fun mainFService(): MainFService = Network.retrofit(Network.Type.spring).create(
        MainFService::class.java)
}
interface MainFService {

//    @POST("users/{studentId}/chapters/{chapterId}")
//    fun createLearningCourse(
//        @Path("chapterId") chapterId: Int,
//        @Path("studentId") studentId: String = user?.studentID!!,
//    ): Completable
}