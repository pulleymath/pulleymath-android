package com.freewheelin.pulley.revision2023.service

import com.freewheelin.pulley.legacy.activities.learning.tabFragment.book.FilterCategory
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.book.FilterOrder
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.legacy.core.API.ResponseModel.CommercialBook
import com.freewheelin.pulley.legacy.core.API.ResponseModel.CommercialSubject
import com.freewheelin.pulley.legacy.core.API.ResponseModel.MyBookList
import com.freewheelin.pulley.legacy.core.API.ResponseModel.RecommendBookList
import com.freewheelin.pulley.legacy.core.manage.ResponseBookList
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.model.ResponseListBody
import com.freewheelin.pulley.legacy.model.contents.Book
import com.freewheelin.pulley.revision2021.repository.remote.Network
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.model.BookFilterItem
import com.freewheelin.pulley.revision2023.model.BookFilterSection
import com.freewheelin.pulley.revision2023.model.PriorConceptWrapper
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.*

object WhaleSpaceLoginApi {
    fun whaleSpaceLoginService(): WhaleSpaceLoginService = Network.retrofit(Network.Type.spring).create(
        WhaleSpaceLoginService::class.java)
}
interface WhaleSpaceLoginService {

    @GET("login/oauth2/code/whalespace")
    suspend fun sendCode(
        @Query("code") code: String
    ): okhttp3.ResponseBody

}