package com.freewheelin.pulley.revision2021.viewmodel

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.revision2021.activity.AffiliatedTestSolveActivity
import com.freewheelin.pulley.revision2021.model.response.*
import com.freewheelin.pulley.revision2021.repository.AffiliatedTestRepository
import com.freewheelin.pulley.views.DaebakToast
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class AffiliatedTestViewModel: BaseViewModel(), LifecycleObserver {
    private val affiliatedTestRepository: AffiliatedTestRepository by lazy { AffiliatedTestRepository.instance }

    val affiliatedTestCardList by lazy { MutableLiveData<List<AffiliatedTestCard>>() }

    val selectedUnivTestCard by lazy { MutableLiveData<AffiliatedTestCard>() }
    val currentTimeString by lazy { MutableLiveData<String>() }
    val selectedTabIndex by lazy { MutableLiveData<Int>(0) }
    val showNothingDataView by lazy { MutableLiveData(false) }

    fun fetchUnivTestGroup(callback: ((AffiliatedTestCard)->Unit)?) {
        val studentId = user?.studentID ?: return
        val schoolId = user?.schoolID ?: return

        compositeDisposable += affiliatedTestRepository.getGroupList2(studentId, schoolId)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
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
                                    .map { AffiliatedTestCard(it.key, it.value.sortedBy { that -> that.seq }) }
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

                    affiliatedTestCardList.postValue(cardList)
                    cardList[0].isSelected.set(true)
                    selectedUnivTestCard.postValue(cardList[0])

                    callback?.invoke(cardList[0])
                }
            }, { error ->
                Log.e(javaClass.simpleName, "group error=${error.localizedMessage}")
            })
    }
    private lateinit var solveResultLauncher: ActivityResultLauncher<Intent>

    fun onTestStart(v: View) {
        selectedUnivTestCard.value?.let {
            if (it.isTestEnable(currentTimeString.value)) {
                val intent = AffiliatedTestSolveActivity.getIntent(v.context, it.selectedWorkbook)
                v.context.startActivity(intent)
            } else {
                DaebakToast.show(v.context, "시험시작 30분 전부터 입장할 수 있습니다.")
            }
        }
    }

    fun onStep(v: View, num: Int) {
        selectedUnivTestCard.value?.let {
            it.selectedWorkbook = when (num) {
                1 -> {
                    it.stepSelectRelease()
                    it.firstWorkbook.select()
                }
                2 -> {
                    if (it.firstWorkbook.isFinished()) {
                        it.stepSelectRelease()
                        it.secondWorkbook.select()
                    } else {
                        DaebakToast.show(v.context, "이전 테스트를 완료하지 않았습니다.")
                        it.selectedWorkbook.select()
                    }
                }
                else -> {
                    if (it.secondWorkbook.isFinished()) {
                        it.stepSelectRelease()
                        it.thirdWorkbook.select()
                    } else {
                        DaebakToast.show(v.context, "이전 테스트를 완료하지 않았습니다.")
                        it.selectedWorkbook.select()
                    }
                }
            }
        }

        selectedUnivTestCard.postValue(selectedUnivTestCard.value)
    }

    fun selectCard(ut: AffiliatedTestCard) {
        affiliatedTestCardList.value?.forEach {
            it.isSelected.set(it.getId() == ut.getId())
        }
        ut.stepSelectRelease()
        when {
            ut.thirdWorkbook.isFinished() -> ut.thirdWorkbook.select()
            ut.secondWorkbook.isFinished() -> ut.thirdWorkbook.select()
            ut.firstWorkbook.isFinished() -> ut.secondWorkbook.select()
            else -> ut.firstWorkbook.select()
        }

        when {
            ut.firstWorkbook.isNotFinished() -> ut.selectedWorkbook = ut.firstWorkbook
            ut.secondWorkbook.isNotFinished() -> ut.selectedWorkbook = ut.secondWorkbook
            ut.thirdWorkbook.isNotFinished() -> ut.selectedWorkbook = ut.thirdWorkbook
            else -> ut.selectedWorkbook = ut.thirdWorkbook
        }

        selectedUnivTestCard.postValue(ut)
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
}