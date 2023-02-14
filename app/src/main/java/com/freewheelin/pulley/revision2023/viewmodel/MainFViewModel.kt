package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.freewheelin.pulley.bases.MyApplication.Companion.user
import com.freewheelin.pulley.core.API.ResponseModel.MainProfile
import com.freewheelin.pulley.core.manage.UserManager
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeCourse
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.challenge.MainChallengeHeaderItem
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.MainFRepository
import com.freewheelin.pulley.revision2023.ui.adapter.ChallengeMissionAdapter
import com.freewheelin.pulley.revision2023.ui.adapter.ChallengeHeaderListAdapter
import com.freewheelin.pulley.revision2023.utils.listeners.ChallengeClickListener
import com.freewheelin.pulley.revision2023.utils.listeners.ChallengeMissionClickListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainFViewModel(application: Application): BaseAndroidViewModel(application),
    ChallengeClickListener,
    ChallengeMissionClickListener {

    private val repository: MainFRepository = MainFRepository(getApplication<Application>().applicationContext, viewModelScope)
    private val challengeRepository by lazy { ChallengeRepository.instance }

    lateinit var challengeListAdapter: ChallengeHeaderListAdapter
    lateinit var challengeDescAdapter: ChallengeMissionAdapter

    val showStartChallengeGuide = MutableLiveData<Boolean>(false)
    val toastMessage = MutableLiveData<String>()
    val showPaidView = MutableLiveData<Boolean>()
    val showWholeProgressBar = MutableLiveData<Boolean>()
    val userPaidServiceType = MutableLiveData<PaidServiceType>()
    val currentMission = MutableLiveData<Challenge>()
    val mainProfile = MutableLiveData<MainProfile>()
    private val _challengeHeaders = MutableLiveData<List<MainChallengeHeaderItem>>()
    val challengeHeaders: LiveData<List<MainChallengeHeaderItem>> = _challengeHeaders

    // 서버에서는 Course라는 명칭을, 클라이언트와 디자인에서는 Mission이라는 명칭을쓴다
    private val _challengeMissions = MutableLiveData<List<ChallengeCourse>>()
    val challengeMission: LiveData<List<ChallengeCourse>> = _challengeMissions

    val blurTitle: String
        get() {
            val mainProfile = mainProfile.value ?: return ""
            val mission = currentMission.value ?: return ""
            val rewardName = currentMission.value?.reward?.name ?: return ""
            return "${mainProfile.studentName}님 ${mission.challengeName}에 참여해\n${rewardName}을 받아보세요!"

        }

    fun initUserInfo() {
        user?.let { user ->
            showPaidView.postValue(user.serviceType != PaidServiceType.NONE)
            userPaidServiceType.postValue(user.serviceType)
        }
    }
    fun initChallengeSetting() {
        challengeHeaders.value?.first()?.let { onChallengeHeaderClick(it) }
    }
    fun initChallenge() {
        repository.run {
            flowAllChallengeHeader()
                .onEach { items ->
                    _challengeHeaders.value = items.sortedBy { it.id }
                }
                .launchIn(viewModelScope)
//            flowAllChallengeDetails()
//                .onEach { items ->
//                    _challengeDetails.value = items
//                }
        }
        collectChallengeHeaderItem()
//        collectChallengeDetail()
    }

    fun collectChallengeHeaderItem() {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            _isLoading.postValue(true)
            var newHeaders = fetchAllMainChallengeHeaders()

            val oldHeaders = challengeHeaders.value?.filterNot { it in newHeaders }
            oldHeaders?.forEach { deleteChallengeHeader(it) }
            newHeaders = newHeaders.map { it.copy(studentId = user?.studentID!!) }
            upsertChallengeHeaders(newHeaders)
        }
    }

    fun collectChallengeDetail(challengeId: Int) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            _isLoading.postValue(true)
            fetchAllChallengeDetailItem(challengeId)?.let { newItem ->
                currentMission.postValue(newItem)
                updateChallengeMissions(newItem)
            }
        }
    }
    fun fetchUserProfile(context: Context) {
        UserManager.getProfile(context, user!!) {
            mainProfile.postValue(it)
        }
    }

    suspend fun fetchAllMainChallengeHeaders(): List<MainChallengeHeaderItem> {
        return repository.fetchAllMainChallengeHeaderItem()
    }
    suspend fun upsertChallengeHeaders(items: List<MainChallengeHeaderItem>) {
        repository.upsertAllHeaders(items)
    }
    suspend fun deleteChallengeHeader(item: MainChallengeHeaderItem){
        repository.deleteChallengeHeader(item)
    }

    suspend fun fetchAllChallengeDetailItem(challengeId: Int): Challenge? {
        return repository.fetchAllMainChallengeDetailItem(challengeId)
    }
    suspend fun updateChallengeMissions(item: Challenge) {
        _challengeMissions.run {
            val missionHeader = item.courses[0].copy(userChallengeCourseId = -1, parentDetailItem = item)
            val missionList = item.courses

            postValue(listOf(missionHeader) + missionList)
        }
        delay(200)
        _isLoading.postValue(false)
    }
//    suspend fun deleteChallengeDetail(item: MainChallengeDetailItem) {
//        repository.deleteChallengeDetail(item)
//    }

    fun disappearStartChallengeGuide() {
        showStartChallengeGuide.postValue(false)
    }
    val joinedChallengeList = challengeRepository.joinedChallengeList

    fun joinChallenge(challengeId: Int, cb: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            repository.joinChallenge(challengeId)?.let { detailItem ->
                challengeRepository.updateChallengeList(detailItem)
                currentMission.postValue(detailItem)
                cb()
            }
        }
    }

    override fun onChallengeHeaderClick(item: MainChallengeHeaderItem) {
        contentJob?.cancel("다른 챌린지 헤더 클릭으로 인한 취소", CancellationException())

        if (item.status.isNotAvailable()) {
            toastMessage.postValue("2주, 4주 완성 챌린지는 준비 중이에요 :)")
            return
        }

        _challengeHeaders.postValue(_challengeHeaders.value?.map { header ->
            header.copy(isSelected = header == item)
        })

        collectChallengeDetail(item.id)
    }

    override fun onMissionClick(item: ChallengeCourse) {

    }
}