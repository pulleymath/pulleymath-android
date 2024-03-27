package com.freewheelin.pulley.revision2021.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.freewheelin.pulley.legacy.core.Parameter
import com.freewheelin.pulley.revision2021.model.response.AssessmentGroup
import com.freewheelin.pulley.revision2021.model.response.AssessmentTestGroup
import com.freewheelin.pulley.revision2021.model.response.AssessmentMediaLog
import com.freewheelin.pulley.revision2021.model.response.AssessmentProblem
import com.freewheelin.pulley.revision2021.repository.remote.AssessmentApi
import com.freewheelin.pulley.revision2021.repository.remote.AssessmentService
import com.freewheelin.pulley.revision2021.repository.remote.SpringService2
import com.freewheelin.pulley.revision2023.model.response.AssessmentMetadata

class AssessmentRepository private constructor() {
    companion object {
        val instance: AssessmentRepository by lazy { AssessmentRepository() }
    }
    val currentProblem by lazy { MutableLiveData<AssessmentProblem>() }

    private val _assessmentMetadata = MutableLiveData<AssessmentMetadata?>()
    val assessmentMetadata: LiveData<AssessmentMetadata?> = _assessmentMetadata
    private val _assessmentExamGroup = MutableLiveData<List<AssessmentGroup>>(listOf())
    val assessmentExamGroup: LiveData<List<AssessmentGroup>> = _assessmentExamGroup

    private val assessmentService : AssessmentService by lazy { AssessmentApi.univTestService() }
    private val springService : SpringService2 by lazy { AssessmentApi.springService() }

    fun groupList(schoolID: Int, majorID: String) = assessmentService.getGroupList(schoolID, majorID)
    fun workbookList(groupIdList: String) = assessmentService.getWorkbookList(groupIdList)
    fun workbookListOnStudentId(studentId: String) = assessmentService.getWorkbookListOnStudentId(studentId)
    fun getTestProblems(workbookId: Int, version: Int) = assessmentService.getAssessmentProblems(workbookId, version)
    fun openWorkbook(studentId: String, workbookId: Int, version: Int) = assessmentService.openWorkbook(studentId, workbookId, version)
    fun openProblem(studentId: String, workbookId: Int, problemNo: Int) = assessmentService.openProblem(studentId, workbookId, problemNo)
    fun getTestAnswerList(studentId: String, workbookId: Int) = assessmentService.getWorkbookAnswerList(studentId, workbookId)
    suspend fun getGroupList(studentId: String, schoolId: Int): AssessmentTestGroup? {
        val testGroup = assessmentService.getGroupList(studentId, schoolId).data
        _assessmentExamGroup.postValue(testGroup?.group_list ?: listOf())
        return testGroup
    }

    suspend fun fetchAssessmentGroupMetadata(schoolId: Int): AssessmentMetadata? {
        val metadata = assessmentService.fetchAssessmentGroupMetadata(schoolId).data
        _assessmentMetadata.postValue(metadata)
        return metadata
    }
    fun getGroupList2(studentId: String, schoolId: Int) = assessmentService.getGroupList2(studentId, schoolId)
    fun finishTest(studentId: String, workbookId: Int) = assessmentService.finish(studentId, workbookId)
    fun insertAnswer(studentId: String, workbookId: Int, problemNo: Int, param: Parameter) = assessmentService.insertAnswer(studentId, workbookId, problemNo, param)
    fun fetchScoringResult(studentId: String, workbookId: Int, version: Int) = assessmentService.fetchScoringResult(studentId, workbookId, version)
    fun fetchAdditionalLearningBySchool(studentId: String, schoolId: Int, subject: String) = assessmentService.fetchAdditionalLearningBySchool(studentId, schoolId, subject)
    fun fetchMedia(problemId: Int) = assessmentService.fetchMedia(problemId)
    fun makeMediaLog(params: AssessmentMediaLog) = assessmentService.makeMediaLog(params)
    fun finishMediaLog(responseMediaId: Int, params: AssessmentMediaLog) = assessmentService.finishMediaLog(responseMediaId, params)

    fun getServerTimeNow() = springService.getServerTime()
}