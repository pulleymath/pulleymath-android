package com.freewheelin.pulley.revision2023.repository

import android.content.Context
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.book.FilterCategory
import com.freewheelin.pulley.legacy.activities.learning.tabFragment.book.FilterOrder
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.schoolType
import com.freewheelin.pulley.legacy.core.API.ResponseModel.CommercialBook
import com.freewheelin.pulley.legacy.core.API.ResponseModel.CommercialSubject
import com.freewheelin.pulley.legacy.core.API.ResponseModel.MyBookList
import com.freewheelin.pulley.legacy.core.API.ResponseModel.RecommendBookList
import com.freewheelin.pulley.legacy.model.ResponseBody
import com.freewheelin.pulley.legacy.model.contents.Book
import com.freewheelin.pulley.legacy.model.contents.BookType
import com.freewheelin.pulley.revision2023.model.BookFilterElement
import com.freewheelin.pulley.revision2023.model.BookFilterElement.Type
import com.freewheelin.pulley.revision2023.model.BookFilterParent
import com.freewheelin.pulley.revision2023.model.BookFilterSection
import com.freewheelin.pulley.revision2023.model.SignInAppToken
//import com.freewheelin.pulley.revision2023.room.patternstudy.PatternStudyDao
//import com.freewheelin.pulley.revision2023.room.patternstudy.PatternStudyDatabase
import com.freewheelin.pulley.revision2023.service.PatternStudyApi
import com.freewheelin.pulley.revision2023.service.PatternStudyService
import com.freewheelin.pulley.revision2023.service.WhaleSpaceLoginApi
import com.freewheelin.pulley.revision2023.service.WhaleSpaceLoginService
import com.google.gson.Gson
import io.reactivex.Completable
import kotlinx.coroutines.CoroutineScope

class WhaleSpaceLoginRepository(val context: Context, private val applicationScope: CoroutineScope) {
    private val whaleSpaceLoginApi: WhaleSpaceLoginService by lazy { WhaleSpaceLoginApi.whaleSpaceLoginService() }


    suspend fun sendCode(code: String): ResponseBody<SignInAppToken> {
        return whaleSpaceLoginApi.sendCode(code)
    }


}