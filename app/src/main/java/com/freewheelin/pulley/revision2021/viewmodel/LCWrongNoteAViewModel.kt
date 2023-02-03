package com.freewheelin.pulley.revision2021.viewmodel

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.revision2021.model.StudyChapter
import com.freewheelin.pulley.revision2021.model.response.LCWrongNoteMapCard
import com.freewheelin.pulley.revision2021.repository.LCWrongNoteRepository
import com.freewheelin.pulley.revision2021.repository.LearningCourseRepository
import com.freewheelin.pulley.revision2021.views.CookingPencilcase
import com.freewheelin.pulley.revision2021.views.DisallowTouchEventViewPager

class LCWrongNoteAViewModel : BaseViewModel(), LifecycleObserver {

    private val lcwrongNoteRepository: LCWrongNoteRepository by lazy { LCWrongNoteRepository() }

    val headerTitle by lazy { MutableLiveData<String>("") }
    val filteredNoteCardList by lazy { MutableLiveData<List<LCWrongNoteMapCard>>() }
    val currNoteCard by lazy { MutableLiveData<LCWrongNoteMapCard>() }
    val patternName by lazy { MutableLiveData<String>("") }
    val remainingHintSize by lazy { MutableLiveData(0) }
    val isHintBtnDisabled by lazy { MutableLiveData<Boolean>(false) }
    val isNoteSelectorScrollPositionEnd by lazy { MutableLiveData<Boolean>(false) }

//    var currentCardIndex = 0
    val currentCardIndex by lazy { MutableLiveData(0) }

    var pencilcaseType: CookingPencilcase.EditType? = null
    var pencilcaseColor: CookingPencilcase.PenColor? = null
    var pencilcaseThickness: CookingPencilcase.Thickness? = null
    var pencilcaseModeFixed: Boolean = false
//    var selectedChapter: StudyChapter? = null

    val isPagerFirstIndex by lazy { MutableLiveData(true) }
    val isPagerLastIndex by lazy { MutableLiveData(false) }
    var selectedChapterId: Int = -1

    fun init(list: List<LCWrongNoteMapCard>, noteItem: LCWrongNoteMapCard, title: String, chapterId: Int) {
        headerTitle.postValue(title)
        filteredNoteCardList.postValue(list)
        currNoteCard.postValue(noteItem)
//        selectedChapter = chapter

        patternName.postValue(noteItem.patternName)
        selectedChapterId = chapterId
    }

    fun setHintBtnText(size: Int) {
        if (size < 0) return
        remainingHintSize.postValue(size)
    }

    fun resetHint() {
        filteredNoteCardList.value?.let { list ->
            currentCardIndex.value?.let { index ->
                val hintSize = list[index].hints.size
                setHintBtnText(hintSize)
            }
        }


    }

    fun forceUpdateNoteCardList() {
        filteredNoteCardList.postValue(filteredNoteCardList.value)
    }
}