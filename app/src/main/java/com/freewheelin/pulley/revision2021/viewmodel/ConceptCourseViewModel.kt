package com.freewheelin.pulley.revision2021.viewmodel

import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.bases.MyApplication.Companion.user
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.revision2021.repository.ConceptCourseFragRepository
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class ConceptCourseViewModel: BaseViewModel(), LifecycleObserver {

    private val studyRepository: ConceptCourseFragRepository by lazy { ConceptCourseFragRepository() }

    val subjectList by lazy { MutableLiveData<List<LCSubject>>() }
    val chapterList by lazy { MutableLiveData<List<StudyChapter>>() }

    val showProgress = MutableLiveData<Boolean>(true)
    val selectedSubjectId = MutableLiveData<Int>(-1)
    val availableLastSubjectId = MutableLiveData<Int>(7)

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

    var studyChapterHeader: StudyChapter? = null
    var studyChapterFooter: StudyChapter? = null

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
                    studyChapterHeader = studyChapterHeader ?: StudyChapter.createHeader()
                    studyChapterFooter = studyChapterFooter ?: StudyChapter.createFooter()

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

                    val listWithHeaderAndFooter = listOf(studyChapterHeader!!) + cList + listOf(studyChapterFooter!!)

                    chapterList.postValue(listWithHeaderAndFooter)
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
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "createLearningCourseOnStudentId =>${response.data}")
                callback()
            }, { error ->
                Log.e(javaClass.simpleName, "createLearningCourseOnStudentId error=${error.localizedMessage}")
            })
    }


    fun onHeaderSubjectBtnClick(subjectId: Int) {
        this.selectedSubjectId.postValue(subjectId)
    }

    fun setTutorialList() {
        studyChapterHeader = studyChapterHeader ?: StudyChapter.createHeader()
        studyChapterFooter = studyChapterFooter ?: StudyChapter.createFooter()

        val tutorialChapter = StudyChapter.createTutorial()

        val listWithHeaderAndFooter = listOf(studyChapterHeader!!) + tutorialChapter + listOf(studyChapterFooter!!)

        chapterList.postValue(listWithHeaderAndFooter)
    }

}