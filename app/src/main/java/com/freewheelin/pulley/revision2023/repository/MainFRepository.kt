package com.freewheelin.pulley.revision2023.repository

import android.content.Context
import com.freewheelin.pulley.bases.MyApplication.Companion.user
import com.freewheelin.pulley.model.ResponseBody
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.challenge.MainChallengeHeaderItem
import com.freewheelin.pulley.revision2023.room.challenge.*
import com.freewheelin.pulley.revision2023.service.ChallengeApi
import com.freewheelin.pulley.revision2023.service.ChallengeService
import com.freewheelin.pulley.revision2023.service.MainFApi
import com.freewheelin.pulley.revision2023.service.MainFService
import com.freewheelin.pulley.utils.checkErrorAndReturn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class MainFRepository(val context: Context, private val applicationScope: CoroutineScope) {
    private val mainFApi: MainFService by lazy { MainFApi.mainFService() }
    private val challengeApi: ChallengeService by lazy { ChallengeApi.challengeService() }
    private val challengeHeaderDao: MainChallengeHeaderItemDao = MainChallengeHeaderItemDatabase.getDatabase(context, applicationScope).headerItemDao()
//    private val challengeDetailDao: MainChallengeDetailDao = MainChallengeDetailDatabase.getDatabase(context, applicationScope).dao()


    suspend fun fetchAllMainChallengeHeaderItem(): List<MainChallengeHeaderItem> {
        return challengeApi.getAllChallengeHeaderItem()
            .data
    }

    fun flowAllChallengeHeader(): Flow<List<MainChallengeHeaderItem>> {
        println("asoaso flow check StudentID : ${user?.studentID} , ${user?.fullName}")
        return challengeHeaderDao.getAllHeaderItem()
    }

//    suspend fun compareHeaderInfo(id: Int): Boolean {
//        return challengeHeaderDao.compareInfo(id) > 0
//    }

    suspend fun upsertAllHeaders(items: List<MainChallengeHeaderItem>) {
        challengeHeaderDao.upsertAll(items)
    }

    suspend fun deleteChallengeHeader(item: MainChallengeHeaderItem) {
        challengeHeaderDao.delete(item.id, item.studentId ?: user?.studentID!!)
//        challengeHeaderDao.delete(item)
    }

    suspend fun fetchAllMainChallengeDetailItem(challengeId: Int): Challenge? {
        val res = challengeApi.getAllChallengeDetailItem(challengeId)
        return checkErrorAndReturn(context, res)
    }

//    fun flowAllChallengeDetails(): Flow<List<MainChallengeDetailItem>> {
//        return challengeDetailDao.getAllChallengeDetails()
//    }

//    suspend fun compareDetailInfo(id: Int): Boolean {
//        return challengeDetailDao.compareInfo(id) > 0
//    }
//
//    suspend fun upsertAllDetails(items: List<MainChallengeDetailItem>) {
//        challengeDetailDao.upsertAll(items)
//    }
//
//    suspend fun deleteChallengeDetail(item: MainChallengeDetailItem) {
//        challengeDetailDao.delete(item)
//    }

    suspend fun joinChallenge(challengeId: Int): Challenge? {
        val res = challengeApi.joinChallenge(challengeId)
        return checkErrorAndReturn(context, res)
    }

    suspend fun askForRedeemOfChallenge(userChallengeId: Int): ResponseBody<Challenge> {
        return challengeApi.askForRedeemOfChallenge(userChallengeId)
    }

//    fun <T>checkReturn(res: ResponseBody<T>): T? {
//        if (res.error != null) {
//            responseFailed(context, Throwable("${res.error} ${res.message}"))
//            return null
//        }
//        return res.data
//    }

}
