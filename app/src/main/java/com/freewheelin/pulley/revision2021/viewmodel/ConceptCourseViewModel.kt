package com.freewheelin.pulley.revision2021.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.user
import com.freewheelin.pulley.legacy.utils.PulleyEvent
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.model.StudyChapter.Companion.TUTORIAL_SEQUENCE
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.revision2021.repository.ConceptCourseFragRepository
import com.freewheelin.pulley.revision2023.model.V2LogUserResponse
import com.freewheelin.pulley.revision2023.model.challenge.Challenge
import com.freewheelin.pulley.revision2023.model.challenge.ChallengeUserStatus
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.LegacyV2Repository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ConceptCourseViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {
    private val studyRepository: ConceptCourseFragRepository by lazy { ConceptCourseFragRepository() }
    private val legacyV2Repository = LegacyV2Repository(getApplication<Application>().applicationContext, viewModelScope)
    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val userRepository by lazy { UserRepository.instance }

    val userInRepo = userRepository.user
    val schoolType = userRepository.schoolType

    private val _lcSubjects = MutableLiveData<List<LCSubject>>()
    val lcSubjects: LiveData<List<LCSubject>> = _lcSubjects
    val selectedLcSubject = MutableLiveData<LCSubject>()

    val chapterList by lazy { MutableLiveData<List<StudyChapter>>() }

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
                    it.sortedBy { it.seq }.let {
                        _lcSubjects.postValue(it)
                        cb()
                    }
                }
            }, { error ->
                Log.e(javaClass.simpleName, "concept course fetch error=${error.localizedMessage}")
            })
    }

    fun showLoading(value: Boolean) {
        _isLoading.postValue(value)
    }
    fun fetch(subjectId: Int) {
        val isTutorial = subjectId == 0 // 앱 내에서 tutorialSubjectId는 이렇게 정함.
        val studentId = user?.studentID ?: return
        compositeDisposable += studyRepository.getChapterOnSubject(subjectId, studentId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .doOnError { _isLoading.postValue(false) }
            .doOnComplete {
                compositeDisposable += Observable
                    .timer(200, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .subscribe ({
                        _isLoading.postValue(false)
                    }, { /* error */ })
            }
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "getChapterOnSubject =>${response.data}")
                response.data?.let {
                    val cList = mutableListOf<StudyChapter>()
                    it.forEachIndexed { largeIndex, largeChapter ->
                        largeChapter.children.forEachIndexed { index, middleChapter ->
                            val parentSequence = if (isTutorial) TUTORIAL_SEQUENCE else largeChapter.sequence
                            val sequence = if (isTutorial) TUTORIAL_SEQUENCE else middleChapter.sequence
                            val parentName = if (isTutorial) "" else largeChapter.name
                            val isParentChapterLast = if (isTutorial) true else it.size - 1 == largeIndex
                            val isFirstMiddleChapter = if (isTutorial) false else index == 0

                            middleChapter.isParentChapterLast = isParentChapterLast
                            middleChapter.parentSequence = parentSequence
                            middleChapter.sequence = sequence
                            middleChapter.parentName = parentName
                            middleChapter.isFirstMiddleChapter = isFirstMiddleChapter
                            middleChapter.isLastMiddleChapter = (largeChapter.children.size - 1) == index
                            middleChapter.setNextItemExist(largeChapter)
                            middleChapter.children.forEach { smallChapter ->
                                val parentName = if (isTutorial) "" else middleChapter.name
                                val name = if (isTutorial) "개념학습 튜토리얼" else smallChapter.name
                                val sequence = if (isTutorial) TUTORIAL_SEQUENCE else smallChapter.sequence

                                smallChapter.parentName = parentName
                                smallChapter.setNextItemExist(middleChapter)
                                smallChapter.checkBothEndsItem(middleChapter)
                                smallChapter.name = name
                                smallChapter.sequence = sequence
                            }
                            cList.add(middleChapter)
                        }
                    }

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
            .subscribe({
                callback()
            },   { error ->
                Log.e(javaClass.simpleName, "createLearningCourseOnStudentId error=${error.localizedMessage}")
            })
    }

    fun onHeaderSubjectBtnClick2(subject: LCSubject) {
        this.selectedLcSubject.postValue(subject)
    }

    fun chapterReset() {
        chapterList.postValue(listOf())
    }


    fun completedTutorial(callback: (Challenge) -> Unit) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val logResponse = postLog()
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
            ?.forEach { _ ->
                val tutorialLCSubject = LCSubject().apply {
                    subjectId = 0
                    name = "튜토리얼"
                    unitcode = "0"
                }
                onHeaderSubjectBtnClick2(tutorialLCSubject)
            }
    }

    fun updateChallenge (challenge: Challenge) {
        challengeRepository.updateChallengeList(challenge)
    }

}