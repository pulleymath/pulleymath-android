package com.freewheelin.pulley.revision2021.repository

import com.freewheelin.pulley.core.Parameter
import com.freewheelin.pulley.revision2021.model.response.AffiliatedTestProblem
import com.freewheelin.pulley.revision2021.repository.remote.AffiliatedTestApi
import com.freewheelin.pulley.revision2021.repository.remote.AffiliatedTestService

class AffiliatedTestRepository {

    private val affiliatedTestService : AffiliatedTestService by lazy { AffiliatedTestApi.univTestService() }

    fun groupList(schoolID: Int, majorID: String) = affiliatedTestService.getGroupList(schoolID, majorID)
    fun workbookList(groupIdList: String) = affiliatedTestService.getWorkbookList(groupIdList)
    fun workbookListOnStudentId(studentId: String) = affiliatedTestService.getWorkbookListOnStudentId(studentId)
    fun getTestProblems(workbookId: Int, version: Int) = affiliatedTestService.getAffiliatedTestProblems(workbookId, version)
    fun openWorkbook(studentId: String, workbookId: Int, version: Int) = affiliatedTestService.openWorkbook(studentId, workbookId, version)
    fun openProblem(studentId: String, workbookId: Int, problemNo: Int) = affiliatedTestService.openProblem(studentId, workbookId, problemNo)
    fun getTestAnswerList(studentId: String, workbookId: Int) = affiliatedTestService.getWorkbookAnswerList(studentId, workbookId)
    fun getGroupList2(studentId: String, schoolId: Int, majorCode: String) = affiliatedTestService.getGroupList2(studentId, schoolId, majorCode)
    fun finishTest(studentId: String, workbookId: Int) = affiliatedTestService.finish(studentId, workbookId)
    fun insertAnswer(studentId: String, workbookId: Int, problemNo: Int, param: Parameter) = affiliatedTestService.insertAnswer(studentId, workbookId, problemNo, param)
    fun fetchScoringResult(studentId: String, workbookId: Int, version: Int) = affiliatedTestService.fetchScoringResult(studentId, workbookId, version)
}