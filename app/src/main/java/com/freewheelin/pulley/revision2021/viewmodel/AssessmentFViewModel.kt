package com.freewheelin.pulley.revision2021.viewmodel

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.revision2021.model.response.*
import com.freewheelin.pulley.revision2021.repository.AssessmentRepository
import com.freewheelin.pulley.revision2023.repository.AuthRepository
import com.freewheelin.pulley.revision2023.viewmodel.BaseAndroidViewModel
import com.freewheelin.pulley.legacy.views.DaebakToast
import com.freewheelin.pulley.revision2023.model.AssessmentDesignSkin
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AssessmentFViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {
    private val assessmentRepository: AssessmentRepository by lazy { AssessmentRepository.instance }
    private val authRepository by lazy { AuthRepository.instance }

    val assessmentCardList by lazy { MutableLiveData<List<AssessmentCard>>() }

    val selectedUnivTestCard by lazy { MutableLiveData<AssessmentCard>() }
    val selectedCardTitle by lazy { MutableLiveData<String>("-") }
    val currentTimeString by lazy { MutableLiveData<String>() }
    val selectedTabIndex by lazy { MutableLiveData<Int>(0) }
    val showNothingDataView by lazy { MutableLiveData(false) }
    val assessmentMetadata = assessmentRepository.assessmentMetadata

//    val showAdditionalLearning by lazy { MutableLiveData(false) }

    fun fetchUnivTestGroup(callback: ((AssessmentCard)->Unit)?) {
        val studentId = user?.studentID ?: return
        val schoolId = user?.schoolID ?: return

        compositeDisposable += assessmentRepository.getGroupList2(studentId, schoolId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res ->
                Log.d(javaClass.simpleName, "group list=>${res.data}")
                currentTimeString.postValue(res.current_time)
                showNothingDataView.postValue(res.data == null)
                res.data?.let {
                    val groupList = it.group_list
                    val workbookList = it.workbook_list
                    val studentWorkbookList = it.student_workbook_list

                    val cardList = workbookList
                                    .map { it.setStudentWorkbook(studentWorkbookList) }
                                    .groupBy { it.group_id }
                                    .map { AssessmentCard(it.key, it.value.sortedBy { that -> that.seq }) }
                                    .map { val filteredGroup = groupList.filter { that -> that.id == it.groupId }
                                        it.group_title = filteredGroup[0].group_title
                                        it.seq = filteredGroup[0].seq
                                        it
                                    }
                                    .sortedBy { it.seq }
                                    .sortedBy {
                                        val b1 = if (it.areAllWorkbookFinished()) 1 else 0
                                        b1
                                    }

                    assessmentCardList.postValue(cardList)
                    cardList[0].isSelected.set(true)
                    selectedUnivTestCard.postValue(cardList[0])

                    callback?.invoke(cardList[0])
                }
            }, { error ->
                Log.e(javaClass.simpleName, "group error=${error.localizedMessage}")
            })
    }

    fun onStep(v: View, num: Int) {
        selectedUnivTestCard.value?.let {
            it.selectedWorkbook = when (num) {
                1 -> {
                    it.stepSelectRelease()
                    it.firstWorkbook?.select()
                }
                2 -> {
                    if (it.firstWorkbook?.isFinished() == true) {

                        it.stepSelectRelease()
                        it.secondWorkbook?.select()
                    } else {
                        DaebakToast.show(v.context, "이전 테스트를 완료하지 않았습니다.")
                        it.selectedWorkbook?.select()
                    }
                }
                else -> {
                    if (it.secondWorkbook?.isFinished() == true) {
                        it.stepSelectRelease()
                        it.thirdWorkbook?.select()
                    } else {
                        DaebakToast.show(v.context, "이전 테스트를 완료하지 않았습니다.")
                        it.selectedWorkbook?.select()
                    }
                }
            }
        }

        selectedUnivTestCard.postValue(selectedUnivTestCard.value)
    }

    fun selectCard(card: AssessmentCard) {
        assessmentCardList.value?.forEach {
            it.isSelected.set(it.getId() == card.getId())
        }
        card.stepSelectRelease()
        card.selectedWorkbook = when {
            card.thirdWorkbook?.isFinished() == true -> {
                card.thirdWorkbook?.select()
            }
            card.secondWorkbook?.isFinished() == true -> {
                val testSize = card.workbookList.size
                if (testSize == 2) {
                    card.secondWorkbook?.select()
                } else {
                    card.thirdWorkbook?.select()
                }
            }
            card.firstWorkbook?.isFinished() == true -> card.secondWorkbook?.select()
            else -> card.firstWorkbook?.select()
        }

        selectedUnivTestCard.postValue(card)
    }

    var showReportDialog = MutableLiveData<Boolean>(false)
    fun onReportDialogClick() {
        showReportDialog.value = true
    }

    fun onTimerSwitch(isChecked: Boolean) {
        selectedUnivTestCard.value?.selectedWorkbook?.showTimer = isChecked
    }

    fun setTabIndex(index: Int) {
        selectedTabIndex.postValue(index)
    }
    fun getTempToken(cb: (String) -> Unit = {}) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val tempToken = authRepository.getTempToken()
            withContext(Dispatchers.Main) {
                cb(tempToken.token)
            }
        }
    }

    fun selectedCardSecondWorkbookTitle(): String {
        return selectedUnivTestCard.value?.secondWorkbook?.title ?: ""
    }
    fun selectedCardThirdWorkbookTitle(): String {
        return selectedUnivTestCard.value?.thirdWorkbook?.title ?: ""
    }

    fun getDesignSkin() =
        AssessmentDesignSkin.convertGroupCodeToSkin(assessmentRepository.assessmentMetadata.value?.group_code)
}