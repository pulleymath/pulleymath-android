package com.freewheelin.pulley.revision2021.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestCard
import com.freewheelin.pulley.revision2021.repository.AffiliatedTestRepository
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class AffiliatedSolveSolutionViewModel private constructor(): BaseViewModel(), LifecycleObserver {

    companion object {
        val instance: AffiliatedSolveSolutionViewModel by lazy { AffiliatedSolveSolutionViewModel() }
    }
    val affiliatedTestRepository: AffiliatedTestRepository by lazy { AffiliatedTestRepository.instance }

    val currentProblem by lazy { affiliatedTestRepository.currentProblem }

    @SuppressLint("CheckResult")
    fun fetchUnivTestGroup(callback: (()->Unit)?) {
        val studentId = user?.studentID ?: return
        val schoolId = user?.schoolID ?: return
        val majorCode = user?.userUniversityMajorCode ?: return

        affiliatedTestRepository.getGroupList2(studentId, schoolId, majorCode)
            .subscribeOn(Schedulers.io())
            .timeout(3, TimeUnit.SECONDS)
            .subscribe({ res ->
                Log.d(javaClass.simpleName, "group list=>${res.data}")

                res.data?.let {
                    val groupList = it.group_list
                    val workbookList = it.workbook_list
                    val studentWorkbookList = it.student_workbook_list

                }
            }, { error ->
                Log.e(javaClass.simpleName, "group error=${error.localizedMessage}")
            })
    }
}