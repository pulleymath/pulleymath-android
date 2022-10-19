package com.freewheelin.pulley.revision2021.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.bases.MyApplication.Companion.user
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.model.response.LCSubject
import com.freewheelin.pulley.revision2021.repository.ConceptCourseFragRepository
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class ConceptCourseViewModel: BaseViewModel(), LifecycleObserver {

    private val studyRepository: ConceptCourseFragRepository by lazy { ConceptCourseFragRepository() }

    val subjectList by lazy { MutableLiveData<List<LCSubject>>() }
    val chapterList by lazy { MutableLiveData<List<StudyChapter>>() }
    val largeChapter1 by lazy { MutableLiveData<StudyChapter>() }
    val largeChapter2 by lazy { MutableLiveData<StudyChapter>() }
    val largeChapter3 by lazy { MutableLiveData<StudyChapter>() }

//    val subjectIndex = MutableLiveData<Int>(3)
    val showProgress = MutableLiveData<Boolean>(true)
    val selectedSubjectId = MutableLiveData<Int>(-1)
    val availableLastSubjectId = MutableLiveData<Int>(7)


    @SuppressLint("CheckResult")
    fun fetchAvailableSubjects() {
        studyRepository.getAvailableSubject()
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
    @SuppressLint("CheckResult")
    fun fetch(subjectId: Int) {
        val studentId = user?.studentID ?: return
        studyRepository.getChapterOnSubject(subjectId, studentId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "getChapterOnSubject =>${response.data}")
                showProgress.postValue(false)
                response.data?.let {

                    studyChapterHeader = studyChapterHeader ?: StudyChapter.createHeader()
                    studyChapterFooter = studyChapterFooter ?: StudyChapter.createFooter()
                    val listWithHeaderAndFooter = listOf(studyChapterHeader!!) + it + listOf(studyChapterFooter!!)
                    chapterList.postValue(listWithHeaderAndFooter)

                    it.forEachIndexed { index, sc ->
                        when(index) {
                            0 -> { largeChapter1.postValue(sc) }
                            1 -> { largeChapter2.postValue(sc) }
                            2 -> { largeChapter3.postValue(sc) }
                            else -> { largeChapter1.postValue(sc) }
                        }
                    }

                }
            }, { error ->
                showProgress.postValue(false)
                Log.e(javaClass.simpleName, "concept course fetch error=${error.localizedMessage}")
            })
    }

    @SuppressLint("CheckResult")
    fun createLearningCourseOnStudentId(chapterId: Int, callback: () -> Unit) {
        val studentId = user?.studentID ?: return

        studyRepository.createLearningCourse(chapterId, studentId)
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

}