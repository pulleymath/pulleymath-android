package com.freewheelin.pulley.revision2023.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.service.ChallengeApi
import com.freewheelin.pulley.revision2023.service.ChallengeService

//class ChallengeRepository(val context: Context, val applicationScope: CoroutineScope) {
class ChallengeRepository() {

    companion object {
        val instance: ChallengeRepository by lazy { ChallengeRepository() }
    }
    private val api: ChallengeService by lazy { ChallengeApi.challengeService() }

    private val _joinedChallengeList = MutableLiveData<List<Challenge>>()
    val joinedChallengeList: LiveData<List<Challenge>> = _joinedChallengeList
    // TODO joined challenge list 를 업데이트하는 api와 로직추가해야함


    suspend fun getChallengesOnStatus(status: ChallengeUserStatus) {
        api.getChallengesOnStatus(status = status).data?.let {
            _joinedChallengeList.postValue(it)
        }
    }
    fun updateChallengeList(newChallenge: Challenge) {
        _joinedChallengeList.value?.let { list ->
            val challengeList = list.map {
                println("qwoqwo id 비교 : ${it.challengeId} / ${newChallenge.challengeId}")
                if (it.challengeId == newChallenge.challengeId) {
                    newChallenge
                } else {
                    it
                }
            }
            println("qwoqwo challengeList :${challengeList.size}")
            challengeList.forEach {
                println("qwoqwo ${it.challengeId} / ${it.challengeName} / ${it.userStatus}")
            }
            _joinedChallengeList.postValue(challengeList.ifEmpty {
                listOf(newChallenge)
            })
        }

    }
}