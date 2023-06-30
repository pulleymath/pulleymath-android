package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.user
import com.freewheelin.pulley.legacy.core.API.ResponseModel.MainProfile
import com.freewheelin.pulley.legacy.core.manage.UserManager
import com.freewheelin.pulley.legacy.model.User
import com.freewheelin.pulley.revision2023.model.CoroutineExceptionType
import com.freewheelin.pulley.revision2023.model.PaidServiceType
import com.freewheelin.pulley.revision2023.model.challenge.*
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.MainFRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.ui.adapter.ChallengeMissionAdapter
import com.freewheelin.pulley.revision2023.ui.adapter.ChallengeHeaderListAdapter
import com.freewheelin.pulley.revision2023.utils.listeners.ChallengeClickListener
import com.freewheelin.pulley.revision2023.utils.listeners.ChallengeMissionClickListener
import com.freewheelin.pulley.legacy.utils.responseFailed
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainFViewModel(application: Application): BaseAndroidViewModel(application),
    ChallengeClickListener,
    ChallengeMissionClickListener {

    private val repository: MainFRepository = MainFRepository(getApplication<Application>().applicationContext, viewModelScope)
    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val userRepository by lazy { UserRepository.instance }

    lateinit var challengeListAdapter: ChallengeHeaderListAdapter
    lateinit var challengeDescAdapter: ChallengeMissionAdapter

    val showStartChallengeGuide = MutableLiveData<Boolean>(false)
    val toastMessage = MutableLiveData<String>()
    val blurTitle = MutableLiveData<String>()
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

    val joinedChallengeList = challengeRepository.joinedChallengeList
    val userInRepo = userRepository.user
    var teacherSpyModeCount = 0

    fun initChallengeSetting() {
        challengeHeaders.value?.first()?.let { onChallengeHeaderClick(it) }
    }
    var challengeHeaderJob: Job? = null
    fun initChallenge() {
        repository.run {
            if (user?.studentID == null) return
            challengeHeaderJob = flowAllChallengeHeader()
                .cancellable()
                .onEach { items ->
                    if (items.size > 1) {
                        _challengeHeaders.value = items.sortedBy { it.seq }
                    }
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
    fun cancelChallengeHeaderJob() {
        challengeHeaderJob?.cancel()
    }

    fun collectChallengeHeaderItem() {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            _isLoading.postValue(true)
            var newHeaders = fetchAllMainChallengeHeaders()
            val oldHeaders = challengeHeaders.value?.filterNot { it in newHeaders }
            oldHeaders?.forEach { deleteChallengeHeader(it) }
            newHeaders = newHeaders.map { it.copy(studentId = user?.studentID!!) }
            upsertChallengeHeaders(newHeaders)
            _isLoading.postValue(false)
            _errorAction.postValue(CoroutineExceptionType.NONE)
        }
    }

    fun collectChallengeDetail(challengeId: Int) {
        contentJob?.cancel("다른 챌린지 헤더 클릭으로 인한 취소", CancellationException())
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            _isLoading.postValue(true)
            fetchAllChallengeDetailItem(challengeId)?.let { newItem ->
                currentMission.postValue(newItem)
                updateChallengeMissions(newItem)
                delay(300)
                _isLoading.postValue(false)
                _errorAction.postValue(CoroutineExceptionType.NONE)
            }
        }
    }
    fun fetchUserProfile() {
        CoroutineScope(Dispatchers.IO + contentExceptionHandler).launch {
            val profile = userRepository.getMainProfile()
            mainProfile.postValue(profile)
            _errorAction.postValue(CoroutineExceptionType.NONE)
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
    fun isStartChallengeUserStatusNotYet(cb: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO + contentExceptionHandler).launch {
            val challenge = repository.fetchAllMainChallengeDetailItem(1)
            val isNotYet = challenge?.userStatus != ChallengeUserStatus.YET
            _errorAction.postValue(CoroutineExceptionType.NONE)
            cb(isNotYet)
        }
    }

    suspend fun updateChallengeMissions(item: Challenge) {
        _challengeMissions.run {
            val missionHeader = item.courses[0].copy(userChallengeCourseId = -1, parentDetailItem = item)
            val missionList = item.courses

            postValue(listOf(missionHeader) + missionList)
        }
    }
//    suspend fun deleteChallengeDetail(item: MainChallengeDetailItem) {
//        repository.deleteChallengeDetail(item)
//    }

    fun disappearStartChallengeGuide() {
        showStartChallengeGuide.postValue(false)
    }

    fun joinChallengeById(challengeId: Int, cb: () -> Unit = {}) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {

            repository.joinChallenge(challengeId)?.let { detailItem ->
                challengeRepository.getChallengesOnStatus()
//                challengeRepository.updateChallengeList(detailItem)
                currentMission.postValue(detailItem)
                _errorAction.postValue(CoroutineExceptionType.NONE)
                cb()
            }
        }
    }
    fun askForRedeemOfChallenge(userChallengeId: Int, cb: () -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val res = repository.askForRedeemOfChallenge(userChallengeId)
            _errorAction.postValue(CoroutineExceptionType.NONE)
            if (res.error != null) {
                println("[[[[[ERROR askForRedeemOfChallenge]]]]]")
                responseFailed(getApplication<Application>().applicationContext, Throwable("${res.error} ${res.message}"))
                return@launch
            }
            withContext(Dispatchers.Main) {
                cb()
            }
            res.data?.let {
                challengeRepository.updateChallengeList(it)
            }

        }
    }

    override fun onChallengeHeaderClick(item: MainChallengeHeaderItem) {
//        contentJob?.cancel("다른 챌린지 헤더 클릭으로 인한 취소", CancellationException())

        if (item.status.isNotAvailable()) {
            toastMessage.postValue("2주, 4주 완성 챌린지는 준비 중이에요 :)")
            return
        }
        val prevSelectedHeader = _challengeHeaders.value?.find { it.id == item.id && it.isSelected }
        if (prevSelectedHeader != null) {
            println("asoaso 기존 클릭되어있던 대상")
            return
        }

        _challengeHeaders.postValue(challengeHeaders.value?.map { header ->
            header.copy(isSelected = header == item)
        })

        collectChallengeDetail(item.id)
    }

    override fun onMissionClick(item: ChallengeCourse) {
        val context = getApplication<Application>().applicationContext
        ChallengeManager.getMainTabMoveIntent(item).let {
            LocalBroadcastManager.getInstance(context).sendBroadcast(it)
        }
    }
}