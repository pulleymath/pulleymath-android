package com.freewheelin.pulley.revision2021.repository

import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.revision2021.model.response.AffiliatedMediaLog
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestProblem
import com.freewheelin.pulley.revision2021.model.response.base.BaseIntResponseNode
import com.freewheelin.pulley.revision2021.model.response.base.BaseSingleResponseNode
import com.freewheelin.pulley.revision2021.repository.remote.AffiliatedTestApi
import com.freewheelin.pulley.revision2021.repository.remote.AffiliatedTestService
import com.freewheelin.pulley.revision2021.repository.remote.SpringService
import com.freewheelin.pulley.revision2021.repository.remote.SpringService2
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Response

class AffiliatedTestRepository private constructor() {
    companion object {
        val instance: AffiliatedTestRepository by lazy { AffiliatedTestRepository() }
    }
    val currentProblem by lazy { MutableLiveData<AffiliatedTestProblem>() }

    private val affiliatedTestService : AffiliatedTestService by lazy { AffiliatedTestApi.univTestService() }
    private val springService : SpringService2 by lazy { AffiliatedTestApi.springService() }

    fun groupList(schoolID: Int, majorID: String) = affiliatedTestService.getGroupList(schoolID, majorID)
    fun workbookList(groupIdList: String) = affiliatedTestService.getWorkbookList(groupIdList)
    fun workbookListOnStudentId(studentId: String) = affiliatedTestService.getWorkbookListOnStudentId(studentId)
    fun getTestProblems(workbookId: Int, version: Int) = affiliatedTestService.getAffiliatedTestProblems(workbookId, version)
    fun openWorkbook(studentId: String, workbookId: Int, version: Int) = affiliatedTestService.openWorkbook(studentId, workbookId, version)
    fun openProblem(studentId: String, workbookId: Int, problemNo: Int) = affiliatedTestService.openProblem(studentId, workbookId, problemNo)
    fun getTestAnswerList(studentId: String, workbookId: Int) = affiliatedTestService.getWorkbookAnswerList(studentId, workbookId)
    fun getGroupList2(studentId: String, schoolId: Int) = affiliatedTestService.getGroupList2(studentId, schoolId)
    fun finishTest(studentId: String, workbookId: Int) = affiliatedTestService.finish(studentId, workbookId)
    fun insertAnswer(studentId: String, workbookId: Int, problemNo: Int, param: Parameter) = affiliatedTestService.insertAnswer(studentId, workbookId, problemNo, param)
    fun fetchScoringResult(studentId: String, workbookId: Int, version: Int) = affiliatedTestService.fetchScoringResult(studentId, workbookId, version)
    fun fetchAdditionalLearningBySchool(studentId: String, schoolId: Int, subject: String) = affiliatedTestService.fetchAdditionalLearningBySchool(studentId, schoolId, subject)
    fun fetchMedia(problemId: Int) = affiliatedTestService.fetchMedia(problemId)
    fun makeMediaLog(params: AffiliatedMediaLog) = affiliatedTestService.makeMediaLog(params)
    fun finishMediaLog(responseMediaId: Int, params: AffiliatedMediaLog) = affiliatedTestService.finishMediaLog(responseMediaId, params)

    fun getServerTimeNow() = springService.getServerTime()
}