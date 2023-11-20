package com.freewheelin.pulley.revision2021.viewmodel

import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.legacy.bases.MyApplication.Companion.user
//import com.freewheelin.pulley.revision2021.model.CourseContentTable
import com.freewheelin.pulley.revision2021.model.CourseType
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.model.request.channelio.PostImageMessageReq
import com.freewheelin.pulley.revision2021.model.request.channelio.PostTextMessageReq
import com.freewheelin.pulley.revision2021.model.response.SingleCourseDesc
import com.freewheelin.pulley.revision2021.model.response.channelio.ChannelIOImageUploadRes
import com.freewheelin.pulley.revision2021.repository.ChannelTalkRepository
import com.freewheelin.pulley.revision2021.repository.ConceptCourseFragRepository
import com.freewheelin.pulley.revision2021.repository.LearningCourseRepository
import com.freewheelin.pulley.legacy.utils.Preferences
import com.freewheelin.pulley.revision2023.ui.view.DrawType
import com.freewheelin.pulley.revision2023.ui.view.PenColorType
import com.zoyi.channel.plugin.android.store.ChannelStore
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.File
import java.util.concurrent.TimeUnit

class LearningCourseViewModel : BaseViewModel(), LifecycleObserver {

    private val courseRepository: LearningCourseRepository by lazy { LearningCourseRepository() }
    private val channelTalkRepository: ChannelTalkRepository by lazy { ChannelTalkRepository() }
    private val studyRepository: ConceptCourseFragRepository by lazy { ConceptCourseFragRepository() }

    val courseHeaderContentTable by lazy { MutableLiveData<List<SingleCourseDesc>>() }

    // courseContent에는 review페이지가 포함되어있지 않음
    val courseContentTable by lazy { MutableLiveData<List<SingleCourseDesc>>() }
    val currentCourseType by lazy { MutableLiveData(CourseType.PriorConcept) }

    val headerCookingBtnText by lazy { MutableLiveData("개념 익히기") }
    val headerPatternBtnText by lazy { MutableLiveData("유형 학습") }

    val headerTitle by lazy { MutableLiveData<String>("") }

    var pencilDrawType: DrawType? = null
    var pencilColorType: PenColorType? = null
    var pencilThickness: Float? = null

    var fingerDrawModeMode: Boolean = false
    var selectedChapter: StudyChapter? = null
    var selectedSubjectId: Int? = null
    var selectedChapterId: Int? = null

    val showProgress by lazy { MutableLiveData(false) }
    val showChannelIoFrame by lazy { MutableLiveData(false) }
    val isPagerFirstIndex by lazy { MutableLiveData(true) }
    val isPagerLastIndex by lazy { MutableLiveData(false) }
    val isPriorConceptScene by lazy { MutableLiveData(false) }

    val showHeaderNaviDialog by lazy { MutableLiveData(false) }
    val isPriorConceptNaviSelected by lazy { MutableLiveData(false) }
    val isCookingNaviSelected by lazy { MutableLiveData(false) }
    val isPatternNaviSelected by lazy { MutableLiveData(false) }

    var currPagerPosition = 0
    val selectedPagerIndex by lazy { MutableLiveData<Int>(0) }

    var currChannelIOImage: ChannelIOImageUploadRes? = null


