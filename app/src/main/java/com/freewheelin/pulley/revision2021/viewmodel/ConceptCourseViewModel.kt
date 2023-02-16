package com.freewheelin.pulley.revision2021.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.bases.MyApplication.Companion.user
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.revision2021.repository.ConceptCourseFragRepository
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeFormat
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel
import com.freewheelin.pulley.utils.PulleyEvent
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ConceptCourseViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {
    private val studyRepository: ConceptCourseFragRepository by lazy { ConceptCourseFragRepository() }
    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val challengeRepository by lazy { ChallengeRepository.instance }

    val subjectList by lazy { MutableLiveData<List<LCSubject>>() }
    val chapterList by lazy { MutableLiveData<List<StudyChapter>>() }

    val showProgress = MutableLiveData<Boolean>(true)
    val selectedSubjectId = MutableLiveData<Int>(-1)
    val availableLastSubjectId = MutableLiveData<Int>(7)
    val showMobileHeader = MutableLiveData<Boolean>(false)
    val showTabletHeader = MutableLiveData<Boolean>(false)
    val joinedChallengeList = challengeRepository.joinedChallengeList

    fun fetchAvailableSubjects() {
        compositeDisposable += studyRepository.getAvailableSubject()
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "fetchAvailableSubjects =>${response.data}")
                response.data?.let {
                    it.sortedBy { it.subjectId }.let {
                        subjectList.postValue(it)
                        val lastId = it.last().subjectId
                        availableLastSubjectId.postValue(lastId)
                    }
                }
            }, { error ->
                Log.e(javaClass.simpleName, "concept course fetch error=${error.localizedMessage}")
            })
    }

    fun fetch(subjectId: Int) {
        if (subjectId == LCSubject.SubjectIndicator.Tutorial.rawValue) {
            setTutorialList()
            return
        }
        val studentId = user?.studentID ?: return
        compositeDisposable += studyRepository.getChapterOnSubject(subjectId, studentId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .doOnError { showProgress.postValue(false) }
            .doOnComplete { showProgress.postValue(false) }
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "getChapterOnSubject =>${response.data}")
                response.data?.let {

                    val cList = mutableListOf<StudyChapter>()

                    it.forEachIndexed { largeIndex, largeChapter ->
                        largeChapter.children.forEachIndexed { index, middleChapter ->
                            middleChapter.isParentChapterLast = it.size - 1 == largeIndex
                            middleChapter.parentSequence = largeChapter.sequence
                            middleChapter.parentName = largeChapter.name
                            middleChapter.isFirstMiddleChapter = index == 0
                            middleChapter.isLastMiddleChapter =
                                (largeChapter.children.size - 1) == index
                            middleChapter.setNextItemExist(largeChapter)
                            middleChapter.children.forEach { smallChapter ->
                                smallChapter.parentName = middleChapter.name
                                smallChapter.setNextItemExist(middleChapter)
                                smallChapter.checkBothEndsItem(middleChapter)
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

    fun onHeaderSubjectBtnClick(subjectId: Int) {
        this.selectedSubjectId.postValue(subjectId)
    }

    fun setTutorialList() {
        val startChallenge = joinedChallengeList.value?.find { it.isStartChallenge }?.startChallenge
        val isCourseInProgress = startChallenge?.isConceptOfCourseInProgress
        val tutorialChapter = StudyChapter.createTutorial(isCourseInProgress)
        val listWithHeaderAndFooter = listOf(tutorialChapter)

        chapterList.postValue(listWithHeaderAndFooter)
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
            ?.filter { it.startChallenge?.isConceptOfCourseInProgress == true }
            ?.forEach { _ -> onHeaderSubjectBtnClick(LCSubject.SubjectIndicator.Tutorial.rawValue) }
    }
    fun getStartChallengeId(): Int? {
        val sc = joinedChallengeList.value?.find { it.isStartChallenge }
        return sc?.challengeId
    }

    fun updateChallenge (challenge: Challenge) {
        challengeRepository.updateChallengeList(challenge)
    }
}