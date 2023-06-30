package com.freewheelin.pulley.revision2021.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.user
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.model.StudyChapter.Companion.TUTORIAL_SEQUENCE
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.revision2021.repository.ConceptCourseFragRepository
import com.freewheelin.pulley.revision2023.SchoolType
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ConceptCourseViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {
    private val studyRepository: ConceptCourseFragRepository by lazy { ConceptCourseFragRepository() }
    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val userRepository by lazy { UserRepository.instance }

    val userInRepo = userRepository.user
    val schoolType = userRepository.schoolType
    val subjectList by lazy { MutableLiveData<List<LCSubject>>() }
    val availableSubjectIndicator by lazy { MutableLiveData<List<LCSubject.SubjectIndicator>>() }
    val chapterList by lazy { MutableLiveData<List<StudyChapter>>() }

    val selectedSubjectId = MutableLiveData<Int>(-1)
    val availableLastSubjectId = MutableLiveData<Int>(7)
    var availableFirstSubjectId = 12
    val showMobileHeader = MutableLiveData<Boolean>(false)
    val showTabletHeader = MutableLiveData<Boolean>(false)
    val joinedChallengeList = challengeRepository.joinedChallengeList

    fun fetchAvailableSubjects(cb: () -> Unit) {
        compositeDisposable += studyRepository.getAvailableSubject()
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "fetchAvailableSubjects =>${response.data}")
                response.data?.let {
                    it.sortedBy { it.subjectId }.let {
                        subjectList.postValue(it)
                        availableSubjectIndicator.postValue(it.map { it.subjectIndicator })
                        val firstId = it.first().subjectId
                        val lastId = it.last().subjectId
                        availableLastSubjectId.postValue(lastId)
                        availableFirstSubjectId = firstId
                        cb()
                    }
                }
            }, { error ->
                Log.e(javaClass.simpleName, "concept course fetch error=${error.localizedMessage}")
            })
    }

    fun fetch(subjectId: Int) {
        _isLoading.postValue(true)
        val studentId = user?.studentID ?: return
        compositeDisposable += studyRepository.getChapterOnSubject(subjectId, studentId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .doOnError { _isLoading.postValue(false) }
            .doOnComplete {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(300)
                    _isLoading.postValue(false)
                }
            }
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "getChapterOnSubject =>${response.data}")
                response.data?.let {
                    val cList = mutableListOf<StudyChapter>()

                    it.forEachIndexed { largeIndex, largeChapter ->
                        largeChapter.children.forEachIndexed { index, middleChapter ->
                            val parentSequence = if (subjectId == LCSubject.SubjectIndicator.Tutorial.rawValue) TUTORIAL_SEQUENCE else largeChapter.sequence
                            val sequence = if (subjectId == LCSubject.SubjectIndicator.Tutorial.rawValue) TUTORIAL_SEQUENCE else middleChapter.sequence
                            val parentName = if (subjectId == LCSubject.SubjectIndicator.Tutorial.rawValue) "" else largeChapter.name
                            val isParentChapterLast = if (subjectId == LCSubject.SubjectIndicator.Tutorial.rawValue) true else it.size - 1 == largeIndex
                            val isFirstMiddleChapter = if (subjectId == LCSubject.SubjectIndicator.Tutorial.rawValue) false else index == 0

                            middleChapter.isParentChapterLast = isParentChapterLast
                            middleChapter.parentSequence = parentSequence
                            middleChapter.sequence = sequence
                            middleChapter.parentName = parentName
                            middleChapter.isFirstMiddleChapter = isFirstMiddleChapter
                            middleChapter.isLastMiddleChapter = (largeChapter.children.size - 1) == index
                            middleChapter.setNextItemExist(largeChapter)
                            middleChapter.children.forEach { smallChapter ->
                                val parentName = if (subjectId == LCSubject.SubjectIndicator.Tutorial.rawValue) "" else middleChapter.name
                                val name = if (subjectId == LCSubject.SubjectIndicator.Tutorial.rawValue) "개념학습 튜토리얼" else smallChapter.name
                                val sequence = if (subjectId == LCSubject.SubjectIndicator.Tutorial.rawValue) TUTORIAL_SEQUENCE else smallChapter.sequence

                                smallChapter.parentName = parentName
                                smallChapter.setNextItemExist(middleChapter)
                                smallChapter.checkBothEndsItem(middleChapter)
                                smallChapter.name = name
                                smallChapter.sequence = sequence
                            }
                            cList.add(middleChapter)
                        }
                    }

//                    val listWithHeaderAndFooter = listOf(studyChapterHeader!!) + cList + listOf(studyChapterFooter!!)
//                    val listWithHeaderAndFooter = cList

                    chapterList.postValue(cList)
                }
            }, { error ->
                Log.e(javaClass.simpleName, "concept course fetch error=${error.localizedMessage}")
            })
    }

    fun createLearningCourseOnStudentId(chapterId: Int, callback: () -> Unit) {
        val studentId = user?.studentID ?: return

        compositeDisposable += studyRepository.createLearningCourse(chapterId, studentId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .doOnComplete { callback() }
            .doOnError {
                Log.e(javaClass.simpleName, "createLearningCourseOnStudentId error=${it.localizedMessage}")
            }.subscribe()
    }

    fun initHeaderSubject() {
        val subject = when (schoolType.value) {
            SchoolType.MIDDLE -> {
                availableFirstSubjectId
                val indicator = LCSubject.SubjectIndicator.convertRawToSubject(availableFirstSubjectId)
                indicator
            }
            else -> LCSubject.SubjectIndicator.MathSang
        }
        onHeaderSubjectBtnClick(subject.rawValue)

    }
    fun onHeaderSubjectBtnClick(subjectId: Int) {
        if (this.selectedSubjectId.value == subjectId) return
        this.selectedSubjectId.postValue(subjectId)
    }

    fun setTutorialList() {
        val tutorialChapter = StudyChapter.createTutorial()
        val listWithHeaderAndFooter = listOf(tutorialChapter)

        chapterList.postValue(listWithHeaderAndFooter)
    }
    fun chapterReset() {
        chapterList.postValue(null)
    }


    fun completedTutorial(callback: (Challenge) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val logResponse = postLog()
            println("asoaso logRes : ${logResponse.isChallengeCourse}")
            if (logResponse.isChallengeCourse.not()) return@launch
//            val challengeId = getStartChallengeId()
            val startChallenge = logResponse.challengeStatus.find { it.isStartChallenge } ?: return@launch
            callback(startChallenge)
        }
    }

    suspend fun postLog(): V2LogUserResponse {
        return legacyV2Repository.postLog(
            event = PulleyEvent.BUTTON_CLICK,
            itemCategory = "튜토리얼",
            itemName = "완료",
            itemValue = null,
            itemNote = "개념학습",

            )
    }

    fun checkHeaderSelectedActionOfRelatedChallenge() {
        joinedChallengeList.value
            ?.filter { it.userStatus == ChallengeUserStatus.ING }
            ?.filter { it.startChallenge?.isConceptCourseInProgress == true }
            ?.forEach { _ -> onHeaderSubjectBtnClick(LCSubject.SubjectIndicator.Tutorial.rawValue) }
    }
    fun getStartChallengeId(): Int? {
        val sc = joinedChallengeList.value?.find { it.isStartChallenge }
        return sc?.challengeId
    }

    fun updateChallenge (challenge: Challenge) {
        challengeRepository.updateChallengeList(challenge)
    }
    fun moveAvailableFirstSubject() {
        selectedSubjectId.postValue(availableFirstSubjectId)

    }
}