    fun fetchCourseList(chapterId: Int, callback: (List<SingleCourseDesc>) -> Unit) {
        selectedChapterId = chapterId

        compositeDisposable += courseRepository.fetchCourseList(chapterId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "fetchCourseList =>${response.data}")
                response.data?.let {
                    val firstPattern = it.find { it.courseType == CourseType.Pattern }
                    val firstPatternIndex = it.indexOf(firstPattern)
                    val mutableCourseList = it.toMutableList()

                    mutableCourseList.add(firstPatternIndex, SingleCourseDesc.getPatternMap())
                    val reviewMap = SingleCourseDesc.getReviewMap()
                    val wrongNoteMap = SingleCourseDesc.getWrongNoteMap()
                    val contentTable = listOf(reviewMap) + mutableCourseList + listOf(wrongNoteMap)

                    courseHeaderContentTable.postValue(contentTable)

                    val contents = contentTable.filter { it.courseType != CourseType.PriorConcept }
                    courseContentTable.postValue(contents)


                    callback(contents)
                }
            }, { error ->
                Log.e(javaClass.simpleName, "fetchCourseList error=${error.localizedMessage}")
            })
    }


    fun getCourseTypeByPosition(position: Int): CourseType? {
        courseContentTable.value?.let { list ->
            return list.get(position).courseType
        }
        return null
    }
    fun setCurrentCourseType(position: Int) {
        courseContentTable.value?.let { list ->
            val lessonType = list.get(position).courseType
            lessonType.let { setPageType(it, position) }
        }
    }

    private fun setPageType(courseType: CourseType, currItemPosition: Int) {
        currentCourseType.postValue(courseType)

        val prevTypeCount = when (courseType) {
            CourseType.Cooking -> courseContentTable.value?.count { it.courseType == CourseType.PriorConceptMap || it.courseType == CourseType.PriorConcept }
            CourseType.PatternMap -> 999
            CourseType.Pattern -> courseContentTable.value?.count { it.courseType != CourseType.Pattern }
            else -> 999
        } ?: 999

        val currentPositionInType = currItemPosition - prevTypeCount + 1
        val cookingCourseCount = courseContentTable.value?.count { it.courseType == CourseType.Cooking }
        headerCookingBtnText.postValue(if (courseType == CourseType.Cooking) "개념 익히기 ${currentPositionInType}/${cookingCourseCount}" else "개념 익히기")

        val patternCourseCount = courseContentTable.value?.count { it.courseType == CourseType.Pattern }
        headerPatternBtnText.postValue(if (courseType == CourseType.Pattern) "유형 학습 ${currentPositionInType + 1}/${patternCourseCount}" else "유형 학습")
    }

    fun isPriorConcept(course: SingleCourseDesc): Boolean {
        return course.courseType == CourseType.PriorConcept
//        return courseHeaderContentTable.value?.get(selectedPosition)?.courseType == CourseType.review
    }
    // trim 되는 이유는 헤더 목차에는 사전개념이 들어가있지만 사전개념은 viewPager의 page가 아니기 때문이다.
    fun trimPosition (course: SingleCourseDesc): Int {
        courseContentTable.value?.let { list ->
            list.forEachIndexed { index, singleCourseDesc ->
                val isEqualType = singleCourseDesc.courseType == course.courseType
                val isEqualId = singleCourseDesc.learningCourseDetailId == course.learningCourseDetailId
                if (isEqualType && isEqualId) return index
            }
        }
        return 0
    }

    fun getPagerPositionOnCookingId(cookingId: Int): Int {
        courseContentTable.value?.forEachIndexed { index, course ->
            if (course.courseType == CourseType.Cooking && course.learningCourseDetailId == cookingId) {
                return index
            }
        }
        return 0
    }
    fun getPagerPositionOnPatternId(patternId: Int): Int {
        courseContentTable.value?.forEachIndexed { index, course ->
            if (course.courseType == CourseType.Pattern && course.learningCourseDetailId == patternId) {
                return index
            }
        }
        // 없으면 패턴맵에서 이동하지 않음
        courseContentTable.value?.forEachIndexed { index, course ->
            if (course.courseType == CourseType.PatternMap) {
                return index
            }
        }
        return 0
    }
    fun isPatternLearningPage(position: Int): Boolean {
        courseContentTable.value?.forEachIndexed { index, course ->
            if (index == position && (course.courseType == CourseType.Pattern || course.courseType == CourseType.PatternMap)) {
                return true
            }
        }
        return false
    }

    fun getPatternMapPosition(): Int {
        courseContentTable.value?.forEachIndexed { index, lct ->
            if (lct.courseType == CourseType.PatternMap) {
                return index
            }
        }
        return 0
    }

    fun getWrongNoteMapPosition(): Int {
        courseContentTable.value?.forEachIndexed { index, lct ->
            if (lct.courseType == CourseType.WrongNoteMap) {
                return index
            }
        }
        return 0
    }

    fun setLessonHeaderTitle(title: String) {
//        headerTitle.postValue("Part ${partIndex}. ${title}")
        headerTitle.postValue(title)
    }
    fun getHeaderCourseListOnType(type: CourseType): List<SingleCourseDesc> {
        courseHeaderContentTable.value?.let {
            return it.filter { it.courseType == type }
        }
        return listOf()
    }

    fun getChats(callback: () -> Unit) {
        compositeDisposable += channelTalkRepository.getChats()
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "getChats =>${response}")
                response.let {

                    it.userChats.forEach {
                        if (it.source.page == "LearningCourseActivity") {
                            val chatId = Preferences.channelTalkCurrChatId.get()
                            if (chatId != it.id) {
                                Preferences.studentIdWhenIssuingChatId.set(it.id)
                                val studentId = user?.studentID ?: ""
                                Preferences.studentIdWhenIssuingChatId.set(studentId)
                            }
                            return@forEach
                        }
                    }
                    callback()
                }
            }, { error ->
                Log.e(javaClass.simpleName, "getChats error=${error.localizedMessage}")
                callback()
            })
    }

    fun uploadImageCaptureFile(file: File, callback: (ChannelIOImageUploadRes?) -> Unit) {
        val channelId = ChannelStore.get().channelState.get()?.id ?: "104720"
        val chatId = Preferences.channelTalkCurrChatId.get()
        val fileName = "question_file.png"

        val file = RequestBody.create(MediaType.parse("image/png"), file)
        compositeDisposable += channelTalkRepository.uploadCaptureImage(channelId, chatId, fileName, file)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "uploadImageCaptureFile =>${response}")

                callback(response)
            }, { error ->
                Log.e(javaClass.simpleName, "uploadImageCaptureFile error=${error.localizedMessage}")
                callback(null)
            })
    }

    fun postChannelIoCapturedImageMessage(res: ChannelIOImageUploadRes, callback: () -> Unit) {
        val chatId = Preferences.channelTalkCurrChatId.get()
        val pageName = "LearningCourseActivity" // ChannelIO를 initialize한 Activity의 이름
        val personId = Preferences.channelTalkUserId.get()
        val body = PostImageMessageReq(personId, res)
        compositeDisposable += channelTalkRepository.postCapturedImageMessage(chatId, pageName, body)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "postCapturedImageMessage =>${response}")

                callback()
            }, { error ->
                Log.e(javaClass.simpleName, "postCapturedImageMessage error=${error.localizedMessage}")
                callback()
            })
    }

    fun postChannelIoTextMessage(msg: String, callback: () -> Unit) {
        val chatId = Preferences.channelTalkCurrChatId.get()
        val pageName = "LearningCourseActivity" // ChannelIO를 initialize한 Activity의 이름
        val personId = Preferences.channelTalkUserId.get()
        val body = PostTextMessageReq(personId, msg)
        compositeDisposable += channelTalkRepository.postTextMessage(chatId, pageName, body)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ response ->
                Log.d(javaClass.simpleName, "postCapturedImageMessage =>${response}")

                callback()
            }, { error ->
                Log.e(javaClass.simpleName, "postCapturedImageMessage error=${error.localizedMessage}")
                callback()
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
    fun naviViewDismiss() {
        showHeaderNaviDialog.postValue(false)
        isPriorConceptNaviSelected.postValue(false)
        isCookingNaviSelected.postValue(false)
        isPatternNaviSelected.postValue(false)
    }
    fun showNaviView() {
        showHeaderNaviDialog.postValue(true)
    }

    fun setNaviFlag(selectedType: CourseType) {
        when(selectedType) {
            CourseType.PriorConcept -> {
                isPriorConceptNaviSelected.postValue(true)
                isCookingNaviSelected.postValue(false)
                isPatternNaviSelected.postValue(false)
            }
            CourseType.Cooking -> {
                isPriorConceptNaviSelected.postValue(false)
                isCookingNaviSelected.postValue(true)
                isPatternNaviSelected.postValue(false)
            }
            CourseType.Pattern -> {
                isPriorConceptNaviSelected.postValue(false)
                isCookingNaviSelected.postValue(false)
                isPatternNaviSelected.postValue(true)
            }
            else -> {}
        }
    }
}